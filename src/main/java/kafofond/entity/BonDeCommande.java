package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bons_de_commande")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonDeCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique du bon de commande
     * Format : BC-0018-11-2025
     * Généré automatiquement à la création
     */
    @Column(unique = true, length = 20)
    private String code;

    private String fournisseur;
    private String description;
    // Suppression des champs quantite et prixUnitaire inutiles
    private double montantTotal;
    private String serviceBeneficiaire;
    private String modePaiement;
    private LocalDate dateCreation;
    private LocalDate delaiPaiement;
    private LocalDate dateExecution;
    
    @CreationTimestamp
    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private String urlPdf;

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    /**
     * Relation OneToOne vers DemandeDAchat
     * Un BonDeCommande est généré à partir d'une seule DemandeDAchat
     */
    @OneToOne
    @JoinColumn(name = "demande_achat_id", unique = true)
    @JsonBackReference
    private DemandeDAchat demandeDAchat;

    /**
     * Relation OneToOne vers AttestationDeServiceFait
     * Un BonDeCommande génère une seule AttestationDeServiceFait
     */
    @OneToOne(mappedBy = "bonDeCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private AttestationDeServiceFait attestationDeServiceFait;

}