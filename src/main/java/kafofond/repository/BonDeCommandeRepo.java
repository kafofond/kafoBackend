package kafofond.repository;

import kafofond.entity.BonDeCommande;
import kafofond.entity.Entreprise;
import kafofond.entity.DemandeDAchat;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité BonDeCommande
 * Fournit les méthodes de recherche pour les bons de commande
 */
@Repository
public interface BonDeCommandeRepo extends JpaRepository<BonDeCommande, Long> {

    /**
     * Trouve tous les bons de commande d'une entreprise
     */
    List<BonDeCommande> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve tous les bons de commande par statut
     */
    List<BonDeCommande> findByStatut(Statut statut);

    /**
     * Trouve tous les bons de commande d'une entreprise par statut
     */
    List<BonDeCommande> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);

    /**
     * Trouve le bon de commande généré à partir d'une demande d'achat
     */
    BonDeCommande findByDemandeDAchat(DemandeDAchat demandeDAchat);

    /**
     * Trouve un bon de commande par ID avec son entreprise
     */
    @Query("SELECT b FROM BonDeCommande b LEFT JOIN FETCH b.entreprise WHERE b.id = :id")
    BonDeCommande findByIdWithEntreprise(@Param("id") Long id);

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(b) FROM BonDeCommande b WHERE b.dateCreation >= :startDate AND b.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM BonDeCommande b WHERE b.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);
}