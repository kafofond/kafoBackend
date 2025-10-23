package kafofond.repository;

import kafofond.entity.Designation;
import kafofond.entity.FicheDeBesoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour Designation
 */
@Repository
public interface DesignationRepo extends JpaRepository<Designation, Long> {

    /**
     * Trouve toutes les désignations d'une fiche de besoin
     */
    List<Designation> findByFicheDeBesoin(FicheDeBesoin ficheDeBesoin);

    /**
     * Trouve toutes les désignations par ID de fiche de besoin
     */
    List<Designation> findByFicheDeBesoinId(Long ficheBesoinId);
}
