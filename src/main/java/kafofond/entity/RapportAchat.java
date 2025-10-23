package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Rapport d'achat (anciennement PieceJustificative)
 * Registre de tous les documents liés à une dépense.
 * Chaque champ correspond à un document du cycle de vie.
 */
@Entity
@Table(name = "rapports_achat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapportAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    // Documents du cycle de vie de la dépense
    @Column(name = "fiche_besoin")
    private String ficheBesoin;

    @Column(name = "demande_achat")
    private String demandeAchat;

    @Column(name = "bon_commande")
    private String bonCommande;

    @Column(name = "attestation_service_fait")
    private String attestationServiceFait;

    @Column(name = "decision_prelevement")
    private String decisionPrelevement;

    @Column(name = "ordre_paiement")
    private String ordrePaiement;

    // Date d'ajout du rapport
    private LocalDate dateAjout;

    // Entreprise liée
    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

}
