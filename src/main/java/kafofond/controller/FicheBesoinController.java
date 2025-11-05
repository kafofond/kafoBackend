package kafofond.controller;

import kafofond.dto.FicheBesoinDTO;
import kafofond.dto.FicheBesoinCreateDTO;
import kafofond.dto.CommentaireSimplifieDTO;
import kafofond.entity.FicheDeBesoin;
import kafofond.entity.Utilisateur;
import kafofond.entity.TypeDocument;
import kafofond.mapper.FicheBesoinMapper;
import kafofond.mapper.CommentaireMapper;
import kafofond.service.FicheBesoinService;
import kafofond.service.UtilisateurService;
import kafofond.service.CommentaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des fiches de besoin
 * Workflow : EN_COURS → VALIDÉ par Gestionnaire → APPROUVÉ par Comptable
 */
@RestController
@RequestMapping("/api/fiches-besoin")
@RequiredArgsConstructor
@Slf4j
public class FicheBesoinController {

    private final FicheBesoinService ficheBesoinService;
    private final UtilisateurService utilisateurService;
    private final FicheBesoinMapper ficheBesoinMapper;
    private final CommentaireService commentaireService;
    private final CommentaireMapper commentaireMapper;

    /**
     * Crée une nouvelle fiche de besoin (Trésorerie et Gestionnaire)
     */
    @PostMapping
    public ResponseEntity<?> creerFicheBesoin(@RequestBody FicheBesoinCreateDTO ficheDTO, Authentication authentication) {
        try {
            log.info("Création d'une fiche de besoin par {}", authentication.getName());
            
            // Convertir le DTO de création en entité
            FicheDeBesoin fiche = FicheDeBesoin.builder()
                    .serviceBeneficiaire(ficheDTO.getServiceBeneficiaire())
                    .objet(ficheDTO.getObjet())
                    .description(ficheDTO.getDescription())
                    .montantEstime(ficheDTO.getMontantEstime())
                    .dateAttendu(ficheDTO.getDateAttendu())
                    .urlFichierJoint(ficheDTO.getUrlFichierJoint())
                    .dateCreation(LocalDate.now().atStartOfDay())
                    .build();
            
            // Si des désignations sont fournies, les lier
            if (ficheDTO.getDesignations() != null && !ficheDTO.getDesignations().isEmpty()) {
                List<kafofond.entity.Designation> designations = ficheDTO.getDesignations().stream()
                        .map(d -> kafofond.entity.Designation.builder()
                                .produit(d.getProduit())
                                .quantite(d.getQuantite())
                                .prixUnitaire(d.getPrixUnitaire())
                                .montantTotal(d.getMontantTotal())
                                .date(d.getDate() != null ? d.getDate() : LocalDate.now())
                                .build())
                        .collect(Collectors.toList());
                fiche.setDesignations(designations);
            }
            
            FicheBesoinDTO ficheCreeeDTO = ficheBesoinService.creerDTO(fiche, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fiche de besoin créée avec succès");
            response.put("fiche", ficheCreeeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de la fiche de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifie une fiche de besoin existante (Trésorerie et Gestionnaire)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierFicheBesoin(@PathVariable Long id, @RequestBody FicheBesoinCreateDTO ficheDTO, 
                                               Authentication authentication) {
        try {
            log.info("Modification de la fiche de besoin {} par {}", id, authentication.getName());
            
            // Convertir le DTO de création en entité pour la modification
            FicheDeBesoin fiche = FicheDeBesoin.builder()
                    .serviceBeneficiaire(ficheDTO.getServiceBeneficiaire())
                    .objet(ficheDTO.getObjet())
                    .description(ficheDTO.getDescription())
                    .montantEstime(ficheDTO.getMontantEstime())
                    .dateAttendu(ficheDTO.getDateAttendu())
                    .urlFichierJoint(ficheDTO.getUrlFichierJoint())
                    .build();
            
            // Si des désignations sont fournies, les lier
            if (ficheDTO.getDesignations() != null && !ficheDTO.getDesignations().isEmpty()) {
                List<kafofond.entity.Designation> designations = ficheDTO.getDesignations().stream()
                        .map(d -> kafofond.entity.Designation.builder()
                                .produit(d.getProduit())
                                .quantite(d.getQuantite())
                                .prixUnitaire(d.getPrixUnitaire())
                                .montantTotal(d.getMontantTotal())
                                .date(d.getDate() != null ? d.getDate() : LocalDate.now())
                                .build())
                        .collect(Collectors.toList());
                fiche.setDesignations(designations);
            }
            
            FicheBesoinDTO ficheModifieeDTO = ficheBesoinService.modifierDTO(id, fiche, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fiche de besoin modifiée avec succès");
            response.put("fiche", ficheModifieeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la fiche de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Valide une fiche de besoin (Gestionnaire uniquement)
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerFicheBesoin(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Validation de la fiche de besoin {} par {}", id, authentication.getName());
            
            FicheBesoinDTO ficheValideeDTO = ficheBesoinService.validerDTO(id, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fiche de besoin validée avec succès");
            response.put("fiche", ficheValideeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la validation de la fiche de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Approuve une fiche de besoin (Comptable uniquement)
     */
    @PostMapping("/{id}/approuver")
    public ResponseEntity<?> approuverFicheBesoin(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Approbation de la fiche de besoin {} par {}", id, authentication.getName());
            
            FicheBesoinDTO ficheApprouveeDTO = ficheBesoinService.approuverDTO(id, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fiche de besoin approuvée avec succès");
            response.put("fiche", ficheApprouveeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de la fiche de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rejette une fiche de besoin avec commentaire obligatoire
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterFicheBesoin(@PathVariable Long id, @RequestBody Map<String, String> request, 
                                              Authentication authentication) {
        try {
            log.info("Rejet de la fiche de besoin {} par {}", id, authentication.getName());
            
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
            }
            
            FicheBesoinDTO ficheRejeteeDTO = ficheBesoinService.rejeterDTO(id, authentication.getName(), commentaire);
            
            // Récupérer les commentaires simplifiés pour le DTO
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.FICHE_BESOIN)
                    .stream().map(commentaireMapper::toSimplifieDTO).toList();
            ficheRejeteeDTO.setCommentaires(commentaires);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fiche de besoin rejetée avec succès");
            response.put("fiche", ficheRejeteeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la fiche de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste toutes les fiches de besoin de l'entreprise
     */
    @GetMapping
    public ResponseEntity<?> listerFichesBesoin(Authentication authentication) {
        try {
            log.info("Liste des fiches de besoin demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<FicheBesoinDTO> fichesDTO = ficheBesoinService.listerParEntrepriseDTO(utilisateur.getEntreprise());
            
            // Récupérer les commentaires pour chaque fiche
            List<FicheBesoinDTO> fichesAvecCommentaires = fichesDTO.stream()
                    .map(ficheDTO -> {
                        var commentaires = commentaireService.getCommentairesByDocument(ficheDTO.getId(), TypeDocument.FICHE_BESOIN)
                                .stream().map(commentaireMapper::toSimplifieDTO).toList();
                        ficheDTO.setCommentaires(commentaires);
                        return ficheDTO;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("fiches", fichesAvecCommentaires);
            response.put("total", fichesAvecCommentaires.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des fiches de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'une fiche de besoin
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirFicheBesoin(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Détails de la fiche de besoin {} demandés par {}", id, authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            Optional<FicheBesoinDTO> ficheDTO = ficheBesoinService.trouverParIdDTO(id);
            
            // Vérifier que la fiche appartient à l'entreprise de l'utilisateur
            if (ficheDTO.isEmpty() || 
                (ficheDTO.get().getEntrepriseNom() != null && 
                 !ficheDTO.get().getEntrepriseNom().equals(utilisateur.getEntreprise().getNom()))) {
                return ResponseEntity.notFound().build();
            }
            
            // Récupérer les commentaires pour la fiche
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.FICHE_BESOIN)
                    .stream().map(commentaireMapper::toSimplifieDTO).toList();
            ficheDTO.get().setCommentaires(commentaires);
            
            return ResponseEntity.ok(ficheDTO.get());
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la fiche de besoin : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}