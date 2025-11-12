package kafofond.service;

import kafofond.entity.DemandeDAchat;
import kafofond.entity.BonDeCommande;
import kafofond.entity.Entreprise;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import kafofond.repository.DemandeDAchatRepo;
import kafofond.repository.BonDeCommandeRepo;
import kafofond.repository.UtilisateurRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeDAchatService {

    private final DemandeDAchatRepo demandeDAchatRepo;
    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final BonDeCommandeService bonDeCommandeService;
    private final UtilisateurService utilisateurService;
    private final FicheBesoinService ficheBesoinService;
    private final CodeGeneratorService codeGeneratorService;
    private final TableValidationService tableValidationService;
    private final CommentaireService commentaireService;

    @Transactional
    public DemandeDAchat creer(DemandeDAchat demande, Utilisateur utilisateur) {
        log.info("Création d'une demande d'achat par {}", utilisateur.getEmail());

        // Forcer le chargement de l'entreprise
        if (utilisateur.getEntreprise() != null) {
            utilisateur.getEntreprise().getNom();
        }

        // Autoriser la création par Trésorerie et Gestionnaire
        if (utilisateur.getRole() != kafofond.entity.Role.TRESORERIE &&
                utilisateur.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException(
                    "Seule la Trésorerie et le Gestionnaire peuvent créer des demandes d'achat");
        }

        // Si une fiche de besoin est fournie, récupérer les informations
        if (demande.getFicheDeBesoin() != null && demande.getFicheDeBesoin().getId() != null) {
            kafofond.entity.FicheDeBesoin fiche = ficheBesoinService.trouverParId(demande.getFicheDeBesoin().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiche de besoin introuvable"));

            demande.setFicheDeBesoin(fiche);
            // Récupérer le montant et le service de la fiche de besoin
            demande.setMontantTotal(fiche.getMontantEstime());
            demande.setServiceBeneficiaire(fiche.getServiceBeneficiaire());
            if (demande.getDateAttendu() == null) {
                demande.setDateAttendu(fiche.getDateAttendu());
            }
        }

        demande.setCreePar(utilisateur);
        demande.setEntreprise(utilisateur.getEntreprise());
        demande.setStatut(Statut.EN_COURS);
        demande.setDateCreation(LocalDate.now().atStartOfDay());

        DemandeDAchat demandeCreee = demandeDAchatRepo.save(demande);

        // Générer le code unique automatiquement
        String code = codeGeneratorService.generateDemandeAchatCode(demandeCreee.getId(),
                LocalDate.from(demandeCreee.getDateCreation()));
        demandeCreee.setCode(code);
        demandeCreee = demandeDAchatRepo.save(demandeCreee);

        // Historique corrigé avec 9 arguments
        historiqueService.enregistrerAction(
                "DEMANDE_ACHAT",
                demandeCreee.getId(),
                "CREATION",
                utilisateur,
                null,
                null,
                null,
                Statut.EN_COURS.name(),
                "Créée par " + utilisateur.getRole());

        Utilisateur gestionnaire = trouverGestionnaire(utilisateur.getEntreprise());
        if (gestionnaire != null) {
            notificationService.notifierModification("DEMANDE_ACHAT", demandeCreee.getId(),
                    utilisateur, gestionnaire, "créée");
        }

        return demandeCreee;
    }

    @Transactional
    public kafofond.dto.DemandeDAchatDTO creerDTO(DemandeDAchat demande, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        DemandeDAchat created = creer(demande, utilisateur);

        // Forcer le chargement des relations lazy
        if (created.getFicheDeBesoin() != null) {
            created.getFicheDeBesoin().getCode();
        }
        if (created.getCreePar() != null) {
            created.getCreePar().getNom();
        }
        if (created.getEntreprise() != null) {
            created.getEntreprise().getNom();
        }

        return kafofond.dto.DemandeDAchatDTO.fromEntity(created);
    }

    @Transactional
    public DemandeDAchat modifier(Long id, DemandeDAchat demandeModifiee, Utilisateur modificateur) {
        log.info("Modification de la demande d'achat {} par {}", id, modificateur.getEmail());

        DemandeDAchat demande = demandeDAchatRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'achat introuvable"));

        // Autoriser la modification par Trésorerie et Gestionnaire
        if (modificateur.getRole() != kafofond.entity.Role.TRESORERIE &&
                modificateur.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException(
                    "Seule la Trésorerie et le Gestionnaire peuvent modifier des demandes d'achat");
        }

        Statut ancienStatut = demande.getStatut();

        demande.setReferenceBesoin(demandeModifiee.getReferenceBesoin());
        demande.setDescription(demandeModifiee.getDescription());
        demande.setFournisseur(demandeModifiee.getFournisseur());
        // Suppression des champs quantite et prixUnitaire
        demande.setMontantTotal(demandeModifiee.getMontantTotal());
        demande.setServiceBeneficiaire(demandeModifiee.getServiceBeneficiaire());
        demande.setDateAttendu(demandeModifiee.getDateAttendu());
        demande.setUrlFichierJoint(demandeModifiee.getUrlFichierJoint());

        if (demande.getStatut() == Statut.VALIDE || demande.getStatut() == Statut.APPROUVE) {
            demande.setStatut(Statut.EN_COURS);
        }

        DemandeDAchat demandeModifie = demandeDAchatRepo.save(demande);

        historiqueService.enregistrerAction(
                "DEMANDE_ACHAT",
                id,
                "MODIFICATION",
                modificateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                demande.getStatut() != null ? demande.getStatut().name() : null,
                "Demande modifiée par " + modificateur.getRole());

        Utilisateur gestionnaire = trouverGestionnaire(modificateur.getEntreprise());
        if (gestionnaire != null) {
            notificationService.notifierModification("DEMANDE_ACHAT", id,
                    modificateur, gestionnaire, "modifiée");
        }

        return demandeModifiee;
    }

    @Transactional
    public kafofond.dto.DemandeDAchatDTO modifierDTO(Long id, DemandeDAchat demandeModifiee, String emailModificateur) {
        Utilisateur modificateur = utilisateurService.trouverParEmail(emailModificateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        DemandeDAchat modified = modifier(id, demandeModifiee, modificateur);

        // Forcer le chargement des relations lazy
        if (modified.getFicheDeBesoin() != null) {
            modified.getFicheDeBesoin().getCode();
        }
        if (modified.getCreePar() != null) {
            modified.getCreePar().getNom();
        }
        if (modified.getEntreprise() != null) {
            modified.getEntreprise().getNom();
        }

        return kafofond.dto.DemandeDAchatDTO.fromEntity(modified);
    }

    @Transactional
    public DemandeDAchat valider(Long id, Utilisateur gestionnaire) {
        log.info("Validation de la demande d'achat {} par {}", id, gestionnaire.getEmail());

        if (gestionnaire.getEntreprise() != null) {
            gestionnaire.getEntreprise().getNom();
        }

        if (gestionnaire.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException("Seul le Gestionnaire peut valider une demande d'achat");
        }

        DemandeDAchat demande = demandeDAchatRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'achat introuvable"));

        Statut ancienStatut = demande.getStatut();
        demande.setStatut(Statut.VALIDE);

        DemandeDAchat demandeValidee = demandeDAchatRepo.save(demande);

        historiqueService.enregistrerAction(
                "DEMANDE_ACHAT",
                id,
                "VALIDATION",
                gestionnaire,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.VALIDE.name(),
                "Validée par Gestionnaire");

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DEMANDE_ACHAT,
                gestionnaire,
                "VALIDE",
                "Validée par Gestionnaire");

        if (demande.getCreePar() != null) {
            notificationService.notifierValidation("DEMANDE_ACHAT", id, gestionnaire,
                    demande.getCreePar(), "validée", null);
        }

        Utilisateur comptable = trouverComptable(gestionnaire.getEntreprise());
        if (comptable != null) {
            notificationService.notifierModification("DEMANDE_ACHAT", id,
                    gestionnaire, comptable, "validée");
        }

        return demandeValidee;
    }

    @Transactional
    public kafofond.dto.DemandeDAchatDTO validerDTO(Long id, String emailGestionnaire) {
        Utilisateur gestionnaire = utilisateurService.trouverParEmail(emailGestionnaire)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        DemandeDAchat validated = valider(id, gestionnaire);

        // Forcer le chargement des relations lazy
        if (validated.getFicheDeBesoin() != null) {
            validated.getFicheDeBesoin().getCode();
        }
        if (validated.getCreePar() != null) {
            validated.getCreePar().getNom();
        }
        if (validated.getEntreprise() != null) {
            validated.getEntreprise().getNom();
        }

        return kafofond.dto.DemandeDAchatDTO.fromEntity(validated);
    }

    @Transactional
    public DemandeDAchat approuver(Long id, Utilisateur comptable) {
        log.info("Approbation de la demande d'achat {} par {}", id, comptable.getEmail());

        if (comptable.getEntreprise() != null) {
            comptable.getEntreprise().getNom();
        }

        if (comptable.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut approuver une demande d'achat");
        }

        DemandeDAchat demande = demandeDAchatRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'achat introuvable"));

        Statut ancienStatut = demande.getStatut();
        demande.setStatut(Statut.APPROUVE);

        DemandeDAchat demandeApprouvee = demandeDAchatRepo.save(demande);

        historiqueService.enregistrerAction(
                "DEMANDE_ACHAT",
                id,
                "APPROBATION",
                comptable,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.APPROUVE.name(),
                "Approuvée par Comptable");

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DEMANDE_ACHAT,
                comptable,
                "APPROUVE",
                "Approuvée par Comptable");

        if (demande.getCreePar() != null) {
            notificationService.notifierValidation("DEMANDE_ACHAT", id, comptable,
                    demande.getCreePar(), "approuvée", null);
        }

        BonDeCommande bonDeCommande = genererBonDeCommande(demande, comptable);
        log.info("Bon de commande {} généré automatiquement pour la demande d'achat {}",
                bonDeCommande.getId(), id);

        return demandeApprouvee;
    }

    @Transactional
    public kafofond.dto.DemandeDAchatDTO approuverDTO(Long id, String emailComptable) {
        Utilisateur comptable = utilisateurService.trouverParEmail(emailComptable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        DemandeDAchat approved = approuver(id, comptable);

        // Forcer le chargement des relations lazy
        if (approved.getFicheDeBesoin() != null) {
            approved.getFicheDeBesoin().getCode();
        }
        if (approved.getCreePar() != null) {
            approved.getCreePar().getNom();
        }
        if (approved.getEntreprise() != null) {
            approved.getEntreprise().getNom();
        }

        return kafofond.dto.DemandeDAchatDTO.fromEntity(approved);
    }

    @Transactional
    public DemandeDAchat rejeter(Long id, Utilisateur validateur, String commentaire) {
        log.info("Rejet de la demande d'achat {} par {}", id, validateur.getEmail());

        if (validateur.getRole() != kafofond.entity.Role.GESTIONNAIRE &&
                validateur.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException(
                    "Seuls le Gestionnaire et le Comptable peuvent rejeter une demande d'achat");
        }

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
        }

        DemandeDAchat demande = demandeDAchatRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'achat introuvable"));

        Statut ancienStatut = demande.getStatut();
        demande.setStatut(Statut.REJETE);

        DemandeDAchat demandeRejetee = demandeDAchatRepo.save(demande);

        // L'enregistrement du commentaire se fait maintenant dans la table de
        // validation
        // commentaireService.creerCommentaire(
        // id,
        // kafofond.entity.TypeDocument.DEMANDE_ACHAT,
        // commentaire,
        // validateur
        // );

        // Enregistrer dans TableValidation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DEMANDE_ACHAT,
                validateur,
                "REJETE",
                commentaire);

        historiqueService.enregistrerAction(
                "DEMANDE_ACHAT",
                id,
                "REJET",
                validateur,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.REJETE.name(),
                commentaire);

        if (demande.getCreePar() != null) {
            notificationService.notifierValidation("DEMANDE_ACHAT", id, validateur,
                    demande.getCreePar(), "rejetée", commentaire);
        }

        return demandeRejetee;
    }

    @Transactional
    public kafofond.dto.DemandeDAchatDTO rejeterDTO(Long id, String emailValidateur, String commentaire) {
        Utilisateur validateur = utilisateurService.trouverParEmail(emailValidateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        DemandeDAchat rejected = rejeter(id, validateur, commentaire);

        // Forcer le chargement des relations lazy
        if (rejected.getFicheDeBesoin() != null) {
            rejected.getFicheDeBesoin().getCode();
        }
        if (rejected.getCreePar() != null) {
            rejected.getCreePar().getNom();
        }
        if (rejected.getEntreprise() != null) {
            rejected.getEntreprise().getNom();
        }

        return kafofond.dto.DemandeDAchatDTO.fromEntity(rejected);
    }

    private BonDeCommande genererBonDeCommande(DemandeDAchat demande, Utilisateur comptable) {
        BonDeCommande bonDeCommande = BonDeCommande.builder()
                .fournisseur(demande.getFournisseur())
                .description(demande.getDescription())
                .montantTotal(demande.getMontantTotal())
                .serviceBeneficiaire(demande.getServiceBeneficiaire())
                .modePaiement("Virement bancaire")
                .dateCreation(LocalDate.now().atStartOfDay())
                .delaiPaiement(LocalDate.now().plusDays(30))
                .dateExecution(LocalDate.now().plusDays(7))
                .statut(Statut.EN_COURS)
                .creePar(comptable)
                .entreprise(demande.getEntreprise())
                .demandeDAchat(demande)
                .build();

        return bonDeCommandeRepo.save(bonDeCommande);
    }

    private Utilisateur trouverGestionnaire(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo
                .findByEmail("gestionnaire@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    private Utilisateur trouverComptable(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("comptable@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    /**
     * Compte le nombre total de demandes d'achat créées par un utilisateur
     */
    public long compterParUtilisateur(Long utilisateurId) {
        return demandeDAchatRepo.countByCreeParId(utilisateurId);
    }

    /**
     * Compte le nombre de demandes d'achat créées par un utilisateur avec un statut spécifique
     */
    public long compterParUtilisateurEtStatut(Long utilisateurId, Statut statut) {
        return demandeDAchatRepo.countByCreeParIdAndStatut(utilisateurId, statut);
    }

    /**
     * Récupère toutes les demandes d'achat créées par un utilisateur
     */
    public List<DemandeDAchat> listerParUtilisateur(Long utilisateurId) {
        return utilisateurRepo.findById(utilisateurId)
                .map(utilisateur -> demandeDAchatRepo.findByCreePar(utilisateur))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    /**
     * Récupère toutes les demandes d'achat créées par un utilisateur avec un statut spécifique
     */
    public List<DemandeDAchat> listerParUtilisateurEtStatut(Long utilisateurId, Statut statut) {
        return utilisateurRepo.findById(utilisateurId)
                .map(utilisateur -> demandeDAchatRepo.findByCreeParAndStatut(utilisateur, statut))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    /**
     * Récupère les statistiques des demandes d'achat pour un utilisateur
     * Retourne le nombre total et le nombre par statut
     */
    public java.util.Map<String, Long> obtenirStatistiquesParUtilisateur(Long utilisateurId) {
        java.util.Map<String, Long> statistiques = new java.util.HashMap<>();
        
        statistiques.put("total", compterParUtilisateur(utilisateurId));
        statistiques.put("en_cours", compterParUtilisateurEtStatut(utilisateurId, Statut.EN_COURS));
        statistiques.put("valide", compterParUtilisateurEtStatut(utilisateurId, Statut.VALIDE));
        statistiques.put("approuve", compterParUtilisateurEtStatut(utilisateurId, Statut.APPROUVE));
        statistiques.put("rejete", compterParUtilisateurEtStatut(utilisateurId, Statut.REJETE));
        
        return statistiques;
    }

    public List<DemandeDAchat> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return demandeDAchatRepo.findByEntreprise(entreprise);
    }

    /**
     * Liste toutes les demandes d'achat créées par un utilisateur
     */
    public List<DemandeDAchat> listerParCreateur(Utilisateur utilisateur) {
        return demandeDAchatRepo.findByCreePar(utilisateur);
    }

    /**
     * Liste toutes les demandes d'achat d'une entreprise (version avec paramètre
     * ID)
     */
    public List<DemandeDAchat> listerParEntrepriseId(Long entrepriseId) {
        return demandeDAchatRepo.findByEntrepriseId(entrepriseId);
    }

    /**
     * Liste toutes les demandes d'achat créées par un utilisateur et retourne les
     * DTOs
     */
    @Transactional(readOnly = true)
    public List<kafofond.dto.DemandeDAchatDTO> listerParCreateurDTO(Utilisateur utilisateur) {
        List<DemandeDAchat> demandes = demandeDAchatRepo.findByCreePar(utilisateur);
        return demandes.stream()
                .map(demande -> {
                    // Forcer le chargement des relations lazy
                    if (demande.getFicheDeBesoin() != null) {
                        demande.getFicheDeBesoin().getCode();
                    }
                    if (demande.getCreePar() != null) {
                        demande.getCreePar().getNom();
                    }
                    if (demande.getEntreprise() != null) {
                        demande.getEntreprise().getNom();
                    }

                    return kafofond.dto.DemandeDAchatDTO.fromEntity(demande);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Liste toutes les demandes d'achat d'une entreprise par ID et retourne les
     * DTOs
     */
    @Transactional(readOnly = true)
    public List<kafofond.dto.DemandeDAchatDTO> listerParEntrepriseIdDTO(Long entrepriseId) {
        List<DemandeDAchat> demandes = demandeDAchatRepo.findByEntrepriseId(entrepriseId);
        return demandes.stream()
                .map(demande -> {
                    // Forcer le chargement des relations lazy
                    if (demande.getFicheDeBesoin() != null) {
                        demande.getFicheDeBesoin().getCode();
                    }
                    if (demande.getCreePar() != null) {
                        demande.getCreePar().getNom();
                    }
                    if (demande.getEntreprise() != null) {
                        demande.getEntreprise().getNom();
                    }

                    return kafofond.dto.DemandeDAchatDTO.fromEntity(demande);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<DemandeDAchat> trouverParId(Long id) {
        return demandeDAchatRepo.findById(id);
    }

    /**
     * Trouve une demande d'achat par ID avec initialisation des relations
     * Utilisé pour éviter les problèmes de lazy loading
     */
    @Transactional(readOnly = true)
    public Optional<DemandeDAchat> trouverParIdAvecRelations(Long id) {
        return demandeDAchatRepo.findById(id)
                .map(demande -> {
                    // Initialiser les relations pour éviter les problèmes de lazy loading
                    if (demande.getFicheDeBesoin() != null) {
                        demande.getFicheDeBesoin().getCode();
                        if (demande.getFicheDeBesoin().getCreePar() != null) {
                            demande.getFicheDeBesoin().getCreePar().getNom();
                        }
                        if (demande.getFicheDeBesoin().getEntreprise() != null) {
                            demande.getFicheDeBesoin().getEntreprise().getNom();
                        }
                    }
                    if (demande.getCreePar() != null) {
                        demande.getCreePar().getNom();
                        demande.getCreePar().getPrenom();
                        demande.getCreePar().getEmail();
                        if (demande.getCreePar().getEntreprise() != null) {
                            demande.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (demande.getEntreprise() != null) {
                        demande.getEntreprise().getNom();
                    }
                    if (demande.getBonDeCommande() != null) {
                        demande.getBonDeCommande().getId();
                    }
                    return demande;
                });
    }
}