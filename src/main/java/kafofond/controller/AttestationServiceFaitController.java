package kafofond.controller;

import kafofond.dto.AttestationServiceFaitDTO;
import kafofond.dto.AttestationServiceFaitCreateDTO;
import kafofond.entity.AttestationDeServiceFait;
import kafofond.entity.Utilisateur;
import kafofond.mapper.AttestationServiceFaitMapper;
import kafofond.service.AttestationServiceFaitService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des attestations de service fait
 * Workflow : EN_COURS → VALIDÉ par Gestionnaire → APPROUVÉ par Comptable
 */
@RestController
@RequestMapping("/api/asf")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attestations de Service Fait", description = "Gestion des attestations de service fait avec workflow de validation")
@SecurityRequirement(name = "bearerAuth")
public class AttestationServiceFaitController {

    private final AttestationServiceFaitService attestationServiceFaitService;
    private final UtilisateurService utilisateurService;
    private final AttestationServiceFaitMapper attestationServiceFaitMapper;

    /**
     * Crée une nouvelle attestation de service fait (Trésorerie et Gestionnaire)
     */
    @Operation(summary = "Créer une attestation de service fait", 
               description = "Crée une nouvelle attestation de service fait. Accessible à la Trésorerie et au Gestionnaire.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attestation de service fait créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Trésorerie ou Gestionnaire requis")
    })
    @PostMapping
    public ResponseEntity<?> creerAttestationServiceFait(@RequestBody AttestationServiceFaitCreateDTO attestationDTO, Authentication authentication) {
        try {
            log.info("Création d'une attestation de service fait par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Convertir le DTO de création en entité
            AttestationDeServiceFait attestation = AttestationDeServiceFait.builder()
                    .fournisseur(attestationDTO.getFournisseur())
                    .titre(attestationDTO.getTitre())
                    .constat(attestationDTO.getConstat())
                    .dateLivraison(attestationDTO.getDateLivraison())
                    .urlFichierJoint(attestationDTO.getUrlFichierJoint())
                    .dateCreation(LocalDate.now().atStartOfDay())
                    .build();
            
            // Si un bon de commande est référencé, le lier
            if (attestationDTO.getBonDeCommandeId() != null) {
                kafofond.entity.BonDeCommande bonDeCommande = new kafofond.entity.BonDeCommande();
                bonDeCommande.setId(attestationDTO.getBonDeCommandeId());
                attestation.setBonDeCommande(bonDeCommande);
                // Définir la référence du bon de commande comme référence de l'attestation
                attestation.setReferenceBonCommande("BC-" + attestationDTO.getBonDeCommandeId());
            }
            
            AttestationDeServiceFait attestationCreee = attestationServiceFaitService.creer(attestation, utilisateur);
            
            // Créer manuellement le DTO pour éviter les problèmes de proxy
            AttestationServiceFaitDTO attestationCreeeDTO = AttestationServiceFaitDTO.builder()
                    .id(attestationCreee.getId())
                    .code(attestationCreee.getCode())
                    .referenceBonCommande(attestationCreee.getReferenceBonCommande())
                    .fournisseur(attestationCreee.getFournisseur())
                    .titre(attestationCreee.getTitre())
                    .constat(attestationCreee.getConstat())
                    .dateLivraison(attestationCreee.getDateLivraison())
                    .dateCreation(LocalDate.from(attestationCreee.getDateCreation()))
                    .urlFichierJoint(attestationCreee.getUrlFichierJoint())
                    .createurNom(attestationCreee.getCreePar() != null ? 
                        attestationCreee.getCreePar().getPrenom() + " " + attestationCreee.getCreePar().getNom() : null)
                    .createurEmail(attestationCreee.getCreePar() != null ? attestationCreee.getCreePar().getEmail() : null)
                    // Éviter l'accès direct au bonDeCommande pour éviter les problèmes de proxy
                    .bonDeCommandeId(attestationDTO.getBonDeCommandeId())
                    .build();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Attestation de service fait créée avec succès");
            response.put("attestation", attestationCreeeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'attestation de service fait : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste toutes les attestations de service fait d'une entreprise spécifique
     */
    @Operation(summary = "Lister les attestations de service fait par entreprise", 
               description = "Liste toutes les attestations de service fait d'une entreprise donnée par son ID.")
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<?> listerAttestationsServiceFaitParEntreprise(@PathVariable Long entrepriseId, Authentication authentication) {
        try {
            log.info("Liste des attestations de service fait de l'entreprise {} demandée par {}", 
                    entrepriseId, authentication.getName());
            
            // Vérifier que l'utilisateur a le droit d'accéder à cette entreprise
            Utilisateur utilisateur = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Vérifier que l'utilisateur appartient à l'entreprise ou est admin
            if (!utilisateur.getEntreprise().getId().equals(entrepriseId) && 
                utilisateur.getRole() != kafofond.entity.Role.SUPER_ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Accès non autorisé à cette entreprise");
                return ResponseEntity.status(403).body(error);
            }
            
            // Utiliser la méthode du service pour récupérer les attestations de service fait
            List<AttestationDeServiceFait> attestations = attestationServiceFaitService.listerParEntrepriseId(entrepriseId);
            
            // Créer manuellement les DTOs pour éviter les problèmes de proxy
            List<AttestationServiceFaitDTO> attestationsDTO = attestations.stream()
                    .map(attestation -> {
                        try {
                            return AttestationServiceFaitDTO.builder()
                                    .id(attestation.getId())
                                    .code(attestation.getCode())
                                    .referenceBonCommande(attestation.getReferenceBonCommande())
                                    .fournisseur(attestation.getFournisseur())
                                    .titre(attestation.getTitre())
                                    .constat(attestation.getConstat())
                                    .dateLivraison(attestation.getDateLivraison())
                                    .dateCreation(LocalDate.from(attestation.getDateCreation()))
                                    .urlFichierJoint(attestation.getUrlFichierJoint())
                                    .createurNom(attestation.getCreePar() != null ? 
                                        attestation.getCreePar().getPrenom() + " " + attestation.getCreePar().getNom() : null)
                                    .createurEmail(attestation.getCreePar() != null ? attestation.getCreePar().getEmail() : null)
                                    // Éviter l'accès direct au bonDeCommande pour éviter les problèmes de proxy
                                    .bonDeCommandeId(attestation.getBonDeCommande() != null ? attestation.getBonDeCommande().getId() : null)
                                    .build();
                        } catch (Exception e) {
                            log.error("Erreur lors de la conversion de l'attestation en DTO : {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("attestations", attestationsDTO);
            response.put("total", attestationsDTO.size());
            response.put("entrepriseId", entrepriseId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des attestations de service fait par entreprise : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste toutes les attestations de service fait de l'entreprise
     */
    @Operation(summary = "Lister les attestations de service fait", 
               description = "Liste toutes les attestations de service fait de l'entreprise.")
    @GetMapping
    public ResponseEntity<?> listerAttestationsServiceFait(Authentication authentication) {
        try {
            log.info("Liste des attestations de service fait demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<AttestationDeServiceFait> attestations = attestationServiceFaitService.listerParEntreprise(utilisateur.getEntreprise());
            
            // Créer manuellement les DTOs pour éviter les problèmes de proxy
            List<AttestationServiceFaitDTO> attestationsDTO = attestations.stream()
                    .map(attestation -> {
                        try {
                            return AttestationServiceFaitDTO.builder()
                                    .id(attestation.getId())
                                    .code(attestation.getCode())
                                    .referenceBonCommande(attestation.getReferenceBonCommande())
                                    .fournisseur(attestation.getFournisseur())
                                    .titre(attestation.getTitre())
                                    .constat(attestation.getConstat())
                                    .dateLivraison(attestation.getDateLivraison())
                                    .dateCreation(LocalDate.from(attestation.getDateCreation()))
                                    .urlFichierJoint(attestation.getUrlFichierJoint())
                                    .createurNom(attestation.getCreePar() != null ? 
                                        attestation.getCreePar().getPrenom() + " " + attestation.getCreePar().getNom() : null)
                                    .createurEmail(attestation.getCreePar() != null ? attestation.getCreePar().getEmail() : null)
                                    // Éviter l'accès direct au bonDeCommande pour éviter les problèmes de proxy
                                    .bonDeCommandeId(attestation.getBonDeCommande() != null ? attestation.getBonDeCommande().getId() : null)
                                    .build();
                        } catch (Exception e) {
                            log.error("Erreur lors de la conversion de l'attestation en DTO : {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("attestations", attestationsDTO);
            response.put("total", attestationsDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des attestations de service fait : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'une attestation de service fait
     */
    @Operation(summary = "Obtenir une attestation de service fait", 
               description = "Récupère les détails d'une attestation de service fait.")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirAttestationServiceFait(
            @Parameter(description = "ID de l'attestation de service fait") @PathVariable Long id, 
            Authentication authentication) {
        try {
            log.info("Détails de l'attestation de service fait {} demandés par {}", id, authentication.getName());
            
            Optional<AttestationDeServiceFait> attestationOpt = attestationServiceFaitService.trouverParId(id);
            
            if (attestationOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Attestation de service fait introuvable");
                return ResponseEntity.notFound().build();
            }
            
            AttestationDeServiceFait attestation = attestationOpt.get();
            
            // Créer manuellement le DTO pour éviter les problèmes de proxy
            AttestationServiceFaitDTO attestationDTO = AttestationServiceFaitDTO.builder()
                    .id(attestation.getId())
                    .code(attestation.getCode())
                    .referenceBonCommande(attestation.getReferenceBonCommande())
                    .fournisseur(attestation.getFournisseur())
                    .titre(attestation.getTitre())
                    .constat(attestation.getConstat())
                    .dateLivraison(attestation.getDateLivraison())
                    .dateCreation(LocalDate.from(attestation.getDateCreation()))
                    .urlFichierJoint(attestation.getUrlFichierJoint())
                    .createurNom(attestation.getCreePar() != null ? 
                        attestation.getCreePar().getPrenom() + " " + attestation.getCreePar().getNom() : null)
                    .createurEmail(attestation.getCreePar() != null ? attestation.getCreePar().getEmail() : null)
                    // Éviter l'accès direct à l'entreprise et au bonDeCommande pour éviter les problèmes de proxy
                    .entrepriseNom(null) // Ne pas accéder directement à l'entreprise
                    .bonDeCommandeId(attestation.getBonDeCommande() != null ? attestation.getBonDeCommande().getId() : null)
                    .build();
            
            return ResponseEntity.ok(attestationDTO);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'attestation de service fait : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}