package kafofond.controller;

import kafofond.dto.OrdreDePaiementDTO;
import kafofond.dto.OrdreDePaiementCreationDTO;
import kafofond.entity.OrdreDePaiement;
import kafofond.entity.Utilisateur;
import kafofond.mapper.OrdreDePaiementMapper;
import kafofond.service.OrdreDePaiementService;
import kafofond.service.UtilisateurService;
import kafofond.service.TableValidationService;
import kafofond.entity.TableValidation;
import kafofond.entity.TypeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des ordres de paiement
 * Workflow : EN_COURS → VALIDÉ par Responsable (si montant < seuil) ou APPROUVÉ par Directeur (si montant >= seuil)
 */
@RestController
@RequestMapping("/api/ordres-paiement")
@RequiredArgsConstructor
@Slf4j
public class OrdreDePaiementController {

    private final OrdreDePaiementService ordreDePaiementService;
    private final UtilisateurService utilisateurService;
    private final OrdreDePaiementMapper ordreDePaiementMapper;
    private final TableValidationService tableValidationService;

    /**
     * Crée un nouvel ordre de paiement (Comptable uniquement)
     */
    @PostMapping
    public ResponseEntity<?> creerOrdreDePaiement(@RequestBody OrdreDePaiementCreationDTO ordreCreationDTO, Authentication authentication) {
        try {
            log.info("Création d'un ordre de paiement par {}", authentication.getName());
            
            // Convertir le DTO de création en entité
            OrdreDePaiement ordre = OrdreDePaiement.builder()
                    .montant(ordreCreationDTO.getMontant())
                    .description(ordreCreationDTO.getDescription())
                    .compteOrigine(ordreCreationDTO.getCompteOrigine())
                    .compteDestinataire(ordreCreationDTO.getCompteDestinataire())
                    .build();
            
            // Si une décision est référencée, la lier
            if (ordreCreationDTO.getDecisionId() != null) {
                kafofond.entity.DecisionDePrelevement decision = new kafofond.entity.DecisionDePrelevement();
                decision.setId(ordreCreationDTO.getDecisionId());
                ordre.setDecisionDePrelevement(decision);
            }
            
            OrdreDePaiement ordreCree = ordreDePaiementService.creerDTO(ordre, authentication.getName());
            OrdreDePaiementDTO ordreCreeDTO = ordreDePaiementMapper.toDTO(ordreCree);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ordre de paiement créé avec succès");
            response.put("ordre", ordreCreeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'ordre de paiement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Valide un ordre de paiement (Responsable uniquement)
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerOrdreDePaiement(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Validation de l'ordre de paiement {} par {}", id, authentication.getName());
            
            OrdreDePaiement ordreValide = ordreDePaiementService.validerDTO(id, authentication.getName());
            OrdreDePaiementDTO ordreValideDTO = ordreDePaiementMapper.toDTO(ordreValide);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ordre de paiement validé avec succès");
            response.put("ordre", ordreValideDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la validation de l'ordre de paiement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Approuve un ordre de paiement (Directeur uniquement)
     */
    @PostMapping("/{id}/approuver")
    public ResponseEntity<?> approuverOrdreDePaiement(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Approbation de l'ordre de paiement {} par {}", id, authentication.getName());
            
            OrdreDePaiement ordreApprouve = ordreDePaiementService.approuverDTO(id, authentication.getName());
            OrdreDePaiementDTO ordreApprouveDTO = ordreDePaiementMapper.toDTO(ordreApprouve);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ordre de paiement approuvé avec succès");
            response.put("ordre", ordreApprouveDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de l'ordre de paiement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rejette un ordre de paiement avec commentaire obligatoire
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterOrdreDePaiement(@PathVariable Long id, @RequestBody Map<String, String> request, 
                                                  Authentication authentication) {
        try {
            log.info("Rejet de l'ordre de paiement {} par {}", id, authentication.getName());
            
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
            }
            
            OrdreDePaiement ordreRejete = ordreDePaiementService.rejeterDTO(id, authentication.getName(), commentaire);
            OrdreDePaiementDTO ordreRejeteDTO = ordreDePaiementMapper.toDTO(ordreRejete);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ordre de paiement rejeté avec succès");
            response.put("ordre", ordreRejeteDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du rejet de l'ordre de paiement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste tous les ordres de paiement d'une entreprise spécifique
     */
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<?> listerOrdresDePaiementParEntreprise(@PathVariable Long entrepriseId, Authentication authentication) {
        try {
            log.info("Liste des ordres de paiement de l'entreprise {} demandée par {}", 
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
            
            // Utiliser la méthode du service pour récupérer les ordres de paiement
            List<OrdreDePaiement> ordres = ordreDePaiementService.listerParEntrepriseId(entrepriseId);
            List<OrdreDePaiementDTO> ordresDTO = ordres.stream()
                    .map(ordreDePaiementMapper::toDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("ordres", ordresDTO);
            response.put("total", ordresDTO.size());
            response.put("entrepriseId", entrepriseId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des ordres de paiement par entreprise : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste tous les ordres de paiement de l'entreprise
     */
    @GetMapping
    public ResponseEntity<?> listerOrdresDePaiement(Authentication authentication) {
        try {
            log.info("Liste des ordres de paiement demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<OrdreDePaiement> ordres = ordreDePaiementService.listerParEntreprise(utilisateur.getEntreprise());
            List<OrdreDePaiementDTO> ordresDTO = ordres.stream()
                    .map(ordreDePaiementMapper::toDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("ordres", ordresDTO);
            response.put("total", ordresDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des ordres de paiement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'un ordre de paiement
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirOrdreDePaiement(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Détails de l'ordre de paiement {} demandés par {}", id, authentication.getName());
            
            Optional<OrdreDePaiement> ordre = ordreDePaiementService.trouverParId(id);
            
            if (ordre.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ordre de paiement introuvable");
                return ResponseEntity.notFound().build();
            }
            
            OrdreDePaiementDTO ordreDTO = ordreDePaiementMapper.toDTO(ordre.get());
            
            // Récupérer les commentaires depuis TableValidation
            List<TableValidation> validations = tableValidationService.consulterValidationsDocument(
                id, TypeDocument.ORDRE_PAIEMENT);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ordre", ordreDTO);
            response.put("commentaires", validations.stream()
                .filter(v -> v.getCommentaire() != null && !v.getCommentaire().isEmpty())
                .map(v -> Map.of(
                    "contenu", v.getCommentaire(),
                    "date", v.getDateValidation(),
                    "statut", v.getStatut(),
                    "validateurId", v.getValidateurId()
                ))
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ordre de paiement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}