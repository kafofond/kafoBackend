package kafofond.repository;

import kafofond.entity.HistoriqueAction;
import kafofond.entity.Entreprise;
import kafofond.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité HistoriqueAction
 * Fournit les méthodes de recherche pour l'historique des actions
 */
@Repository
public interface HistoriqueActionRepo extends JpaRepository<HistoriqueAction, Long> {
    
    /**
     * Trouve toutes les actions d'une entreprise
     */
    List<HistoriqueAction> findByEntreprise(Entreprise entreprise);
    
    /**
     * Trouve toutes les actions d'un type de document et d'un document spécifique
     */
    List<HistoriqueAction> findByTypeDocumentAndIdDocument(String typeDocument, Long idDocument);
    
    /**
     * Trouve toutes les actions d'un utilisateur
     */
    List<HistoriqueAction> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Trouve toutes les actions d'une entreprise par type de document
     */
    List<HistoriqueAction> findByEntrepriseAndTypeDocument(Entreprise entreprise, String typeDocument);
}
