package kafofond.repository;

import kafofond.entity.LigneCredit;
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
 * Repository pour l'entité LigneCredit
 * Fournit les méthodes de recherche pour les lignes de crédit
 */
@Repository
public interface LigneCreditRepo extends JpaRepository<LigneCredit, Long> {

        /**
         * Trouve toutes les lignes de crédit d'un budget
         */
        List<LigneCredit> findByBudget(Budget budget);

        /**
         * Trouve toutes les lignes de crédit par statut
         */
        List<LigneCredit> findByStatut(Statut statut);

        /**
         * Trouve toutes les lignes de crédit d'une entreprise
         */
        @Query("SELECT l FROM LigneCredit l WHERE l.budget.entreprise = :entreprise")
        List<LigneCredit> findByBudgetEntreprise(@Param("entreprise") Entreprise entreprise);

        /**
         * Trouve toutes les lignes de crédit d'une entreprise et d'un statut
         */
        @Query("SELECT l FROM LigneCredit l WHERE l.budget.entreprise = :entreprise AND l.statut = :statut")
        List<LigneCredit> findByBudgetEntrepriseAndStatut(@Param("entreprise") Entreprise entreprise,
                        @Param("statut") Statut statut);

        /**
         * Calcule le total des montants alloués pour un budget donné
         */
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.budget.id = :budgetId")
        double calculateTotalMontantAllouerByBudgetId(@Param("budgetId") Long budgetId);

        // Méthodes pour les statistiques par date
        @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.dateCreation >= :startDate AND l.dateCreation < :endDate")
        long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.dateCreation >= :startDate")
        long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

        // Méthodes pour les statistiques par entreprise
        @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.budget.entreprise.id = :entrepriseId")
        long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

        @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.budget.entreprise.id = :entrepriseId AND l.dateCreation >= :startDate AND l.dateCreation < :endDate")
        long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
                        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        // Méthodes pour les statistiques du directeur
        @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.budget.entreprise.id = :entrepriseId AND l.statut = 'EN_COURS'")
        long countByEntrepriseIdAndStatutEnAttente(@Param("entrepriseId") Long entrepriseId);
        
        // Méthodes pour les statistiques du responsable
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.budget.entreprise.id = :entrepriseId")
        double sumMontantAllouerByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
        
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.budget.entreprise.id = :entrepriseId AND l.statut = 'APPROUVE'")
        double sumMontantAllouerByEntrepriseIdAndStatut(@Param("entrepriseId") Long entrepriseId);
        
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.statut = 'APPROUVE'")
        double sumMontantAllouer();
        
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.statut = 'APPROUVE' AND DATE(l.dateCreation) = :date")
        double sumMontantAllouerByDate(@Param("date") java.time.LocalDate date);
        
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.statut = 'APPROUVE' AND l.dateCreation >= :startDate AND l.dateCreation < :endDate")
        double sumMontantAllouerBetweenDates(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
        
        @Query("SELECT COALESCE(SUM(l.montantAllouer), 0) FROM LigneCredit l WHERE l.statut = 'APPROUVE' AND DATE(l.dateCreation) = :date AND HOUR(l.dateCreation) = :heure")
        double sumMontantAllouerByDateAndHeure(@Param("date") java.time.LocalDate date, @Param("heure") int heure);
}