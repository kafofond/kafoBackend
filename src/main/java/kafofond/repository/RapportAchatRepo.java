package kafofond.repository;

import kafofond.entity.RapportAchat;
import kafofond.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour RapportAchat (anciennement PieceJustificativeRepo)
 */
@Repository
public interface RapportAchatRepo extends JpaRepository<RapportAchat, Long> {

    /**
     * Trouve tous les rapports d'achat d'une entreprise
     */
    List<RapportAchat> findByEntreprise(Entreprise entreprise);

    /**
     * Trouve tous les rapports d'achat d'une entreprise par son ID
     */
    @Query("SELECT r FROM RapportAchat r WHERE r.entreprise.id = :entrepriseId")
    List<RapportAchat> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    /**
     * Trouve tous les rapports d'achat contenant un document spécifique
     */
    List<RapportAchat> findByBonCommande(String bonCommande);

    List<RapportAchat> findByFicheBesoin(String ficheBesoin);

    List<RapportAchat> findByDemandeAchat(String demandeAchat);

    List<RapportAchat> findByAttestationServiceFait(String attestationServiceFait);

    List<RapportAchat> findByDecisionPrelevement(String decisionPrelevement);

    List<RapportAchat> findByOrdrePaiement(String ordrePaiement);
}
