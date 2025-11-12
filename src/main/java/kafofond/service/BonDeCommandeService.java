package kafofond.service;

import kafofond.dto.BonDeCommandeDTO;
import kafofond.entity.BonDeCommande;
import kafofond.entity.DemandeDAchat;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import kafofond.entity.TypeDocument;
import kafofond.mapper.BonDeCommandeMapper;
import kafofond.repository.BonDeCommandeRepo;
import kafofond.repository.UtilisateurRepo;
import kafofond.repository.EntrepriseRepo;
import kafofond.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des bons de commande
 * Implémente le workflow : EN_COURS → VALIDÉ par Comptable → APPROUVÉ par Responsable → génère PDF
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BonDeCommandeService {

    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final BonDeCommandeMapper bonDeCommandeMapper;
    private final DocumentService documentService;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final TableValidationService tableValidationService;
    private final CommentaireService commentaireService;
    private final CodeGeneratorService codeGeneratorService;
    private final UtilisateurService utilisateurService;

    /**
     * Génère un bon de commande à partir d'une demande d'achat approuvée
     */
    @Transactional
    public BonDeCommande genererDepuisDA(DemandeDAchat demandeDAchat, Utilisateur comptable) {
        log.info("Génération d'un bon de commande depuis la demande d'achat {}", demandeDAchat.getId());

        BonDeCommande bonDeCommande = BonDeCommande.builder()
                .fournisseur(demandeDAchat.getFournisseur())
                .description(demandeDAchat.getDescription())
                // Suppression des champs quantite et prixUnitaire inutiles
                // Calcul automatique du montant total depuis la demande d'achat
                .montantTotal(demandeDAchat.getMontantTotal())
                .serviceBeneficiaire(demandeDAchat.getServiceBeneficiaire())
                .modePaiement("Virement bancaire")
                .dateCreation(LocalDate.now().atStartOfDay())
                .delaiPaiement(LocalDate.now().plusDays(30))
                .dateExecution(LocalDate.now().plusDays(7))
                .statut(Statut.EN_COURS)
                .creePar(comptable)
                .entreprise(demandeDAchat.getEntreprise())
                .demandeDAchat(demandeDAchat)
                .build();

        // Sauvegarder d'abord pour obtenir l'ID
        BonDeCommande bonCree = bonDeCommandeRepo.save(bonDeCommande);
        
        // Générer le code automatiquement
        String code = codeGeneratorService.generateBonCommandeCode(bonCree.getId(), LocalDate.from(bonCree.getDateCreation()));
        bonCree.setCode(code);
        bonCree = bonDeCommandeRepo.save(bonCree);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BON_COMMANDE",
                bonCree.getId(),
                "GENERATION_AUTOMATIQUE",
                comptable,
                null,                       // ancienEtat
                null,                       // nouveauEtat
                null,                       // ancienStatut
                Statut.EN_COURS.name(),     // nouveauStatut
                "Généré automatiquement depuis DA #" + demandeDAchat.getId()
        );

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerCreation(
                bonCree.getId(),
                kafofond.entity.TypeDocument.BON_COMMANDE,
                comptable
        );

        // Notifier le Comptable
        Utilisateur responsable = trouverResponsable(comptable.getEntreprise());
        if (responsable != null) {
            notificationService.notifierModification("BON_COMMANDE", bonCree.getId(),
                    comptable, responsable, "généré");
        }

        return bonCree;
    }

    /**
     * Personnalise un bon de commande (Comptable, Responsable, Directeur)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public BonDeCommandeDTO personnaliserDTO(Long id, BonDeCommandeDTO bonDTO, String emailModificateur) {
        log.info("Personnalisation du bon de commande {} par {}", id, emailModificateur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur modificateur = utilisateurRepo.findByEmail(emailModificateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (modificateur.getEntreprise() != null) {
            modificateur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        BonDeCommande bon = bonDeCommandeMapper.toEntity(bonDTO);
        BonDeCommande bonPersonnalise = personnaliser(id, bon, modificateur);
        
        // Initialiser les relations pour éviter les problèmes de lazy loading
        if (bonPersonnalise.getCreePar() != null) {
            bonPersonnalise.getCreePar().getNom();
            bonPersonnalise.getCreePar().getPrenom();
            bonPersonnalise.getCreePar().getEmail();
            if (bonPersonnalise.getCreePar().getEntreprise() != null) {
                bonPersonnalise.getCreePar().getEntreprise().getNom();
            }
        }
        if (bonPersonnalise.getEntreprise() != null) {
            bonPersonnalise.getEntreprise().getNom();
        }
        if (bonPersonnalise.getDemandeDAchat() != null) {
            bonPersonnalise.getDemandeDAchat().getId();
        }
        
        return bonDeCommandeMapper.toDTO(bonPersonnalise);
    }

    /**
     * Personnalise un bon de commande (Comptable, Responsable, Directeur)
     */
    @Transactional
    public BonDeCommande personnaliser(Long id, BonDeCommande bonModifie, Utilisateur modificateur) {
        log.info("Personnalisation du bon de commande {} par {}", id, modificateur.getEmail());

        BonDeCommande bon = bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable"));

        // Vérifier les droits
        if (modificateur.getRole() != kafofond.entity.Role.COMPTABLE &&
                modificateur.getRole() != kafofond.entity.Role.RESPONSABLE &&
                modificateur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seuls le Comptable, Responsable et Directeur peuvent personnaliser un bon de commande");
        }

        Statut ancienStatut = bon.getStatut();

        // Mettre à jour les champs
        bon.setFournisseur(bonModifie.getFournisseur());
        bon.setDescription(bonModifie.getDescription());
        // Suppression des champs quantite et prixUnitaire inutiles
        bon.setMontantTotal(bonModifie.getMontantTotal());
        bon.setServiceBeneficiaire(bonModifie.getServiceBeneficiaire());
        bon.setModePaiement(bonModifie.getModePaiement());
        bon.setDelaiPaiement(bonModifie.getDelaiPaiement());
        bon.setDateExecution(bonModifie.getDateExecution());

        // Si le bon était validé ou approuvé, repasser en EN_COURS
        if (bon.getStatut() == Statut.VALIDE || bon.getStatut() == Statut.APPROUVE) {
            bon.setStatut(Statut.EN_COURS);
        }

        // Si le bon n'a pas de code, le générer
        if (bon.getCode() == null || bon.getCode().isEmpty()) {
            String code = codeGeneratorService.generateBonCommandeCode(bon.getId(), LocalDate.from(bon.getDateCreation()));
            bon.setCode(code);
        }

        BonDeCommande bonPersonnalise = bonDeCommandeRepo.save(bon);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BON_COMMANDE",
                id,
                "PERSONNALISATION",
                modificateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                bon.getStatut() != null ? bon.getStatut().name() : null,
                null
        );

        // Notifier le Responsable si ce n'est pas lui qui modifie
        if (modificateur.getRole() != kafofond.entity.Role.RESPONSABLE &&
                modificateur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            Utilisateur responsable = trouverResponsable(modificateur.getEntreprise());
            if (responsable != null) {
                notificationService.notifierModification("BON_COMMANDE", id,
                        modificateur, responsable, "personnalisé");
            }
        }

        return bonPersonnalise;
    }

    /**
     * Valide un bon de commande (Comptable)
     * Première étape du workflow: EN_COURS → VALIDÉ
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public BonDeCommande validerDTO(Long id, String emailValidateur) {
        log.info("Validation du bon de commande {} par {}", id, emailValidateur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur validateur = utilisateurRepo.findByEmail(emailValidateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (validateur.getEntreprise() != null) {
            validateur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return valider(id, validateur);
    }

    /**
     * Valide un bon de commande (Comptable)
     * Première étape du workflow: EN_COURS → VALIDÉ
     */
    @Transactional
    public BonDeCommande valider(Long id, Utilisateur validateur) {
        log.info("Validation du bon de commande {} par {}", id, validateur.getEmail());

        // Seul le Comptable peut valider (première étape)
        if (validateur.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut valider un bon de commande");
        }

        BonDeCommande bon = bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable"));

        // Vérifier que le bon est en cours
        if (bon.getStatut() != Statut.EN_COURS) {
            throw new IllegalArgumentException("Le bon de commande doit être en cours pour être validé");
        }

        Statut ancienStatut = bon.getStatut();
        bon.setStatut(Statut.VALIDE);

        // Si le bon n'a pas de code, le générer
        if (bon.getCode() == null || bon.getCode().isEmpty()) {
            String code = codeGeneratorService.generateBonCommandeCode(bon.getId(), LocalDate.from(bon.getDateCreation()));
            bon.setCode(code);
        }

        BonDeCommande bonValide = bonDeCommandeRepo.save(bon);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BON_COMMANDE",
                id,
                "VALIDATION",
                validateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.VALIDE.name(),
                "Bon de commande validé"  // Ajout du commentaire
        );

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                TypeDocument.BON_COMMANDE,
                validateur,
                "VALIDE",
                "Bon de commande validé"
        );

        // Notifier le Responsable pour approbation
        // Récupérer le responsable dans la même transaction
        Utilisateur responsable = trouverResponsableDansTransaction(validateur.getEntreprise().getId(), validateur.getEntreprise().getNom());
        if (responsable != null) {
            notificationService.notifierValidation("BON_COMMANDE", id, validateur,
                    responsable, "validé et en attente d'approbation", null);
        }

        // Initialiser les relations pour éviter les problèmes de lazy loading
        if (bonValide.getCreePar() != null) {
            bonValide.getCreePar().getNom();
            bonValide.getCreePar().getPrenom();
            bonValide.getCreePar().getEmail();
            if (bonValide.getCreePar().getEntreprise() != null) {
                bonValide.getCreePar().getEntreprise().getNom();
            }
        }
        if (bonValide.getEntreprise() != null) {
            bonValide.getEntreprise().getNom();
        }
        if (bonValide.getDemandeDAchat() != null) {
            bonValide.getDemandeDAchat().getId();
        }
        
        return bonValide;
    }

    /**
     * Approuve un bon de commande (Responsable)
     * Deuxième étape du workflow: VALIDÉ → APPROUVÉ (génère PDF)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public BonDeCommande approuverDTO(Long id, String emailApprobateur) {
        log.info("Approbation du bon de commande {} par {}", id, emailApprobateur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur approbateur = utilisateurRepo.findByEmail(emailApprobateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (approbateur.getEntreprise() != null) {
            approbateur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return approuver(id, approbateur);
    }

    /**
     * Approuve un bon de commande (Responsable)
     * Deuxième étape du workflow: VALIDÉ → APPROUVÉ (génère PDF)
     */
    @Transactional
    public BonDeCommande approuver(Long id, Utilisateur approbateur) {
        log.info("Approbation du bon de commande {} par {}", id, approbateur.getEmail());

        // Seul le Responsable peut approuver (deuxième étape)
        if (approbateur.getRole() != kafofond.entity.Role.RESPONSABLE &&
            approbateur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Responsable ou le Directeur peut approuver un bon de commande");
        }

        BonDeCommande bon = bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable"));

        // Vérifier que le bon est validé
        if (bon.getStatut() != Statut.VALIDE) {
            throw new IllegalArgumentException("Le bon de commande doit être validé pour être approuvé");
        }

        Statut ancienStatut = bon.getStatut();
        bon.setStatut(Statut.APPROUVE);

        // Si le bon n'a pas de code, le générer
        if (bon.getCode() == null || bon.getCode().isEmpty()) {
            String code = codeGeneratorService.generateBonCommandeCode(bon.getId(), LocalDate.from(bon.getDateCreation()));
            bon.setCode(code);
        }

        BonDeCommande bonApprouve = bonDeCommandeRepo.save(bon);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BON_COMMANDE",
                id,
                "APPROBATION",
                approbateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.APPROUVE.name(),
                "Bon de commande approuvé"  // Ajout du commentaire
        );

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                TypeDocument.BON_COMMANDE,
                approbateur,
                "APPROUVE",
                "Bon de commande approuvé"
        );

        // Générer le PDF
        try {
            String urlPdf = documentService.genererBonCommandePdf(bon);
            bon.setUrlPdf(urlPdf);
            bonDeCommandeRepo.save(bon);
            log.info("PDF généré pour le bon de commande {} : {}", id, urlPdf);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le bon de commande {} : {}", id, e.getMessage());
        }

        // Notifier le créateur
        if (bon.getCreePar() != null) {
            notificationService.notifierValidation("BON_COMMANDE", id, approbateur,
                    bon.getCreePar(), "approuvé", null);
        }

        // Initialiser les relations pour éviter les problèmes de lazy loading
        if (bonApprouve.getCreePar() != null) {
            bonApprouve.getCreePar().getNom();
            bonApprouve.getCreePar().getPrenom();
            bonApprouve.getCreePar().getEmail();
            if (bonApprouve.getCreePar().getEntreprise() != null) {
                bonApprouve.getCreePar().getEntreprise().getNom();
            }
        }
        if (bonApprouve.getEntreprise() != null) {
            bonApprouve.getEntreprise().getNom();
        }
        if (bonApprouve.getDemandeDAchat() != null) {
            bonApprouve.getDemandeDAchat().getId();
        }
        
        return bonApprouve;
    }

    /**
     * Rejette un bon de commande avec commentaire obligatoire
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public BonDeCommande rejeterDTO(Long id, String emailValidateur, String commentaire) {
        log.info("Rejet du bon de commande {} par {}", id, emailValidateur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur validateur = utilisateurRepo.findByEmail(emailValidateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (validateur.getEntreprise() != null) {
            validateur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return rejeter(id, validateur, commentaire);
    }

    /**
     * Rejette un bon de commande avec commentaire obligatoire
     */
    @Transactional
    public BonDeCommande rejeter(Long id, Utilisateur validateur, String commentaire) {
        log.info("Rejet du bon de commande {} par {}", id, validateur.getEmail());

        // Responsable, Directeur ou Comptable peuvent rejeter
        if (validateur.getRole() != kafofond.entity.Role.RESPONSABLE &&
                validateur.getRole() != kafofond.entity.Role.DIRECTEUR &&
                validateur.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seuls le Responsable, le Directeur et le Comptable peuvent rejeter un bon de commande");
        }

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
        }

        BonDeCommande bon = bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable"));

        Statut ancienStatut = bon.getStatut();
        bon.setStatut(Statut.REJETE);

        // Si le bon n'a pas de code, le générer
        if (bon.getCode() == null || bon.getCode().isEmpty()) {
            String code = codeGeneratorService.generateBonCommandeCode(bon.getId(), LocalDate.from(bon.getDateCreation()));
            bon.setCode(code);
        }

        BonDeCommande bonRejete = bonDeCommandeRepo.save(bon);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BON_COMMANDE",
                id,
                "REJET",
                validateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.REJETE.name(),
                commentaire  // Le commentaire est déjà présent
        );

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                TypeDocument.BON_COMMANDE,
                validateur,
                "REJETE",
                commentaire
        );

        // L'enregistrement du commentaire se fait maintenant dans la table de validation
        // commentaireService.creerCommentaire(
        //         id,
        //         TypeDocument.BON_COMMANDE,
        //         commentaire,
        //         validateur
        // );

        // Notifier le créateur
        if (bon.getCreePar() != null) {
            notificationService.notifierValidation("BON_COMMANDE", id, validateur,
                    bon.getCreePar(), "rejeté", commentaire);
        }

        // Initialiser les relations pour éviter les problèmes de lazy loading
        if (bonRejete.getCreePar() != null) {
            bonRejete.getCreePar().getNom();
            bonRejete.getCreePar().getPrenom();
            bonRejete.getCreePar().getEmail();
            if (bonRejete.getCreePar().getEntreprise() != null) {
                bonRejete.getCreePar().getEntreprise().getNom();
            }
        }
        if (bonRejete.getEntreprise() != null) {
            bonRejete.getEntreprise().getNom();
        }
        if (bonRejete.getDemandeDAchat() != null) {
            bonRejete.getDemandeDAchat().getId();
        }
        
        return bonRejete;
    }

    /**
     * Génère le PDF d'un bon de commande
     */
    public String genererPdf(Long id) {
        log.info("Génération du PDF pour le bon de commande {}", id);

        BonDeCommande bon = bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable"));

        try {
            String urlPdf = documentService.genererBonCommandePdf(bon);
            bon.setUrlPdf(urlPdf);
            bonDeCommandeRepo.save(bon);
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le bon de commande {} : {}", id, e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Récupère un bon de commande par son ID
     */
    public BonDeCommande getBonDeCommandeById(Long id) {
        return bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable avec ID: " + id));
    }

    /**
     * Trouve le responsable d'une entreprise
     */
    private Utilisateur trouverResponsable(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("responsable@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    /**
     * Trouve le responsable d'une entreprise dans une transaction
     * Cette méthode est utilisée pour éviter les problèmes de lazy loading
     */
    @Transactional
    public Utilisateur trouverResponsableDansTransaction(Long entrepriseId, String entrepriseNom) {
        // Utiliser le nom de l'entreprise pour trouver le responsable
        return utilisateurRepo.findByEmail("responsable@" + entrepriseNom.toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    /**
     * Liste tous les bons de commande d'une entreprise
     */
    public List<BonDeCommande> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return bonDeCommandeRepo.findByEntreprise(entreprise);
    }
    
    /**
     * Liste tous les bons de commande d'une entreprise par son ID
     */
    public List<BonDeCommande> listerParEntrepriseId(Long entrepriseId) {
        // Récupérer l'entreprise par son ID
        kafofond.entity.Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise introuvable avec ID: " + entrepriseId));
        return bonDeCommandeRepo.findByEntreprise(entreprise);
    }
    
    /**
     * Liste tous les bons de commande d'une entreprise et retourne les DTO
     */
    @Transactional(readOnly = true)
    public List<BonDeCommandeDTO> listerParEntrepriseDTO(String emailUtilisateur) {
        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur utilisateur = utilisateurRepo.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        List<BonDeCommande> bons = bonDeCommandeRepo.findByEntreprise(utilisateur.getEntreprise());
        
        // Initialiser les relations pour éviter les problèmes de lazy loading
        for (BonDeCommande bon : bons) {
            if (bon.getCreePar() != null) {
                bon.getCreePar().getNom();
                bon.getCreePar().getPrenom();
                bon.getCreePar().getEmail();
                if (bon.getCreePar().getEntreprise() != null) {
                    bon.getCreePar().getEntreprise().getNom();
                }
            }
            if (bon.getEntreprise() != null) {
                bon.getEntreprise().getNom();
            }
            if (bon.getDemandeDAchat() != null) {
                bon.getDemandeDAchat().getId();
            }
        }
        
        return bons.stream()
                .map(bonDeCommandeMapper::toDTO)
                .toList();
    }

    /**
     * Trouve un bon de commande par ID
     */
    public Optional<BonDeCommande> trouverParId(Long id) {
        return bonDeCommandeRepo.findById(id);
    }
    
    /**
     * Trouve un bon de commande par ID et retourne le DTO
     */
    @Transactional(readOnly = true)
    public BonDeCommandeDTO obtenirBonDeCommandeDTO(Long id) {
        BonDeCommande bon = bonDeCommandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de commande introuvable"));
        
        // Initialiser les relations pour éviter les problèmes de lazy loading
        if (bon.getCreePar() != null) {
            bon.getCreePar().getNom();
            bon.getCreePar().getPrenom();
            bon.getCreePar().getEmail();
            if (bon.getCreePar().getEntreprise() != null) {
                bon.getCreePar().getEntreprise().getNom();
            }
        }
        if (bon.getEntreprise() != null) {
            bon.getEntreprise().getNom();
        }
        if (bon.getDemandeDAchat() != null) {
            bon.getDemandeDAchat().getId();
        }
        
        return bonDeCommandeMapper.toDTO(bon);
    }

    /**
     * Trouve un bon de commande par demande d'achat
     */
    public Optional<BonDeCommande> trouverParDemandeDAchat(DemandeDAchat demandeDAchat) {
        return Optional.ofNullable(bonDeCommandeRepo.findByDemandeDAchat(demandeDAchat));
    }

    @Transactional(readOnly = true)
    public List<BonDeCommande> getApprovedWithoutAttestation() {
        log.info("Récupération des bons de commande approuvés sans attestation de service fait");
        return bonDeCommandeRepo.findApprovedWithoutAttestation();
    }
}