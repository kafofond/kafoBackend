package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique du budget
     * Format : BUD-0001-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String intituleBudget;
    private String description;
    private double montantBudget;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private Boolean etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneCredit> lignesCredits;

    @PrePersist
    public void prePersist() {
        dateCreation = LocalDate.now();
        dateModification = LocalDateTime.now();
        if (etat == null) etat = false;
        // Le code sera généré par le service après la persistance
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }
}
