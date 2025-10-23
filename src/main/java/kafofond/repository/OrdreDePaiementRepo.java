package kafofond.repository;

import kafofond.entity.OrdreDePaiement;
import kafofond.entity.Entreprise;
import kafofond.entity.DecisionDePrelevement;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité OrdreDePaiement
 * Fournit les méthodes de recherche pour les ordres de paiement
 */
@Repository
public interface OrdreDePaiementRepo extends JpaRepository<OrdreDePaiement, Long> {
    
    /**
     * Trouve tous les ordres d'une entreprise
     */
    List<OrdreDePaiement> findByEntreprise(Entreprise entreprise);
    
    /**
     * Trouve tous les ordres par statut
     */
    List<OrdreDePaiement> findByStatut(Statut statut);
    
    /**
     * Trouve tous les ordres d'une entreprise par statut
     */
    List<OrdreDePaiement> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);
    
    /**
     * Trouve tous les ordres d'une décision de prélèvement
     */
    List<OrdreDePaiement> findByDecisionDePrelevement(DecisionDePrelevement decisionDePrelevement);
}
