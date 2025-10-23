package kafofond.controller;

import kafofond.dto.SeuilValidationDTO;
import kafofond.entity.SeuilValidation;
import kafofond.entity.Utilisateur;
import kafofond.mapper.SeuilValidationMapper;
import kafofond.service.SeuilValidationService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller pour la gestion des seuils de validation
 * Permet au Directeur de configurer les seuils par entreprise
 */
@RestController
@RequestMapping("/api/seuils")
@RequiredArgsConstructor
@Slf4j
public class SeuilValidationController {

    private final SeuilValidationService seuilValidationService;
    private final UtilisateurService utilisateurService;
    private final SeuilValidationMapper seuilValidationMapper;

    /**
     * Configure un seuil de validation (Directeur uniquement)
     */
    @PostMapping
    public ResponseEntity<?> configurerSeuil(@RequestBody SeuilValidationDTO seuilDTO, Authentication authentication) {
        try {
            log.info("Configuration d'un seuil de validation par {}", authentication.getName());
            
            SeuilValidation seuil = seuilValidationMapper.toEntity(seuilDTO);
            SeuilValidationDTO seuilConfigureDTO = seuilValidationService.configurerSeuilDTO(seuil, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Seuil de validation configuré avec succès");
            response.put("seuil", seuilConfigureDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la configuration du seuil : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Obtient le seuil de validation actif pour l'entreprise
     */
    @GetMapping("/actif")
    public ResponseEntity<?> obtenirSeuilActif(Authentication authentication) {
        try {
            log.info("Récupération du seuil actif par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            SeuilValidationDTO seuilActifDTO = seuilValidationService.obtenirSeuilActifDTO(utilisateur.getEntreprise());
            
            if (seuilActifDTO == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Aucun seuil de validation configuré");
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(seuilActifDTO);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du seuil actif : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste tous les seuils de l'entreprise
     */
    @GetMapping
    public ResponseEntity<?> listerSeuils(Authentication authentication) {
        try {
            log.info("Liste des seuils demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            var seuils = seuilValidationService.listerSeuilsParEntreprise(utilisateur.getEntreprise());
            
            Map<String, Object> response = new HashMap<>();
            response.put("seuils", seuils);
            response.put("total", seuils.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des seuils : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Obtient les détails d'un seuil
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirSeuil(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Détails du seuil {} demandés par {}", id, authentication.getName());
            
            return seuilValidationService.trouverParIdDTO(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du seuil : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifie un seuil existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierSeuil(@PathVariable Long id, @RequestBody SeuilValidationDTO seuilDTO, 
                                          Authentication authentication) {
        try {
            log.info("Modification du seuil {} par {}", id, authentication.getName());
            
            SeuilValidation modification = seuilValidationMapper.toEntity(seuilDTO);
            SeuilValidationDTO seuilModifieDTO = seuilValidationService.modifierSeuilDTO(id, modification, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Seuil modifié avec succès");
            response.put("seuil", seuilModifieDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la modification du seuil : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Active un seuil (désactive automatiquement les autres)
     */
    @PostMapping("/{id}/activer")
    public ResponseEntity<?> activerSeuil(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Activation du seuil {} par {}", id, authentication.getName());
            
            SeuilValidationDTO seuilActiveDTO = seuilValidationService.activerSeuilDTO(id, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Seuil activé avec succès");
            response.put("seuil", seuilActiveDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'activation du seuil : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Désactive un seuil
     */
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverSeuil(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Désactivation du seuil {} par {}", id, authentication.getName());
            
            SeuilValidationDTO seuilDesactiveDTO = seuilValidationService.desactiverSeuilDTO(id, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Seuil désactivé avec succès");
            response.put("seuil", seuilDesactiveDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la désactivation du seuil : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
