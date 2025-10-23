package kafofond.service;

import kafofond.entity.RapportAchat;
import kafofond.entity.Utilisateur;
import kafofond.entity.Entreprise;
import kafofond.entity.Role;
import kafofond.repository.RapportAchatRepo;
import kafofond.repository.UtilisateurRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour RapportAchat (anciennement PieceJustificativeService)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RapportAchatService {

    private final RapportAchatRepo rapportAchatRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;

    /**
     * Crée un nouveau rapport d'achat (Comptable uniquement)
     */
    @Transactional
    public RapportAchat creer(RapportAchat rapport, Utilisateur comptable) {
        log.info("Création d'un rapport d'achat par {}", comptable.getEmail());

        if (comptable.getRole() != Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut créer des rapports d'achat");
        }

        rapport.setDateAjout(LocalDate.now());
        rapport.setEntreprise(comptable.getEntreprise());

        RapportAchat rapportCree = rapportAchatRepo.save(rapport);

        // Historique
        historiqueService.enregistrerAction(
                "RAPPORT_ACHAT",
                rapportCree.getId(),
                "CREATION",
                comptable,
                null,
                null,
                null,
                null,
                "Créé par Comptable"
        );

        // Notification au directeur de l'entreprise
        Utilisateur directeur = utilisateurRepo.findByEntrepriseAndRole(rapportCree.getEntreprise(), Role.DIRECTEUR)
                .orElse(null);

        if (directeur != null) {
            notificationService.notifierModification(
                    "RAPPORT_ACHAT",
                    rapportCree.getId(),
                    comptable,
                    directeur,
                    "Nouveau rapport d'achat créé"
            );
        }

        return rapportCree;
    }

    /**
     * Liste tous les rapports d'achat d'une entreprise
     */
    public List<RapportAchat> listerParEntreprise(Entreprise entreprise) {
        return rapportAchatRepo.findByEntreprise(entreprise);
    }

    /**
     * Récupère les détails d'un rapport d'achat
     */
    public Optional<RapportAchat> trouverParId(Long id) {
        return rapportAchatRepo.findById(id);
    }

    /**
     * Récupère tous les rapports contenant un document spécifique
     */
    public List<RapportAchat> listerParDocument(String document) {
        List<RapportAchat> result = rapportAchatRepo.findByBonCommande(document);
        result.addAll(rapportAchatRepo.findByFicheBesoin(document));
        result.addAll(rapportAchatRepo.findByDemandeAchat(document));
        result.addAll(rapportAchatRepo.findByAttestationServiceFait(document));
        result.addAll(rapportAchatRepo.findByDecisionPrelevement(document));
        result.addAll(rapportAchatRepo.findByOrdrePaiement(document));
        return result;
    }
    
    /**
     * Récupère tous les rapports contenant un document spécifique pour une entreprise donnée
     */
    public List<RapportAchat> listerParDocumentEtEntreprise(String document, Entreprise entreprise) {
        List<RapportAchat> tousRapports = listerParDocument(document);
        return tousRapports.stream()
                .filter(rapport -> rapport.getEntreprise().getId().equals(entreprise.getId()))
                .collect(Collectors.toList());
    }
}