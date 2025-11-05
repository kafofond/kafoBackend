package kafofond.repository;

import kafofond.entity.FicheDeBesoin;
import kafofond.entity.Entreprise;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité FicheDeBesoin
 * Fournit les méthodes de recherche pour les fiches de besoin
 */
@Repository
public interface FicheBesoinRepo extends JpaRepository<FicheDeBesoin, Long> {

    /**
     * Trouve toutes les fiches de besoin d'une entreprise
     */
    List<FicheDeBesoin> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve toutes les fiches de besoin par statut
     */
    List<FicheDeBesoin> findByStatut(Statut statut);

    /**
     * Trouve toutes les fiches de besoin créées par un utilisateur
     */
    List<FicheDeBesoin> findByCreePar(Utilisateur creePar);

    /**
     * Trouve toutes les fiches de besoin d'une entreprise par statut
     */
    List<FicheDeBesoin> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.dateCreation >= :startDate AND f.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);
}