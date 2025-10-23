package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demandes_achat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeDAchat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique de la demande d'achat
     * Format : DA-0042-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String referenceBesoin;
    private String description;
    private String fournisseur;
    // Suppression des champs quantite et prixUnitaire inutiles
    private double montantTotal;
    private String serviceBeneficiaire;
    private LocalDate dateCreation;
    private LocalDate dateAttendu;
    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private String urlFichierJoint; // URL vers devis, spécifications

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    /**
     * Relation OneToOne vers FicheDeBesoin
     * Une DemandeDAchat est créée à partir d'une seule FicheDeBesoin
     */
    @OneToOne
    @JoinColumn(name = "fiche_besoin_id", unique = true)
    @JsonBackReference
    private FicheDeBesoin ficheDeBesoin;

    /**
     * Relation OneToOne vers BonDeCommande
     * Une DemandeDAchat génère un seul BonDeCommande
     */
    @OneToOne(mappedBy = "demandeDAchat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private BonDeCommande bonDeCommande;

}