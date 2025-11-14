package kafofond.repository;

import kafofond.entity.OrdreDePaiement;
import kafofond.entity.DecisionDePrelevement;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité OrdreDePaiement
 * Fournit les méthodes de recherche pour les ordres de paiement
 */
@Repository
public interface OrdreDePaiementRepo extends JpaRepository<OrdreDePaiement, Long> {

        /**
         * Trouve tous les ordres d'une entreprise
         */
        List<OrdreDePaiement> findByEntreprise(Entreprise entreprise);

        /**
         * Trouve tous les ordres par statut
         */
        List<OrdreDePaiement> findByStatut(Statut statut);

        /**
         * Trouve tous les ordres d'une entreprise par statut
         */
        List<OrdreDePaiement> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

        /**
         * Trouve tous les ordres d'une décision de prélèvement
         */
        List<OrdreDePaiement> findByDecisionDePrelevement(DecisionDePrelevement decision);

        // Méthodes pour les statistiques par date
        @Query("SELECT COUNT(o) FROM OrdreDePaiement o WHERE o.dateCreation >= :startDate AND o.dateCreation < :endDate")
        long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(o) FROM OrdreDePaiement o WHERE o.dateCreation >= :startDate")
        long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

        // Méthodes pour les statistiques par entreprise
        @Query("SELECT COUNT(o) FROM OrdreDePaiement o WHERE o.entreprise.id = :entrepriseId")
        long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

        @Query("SELECT COUNT(o) FROM OrdreDePaiement o WHERE o.entreprise.id = :entrepriseId AND o.dateCreation >= :startDate AND o.dateCreation < :endDate")
        long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
                @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
                
        // Méthodes pour les statistiques par statut
        @Query("SELECT COUNT(o) FROM OrdreDePaiement o WHERE o.entreprise.id = :entrepriseId AND o.statut = kafofond.entity.Statut.EN_COURS")
        long countByEntrepriseIdAndStatutEnAttente(@Param("entrepriseId") Long entrepriseId);
        
        // Méthodes pour les statistiques du directeur
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o")
        double sumMontantTotal();
        
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o WHERE o.entreprise.id = :entrepriseId")
        double sumMontantTotalByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
        
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o WHERE o.entreprise.id = :entrepriseId AND DATE(o.dateCreation) = :date")
        double sumMontantTotalByEntrepriseIdAndDate(@Param("entrepriseId") Long entrepriseId, @Param("date") java.time.LocalDate date);
        
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o WHERE o.entreprise.id = :entrepriseId AND DATE(o.dateCreation) = :date AND HOUR(o.dateCreation) = :heure")
        double sumMontantTotalByEntrepriseIdAndDateAndHeure(@Param("entrepriseId") Long entrepriseId, @Param("date") java.time.LocalDate date, @Param("heure") int heure);
        
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o WHERE DATE(o.dateCreation) = :date")
        double sumMontantTotalByDate(@Param("date") java.time.LocalDate date);
        
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o WHERE o.dateCreation >= :startDate AND o.dateCreation < :endDate")
        double sumMontantTotalBetweenDates(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
        
        @Query("SELECT COALESCE(SUM(o.montant), 0) FROM OrdreDePaiement o WHERE DATE(o.dateCreation) = :date AND HOUR(o.dateCreation) = :heure")
        double sumMontantTotalByDateAndHeure(@Param("date") java.time.LocalDate date, @Param("heure") int heure);
}