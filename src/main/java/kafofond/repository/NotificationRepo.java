package kafofond.repository;

import kafofond.entity.Notification;
import kafofond.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Notification
 * Fournit les méthodes de recherche pour les notifications
 */
@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    
    /**
     * Trouve toutes les notifications d'un destinataire
     */
    List<Notification> findByDestinataire(Utilisateur destinataire);
    
    /**
     * Trouve toutes les notifications non lues d'un destinataire
     */
    List<Notification> findByDestinataireAndEtat(Utilisateur destinataire, boolean etat);
    
    /**
     * Compte les notifications non lues d'un destinataire
     */
    long countByDestinataireAndEtat(Utilisateur destinataire, boolean etat);
    
    /**
     * Trouve les notifications par statut de transmission
     */
    List<Notification> findByTransmission(Boolean transmission);
}
