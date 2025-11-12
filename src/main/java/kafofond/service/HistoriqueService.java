package kafofond.service;

import kafofond.entity.HistoriqueAction;
import kafofond.entity.Utilisateur;
import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import kafofond.repository.HistoriqueActionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HistoriqueService {

    private final HistoriqueActionRepo historiqueActionRepo;

    /**
     * Enregistre une action pour les documents ou entités
     * Pour entités booléennes : ancienEtat/nouveauEtat ("ACTIF"/"INACTIF")
     * Pour documents enum : ancienStatut/nouveauStatut
     */
    @Transactional
    public HistoriqueAction enregistrerAction(
            String typeDocument,
            Long idDocument,
            String action,
            Utilisateur utilisateur,
            String ancienEtat,
            String nouveauEtat,
            String ancienStatut,
            String nouveauStatut,
            String commentaire
    ) {
        log.info("Enregistrement de l'action {} sur {} #{} par {}",
                action, typeDocument, idDocument, utilisateur.getEmail());

        HistoriqueAction historique = HistoriqueAction.builder()
                .typeDocument(typeDocument)
                .idDocument(idDocument)
                .action(action)
                .ancienEtat(ancienEtat)
                .nouveauEtat(nouveauEtat)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .dateAction(LocalDateTime.now())
                .utilisateur(utilisateur)
                .entreprise(utilisateur.getEntreprise())
                .build();

        return historiqueActionRepo.save(historique);
    }

    /**
     * Enregistre une action de création pour un document
     */
    @Transactional
    public HistoriqueAction enregistrerCreation(
            String typeDocument,
            Long idDocument,
            Utilisateur utilisateur,
            Statut statut
    ) {
        return enregistrerAction(
                typeDocument,
                idDocument,
                "CREATION",
                utilisateur,
                null,
                null,
                null,
                statut != null ? statut.name() : null,
                "Document créé"
        );
    }

    /**
     * Enregistre une action de création pour une entité avec état
     */
    @Transactional
    public HistoriqueAction enregistrerCreation(
            String typeDocument,
            Long idDocument,
            Utilisateur utilisateur,
            boolean etat,
            Statut statut
    ) {
        return enregistrerAction(
                typeDocument,
                idDocument,
                "CREATION",
                utilisateur,
                null,
                etat ? "ACTIF" : "INACTIF",
                null,
                statut != null ? statut.name() : null,
                "Entité créée"
        );
    }

    /**
     * Enregistre une action de modification pour un document
     */
    @Transactional
    public HistoriqueAction enregistrerModification(
            String typeDocument,
            Long idDocument,
            Utilisateur utilisateur,
            Statut ancienStatut,
            Statut nouveauStatut
    ) {
        return enregistrerAction(
                typeDocument,
                idDocument,
                "MODIFICATION",
                utilisateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                nouveauStatut != null ? nouveauStatut.name() : null,
                "Document modifié"
        );
    }

    /**
     * Enregistre une action de modification pour une entité avec état
     */
    @Transactional
    public HistoriqueAction enregistrerModification(
            String typeDocument,
            Long idDocument,
            Utilisateur utilisateur,
            boolean ancienEtat,
            boolean nouveauEtat,
            Statut ancienStatut,
            Statut nouveauStatut
    ) {
        return enregistrerAction(
                typeDocument,
                idDocument,
                "MODIFICATION",
                utilisateur,
                ancienEtat ? "ACTIF" : "INACTIF",
                nouveauEtat ? "ACTIF" : "INACTIF",
                ancienStatut != null ? ancienStatut.name() : null,
                nouveauStatut != null ? nouveauStatut.name() : null,
                "Entité modifiée"
        );
    }

    public List<HistoriqueAction> consulterHistorique(String typeDocument, Long idDocument) {
        log.info("Consultation de l'historique pour {} #{}", typeDocument, idDocument);
        List<HistoriqueAction> historique = historiqueActionRepo.findByTypeDocumentAndIdDocument(typeDocument, idDocument);
        // Initialiser les entités pour éviter les problèmes de proxy lors de la sérialisation
        initializeHistoriqueActions(historique);
        return historique;
    }

    public List<HistoriqueAction> consulterHistoriqueEntreprise(Entreprise entreprise) {
        log.info("Consultation de l'historique complet pour l'entreprise ID: {}", entreprise.getId());
        List<HistoriqueAction> historique = historiqueActionRepo.findByEntreprise(entreprise);
        // Initialiser les entités pour éviter les problèmes de proxy lors de la sérialisation
        initializeHistoriqueActions(historique);
        return historique;
    }

    public List<HistoriqueAction> consulterHistoriqueParType(Entreprise entreprise, String typeDocument) {
        log.info("Consultation de l'historique pour l'entreprise ID: {} et le type {}", entreprise.getId(), typeDocument);
        List<HistoriqueAction> historique = historiqueActionRepo.findByEntrepriseAndTypeDocument(entreprise, typeDocument);
        // Initialiser les entités pour éviter les problèmes de proxy lors de la sérialisation
        initializeHistoriqueActions(historique);
        return historique;
    }

    public List<HistoriqueAction> consulterHistoriqueUtilisateur(Utilisateur utilisateur) {
        log.info("Consultation de l'historique pour l'utilisateur ID: {}", utilisateur.getId());
        List<HistoriqueAction> historique = historiqueActionRepo.findByUtilisateur(utilisateur);
        // Initialiser les entités pour éviter les problèmes de proxy lors de la sérialisation
        initializeHistoriqueActions(historique);
        return historique;
    }
    
    /**
     * Initialise les entités liées pour éviter les problèmes de proxy
     */
    private void initializeHistoriqueActions(List<HistoriqueAction> historique) {
        for (HistoriqueAction action : historique) {
            // Accéder aux propriétés simples pour forcer l'initialisation
            if (action.getUtilisateur() != null) {
                action.getUtilisateur().getId(); // Force l'initialisation du proxy
                action.getUtilisateur().getEmail(); // Force l'initialisation du proxy
                action.getUtilisateur().getNom(); // Force l'initialisation du proxy
                action.getUtilisateur().getPrenom(); // Force l'initialisation du proxy
            }
            if (action.getEntreprise() != null) {
                action.getEntreprise().getId(); // Force l'initialisation du proxy
            }
        }
    }
}