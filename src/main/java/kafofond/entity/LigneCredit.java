package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lignes_credit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique de la ligne de crédit
     * Format : LC-0035-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String intituleLigne;
    private String description;
    private double montantAllouer;
    private double montantEngager;
    private double montantRestant;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private boolean etat;

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @OneToMany(mappedBy = "ligneCredit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DecisionDePrelevement> decisionDePrelevements;

    @OneToMany(mappedBy = "ligneCredit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdreDePaiement> ordreDePaiements;

}
