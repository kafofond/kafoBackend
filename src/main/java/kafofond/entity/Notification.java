package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String message;
    
    /**
     * État de la notification interne (pour l'application)
     * true = lu, false = non lu
     */
    @Column(name = "etat")
    private Boolean etat;
    
    /**
     * État de transmission par email
     * true = email envoyé avec succès, false = échec d'envoi, null = pas d'email envoyé
     */
    @Column(name = "transmission")
    private Boolean transmission;
    
    private LocalDateTime dateEnvoi;
    private Long idDocument;

    @ManyToOne
    @JoinColumn(name = "destinataire_id")
    private Utilisateur destinataire;
    
    @PrePersist
    public void prePersist() {
        if (etat == null) etat = false; // Par défaut non lu
        if (dateEnvoi == null) dateEnvoi = LocalDateTime.now();
    }
}
