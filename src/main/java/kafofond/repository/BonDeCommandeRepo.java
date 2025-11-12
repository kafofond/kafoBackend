package kafofond.repository;

import kafofond.entity.BonDeCommande;
import kafofond.entity.DemandeDAchat;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité BonDeCommande
 * Fournit les méthodes de recherche pour les bons de commande
 */
@Repository
public interface BonDeCommandeRepo extends JpaRepository<BonDeCommande, Long> {

    /**
     * Trouve tous les bons d'une entreprise
     */
    List<BonDeCommande> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve tous les bons par statut
     */
    List<BonDeCommande> findByStatut(Statut statut);

    /**
     * Trouve tous les bons d'une entreprise par statut
     */
    List<BonDeCommande> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

    /**
     * Trouve tous les bons de commande approuvés qui ne sont pas encore associés à une attestation de service fait
     */
    @Query("SELECT b FROM BonDeCommande b WHERE b.statut = kafofond.entity.Statut.APPROUVE AND b.attestationDeServiceFait IS NULL")
    List<BonDeCommande> findApprovedWithoutAttestation();

    /**
     * Trouve un bon de commande par demande d'achat
     */
    BonDeCommande findByDemandeDAchat(DemandeDAchat demandeDAchat);

    /**
     * Trouve un bon de commande par ID avec son entreprise
     */
    @Query("SELECT b FROM BonDeCommande b LEFT JOIN FETCH b.entreprise WHERE b.id = :id")
    Optional<BonDeCommande> findByIdWithEntreprise(@Param("id") Long id);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(b) FROM BonDeCommande b WHERE b.dateCreation >= :startDate AND b.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM BonDeCommande b WHERE b.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

    // Méthodes pour les statistiques par entreprise
    @Query("SELECT COUNT(b) FROM BonDeCommande b WHERE b.entreprise.id = :entrepriseId")
    long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(b) FROM BonDeCommande b WHERE b.entreprise.id = :entrepriseId AND b.dateCreation >= :startDate AND b.dateCreation < :endDate")
    long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}