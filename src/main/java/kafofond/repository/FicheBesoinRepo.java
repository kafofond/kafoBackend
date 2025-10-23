package kafofond.repository;

import kafofond.entity.FicheDeBesoin;
import kafofond.entity.Entreprise;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Trouve toutes les fiches de besoin par statut
     */
    List<FicheDeBesoin> findByStatut(Statut statut);
    
    /**
     * Trouve toutes les fiches de besoin créées par un utilisateur
     */
    List<FicheDeBesoin> findByCreePar(Utilisateur creePar);
    
    /**
     * Trouve toutes les fiches de besoin d'une entreprise par statut
     */
    List<FicheDeBesoin> findByEntrepriseAndStatut(Entreprise entreprise, Statut statut);
}
