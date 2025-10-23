package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entité Designation
 * Représente un produit ou service demandé dans une Fiche de Besoin.
 * Une FicheDeBesoin peut contenir plusieurs désignations.
 */
@Entity
@Table(name = "designations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom du produit ou service demandé
     */
    @Column(nullable = false)
    private String produit;

    /**
     * Quantité demandée
     */
    @Column(nullable = false)
    private int quantite;

    /**
     * Prix unitaire du produit
     */
    @Column(nullable = false)
    private double prixUnitaire;

    /**
     * Montant total = quantité × prix unitaire
     */
    @Column(nullable = false)
    private double montantTotal;

    /**
     * Date de la désignation
     */
    private LocalDate date;

    /**
     * Relation ManyToOne vers FicheDeBesoin
     * Chaque désignation appartient à une seule fiche de besoin
     */
    @ManyToOne
    @JoinColumn(name = "fiche_besoin_id", nullable = false)
    @JsonBackReference
    private FicheDeBesoin ficheDeBesoin;

}
