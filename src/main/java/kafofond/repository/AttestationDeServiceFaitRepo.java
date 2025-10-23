package kafofond.repository;

import kafofond.entity.AttestationDeServiceFait;
import kafofond.entity.Entreprise;
import kafofond.entity.BonDeCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Trouve toutes les attestations d'un bon de commande
     */
    List<AttestationDeServiceFait> findByBonDeCommande(BonDeCommande bonDeCommande);
    
    /**
     * Trouve une attestation par son code unique
     */
    Optional<AttestationDeServiceFait> findByCode(String code);
}
