package kafofond.controller;

import kafofond.entity.Budget;
import kafofond.entity.Utilisateur;
import kafofond.service.BudgetService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller pour la gestion des budgets
 * Endpoints : création, modification, validation, rejet, activation/désactivation
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budgets", description = "Gestion des budgets avec workflow de validation hiérarchique")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;
    private final UtilisateurService utilisateurService;

    /**
     * Crée un nouveau budget
     */
    @PostMapping
    public ResponseEntity<?> creerBudget(@RequestBody Budget budget, Authentication authentication) {
        try {
            kafofond.dto.BudgetDTO budgetCree = budgetService.creerDTO(budget, authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Budget créé avec succès",
                    "budget", budgetCree
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la création du budget", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Modifie un budget existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierBudget(@PathVariable Long id, @RequestBody Budget budget,
                                            Authentication authentication) {
        try {
            kafofond.dto.BudgetDTO budgetModifie = budgetService.modifierDTO(id, budget, authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Budget modifié avec succès",
                    "budget", budgetModifie
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la modification du budget", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Valide un budget
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerBudget(@PathVariable Long id, Authentication authentication) {
        try {
            kafofond.dto.BudgetDTO budgetValide = budgetService.validerDTO(id, authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Budget validé avec succès",
                    "budget", budgetValide
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la validation du budget", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Rejette un budget
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterBudget(@PathVariable Long id, @RequestBody Map<String, String> request,
                                           Authentication authentication) {
        try {
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire");
            }

            kafofond.dto.BudgetDTO budgetRejete = budgetService.rejeterDTO(id, authentication.getName(), commentaire);
            return ResponseEntity.ok(Map.of(
                    "message", "Budget rejeté avec succès",
                    "budget", budgetRejete
            ));

        } catch (Exception e) {
            log.error("Erreur lors du rejet du budget", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Active un budget
     */
    @PostMapping("/{id}/activer")
    public ResponseEntity<?> activerBudget(@PathVariable Long id, Authentication authentication) {
        try {
            kafofond.dto.BudgetDTO budgetActif = budgetService.activerDTO(id, authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Budget activé avec succès",
                    "budget", budgetActif
            ));

        } catch (Exception e) {
            log.error("Erreur lors de l'activation du budget", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Désactive un budget
     */
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverBudget(@PathVariable Long id, Authentication authentication) {
        try {
            kafofond.dto.BudgetDTO budgetDesactive = budgetService.desactiverDTO(id, authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Budget désactivé avec succès",
                    "budget", budgetDesactive
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la désactivation du budget", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Liste tous les budgets
     */
    @GetMapping
    public ResponseEntity<?> listerBudgets(Authentication authentication) {
        try {
            List<kafofond.dto.BudgetDTO> dtoList = budgetService.listerParEntrepriseDTO(authentication.getName());

            return ResponseEntity.ok(Map.of(
                    "total", dtoList.size(),
                    "budgets", dtoList
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des budgets", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Détail d'un budget
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirBudget(@PathVariable Long id) {
        return budgetService.trouverParIdDTO(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Génère le PDF d'un budget
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> genererPdfBudget(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Génération du PDF pour le budget {} par {}", id, authentication.getName());
            
            String urlPdf = budgetService.genererPdf(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "PDF généré avec succès");
            response.put("urlPdf", urlPdf);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Liste les budgets de l'entreprise de l'utilisateur connecté
     * Accessible par Directeur, Responsable et Comptable
     */
    @GetMapping("/mon-entreprise")
    @Operation(summary = "Lister les budgets de mon entreprise", 
               description = "Récupère tous les budgets de l'entreprise de l'utilisateur connecté. " +
                            "Accessible par Directeur, Responsable et Comptable.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budgets récupérés avec succès"),
        @ApiResponse(responseCode = "400", description = "Erreur lors de la récupération des budgets"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<?> listerBudgetsMonEntreprise(Authentication authentication) {
        try {
            List<kafofond.dto.BudgetDTO> dtoList = budgetService.listerParEntrepriseDTO(authentication.getName());

            return ResponseEntity.ok(Map.of(
                    "total", dtoList.size(),
                    "budgets", dtoList
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des budgets de l'entreprise", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

