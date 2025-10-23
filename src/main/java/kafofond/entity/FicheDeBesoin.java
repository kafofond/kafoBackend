package kafofond.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fiches_de_besoin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheDeBesoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique de la fiche de besoin
     * Format : FB-0035-11-2025
     */
    @Column(unique = true, length = 20)
    private String code;

    private String serviceBeneficiaire;
    private String objet;
    private String description;
    private int quantite;
    private double montantEstime;
    private LocalDate dateAttendu;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    private Statut statut;
    
    private String urlFichierJoint; // URL vers document joint

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    /**
     * Relation OneToOne vers DemandeDAchat
     * Une FicheDeBesoin génère une seule DemandeDAchat
     */
    @OneToOne(mappedBy = "ficheDeBesoin", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private DemandeDAchat demandeDAchat;

    /**
     * Relation OneToMany vers Designation
     * Une FicheDeBesoin peut contenir plusieurs désignations (produits/services demandés)
     */
    @OneToMany(mappedBy = "ficheDeBesoin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Designation> designations = new ArrayList<>();

}