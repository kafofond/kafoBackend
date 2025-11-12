package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordres_paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreDePaiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique de l'ordre de paiement
     * Format : OP-0012-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String referenceDecisionPrelevement;
    private double montant;
    private String description;
    private String compteOrigine;
    private String compteDestinataire;
    private LocalDate dateExecution;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    /**
     * Relation OneToOne vers DecisionDePrelevement
     * Un OrdreDePaiement est généré à partir d'une seule DecisionDePrelevement
     */
    @OneToOne
    @JoinColumn(name = "decision_id", unique = true)
    @JsonBackReference
    private DecisionDePrelevement decisionDePrelevement;

    /**
     * Relation ManyToOne vers LigneCredit
     * Plusieurs ordres de paiement peuvent être liés à la même ligne de crédit
     */
    @ManyToOne
    @JoinColumn(name = "ligne_credit_id")
    private LigneCredit ligneCredit;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}