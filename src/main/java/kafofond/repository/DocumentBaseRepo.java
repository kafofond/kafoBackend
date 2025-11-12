package kafofond.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * Interface de base pour les repositories de documents
 * Fournit des méthodes de comptage par date communes à tous les documents
 */
public interface DocumentBaseRepo<T> {

    // Méthodes pour les statistiques par date
    @Query("SELECT COUNT(d) FROM #{#entityName} d WHERE d.dateCreation >= :startDate AND d.dateCreation < :endDate")
    long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM #{#entityName} d WHERE d.dateCreation >= :startDate")
    long countByDateCreationAfter(@Param("startDate") LocalDateTime startDate);
}