package kafofond.controller;

import kafofond.dto.HistoriqueDTO;
import kafofond.entity.HistoriqueAction;
import kafofond.entity.Utilisateur;
import kafofond.service.HistoriqueService;
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
 * Controller pour la gestion de l'historique des actions
 * Permet de consulter l'historique des actions effectuées sur les documents
 */
@RestController
@RequestMapping("/api/historique")
@RequiredArgsConstructor
@Slf4j
public class HistoriqueController {

    private final HistoriqueService historiqueService;
    private final UtilisateurService utilisateurService;

    /**
     * Consulte l'historique d'un document spécifique
     */
    @GetMapping("/{typeDocument}/{idDocument}")
    public ResponseEntity<?> consulterHistoriqueDocument(
            @PathVariable String typeDocument,
            @PathVariable Long idDocument,
            Authentication authentication) {
        try {
            log.info("Consultation de l'historique du document {} #{} par {}", 
                    typeDocument, idDocument, authentication.getName());

            List<HistoriqueAction> historique = historiqueService.consulterHistorique(typeDocument, idDocument);
            
            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<HistoriqueDTO> historiqueDTO = historique.stream()
                    .map(HistoriqueDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("historique", historiqueDTO);
            response.put("total", historiqueDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation de l'historique : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Consulte l'historique complet de l'entreprise
     */
    @GetMapping("/entreprise")
    public ResponseEntity<?> consulterHistoriqueEntreprise(Authentication authentication) {
        try {
            log.info("Consultation de l'historique complet de l'entreprise par {}", authentication.getName());

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<HistoriqueAction> historique = historiqueService.consulterHistoriqueEntreprise(utilisateur.getEntreprise());

            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<HistoriqueDTO> historiqueDTO = historique.stream()
                    .map(HistoriqueDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("historique", historiqueDTO);
            response.put("total", historiqueDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation de l'historique de l'entreprise : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Consulte l'historique de l'entreprise par type de document
     */
    @GetMapping("/entreprise/{typeDocument}")
    public ResponseEntity<?> consulterHistoriqueParType(
            @PathVariable String typeDocument,
            Authentication authentication) {
        try {
            log.info("Consultation de l'historique de l'entreprise pour le type {} par {}", 
                    typeDocument, authentication.getName());

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<HistoriqueAction> historique = historiqueService.consulterHistoriqueParType(
                    utilisateur.getEntreprise(), typeDocument);

            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<HistoriqueDTO> historiqueDTO = historique.stream()
                    .map(HistoriqueDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("historique", historiqueDTO);
            response.put("total", historiqueDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation de l'historique par type : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Consulte l'historique des actions d'un utilisateur
     */
    @GetMapping("/utilisateur")
    public ResponseEntity<?> consulterHistoriqueUtilisateur(Authentication authentication) {
        try {
            log.info("Consultation de l'historique de l'utilisateur {}", authentication.getName());

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<HistoriqueAction> historique = historiqueService.consulterHistoriqueUtilisateur(utilisateur);

            // Convertir en DTO pour éviter les problèmes de sérialisation
            List<HistoriqueDTO> historiqueDTO = historique.stream()
                    .map(HistoriqueDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("historique", historiqueDTO);
            response.put("total", historiqueDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la consultation de l'historique utilisateur : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}