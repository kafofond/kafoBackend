package kafofond.repository;

import kafofond.entity.DemandeDAchat;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité DemandeDAchat
 * Fournit les méthodes de recherche pour les demandes d'achat
 */
@Repository
public interface DemandeDAchatRepo extends JpaRepository<DemandeDAchat, Long> {
    
    /**
     * Trouve toutes les demandes d'achat d'une entreprise
     */
    List<DemandeDAchat> findByEntreprise(Entreprise entreprise);
    
    /**
     * Trouve toutes les demandes d'achat par statut
     */
    List<DemandeDAchat> findByStatut(Statut statut);
    
    /**
     * Trouve toutes les demandes d'achat d'une entreprise par statut
     */
    List<DemandeDAchat> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);
}
