package kafofond.repository;

import kafofond.entity.SeuilValidation;
import kafofond.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'entité SeuilValidation
 * Fournit les méthodes de recherche pour les seuils de validation
 */
@Repository
public interface SeuilValidationRepo extends JpaRepository<SeuilValidation, Long> {
    
    /**
     * Trouve le seuil actif d'une entreprise
     */
    Optional<SeuilValidation> findByEntrepriseAndActif(Entreprise entreprise, boolean actif);
}
