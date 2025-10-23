package kafofond.controller;

import kafofond.dto.TableValidationDTO;
import kafofond.entity.TableValidation;
import kafofond.entity.Utilisateur;
import kafofond.service.TableValidationService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion de la table de validation
 * Permet de consulter les validations effectuées sur les documents
 */
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@Slf4j
public class TableValidationController {

    private final TableValidationService tableValidationService;
    private final UtilisateurService utilisateurService;

    /**
     * Consulte les validations d'un document spécifique
     */
    @GetMapping("/{typeDocument}/{idDocument}")
    public ResponseEntity<?> consulterValidationsDocument(
            @PathVariable String typeDocument,
            @PathVariable Long idDocument,
            Authentication authentication) {
        try {
            log.info("Consultation des validations du document {} #{} par {}", 
                    typeDocument, idDocument, authentication.getName());

            // Convertir le typeDocument en enum
            kafofond.entity.TypeDocument typeDoc = kafofond.entity.TypeDocument.valueOf(typeDocument);
            
            List<TableValidation> validations = tableValidationService.consulterValidationsDocument(idDocument, typeDoc);
            
            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<TableValidationDTO> validationsDTO = validations.stream()
                    .map(TableValidationDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("validations", validationsDTO);
            response.put("total", validationsDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation des validations : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Consulte les validations d'un utilisateur (validateur)
     */
    @GetMapping("/validateur")
    public ResponseEntity<?> consulterValidationsValidateur(Authentication authentication) {
        try {
            log.info("Consultation des validations de l'utilisateur {}", authentication.getName());

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<TableValidation> validations = tableValidationService.consulterValidationsValidateur(utilisateur.getId());

            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<TableValidationDTO> validationsDTO = validations.stream()
                    .map(TableValidationDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("validations", validationsDTO);
            response.put("total", validationsDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation des validations du validateur : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Consulte les validations par type de document
     */
    @GetMapping("/type/{typeDocument}")
    public ResponseEntity<?> consulterValidationsParType(
            @PathVariable String typeDocument,
            Authentication authentication) {
        try {
            log.info("Consultation des validations pour le type {} par {}", 
                    typeDocument, authentication.getName());

            // Convertir le typeDocument en enum
            kafofond.entity.TypeDocument typeDoc = kafofond.entity.TypeDocument.valueOf(typeDocument);
            
            List<TableValidation> validations = tableValidationService.consulterValidationsParType(typeDoc);

            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<TableValidationDTO> validationsDTO = validations.stream()
                    .map(TableValidationDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("validations", validationsDTO);
            response.put("total", validationsDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation des validations par type : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Consulte les validations par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<?> consulterValidationsParStatut(
            @PathVariable String statut,
            Authentication authentication) {
        try {
            log.info("Consultation des validations avec le statut {} par {}", 
                    statut, authentication.getName());

            List<TableValidation> validations = tableValidationService.consulterValidationsParStatut(statut);

            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<TableValidationDTO> validationsDTO = validations.stream()
                    .map(TableValidationDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("validations", validationsDTO);
            response.put("total", validationsDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation des validations par statut : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}