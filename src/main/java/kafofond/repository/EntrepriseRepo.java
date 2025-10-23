package kafofond.repository;

import kafofond.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Entreprise
 * Fournit les méthodes de recherche pour les entreprises
 */
@Repository
public interface EntrepriseRepo extends JpaRepository<Entreprise, Long> {

    /**
     * Trouve une entreprise par son nom
     */
    Optional<Entreprise> findByNom(String nom);

    /**
     * Trouve une entreprise par son email
     */
    Optional<Entreprise> findByEmail(String email);

    /**
     * Vérifie si une entreprise existe par nom
     */
    boolean existsByNom(String nom);

    /**
     * Vérifie si une entreprise existe par email
     */
    boolean existsByEmail(String email);

    /**
     * Trouve toutes les entreprises actives
     */
    List<Entreprise> findByEtatTrue();

    /**
     * Trouve toutes les entreprises inactives
     */
    List<Entreprise> findByEtatFalse();
}