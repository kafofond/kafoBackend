package kafofond.service;

import kafofond.dto.EntrepriseDTO;
import kafofond.entity.*;
import kafofond.repository.*;
import kafofond.dto.HistoriqueDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HistoriqueService {

    private final HistoriqueActionRepo historiqueActionRepo;
    private final BudgetRepo budgetRepo;
    private final LigneCreditRepo ligneCreditRepo;
    private final FicheBesoinRepo ficheBesoinRepo;
    private final DemandeDAchatRepo demandeDAchatRepo;
    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final AttestationDeServiceFaitRepo attestationDeServiceFaitRepo;
    private final DecisionDePrelevementRepo decisionDePrelevementRepo;
    private final OrdreDePaiementRepo ordreDePaiementRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final UtilisateurRepo utilisateurRepo;

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
    
    /**
     * Enrichit les DTO d'historique avec les codes des documents et les noms des entreprises/utilisateurs
     * @param historiqueDTO Liste des DTO à enrichir
     * @param entreprise L'entreprise concernée
     */
    public void enrichirHistoriqueDTO(List<HistoriqueDTO> historiqueDTO, Entreprise entreprise) {
        // Récupérer tous les documents nécessaires par type
        Map<Long, String> budgetCodes = new HashMap<>();
        Map<Long, String> ligneCreditCodes = new HashMap<>();
        Map<Long, String> ficheBesoinCodes = new HashMap<>();
        Map<Long, String> demandeAchatCodes = new HashMap<>();
        Map<Long, String> bonCommandeCodes = new HashMap<>();
        Map<Long, String> attestationServiceCodes = new HashMap<>();
        Map<Long, String> decisionPrelevementCodes = new HashMap<>();
        Map<Long, String> ordrePaiementCodes = new HashMap<>();
        Map<Long, String> entrepriseNoms = new HashMap<>();
        Map<Long, String> utilisateurNoms = new HashMap<>();
        
        // Récupérer les budgets
        List<Budget> budgets = budgetRepo.findByEntreprise(entreprise);
        for (Budget budget : budgets) {
            budgetCodes.put(budget.getId(), budget.getCode());
        }
        
        // Récupérer les lignes de crédit
        List<LigneCredit> lignesCredit = ligneCreditRepo.findByBudgetEntreprise(entreprise);
        for (LigneCredit ligne : lignesCredit) {
            ligneCreditCodes.put(ligne.getId(), ligne.getCode());
        }
        
        // Récupérer les fiches de besoin
        List<FicheDeBesoin> fichesBesoin = ficheBesoinRepo.findByEntreprise(entreprise);
        for (FicheDeBesoin fiche : fichesBesoin) {
            ficheBesoinCodes.put(fiche.getId(), fiche.getCode());
        }
        
        // Récupérer les demandes d'achat
        List<DemandeDAchat> demandesAchat = demandeDAchatRepo.findByEntreprise(entreprise);
        for (DemandeDAchat demande : demandesAchat) {
            demandeAchatCodes.put(demande.getId(), demande.getCode());
        }
        
        // Récupérer les bons de commande
        List<BonDeCommande> bonsCommande = bonDeCommandeRepo.findByEntreprise(entreprise);
        for (BonDeCommande bon : bonsCommande) {
            bonCommandeCodes.put(bon.getId(), bon.getCode());
        }
        
        // Récupérer les attestations de service fait
        List<AttestationDeServiceFait> attestations = attestationDeServiceFaitRepo.findByEntreprise(entreprise);
        for (AttestationDeServiceFait attestation : attestations) {
            attestationServiceCodes.put(attestation.getId(), attestation.getCode());
        }
        
        // Récupérer les décisions de prélèvement
        List<DecisionDePrelevement> decisions = decisionDePrelevementRepo.findByEntreprise(entreprise);
        for (DecisionDePrelevement decision : decisions) {
            decisionPrelevementCodes.put(decision.getId(), decision.getCode());
        }
        
        // Récupérer les ordres de paiement
        List<OrdreDePaiement> ordres = ordreDePaiementRepo.findByEntreprise(entreprise);
        for (OrdreDePaiement ordre : ordres) {
            ordrePaiementCodes.put(ordre.getId(), ordre.getCode());
        }
        
        // Récupérer les entreprises
        entrepriseNoms.put(entreprise.getId(), entreprise.getNom());
        
        // Récupérer les utilisateurs
        List<Utilisateur> utilisateurs = utilisateurRepo.findByEntreprise(entreprise);
        for (Utilisateur utilisateur : utilisateurs) {
            utilisateurNoms.put(utilisateur.getId(), utilisateur.getPrenom() + " " + utilisateur.getNom());
        }
        
        // Enrichir chaque DTO
        for (HistoriqueDTO dto : historiqueDTO) {
            // Ajouter le code du document selon le type
            if ("BUDGET".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(budgetCodes.get(dto.getIdDocument()));
            } else if ("LIGNE_CREDIT".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(ligneCreditCodes.get(dto.getIdDocument()));
            } else if ("FICHE_BESOIN".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(ficheBesoinCodes.get(dto.getIdDocument()));
            } else if ("DEMANDE_ACHAT".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(demandeAchatCodes.get(dto.getIdDocument()));
            } else if ("BON_COMMANDE".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(bonCommandeCodes.get(dto.getIdDocument()));
            } else if ("ATTESTATION_SERVICE_FAIT".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(attestationServiceCodes.get(dto.getIdDocument()));
            } else if ("DECISION_PRELEVEMENT".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(decisionPrelevementCodes.get(dto.getIdDocument()));
            } else if ("ORDRE_PAIEMENT".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                dto.setDocumentCode(ordrePaiementCodes.get(dto.getIdDocument()));
            }
            
            // Cas spécial : pour les actions sur les utilisateurs, afficher le nom de l'utilisateur concerné
            if ("UTILISATEUR".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                // Récupérer l'utilisateur concerné par l'action
                Optional<Utilisateur> utilisateurConcerne = utilisateurRepo.findById(dto.getIdDocument());
                if (utilisateurConcerne.isPresent()) {
                    Utilisateur user = utilisateurConcerne.get();
                    dto.setUtilisateurConcerneNom(user.getPrenom() + " " + user.getNom());
                    // Ne pas écraser l'email et le nom de l'entreprise de l'auteur
                }
            }
            // Cas spécial : pour les actions sur les entreprises, afficher le nom de l'entreprise concernée
            else if ("ENTREPRISE".equals(dto.getTypeDocument()) && dto.getIdDocument() != null) {
                // Récupérer l'entreprise concernée par l'action
                Optional<Entreprise> entrepriseConcernee = entrepriseRepo.findById(dto.getIdDocument());
                if (entrepriseConcernee.isPresent()) {
                    Entreprise ent = entrepriseConcernee.get();
                    dto.setEntrepriseNom(ent.getNom());
                }
            }
            // Pour les autres types d'actions, utiliser les noms de l'utilisateur et de l'entreprise qui ont effectué l'action
            else {
                // Ajouter le nom de l'entreprise
                if (dto.getEntrepriseId() != null) {
                    dto.setEntrepriseNom(entrepriseNoms.get(dto.getEntrepriseId()));
                }
                
                // Ajouter le nom de l'utilisateur
                if (dto.getUtilisateurId() != null) {
                    dto.setUtilisateurNomComplet(utilisateurNoms.get(dto.getUtilisateurId()));
                }
            }
        }
    }
}