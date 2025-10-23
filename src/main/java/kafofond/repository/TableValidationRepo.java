package kafofond.repository;

import kafofond.entity.TableValidation;
import kafofond.entity.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
