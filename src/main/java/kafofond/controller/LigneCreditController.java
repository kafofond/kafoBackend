package kafofond.controller;

import kafofond.dto.LigneCreditCreateDTO;
import kafofond.dto.LigneCreditDTO;
import kafofond.entity.LigneCredit;
import kafofond.entity.Utilisateur;
import kafofond.entity.Commentaire;
import kafofond.mapper.LigneCreditMapper;
import kafofond.service.LigneCreditService;
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
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des lignes de crédit
 * Workflow : EN_COURS → VALIDÉ/REJETÉ par Directeur
 */
@RestController
@RequestMapping("/api/lignes-credit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lignes de Crédit", description = "Gestion des lignes de crédit avec workflow de validation hiérarchique")
@SecurityRequirement(name = "bearerAuth")
public class LigneCreditController {

    private final LigneCreditService ligneCreditService;
    private final UtilisateurService utilisateurService;
    private final LigneCreditMapper ligneCreditMapper;

    @Operation(summary = "Créer une ligne de crédit", description = "Crée une nouvelle ligne de crédit. Accessible aux Responsable et Directeur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ligne de crédit créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - droits insuffisants")
    })
    @PostMapping
    public ResponseEntity<?> creerLigneCredit(@RequestBody LigneCreditCreateDTO ligneCreateDTO,
            Authentication authentication) {
        try {
            // Convertir le DTO simplifié en entité LigneCredit
            LigneCredit ligne = new LigneCredit();
            ligne.setIntituleLigne(ligneCreateDTO.getIntituleLigne());
            ligne.setDescription(ligneCreateDTO.getDescription());
            ligne.setMontantAllouer(ligneCreateDTO.getMontantAllouer());

            // Associer le budget si l'ID est fourni
            if (ligneCreateDTO.getBudgetId() != null) {
                kafofond.entity.Budget budget = new kafofond.entity.Budget();
                budget.setId(ligneCreateDTO.getBudgetId());
                ligne.setBudget(budget);
            }

            LigneCreditDTO ligneCreeeDTO = ligneCreditService.creerDTO(ligne, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ligne de crédit créée avec succès");
            response.put("ligne", ligneCreeeDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierLigneCredit(@PathVariable Long id, @RequestBody LigneCreditDTO ligneDTO,
            Authentication authentication) {
        try {
            LigneCredit ligne = ligneCreditMapper.toEntity(ligneDTO);
            LigneCreditDTO ligneModifieeDTO = ligneCreditService.modifierDTO(id, ligne, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ligne de crédit modifiée avec succès");
            response.put("ligne", ligneModifieeDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerLigneCredit(@PathVariable Long id, Authentication authentication) {
        try {
            LigneCreditDTO ligneValideeDTO = ligneCreditService.validerDTO(id, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ligne de crédit validée avec succès");
            response.put("ligne", ligneValideeDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterLigneCredit(@PathVariable Long id, @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
            }

            LigneCreditDTO ligneRejeteeDTO = ligneCreditService.rejeterDTO(id, authentication.getName(), commentaire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ligne de crédit rejetée avec succès");
            response.put("ligne", ligneRejeteeDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> listerLignesCredit(Authentication authentication) {
        try {
            List<LigneCreditDTO> lignesDTO = ligneCreditService.listerParEntrepriseDTO(authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("lignes", lignesDTO);
            response.put("total", lignesDTO.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirLigneCredit(@PathVariable Long id, Authentication authentication) {
        try {
            LigneCreditDTO ligneDTO = ligneCreditService.getLigneCreditAvecCommentaires(id);
            return ResponseEntity.ok(ligneDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/activer")
    public ResponseEntity<?> activerLigneCredit(@PathVariable Long id, Authentication authentication) {
        try {
            LigneCreditDTO ligneActiveeDTO = ligneCreditService.activerDTO(id, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ligne de crédit activée avec succès");
            response.put("ligne", ligneActiveeDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverLigneCredit(@PathVariable Long id, Authentication authentication) {
        try {
            LigneCreditDTO ligneDesactiveeDTO = ligneCreditService.desactiverDTO(id, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ligne de crédit désactivée avec succès");
            response.put("ligne", ligneDesactiveeDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "Lister les lignes de crédit par budget", description = "Liste toutes les lignes de crédit d'un budget spécifique")
    @GetMapping("/budget/{budgetId}")
    public ResponseEntity<?> listerParBudget(@PathVariable Long budgetId, Authentication authentication) {
        try {
            List<LigneCreditDTO> lignesDTO = ligneCreditService.listerParBudgetDTO(budgetId, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("lignes", lignesDTO);
            response.put("total", lignesDTO.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "Lister les lignes de crédit par statut", description = "Liste toutes les lignes de crédit avec un statut donné (EN_COURS, VALIDE, REJETE)")
    @GetMapping("/statut/{statut}")
    public ResponseEntity<?> listerParStatut(@PathVariable kafofond.entity.Statut statut,
            Authentication authentication) {
        try {
            List<LigneCreditDTO> lignesDTO = ligneCreditService.listerParStatutDTO(statut, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("lignes", lignesDTO);
            response.put("total", lignesDTO.size());
            response.put("statut", statut);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "Lister les lignes de crédit actives", description = "Liste toutes les lignes de crédit actives de l'entreprise")
    @GetMapping("/actives")
    public ResponseEntity<?> listerActives(Authentication authentication) {
        try {
            List<LigneCreditDTO> lignesDTO = ligneCreditService.listerActivesDTO(authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("lignes", lignesDTO);
            response.put("total", lignesDTO.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "Lister les lignes de crédit inactives", description = "Liste toutes les lignes de crédit inactives de l'entreprise")
    @GetMapping("/inactives")
    public ResponseEntity<?> listerInactives(Authentication authentication) {
        try {
            List<LigneCreditDTO> lignesDTO = ligneCreditService.listerInactivesDTO(authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("lignes", lignesDTO);
            response.put("total", lignesDTO.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}