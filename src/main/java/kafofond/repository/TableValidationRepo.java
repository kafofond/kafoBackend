package kafofond.repository;

import kafofond.entity.TableValidation;
import kafofond.entity.TypeDocument;
import kafofond.entity.Entreprise;
import kafofond.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour TableValidation (remplace CommentaireRepo)
 */
@Repository
public interface TableValidationRepo extends JpaRepository<TableValidation, Long> {

    /**
     * Trouve toutes les validations d'un document spécifique
     */
    List<TableValidation> findByIdDocumentAndTypeDocument(Long idDocument, TypeDocument typeDocument);

    /**
     * Trouve toutes les validations d'un validateur
     */
    List<TableValidation> findByValidateurId(Long validateurId);

    /**
     * Trouve toutes les validations par type de document
     */
    List<TableValidation> findByTypeDocument(TypeDocument typeDocument);

    /**
     * Trouve toutes les validations par statut
     */
    List<TableValidation> findByStatut(String statut);

    /**
     * Trouve toutes les validations d'une entreprise
     */
    @Query("SELECT tv FROM TableValidation tv JOIN Utilisateur u ON tv.validateurId = u.id WHERE u.entreprise = :entreprise")
    List<TableValidation> findByEntreprise(@Param("entreprise") Entreprise entreprise);

    /**
     * Trouve toutes les validations d'une entreprise par son ID
     */
    @Query("SELECT tv FROM TableValidation tv JOIN Utilisateur u ON tv.validateurId = u.id WHERE u.entreprise.id = :entrepriseId")
    List<TableValidation> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    /**
     * Trouve toutes les validations d'un utilisateur
     */
    @Query("SELECT tv FROM TableValidation tv WHERE tv.validateurId = :utilisateurId")
    List<TableValidation> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}
