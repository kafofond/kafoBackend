package kafofond.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kafofond.dto.DecisionPrelevementDTO;
import kafofond.entity.DecisionDePrelevement;
import kafofond.entity.Utilisateur;
import kafofond.mapper.DecisionPrelevementMapper;
import kafofond.service.DecisionPrelevementService;
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
 * Controller pour la gestion des décisions de prélèvement
 * Workflow : Création par Comptable → Validation par Responsable → Approbation par Directeur (si montant > seuil) → Génère Ordre de Paiement
 */
@RestController
@RequestMapping("/api/decisions-prelevement")
@RequiredArgsConstructor
@Slf4j
public class DecisionPrelevementController {

    private final DecisionPrelevementService decisionPrelevementService;
    private final UtilisateurService utilisateurService;
    private final DecisionPrelevementMapper decisionPrelevementMapper;
    private final TableValidationService tableValidationService;

    /**
     * Crée une décision de prélèvement (Comptable uniquement)
     */
    @Operation(summary = "Créer une décision de prélèvement", 
               description = "Crée une décision de prélèvement à partir d'une attestation de service fait. Accessible uniquement au Comptable.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Décision de prélèvement créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Comptable requis")
    })
    @PostMapping
    public ResponseEntity<?> creerDecisionPrelevement(@RequestBody DecisionPrelevementDTO decisionDTO, Authentication authentication) {
        try {
            log.info("Création d'une décision de prélèvement par {}", authentication.getName());
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            DecisionDePrelevement decisionCreee = decisionPrelevementService.creerDTO(decisionDTO, authentication.getName());
            DecisionPrelevementDTO decisionCreeeDTO = decisionPrelevementMapper.toDTO(decisionCreee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Décision de prélèvement créée avec succès");
            response.put("decision", decisionCreeeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de la décision de prélèvement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Valide une décision de prélèvement (Responsable uniquement)
     */
    @Operation(summary = "Valider une décision de prélèvement", 
               description = "Valide une décision de prélèvement. Accessible uniquement au Responsable.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Décision de prélèvement validée avec succès"),
        @ApiResponse(responseCode = "400", description = "Décision introuvable ou déjà validée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Responsable requis")
    })
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerDecisionPrelevement(
            @Parameter(description = "ID de la décision de prélèvement à valider") @PathVariable Long id, 
            Authentication authentication) {
        try {
            log.info("Validation de la décision de prélèvement {} par {}", id, authentication.getName());
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            DecisionDePrelevement decisionValidee = decisionPrelevementService.validerDTO(id, authentication.getName());
            DecisionPrelevementDTO decisionValideeDTO = decisionPrelevementMapper.toDTO(decisionValidee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Décision de prélèvement validée avec succès");
            response.put("decision", decisionValideeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la validation de la décision de prélèvement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Approuve une décision de prélèvement (Directeur uniquement)
     */
    @Operation(summary = "Approuver une décision de prélèvement", 
               description = "Approuve une décision de prélèvement qui dépasse le seuil. Accessible uniquement au Directeur.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Décision de prélèvement approuvée avec succès"),
        @ApiResponse(responseCode = "400", description = "Décision introuvable ou non en attente d'approbation"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Directeur requis")
    })
    @PostMapping("/{id}/approuver")
    public ResponseEntity<?> approuverDecisionPrelevement(
            @Parameter(description = "ID de la décision de prélèvement à approuver") @PathVariable Long id, 
            Authentication authentication) {
        try {
            log.info("Approbation de la décision de prélèvement {} par {}", id, authentication.getName());
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            DecisionDePrelevement decisionApprouvee = decisionPrelevementService.approuverDTO(id, authentication.getName());
            DecisionPrelevementDTO decisionApprouveeDTO = decisionPrelevementMapper.toDTO(decisionApprouvee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Décision de prélèvement approuvée avec succès");
            response.put("decision", decisionApprouveeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de la décision de prélèvement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rejette une décision de prélèvement avec commentaire obligatoire
     */
    @Operation(summary = "Rejeter une décision de prélèvement", 
               description = "Rejette une décision de prélèvement avec commentaire obligatoire.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Décision de prélèvement rejetée avec succès"),
        @ApiResponse(responseCode = "400", description = "Commentaire obligatoire manquant"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Responsable ou Directeur requis")
    })
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterDecisionPrelevement(
            @Parameter(description = "ID de la décision de prélèvement à rejeter") @PathVariable Long id, 
            @RequestBody Map<String, String> request, 
            Authentication authentication) {
        try {
            log.info("Rejet de la décision de prélèvement {} par {}", id, authentication.getName());
            
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
            }
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            DecisionDePrelevement decisionRejetee = decisionPrelevementService.rejeterDTO(id, authentication.getName(), commentaire);
            DecisionPrelevementDTO decisionRejeteeDTO = decisionPrelevementMapper.toDTO(decisionRejetee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Décision de prélèvement rejetée avec succès");
            response.put("decision", decisionRejeteeDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la décision de prélèvement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste toutes les décisions de prélèvement de l'entreprise
     */
    @Operation(summary = "Lister les décisions de prélèvement", 
               description = "Liste toutes les décisions de prélèvement de l'entreprise.")
    @GetMapping
    public ResponseEntity<?> listerDecisionsPrelevement(Authentication authentication) {
        try {
            log.info("Liste des décisions de prélèvement demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<DecisionDePrelevement> decisions = decisionPrelevementService.listerParEntreprise(utilisateur.getEntreprise());
            List<DecisionPrelevementDTO> decisionsDTO = decisions.stream()
                    .map(decisionPrelevementMapper::toDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("decisions", decisionsDTO);
            response.put("total", decisionsDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des décisions de prélèvement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'une décision de prélèvement
     */
    @Operation(summary = "Obtenir une décision de prélèvement", 
               description = "Récupère les détails d'une décision de prélèvement.")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirDecisionPrelevement(
            @Parameter(description = "ID de la décision de prélèvement") @PathVariable Long id, 
            Authentication authentication) {
        try {
            log.info("Détails de la décision de prélèvement {} demandés par {}", id, authentication.getName());
            
            Optional<DecisionDePrelevement> decision = decisionPrelevementService.trouverParId(id);
            
            if (decision.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Décision de prélèvement introuvable");
                return ResponseEntity.notFound().build();
            }
            
            DecisionPrelevementDTO decisionDTO = decisionPrelevementMapper.toDTO(decision.get());
            
            // Récupérer les commentaires depuis TableValidation
            List<TableValidation> validations = tableValidationService.consulterValidationsDocument(
                id, TypeDocument.DECISION_PRELEVEMENT);
            
            Map<String, Object> response = new HashMap<>();
            response.put("decision", decisionDTO);
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
            log.error("Erreur lors de la récupération de la décision de prélèvement : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}