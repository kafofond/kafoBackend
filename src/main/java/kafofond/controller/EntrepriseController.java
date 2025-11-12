package kafofond.controller;

import kafofond.dto.EntrepriseCreateDTO;
import kafofond.dto.EntrepriseDTO;
import kafofond.service.EntrepriseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des entreprises
 * Accessible uniquement aux Super Admin et Admin
 */
@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Entreprises", description = "Gestion des entreprises (Super Admin uniquement)")
@SecurityRequirement(name = "bearerAuth")
public class EntrepriseController {

        private final EntrepriseService entrepriseService;

        /**
         * Crée une nouvelle entreprise (Super Admin uniquement)
         */
        @Operation(summary = "Créer une entreprise", description = "Crée une nouvelle entreprise avec des informations simplifiées. Accessible uniquement au Super Admin.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Entreprise créée avec succès"),
                        @ApiResponse(responseCode = "400", description = "Données invalides"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Super Admin requis"),
                        @ApiResponse(responseCode = "409", description = "Conflit - Entreprise existe déjà")
        })
        @PostMapping
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<Map<String, Object>> creerEntreprise(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Informations simplifiées pour créer une entreprise", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntrepriseCreateDTO.class))) @Valid @RequestBody EntrepriseCreateDTO entrepriseCreateDTO,
                        Authentication authentication) {

                log.info("Création d'une entreprise par {}", authentication.getName());

                EntrepriseDTO entrepriseCreee = entrepriseService.creerEntrepriseFromSimpleDTO(entrepriseCreateDTO,
                                authentication.getName());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Entreprise créée avec succès");
                response.put("entreprise", entrepriseCreee);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Modifie une entreprise existante (Super Admin uniquement)
         */
        @Operation(summary = "Modifier une entreprise", description = "Modifie une entreprise existante. Accessible uniquement au Super Admin.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Entreprise modifiée avec succès"),
                        @ApiResponse(responseCode = "400", description = "Données invalides"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Super Admin requis"),
                        @ApiResponse(responseCode = "404", description = "Entreprise non trouvée")
        })
        @PutMapping("/{id}")
        public ResponseEntity<Map<String, Object>> modifierEntreprise(
                        @Parameter(description = "ID de l'entreprise à modifier") @PathVariable Long id,
                        @Valid @RequestBody EntrepriseDTO entrepriseDTO,
                        Authentication authentication) {

                log.info("Modification de l'entreprise {} par {}", id, authentication.getName());

                EntrepriseDTO entrepriseModifiee = entrepriseService.modifierEntreprise(id, entrepriseDTO,
                                authentication.getName());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Entreprise modifiée avec succès");
                response.put("entreprise", entrepriseModifiee);

                return ResponseEntity.ok(response);
        }

        /**
         * Liste toutes les entreprises (Super Admin uniquement)
         */
        @Operation(summary = "Lister toutes les entreprises", description = "Liste toutes les entreprises. Accessible uniquement au Super Admin.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste des entreprises"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Super Admin requis")
        })
        @GetMapping
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<Map<String, Object>> listerToutesEntreprises(Authentication authentication) {

                log.info("Liste de toutes les entreprises demandée par {}", authentication.getName());

                List<EntrepriseDTO> entreprises = entrepriseService.listerToutesEntreprises(authentication.getName());

                Map<String, Object> response = new HashMap<>();
                response.put("entreprises", entreprises);
                response.put("total", entreprises.size());

                return ResponseEntity.ok(response);
        }

        /**
         * Récupère les détails d'une entreprise par son ID
         */
        @Operation(summary = "Obtenir une entreprise", description = "Récupère les détails d'une entreprise via son ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Détails de l'entreprise"),
                        @ApiResponse(responseCode = "404", description = "Entreprise introuvable")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'UTILISATEUR')")
        public ResponseEntity<EntrepriseDTO> obtenirEntreprise(
                        @Parameter(description = "ID de l'entreprise") @PathVariable Long id) {

                log.info("Demande des détails de l'entreprise ID {}", id);

                EntrepriseDTO entrepriseDTO = entrepriseService.trouverParId(id)
                                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));

                return ResponseEntity.ok(entrepriseDTO);
        }

        /**
         * Active ou désactive une entreprise
         */
        @Operation(summary = "Changer l'état d'une entreprise", description = "Active ou désactive une entreprise. Accessible uniquement au Super Admin.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "État modifié avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Super Admin requis"),
                        @ApiResponse(responseCode = "404", description = "Entreprise introuvable")
        })
        @PatchMapping("/{id}/etat")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<Map<String, Object>> changerEtatEntreprise(
                        @Parameter(description = "ID de l'entreprise") @PathVariable Long id,
                        @Parameter(description = "Nouvel état (true = actif, false = inactif)") @RequestParam boolean etat,
                        Authentication authentication) {

                log.info("Changement d'état de l'entreprise {} à {} par {}", id, etat, authentication.getName());

                EntrepriseDTO entrepriseModifiee = entrepriseService.changerEtatEntreprise(id, etat,
                                authentication.getName());

                Map<String, Object> response = new HashMap<>();
                response.put("message", String.format("Entreprise %s avec succès", etat ? "activée" : "désactivée"));
                response.put("entreprise", entrepriseModifiee);

                return ResponseEntity.ok(response);
        }
}