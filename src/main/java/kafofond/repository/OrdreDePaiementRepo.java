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
}