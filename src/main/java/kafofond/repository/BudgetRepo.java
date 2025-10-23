package kafofond.repository;

import kafofond.entity.Budget;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Budget
 * Fournit les méthodes de recherche pour les budgets
 */
@Repository
public interface BudgetRepo extends JpaRepository<Budget, Long> {
    
    /**
     * Trouve tous les budgets d'une entreprise
     */
    List<Budget> findByEntreprise(Entreprise entreprise);
    
    /**
     * Trouve tous les budgets par statut
     */
    List<Budget> findByStatut(Statut statut);
    
    /**
     * Trouve tous les budgets par état
     */
    List<Budget> findByEtat(boolean etat);
    
    /**
     * Trouve tous les budgets d'une entreprise par statut
     */
    List<Budget> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);
    
    /**
     * Trouve tous les budgets d'une entreprise par état
     */
    List<Budget> findByEntrepriseAndEtat(Entreprise entreprise, boolean etat);
}
