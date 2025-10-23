package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "decisions_prelevement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionDePrelevement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique de la décision de prélèvement
     * Format : DP-0012-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String referenceAttestation;
    private double montant;
    private String compteOrigine;
    private String compteDestinataire;
    private String motifPrelevement;
    private LocalDate dateCreation;
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
     * Relation OneToOne vers AttestationDeServiceFait
     * Une DecisionDePrelevement est générée à partir d'une seule AttestationDeServiceFait
     */
    @OneToOne
    @JoinColumn(name = "attestation_service_fait_id", unique = true)
    @JsonBackReference
    private AttestationDeServiceFait attestationDeServiceFait;

    /**
     * Relation ManyToOne vers LigneCredit
     * Plusieurs décisions peuvent être prélevées sur la même ligne de crédit
     */
    @ManyToOne
    @JoinColumn(name = "ligne_credit_id")
    private LigneCredit ligneCredit;

    /**
     * Relation OneToOne vers OrdreDePaiement
     * Une DecisionDePrelevement génère un seul OrdreDePaiement
     */
    @OneToOne(mappedBy = "decisionDePrelevement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private OrdreDePaiement ordreDePaiement;

}
