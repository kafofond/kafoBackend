package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attestations_service_fait")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttestationDeServiceFait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique de l'attestation de service fait
     * Format : ASF-0018-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String referenceBonCommande;
    private String fournisseur;
    private String titre;
    private String constat;
    private LocalDate dateLivraison;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;

    // ⚠️ Statut supprimé : une AttestationDeServiceFait est émise une seule fois
    // private Statut statut; // SUPPRIMÉ

    private String urlFichierJoint;

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    /**
     * Relation OneToOne vers BonDeCommande
     * Une AttestationDeServiceFait est générée à partir d'un seul BonDeCommande
     */
    @OneToOne
    @JoinColumn(name = "bon_commande_id", unique = true)
    @JsonBackReference
    private BonDeCommande bonDeCommande;

    /**
     * Relation OneToOne vers DecisionDePrelevement
     * Une AttestationDeServiceFait génère une seule DecisionDePrelevement
     */
    @OneToOne(mappedBy = "attestationDeServiceFait", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private DecisionDePrelevement decisionDePrelevement;
}
