package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité TableValidation
 * Remplace l'ancienne entité Commentaire.
 * Sert à stocker les commentaires et statuts de validation pour tous les types de documents.
 * Centralise la traçabilité des validations dans le système.
 */
@Entity
@Table(name = "table_validation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de l'utilisateur qui a validé/rejeté le document
     */
    @Column(name = "validateur_id", nullable = false)
    private Long validateurId;

    /**
     * Commentaire de validation (obligatoire en cas de rejet, optionnel sinon)
     */
    @Column(columnDefinition = "TEXT")
    private String commentaire;

    /**
     * Statut de validation : VALIDE, REJETE, APPROUVE, EN_ATTENTE, etc.
     */
    @Column(nullable = false)
    private String statut;

    /**
     * ID du document validé
     */
    @Column(name = "id_document", nullable = false)
    private Long idDocument;

    /**
     * Type du document validé (FICHE_BESOIN, BON_COMMANDE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false)
    private TypeDocument typeDocument;

    /**
     * Date de création de l'enregistrement de validation
     */
    @Column(name = "date_validation")
    @Builder.Default
    private LocalDateTime dateValidation = LocalDateTime.now();

    /**
     * Relation vers l'utilisateur validateur (optionnelle, pour faciliter les requêtes)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id", insertable = false, updatable = false)
    private Utilisateur validateur;

}
