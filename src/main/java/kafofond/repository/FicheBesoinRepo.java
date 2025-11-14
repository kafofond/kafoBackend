package kafofond.repository;

import kafofond.entity.FicheDeBesoin;
import kafofond.entity.Entreprise;
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
     * Trouve toutes les fiches de besoin d'une entreprise
     */
    List<FicheDeBesoin> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve toutes les fiches de besoin d'une entreprise et d'un statut
     */
    @Query("SELECT f FROM FicheDeBesoin f WHERE f.entreprise = :entreprise AND f.statut = :statut")
    List<FicheDeBesoin> findByEntrepriseAndStatut(@Param("entreprise") Entreprise entreprise,
                    @Param("statut") Statut statut);

    /**
     * Trouve toutes les fiches de besoin créées par un utilisateur
     */
    List<FicheDeBesoin> findByCreePar(Utilisateur utilisateur);

    /**
     * Trouve toutes les fiches de besoin d'une entreprise par ID
     */
    @Query("SELECT f FROM FicheDeBesoin f WHERE f.entreprise.id = :entrepriseId")
    List<FicheDeBesoin> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

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

    // Méthodes pour les statistiques par créateur
    @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.creePar.id = :utilisateurId")
    long countByCreeParId(@Param("utilisateurId") Long utilisateurId);

    // Méthodes pour les statistiques du gestionnaire
    @Query("SELECT COUNT(f) FROM FicheDeBesoin f WHERE f.entreprise.id = :entrepriseId AND f.statut = 'EN_ATTENTE'")
    long countByEntrepriseIdAndStatutEnAttente(@Param("entrepriseId") Long entrepriseId);
    
    /**
     * Trouve toutes les fiches de besoin approuvées sans demande d'achat associée
     */
    @Query("SELECT f FROM FicheDeBesoin f LEFT JOIN f.demandeDAchat d WHERE f.statut = kafofond.entity.Statut.APPROUVE AND d.id IS NULL")
    List<FicheDeBesoin> findApprovedWithoutDemandeDAchat();
}