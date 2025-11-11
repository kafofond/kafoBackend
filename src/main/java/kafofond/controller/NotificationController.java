package kafofond.controller;

import kafofond.entity.Notification;
import kafofond.entity.Utilisateur;
import kafofond.repository.NotificationRepo;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des notifications
 * Permet de consulter et marquer les notifications comme lues
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationRepo notificationRepo;
    private final UtilisateurService utilisateurService;

    /**
     * Liste toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<?> listerNotifications(Authentication authentication) {
        try {
            log.info("Liste des notifications demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<Notification> notifications = notificationRepo.findByDestinataire(utilisateur);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("total", notifications.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des notifications : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste toutes les notifications d'un utilisateur spécifique (par ID)
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<?> listerNotificationsParUtilisateurId(@PathVariable Long utilisateurId, Authentication authentication) {
        try {
            log.info("Liste des notifications de l'utilisateur {} demandée par {}", 
                    utilisateurId, authentication.getName());
            
            // Vérifier que l'utilisateur a le droit d'accéder à ces notifications
            Utilisateur utilisateurActuel = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Vérifier que l'utilisateur demandé existe
            Utilisateur utilisateurCible = utilisateurService.trouverParId(utilisateurId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));
            
            // Vérifier que l'utilisateur actuel a le droit d'accéder aux notifications de l'utilisateur cible
            // (soit il s'agit de lui-même, soit il est admin)
            if (!utilisateurActuel.getId().equals(utilisateurId) && 
                utilisateurActuel.getRole() != kafofond.entity.Role.SUPER_ADMIN) {
                throw new RuntimeException("Accès non autorisé aux notifications de cet utilisateur");
            }
            
            List<Notification> notifications = notificationRepo.findByDestinataire(utilisateurCible);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("total", notifications.size());
            response.put("utilisateurId", utilisateurId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des notifications par utilisateur ID : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Compte les notifications non lues de l'utilisateur connecté
     */
    @GetMapping("/non-lues")
    public ResponseEntity<?> compterNotificationsNonLues(Authentication authentication) {
        try {
            log.info("Comptage des notifications non lues demandé par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            long nombreNonLues = notificationRepo.countByDestinataireAndEtat(utilisateur, false);
            
            Map<String, Object> response = new HashMap<>();
            response.put("nombreNonLues", nombreNonLues);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du comptage des notifications non lues : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Compte les notifications non lues d'un utilisateur spécifique (par ID)
     */
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public ResponseEntity<?> compterNotificationsNonLuesParUtilisateurId(@PathVariable Long utilisateurId, Authentication authentication) {
        try {
            log.info("Comptage des notifications non lues de l'utilisateur {} demandé par {}", 
                    utilisateurId, authentication.getName());
            
            // Vérifier que l'utilisateur a le droit d'accéder à ces notifications
            Utilisateur utilisateurActuel = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Vérifier que l'utilisateur demandé existe
            Utilisateur utilisateurCible = utilisateurService.trouverParId(utilisateurId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));
            
            // Vérifier que l'utilisateur actuel a le droit d'accéder aux notifications de l'utilisateur cible
            if (!utilisateurActuel.getId().equals(utilisateurId) && 
                utilisateurActuel.getRole() != kafofond.entity.Role.SUPER_ADMIN) {
                throw new RuntimeException("Accès non autorisé aux notifications de cet utilisateur");
            }
            
            long nombreNonLues = notificationRepo.countByDestinataireAndEtat(utilisateurCible, false);
            
            Map<String, Object> response = new HashMap<>();
            response.put("nombreNonLues", nombreNonLues);
            response.put("utilisateurId", utilisateurId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du comptage des notifications non lues par utilisateur ID : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Marque une notification comme lue
     */
    @PostMapping("/{id}/marquer-lu")
    public ResponseEntity<?> marquerCommeLu(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Marquage de la notification {} comme lue par {}", id, authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            Notification notification = notificationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notification introuvable"));
            
            // Vérifier que la notification appartient à l'utilisateur
            if (!notification.getDestinataire().getId().equals(utilisateur.getId())) {
                throw new RuntimeException("Vous n'avez pas accès à cette notification");
            }
            
            notification.setEtat(true);  // Marquée comme lue
            notificationRepo.save(notification);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification marquée comme lue");
            response.put("notification", notification);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du marquage de la notification : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Marque toutes les notifications comme lues
     */
    @PostMapping("/marquer-toutes-lues")
    public ResponseEntity<?> marquerToutesCommeLues(Authentication authentication) {
        try {
            log.info("Marquage de toutes les notifications comme lues par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<Notification> notificationsNonLues = notificationRepo.findByDestinataireAndEtat(utilisateur, false);
            
            for (Notification notification : notificationsNonLues) {
                notification.setEtat(true);  // Marquée comme lue
                notificationRepo.save(notification);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Toutes les notifications ont été marquées comme lues");
            response.put("nombreMarquees", notificationsNonLues.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du marquage de toutes les notifications : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
