package kafofond.repository;

import kafofond.entity.Budget;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
         * Trouve tous les budgets d'une entreprise par statut
         */
        List<Budget> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

        // Méthodes pour les statistiques par date
        @Query("SELECT COUNT(b) FROM Budget b WHERE b.dateCreation >= :startDate AND b.dateCreation < :endDate")
        long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(b) FROM Budget b WHERE b.dateCreation >= :startDate")
        long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

        // Méthodes pour les statistiques par entreprise
        @Query("SELECT COUNT(b) FROM Budget b WHERE b.entreprise.id = :entrepriseId")
        long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

        @Query("SELECT COUNT(b) FROM Budget b WHERE b.entreprise.id = :entrepriseId AND b.dateCreation >= :startDate AND b.dateCreation < :endDate")
        long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
                        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(b) FROM Budget b WHERE b.entreprise.id = :entrepriseId AND b.statut = 'EN_COURS'")
        long countByEntrepriseIdAndStatutEnCours(@Param("entrepriseId") Long entrepriseId);

        // Méthodes pour les statistiques du directeur
        @Query("SELECT COALESCE(SUM(b.montantBudget), 0) FROM Budget b WHERE b.entreprise.id = :entrepriseId")
        double sumMontantBudgetByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}