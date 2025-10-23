package kafofond.service;

import kafofond.dto.DecisionPrelevementDTO;
import kafofond.entity.DecisionDePrelevement;
import kafofond.entity.Statut;
import kafofond.entity.Utilisateur;
import kafofond.entity.Entreprise;
import kafofond.entity.AttestationDeServiceFait;
import kafofond.repository.DecisionDePrelevementRepo;
import kafofond.repository.UtilisateurRepo;
import kafofond.repository.AttestationDeServiceFaitRepo;
import kafofond.repository.LigneCreditRepo;
import kafofond.mapper.DecisionPrelevementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionPrelevementService {

    private final DecisionDePrelevementRepo decisionDePrelevementRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final AttestationDeServiceFaitRepo attestationRepo;
    private final DecisionPrelevementMapper mapper;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final LigneCreditRepo ligneCreditRepo;
    private final CodeGeneratorService codeGeneratorService;
    private final SeuilValidationService seuilValidationService;
    private final TableValidationService tableValidationService;

    /**
     * Crée une décision de prélèvement
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public DecisionDePrelevement creerDTO(DecisionPrelevementDTO dto, String emailComptable) {
        log.info("Création d'une décision de prélèvement par {}", emailComptable);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur comptable = utilisateurRepo.findByEmail(emailComptable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (comptable.getEntreprise() != null) {
            comptable.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return creer(dto, comptable);
    }

    @Transactional
    public DecisionDePrelevement creer(DecisionPrelevementDTO dto, Utilisateur comptable) {
        log.info("Création d'une décision de prélèvement par {}", comptable.getEmail());

        if (comptable.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut créer des décisions de prélèvement");
        }

        // Récupérer l'attestation de service fait
        AttestationDeServiceFait attestation = null;
        if (dto.getAttestationId() != null) {
            attestation = attestationRepo.findById(dto.getAttestationId())
                    .orElseThrow(() -> new IllegalArgumentException("Attestation de service fait introuvable"));
        } else if (dto.getReferenceAttestation() != null) {
            // Rechercher par référence si l'ID n'est pas fourni
            attestation = attestationRepo.findByCode(dto.getReferenceAttestation())
                    .orElseThrow(() -> new IllegalArgumentException("Attestation de service fait introuvable"));
        } else {
            throw new IllegalArgumentException("L'ID ou la référence de l'attestation de service fait est requise");
        }

        DecisionDePrelevement decision = new DecisionDePrelevement();
        decision.setReferenceAttestation(dto.getReferenceAttestation());
        decision.setMontant(dto.getMontant());
        decision.setCompteOrigine(dto.getCompteOrigine());
        decision.setCompteDestinataire(dto.getCompteDestinataire());
        decision.setMotifPrelevement(dto.getMotifPrelevement());

        decision.setCreePar(comptable);
        decision.setEntreprise(comptable.getEntreprise());
        decision.setAttestationDeServiceFait(attestation);
        decision.setStatut(Statut.EN_COURS);
        decision.setDateCreation(LocalDate.now());
        decision.setDateModification(LocalDateTime.now());

        // Sauvegarder d'abord pour obtenir l'ID
        DecisionDePrelevement decisionCreee = decisionDePrelevementRepo.save(decision);
        
        // Générer le code automatiquement
        String code = codeGeneratorService.generateDecisionPrelevementCode(decisionCreee.getId(), decisionCreee.getDateCreation());
        decisionCreee.setCode(code);
        decisionCreee = decisionDePrelevementRepo.save(decisionCreee);

        // Historique : conversion Statut → String
        historiqueService.enregistrerAction(
                "DECISION_PRELEVEMENT",
                decisionCreee.getId(),
                "CREATION",
                comptable,
                null, // ancienEtat
                null, // nouveauEtat
                null, // ancienStatut
                Statut.EN_COURS.name(), // nouveauStatut
                "Décision créée"
        );

        Utilisateur responsable = trouverResponsableDansTransaction(comptable.getEntreprise().getId(), comptable.getEntreprise().getNom());
        if (responsable != null) {
            notificationService.notifierModification("DECISION_PRELEVEMENT", decisionCreee.getId(),
                    comptable, responsable, "créée");
        }

        return decisionCreee;
    }

    /**
     * Valide une décision de prélèvement (Responsable)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public DecisionDePrelevement validerDTO(Long id, String emailResponsable) {
        log.info("Validation de la décision de prélèvement {} par {}", id, emailResponsable);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur responsable = utilisateurRepo.findByEmail(emailResponsable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (responsable.getEntreprise() != null) {
            responsable.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return valider(id, responsable);
    }

    @Transactional
    public DecisionDePrelevement valider(Long id, Utilisateur responsable) {
        log.info("Validation de la décision de prélèvement {} par {}", id, responsable.getEmail());

        if (responsable.getRole() != kafofond.entity.Role.RESPONSABLE) {
            throw new IllegalArgumentException("Seul le Responsable peut valider une décision de prélèvement");
        }

        DecisionDePrelevement decision = decisionDePrelevementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Décision de prélèvement introuvable"));

        String ancienStatut = decision.getStatut().name();
        
        // Vérifier si le montant dépasse le seuil
        double montant = decision.getMontant();
        var seuil = seuilValidationService.obtenirSeuilActif(responsable.getEntreprise());
        
        if (seuil != null && montant > seuil.getMontantSeuil()) {
            // Si le montant dépasse le seuil, mettre en attente d'approbation du directeur
            decision.setStatut(Statut.APPROUVE);
        } else {
            // Sinon, valider directement
            decision.setStatut(Statut.VALIDE);
        }
        
        decision.setDateModification(LocalDateTime.now());

        DecisionDePrelevement decisionValidee = decisionDePrelevementRepo.save(decision);

        // Mettre à jour les montants de la ligne de crédit liée (engagement du montant)
        if (decisionValidee.getLigneCredit() != null) {
            var ligne = ligneCreditRepo.findById(decisionValidee.getLigneCredit().getId())
                    .orElse(null);
            if (ligne != null) {
                double nouveauMontantEngage = Math.max(0d, ligne.getMontantEngager()) + Math.max(0d, decisionValidee.getMontant());
                ligne.setMontantEngager(nouveauMontantEngage);
                ligne.setMontantRestant(Math.max(0d, ligne.getMontantAllouer() - nouveauMontantEngage));
                ligneCreditRepo.save(ligne);

                // Historiser la mise à jour des montants
                historiqueService.enregistrerAction(
                        "LIGNE_CREDIT",
                        ligne.getId(),
                        "MISE_A_JOUR_MONTANTS",
                        responsable,
                        null,
                        null,
                        null,
                        null,
                        String.format("Engagé=%.2f, Restant=%.2f après validation décision #%d",
                                ligne.getMontantEngager(), ligne.getMontantRestant(), decisionValidee.getId())
                );
            }
        }

        historiqueService.enregistrerAction(
                "DECISION_PRELEVEMENT",
                id,
                "VALIDATION",
                responsable,
                null, // ancienEtat
                null, // nouveauEtat
                ancienStatut, // ancienStatut
                decision.getStatut().name(), // nouveauStatut
                decision.getStatut() == Statut.APPROUVE ? 
                    "Décision en attente d'approbation (montant > seuil)" : 
                    "Décision validée"
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                responsable,
                decision.getStatut() == Statut.APPROUVE ? "APPROUVE" : "VALIDE",
                decision.getStatut() == Statut.APPROUVE ? 
                    "Décision en attente d'approbation (montant > seuil)" : 
                    "Décision validée"
        );

        if (decision.getCreePar() != null) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, responsable,
                    decision.getCreePar(), 
                    decision.getStatut() == Statut.APPROUVE ? 
                        "en attente d'approbation" : 
                        "validée", 
                    null);
        }

        // Si la décision est en attente d'approbation, notifier le directeur
        if (decision.getStatut() == Statut.APPROUVE) {
            Utilisateur directeur = trouverDirecteurDansTransaction(responsable.getEntreprise().getId(), responsable.getEntreprise().getNom());
            if (directeur != null) {
                notificationService.notifierValidation("DECISION_PRELEVEMENT", id, responsable,
                        directeur, "requiert votre approbation", 
                        String.format("Montant %.2f dépasse le seuil de %.2f", montant, seuil.getMontantSeuil()));
            }
        }

        return decisionValidee;
    }

    /**
     * Approuve une décision de prélèvement (Directeur)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public DecisionDePrelevement approuverDTO(Long id, String emailDirecteur) {
        log.info("Approbation de la décision de prélèvement {} par {}", id, emailDirecteur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur directeur = utilisateurRepo.findByEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return approuver(id, directeur);
    }

    @Transactional
    public DecisionDePrelevement approuver(Long id, Utilisateur directeur) {
        log.info("Approbation de la décision de prélèvement {} par {}", id, directeur.getEmail());

        if (directeur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Directeur peut approuver une décision de prélèvement");
        }

        DecisionDePrelevement decision = decisionDePrelevementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Décision de prélèvement introuvable"));

        // Vérifier que la décision est en attente d'approbation
        if (decision.getStatut() != Statut.APPROUVE) {
            throw new IllegalArgumentException("La décision doit être en attente d'approbation pour être approuvée");
        }

        String ancienStatut = decision.getStatut().name();
        decision.setStatut(Statut.VALIDE);
        decision.setDateModification(LocalDateTime.now());

        DecisionDePrelevement decisionApprouvee = decisionDePrelevementRepo.save(decision);

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                directeur,
                "APPROUVE",
                "Décision approuvée par le directeur"
        );

        historiqueService.enregistrerAction(
                "DECISION_PRELEVEMENT",
                id,
                "APPROBATION",
                directeur,
                null, // ancienEtat
                null, // nouveauEtat
                ancienStatut, // ancienStatut
                Statut.VALIDE.name(), // nouveauStatut
                "Décision approuvée par le directeur"
        );

        if (decision.getCreePar() != null) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, directeur,
                    decision.getCreePar(), "approuvée", null);
        }

        // Notifier le responsable
        Utilisateur responsable = trouverResponsableDansTransaction(directeur.getEntreprise().getId(), directeur.getEntreprise().getNom());
        if (responsable != null && !responsable.getId().equals(decision.getCreePar().getId())) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, directeur,
                    responsable, "approuvée", null);
        }

        return decisionApprouvee;
    }

    /**
     * Rejette une décision de prélèvement
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public DecisionDePrelevement rejeterDTO(Long id, String emailResponsable, String commentaire) {
        log.info("Rejet de la décision de prélèvement {} par {}", id, emailResponsable);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur responsable = utilisateurRepo.findByEmail(emailResponsable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (responsable.getEntreprise() != null) {
            responsable.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return rejeter(id, responsable, commentaire);
    }

    @Transactional
    public DecisionDePrelevement rejeter(Long id, Utilisateur responsable, String commentaire) {
        log.info("Rejet de la décision de prélèvement {} par {}", id, responsable.getEmail());

        if (responsable.getRole() != kafofond.entity.Role.RESPONSABLE && 
            responsable.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Responsable ou le Directeur peut rejeter une décision de prélèvement");
        }

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
        }

        DecisionDePrelevement decision = decisionDePrelevementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Décision de prélèvement introuvable"));

        String ancienStatut = decision.getStatut().name();
        decision.setStatut(Statut.REJETE);
        decision.setDateModification(LocalDateTime.now());

        DecisionDePrelevement decisionRejetee = decisionDePrelevementRepo.save(decision);

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                responsable,
                "REJETE",
                commentaire
        );

        historiqueService.enregistrerAction(
                "DECISION_PRELEVEMENT",
                id,
                "REJET",
                responsable,
                null, // ancienEtat
                null, // nouveauEtat
                ancienStatut, // ancienStatut
                Statut.REJETE.name(), // nouveauStatut
                commentaire
        );

        if (decision.getCreePar() != null) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, responsable,
                    decision.getCreePar(), "rejetée", commentaire);
        }

        return decisionRejetee;
    }

    /**
     * Trouve le responsable d'une entreprise
     */
    private Utilisateur trouverResponsable(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail(
                        "responsable@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    /**
     * Trouve le directeur d'une entreprise
     */
    private Utilisateur trouverDirecteur(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail(
                        "directeur@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
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
     * Trouve le directeur d'une entreprise dans une transaction
     * Cette méthode est utilisée pour éviter les problèmes de lazy loading
     */
    @Transactional
    public Utilisateur trouverDirecteurDansTransaction(Long entrepriseId, String entrepriseNom) {
        // Utiliser le nom de l'entreprise pour trouver le directeur
        return utilisateurRepo.findByEmail("directeur@" + entrepriseNom.toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    public List<DecisionDePrelevement> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return decisionDePrelevementRepo.findByEntreprise(entreprise);
    }

    public Optional<DecisionDePrelevement> trouverParId(Long id) {
        return decisionDePrelevementRepo.findById(id);
    }

    public DecisionPrelevementDTO toDTO(DecisionDePrelevement decision) {
        return mapper.toDTO(decision);
    }
    
    /**
     * Trouve une décision de prélèvement par ID avec initialisation des relations
     * Utilisé pour éviter les problèmes de lazy loading
     */
    @Transactional(readOnly = true)
    public Optional<DecisionDePrelevement> trouverParIdAvecRelations(Long id) {
        return decisionDePrelevementRepo.findById(id)
                .map(decision -> {
                    // Initialiser les relations pour éviter les problèmes de lazy loading
                    if (decision.getCreePar() != null) {
                        decision.getCreePar().getNom();
                        decision.getCreePar().getPrenom();
                        decision.getCreePar().getEmail();
                        if (decision.getCreePar().getEntreprise() != null) {
                            decision.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (decision.getEntreprise() != null) {
                        decision.getEntreprise().getNom();
                    }
                    if (decision.getAttestationDeServiceFait() != null) {
                        decision.getAttestationDeServiceFait().getId();
                    }
                    if (decision.getLigneCredit() != null) {
                        decision.getLigneCredit().getId();
                    }
                    if (decision.getOrdreDePaiement() != null) {
                        decision.getOrdreDePaiement().getId(); // Force le chargement de l'ordre
                    }
                    return decision;
                });
    }
}