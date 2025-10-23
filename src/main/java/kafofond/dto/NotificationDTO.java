package kafofond.dto;

import kafofond.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les notifications
 * Représente une notification dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    
    private Long id;
    private String titre;
    private String message;
    
    /**
     * État de la notification interne (pour l'application)
     * true = lu, false = non lu
     */
    private Boolean etat;
    
    /**
     * État de transmission par email
     * true = email envoyé avec succès, false = échec d'envoi, null = pas d'email envoyé
     */
    private Boolean transmission;
    
    private LocalDateTime dateEnvoi;
    private String typeDocument;
    private Long idDocument;
    private String destinataireNom;
    private String destinataireEmail;
    private String entrepriseNom;
    
    /**
     * Convertit une entité Notification en DTO
     */
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .titre(notification.getTitre())
                .message(notification.getMessage())
                .etat(notification.getEtat())
                .transmission(notification.getTransmission())
                .dateEnvoi(notification.getDateEnvoi())
                .idDocument(notification.getIdDocument())
                .destinataireNom(notification.getDestinataire() != null ? 
                    notification.getDestinataire().getPrenom() + " " + notification.getDestinataire().getNom() : null)
                .destinataireEmail(notification.getDestinataire() != null ? notification.getDestinataire().getEmail() : null)
                .build();
    }
}
