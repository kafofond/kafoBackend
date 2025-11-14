package kafofond.repository;

import kafofond.entity.AttestationDeServiceFait;
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
 * Repository pour l'entité AttestationDeServiceFait
 * Fournit les méthodes de recherche pour les attestations de service fait
 */
@Repository
public interface AttestationDeServiceFaitRepo extends JpaRepository<AttestationDeServiceFait, Long> {

    /**
     * Trouve toutes les attestations d'une entreprise
     */
    List<AttestationDeServiceFait> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve une attestation par son code unique
     */
    Optional<AttestationDeServiceFait> findByCode(String code);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(a) FROM AttestationDeServiceFait a WHERE a.dateCreation >= :startDate AND a.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM AttestationDeServiceFait a WHERE a.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);

    // Méthodes pour les statistiques par entreprise
    @Query("SELECT COUNT(a) FROM AttestationDeServiceFait a WHERE a.entreprise.id = :entrepriseId")
    long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(a) FROM AttestationDeServiceFait a WHERE a.entreprise.id = :entrepriseId AND a.dateCreation >= :startDate AND a.dateCreation < :endDate")
    long countByEntrepriseIdAndDateCreationBetween(@Param("entrepriseId") Long entrepriseId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
            
    // Méthode pour lister les attestations créées par un utilisateur spécifique
    @Query("SELECT a FROM AttestationDeServiceFait a WHERE a.creePar.id = :utilisateurId")
    List<AttestationDeServiceFait> findByCreeParId(@Param("utilisateurId") Long utilisateurId);
            
    // Méthode pour lister les attestations créées par un utilisateur spécifique dans une entreprise
    @Query("SELECT a FROM AttestationDeServiceFait a WHERE a.creePar.id = :utilisateurId AND a.entreprise.id = :entrepriseId")
    List<AttestationDeServiceFait> findByCreeParIdAndEntrepriseId(@Param("utilisateurId") Long utilisateurId, @Param("entrepriseId") Long entrepriseId);
}