package kafofond.repository;

import kafofond.entity.DecisionDePrelevement;
import kafofond.entity.Entreprise;
import kafofond.entity.AttestationDeServiceFait;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité DecisionDePrelevement
 * Fournit les méthodes de recherche pour les décisions de prélèvement
 */
@Repository
public interface DecisionDePrelevementRepo extends JpaRepository<DecisionDePrelevement, Long> {

    /**
     * Trouve toutes les décisions d'une entreprise
     */
    List<DecisionDePrelevement> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve toutes les décisions par statut
     */
    List<DecisionDePrelevement> findByStatut(Statut statut);

    /**
     * Trouve toutes les décisions d'une entreprise par statut
     */
    List<DecisionDePrelevement> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

    /**
     * Trouve toutes les décisions d'une attestation de service fait
     */
    List<DecisionDePrelevement> findByAttestationDeServiceFait(AttestationDeServiceFait attestationDeServiceFait);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(d) FROM DecisionDePrelevement d WHERE d.dateCreation >= :startDate AND d.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM DecisionDePrelevement d WHERE d.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);
}