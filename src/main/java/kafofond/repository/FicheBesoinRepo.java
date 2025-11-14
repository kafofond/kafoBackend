package kafofond.repository;

import kafofond.entity.Entreprise;
import kafofond.entity.FicheDeBesoin;
import kafofond.entity.Statut;
import kafofond.entity.Utilisateur;
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
         * Trouve toutes les fiches d'une entreprise
         */
        List<FicheDeBesoin> findByEntreprise(Entreprise entreprise);

        /**
         * Trouve toutes les fiches par statut
         */
        List<FicheDeBesoin> findByStatut(Statut statut);

        /**
         * Trouve toutes les fiches d'une entreprise par statut
         */
        List<FicheDeBesoin> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

        /**
         * Trouve toutes les fiches d'une entreprise par ID
         */
        @Query("SELECT f FROM FicheDeBesoin f WHERE f.entreprise.id = :entrepriseId")
        List<FicheDeBesoin> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

        /**
         * Compte le nombre de fiches de besoin créées par un utilisateur
         */
        @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.creePar.id = :utilisateurId")
        long countByCreeParId(@Param("utilisateurId") Long utilisateurId);

        /**
         * Compte le nombre de fiches de besoin créées par un utilisateur avec un statut
         * spécifique
         */
        @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.creePar.id = :utilisateurId AND f.statut = :statut")
        long countByCreeParIdAndStatut(@Param("utilisateurId") Long utilisateurId, @Param("statut") Statut statut);

        /**
         * Trouve toutes les fiches de besoin créées par un utilisateur
         */
        List<FicheDeBesoin> findByCreePar(Utilisateur utilisateur);

        /**
         * Trouve toutes les fiches de besoin créées par un utilisateur avec un statut
         * spécifique
         */
        List<FicheDeBesoin> findByCreeParAndStatut(Utilisateur utilisateur, Statut statut);

        // Méthodes pour les statistiques par date
        @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.dateCreation >= :startDate AND f.dateCreation < :endDate")
        long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.dateCreation >= :startDate")
        long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

        // Méthodes pour les statistiques par entreprise
        @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.entreprise.id = :entrepriseId")
        long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

        @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.entreprise.id = :entrepriseId AND f.dateCreation >= :startDate AND f.dateCreation < :endDate")
        long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
                        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        /**
         * Trouve toutes les fiches de besoin approuvées sans demande d'achat associée
         */
        @Query("SELECT f FROM FicheDeBesoin f LEFT JOIN f.demandeDAchat d WHERE f.statut = kafofond.entity.Statut.APPROUVE AND d.id IS NULL")
        List<FicheDeBesoin> findApprovedWithoutDemandeDAchat();
}