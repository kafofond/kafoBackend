package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité HistoriqueAction - Trace toutes les actions effectuées sur les documents ou entités
 */
@Entity
@Table(name = "historiques_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeDocument;
    private Long idDocument;
    private String action; // CREATION, MODIFICATION, APPROBATION, VALIDATION, REJET

    // Pour documents avec enum Statut
    private String ancienStatut;
    private String nouveauStatut;

    // Pour entités avec Boolean etat
    private String ancienEtat;
    private String nouveauEtat;

    private LocalDateTime dateAction;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;
}
