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

@Repository
public interface LigneCreditRepo extends JpaRepository<LigneCredit, Long> {

    List<LigneCredit> findByBudget(Budget budget);

    List<LigneCredit> findByBudgetEntreprise(Entreprise entreprise);

    List<LigneCredit> findByStatut(Statut statut);

    List<LigneCredit> findByEtat(boolean etat);

    List<LigneCredit> findByBudgetAndStatut(Budget budget, Statut statut);

    List<LigneCredit> findByBudgetEntrepriseAndStatut(Entreprise entreprise, Statut statut);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.dateCreation >= :startDate AND l.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(l) FROM LigneCredit l WHERE l.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);
}