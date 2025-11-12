package kafofond.repository;

import kafofond.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Entreprise
 * Fournit les méthodes de recherche pour les entreprises
 */
@Repository
public interface EntrepriseRepo extends JpaRepository<Entreprise, Long> {

    /**
     * Trouve une entreprise par son nom
     */
    Optional<Entreprise> findByNom(String nom);

    /**
     * Trouve une entreprise par son email
     */
    Optional<Entreprise> findByEmail(String email);

    /**
     * Vérifie si une entreprise existe par nom
     */
    boolean existsByNom(String nom);

    /**
     * Vérifie si une entreprise existe par email
     */
    boolean existsByEmail(String email);

    /**
     * Trouve toutes les entreprises actives
     */
    List<Entreprise> findByEtatTrue();

    /**
     * Trouve toutes les entreprises inactives
     */
    List<Entreprise> findByEtatFalse();

    /**
     * Compte les entreprises actives
     */
    long countByEtatTrue();

    /**
     * Compte les entreprises inactives
     */
    long countByEtatFalse();

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(e) FROM Entreprise e WHERE e.dateCreation >= :startDate AND e.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Entreprise e WHERE e.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);
}