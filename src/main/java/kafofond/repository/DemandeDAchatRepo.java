package kafofond.repository;

import kafofond.entity.DemandeDAchat;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité DemandeDAchat
 * Fournit les méthodes de recherche pour les demandes d'achat
 */
@Repository
public interface DemandeDAchatRepo extends JpaRepository<DemandeDAchat, Long> {

    /**
     * Trouve toutes les demandes d'une entreprise
     */
    List<DemandeDAchat> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve toutes les demandes par statut
     */
    List<DemandeDAchat> findByStatut(Statut statut);

    /**
     * Trouve toutes les demandes d'une entreprise par statut
     */
    List<DemandeDAchat> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(d) FROM DemandeDAchat d WHERE d.dateCreation >= :startDate AND d.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM DemandeDAchat d WHERE d.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

    // Méthodes pour les statistiques par entreprise
    @Query("SELECT COUNT(d) FROM DemandeDAchat d WHERE d.entreprise.id = :entrepriseId")
    long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(d) FROM DemandeDAchat d WHERE d.entreprise.id = :entrepriseId AND d.dateCreation >= :startDate AND d.dateCreation < :endDate")
    long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}