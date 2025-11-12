package kafofond.service;

import kafofond.dto.DecisionPrelevementCreateDTO;
import kafofond.dto.DecisionPrelevementDTO;
import kafofond.entity.*;
import kafofond.repository.*;
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
    private final AttestationDeServiceFaitRepo attestationRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final DecisionPrelevementMapper mapper;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final LigneCreditRepo ligneCreditRepo;
    private final CodeGeneratorService codeGeneratorService;
    private final SeuilValidationService seuilValidationService;
    private final TableValidationService tableValidationService;
    private final OrdreDePaiementRepo ordreDePaiementRepo;

    /**
     * Crée une décision de prélèvement à partir du DTO simplifié
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public DecisionDePrelevement creerDTO(DecisionPrelevementCreateDTO dto, String emailComptable) {
        log.info("Création d'une décision de prélèvement par {}", emailComptable);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy
        // loading
        Utilisateur comptable = utilisateurRepo.findByEmail(emailComptable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy
        // loading
        if (comptable.getEntreprise() != null) {
            comptable.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return creer(dto, comptable);
    }

    @Transactional
    public DecisionDePrelevement creer(DecisionPrelevementCreateDTO dto, Utilisateur comptable) {
        log.info("Création d'une décision de prélèvement par {}", comptable.getEmail());

        if (comptable.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut créer des décisions de prélèvement");
        }

        // Récupérer l'attestation de service fait
        AttestationDeServiceFait attestation = null;
        if (dto.getAttestationId() != null) {
            attestation = attestationRepo.findById(dto.getAttestationId())
                    .orElseThrow(() -> new IllegalArgumentException("Attestation de service fait introuvable"));
        } else {
            throw new IllegalArgumentException("L'ID de l'attestation de service fait est requis");
        }

        // Récupérer la ligne de crédit si fournie
        LigneCredit ligneCredit = null;
        if (dto.getLigneCreditId() != null) {
            ligneCredit = ligneCreditRepo.findById(dto.getLigneCreditId())
                    .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));
        }

        // Créer une décision de prélèvement à partir du DTO simplifié
        DecisionDePrelevement decision = new DecisionDePrelevement();
        decision.setMontant(dto.getMontant());
        decision.setCompteOrigine(dto.getCompteOrigine());
        decision.setCompteDestinataire(dto.getCompteDestinataire());
        decision.setMotifPrelevement(dto.getMotifPrelevement());

        decision.setCreePar(comptable);
        decision.setEntreprise(comptable.getEntreprise());
        decision.setAttestationDeServiceFait(attestation);
        decision.setLigneCredit(ligneCredit); // Associer la ligne de crédit si présente
        decision.setStatut(Statut.EN_COURS);
        decision.setDateCreation(LocalDate.now().atStartOfDay());
        decision.setDateModification(LocalDateTime.now());

        // Sauvegarder d'abord pour obtenir l'ID
        DecisionDePrelevement decisionCreee = decisionDePrelevementRepo.save(decision);

        // Générer le code automatiquement
        String code = codeGeneratorService.generateDecisionPrelevementCode(decisionCreee.getId(),
                LocalDate.from(decisionCreee.getDateCreation()));
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
                "Décision créée");

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerCreation(
                decisionCreee.getId(),
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                comptable
        );

        Utilisateur responsable = trouverResponsableDansTransaction(comptable.getEntreprise().getId(),
                comptable.getEntreprise().getNom());
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

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy
        // loading
        Utilisateur responsable = utilisateurRepo.findByEmail(emailResponsable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy
        // loading
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
                double nouveauMontantEngage = Math.max(0d, ligne.getMontantEngager())
                        + Math.max(0d, decisionValidee.getMontant());
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
                                ligne.getMontantEngager(), ligne.getMontantRestant(), decisionValidee.getId()));
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
                decision.getStatut() == Statut.APPROUVE ? "Décision en attente d'approbation (montant > seuil)"
                        : "Décision validée");

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                responsable,
                decision.getStatut() == Statut.APPROUVE ? "APPROUVE" : "VALIDE",
                decision.getStatut() == Statut.APPROUVE ? "Décision en attente d'approbation (montant > seuil)"
                        : "Décision validée");

        if (decision.getCreePar() != null) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, responsable,
                    decision.getCreePar(),
                    decision.getStatut() == Statut.APPROUVE ? "en attente d'approbation" : "validée",
                    null);
        }

        // Si la décision est en attente d'approbation, notifier le directeur
        if (decision.getStatut() == Statut.APPROUVE) {
            Utilisateur directeur = trouverDirecteurDansTransaction(responsable.getEntreprise().getId(),
                    responsable.getEntreprise().getNom());
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

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy
        // loading
        Utilisateur directeur = utilisateurRepo.findByEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy
        // loading
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
            throw new IllegalArgumentException("La décision n'est pas en attente d'approbation");
        }

        String ancienStatut = decision.getStatut().name();
        decision.setStatut(Statut.VALIDE);
        decision.setDateModification(LocalDateTime.now());

        DecisionDePrelevement decisionApprouvee = decisionDePrelevementRepo.save(decision);

        // Mettre à jour les montants de la ligne de crédit liée (engagement du montant)
        if (decisionApprouvee.getLigneCredit() != null) {
            var ligne = ligneCreditRepo.findById(decisionApprouvee.getLigneCredit().getId())
                    .orElse(null);
            if (ligne != null) {
                double nouveauMontantEngage = Math.max(0d, ligne.getMontantEngager())
                        + Math.max(0d, decisionApprouvee.getMontant());
                ligne.setMontantEngager(nouveauMontantEngage);
                ligne.setMontantRestant(Math.max(0d, ligne.getMontantAllouer() - nouveauMontantEngage));
                ligneCreditRepo.save(ligne);

                // Historiser la mise à jour des montants
                historiqueService.enregistrerAction(
                        "LIGNE_CREDIT",
                        ligne.getId(),
                        "MISE_A_JOUR_MONTANTS",
                        directeur,
                        null,
                        null,
                        null,
                        null,
                        String.format("Engagé=%.2f, Restant=%.2f après approbation décision #%d",
                                ligne.getMontantEngager(), ligne.getMontantRestant(), decisionApprouvee.getId()));
            }
        }

        historiqueService.enregistrerAction(
                "DECISION_PRELEVEMENT",
                id,
                "APPROBATION",
                directeur,
                null, // ancienEtat
                null, // nouveauEtat
                ancienStatut, // ancienStatut
                decision.getStatut().name(), // nouveauStatut
                "Décision approuvée");

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                directeur,
                "VALIDE",
                "Décision approuvée");

        if (decision.getCreePar() != null) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, directeur,
                    decision.getCreePar(), "approuvée", null);
        }

        // Générer automatiquement l'ordre de paiement
        try {
            OrdreDePaiement ordreDePaiement = new OrdreDePaiement();
            ordreDePaiement.setDecisionDePrelevement(decisionApprouvee);
            ordreDePaiement.setMontant(decisionApprouvee.getMontant());
            ordreDePaiement.setCompteOrigine(decisionApprouvee.getCompteOrigine());
            ordreDePaiement.setCompteDestinataire(decisionApprouvee.getCompteDestinataire());
            ordreDePaiement.setDescription(decisionApprouvee.getMotifPrelevement());
            ordreDePaiement.setCreePar(directeur);
            ordreDePaiement.setEntreprise(directeur.getEntreprise());
            ordreDePaiement.setStatut(Statut.EN_COURS);
            ordreDePaiement.setDateCreation(LocalDate.now().atStartOfDay());
            ordreDePaiement.setDateModification(LocalDateTime.now());

            // Sauvegarder l'ordre de paiement
            OrdreDePaiement ordreCree = ordreDePaiementRepo.save(ordreDePaiement);

            // Générer le code automatiquement
            String code = codeGeneratorService.generateOrdrePaiementCode(ordreCree.getId(),
                    LocalDate.from(ordreCree.getDateCreation()));
            ordreCree.setCode(code);
            ordreDePaiementRepo.save(ordreCree);

            // Historique : conversion Statut → String
            historiqueService.enregistrerAction(
                    "ORDRE_PAIEMENT",
                    ordreCree.getId(),
                    "CREATION",
                    directeur,
                    null, // ancienEtat
                    null, // nouveauEtat
                    null, // ancienStatut
                    Statut.EN_COURS.name(), // nouveauStatut
                    "Ordre de paiement créé automatiquement");

            // Notifier le comptable
            Utilisateur comptable = decision.getCreePar();
            if (comptable != null) {
                notificationService.notifierModification("ORDRE_PAIEMENT", ordreCree.getId(),
                        directeur, comptable, "généré");
            }

        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'ordre de paiement : {}", e.getMessage());
        }

        return decisionApprouvee;
    }

    /**
     * Rejette une décision de prélèvement
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public DecisionDePrelevement rejeterDTO(Long id, String emailUtilisateur, String commentaire) {
        log.info("Rejet de la décision de prélèvement {} par {}", id, emailUtilisateur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy
        // loading
        Utilisateur utilisateur = utilisateurRepo.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy
        // loading
        if (utilisateur.getEntreprise() != null) {
            utilisateur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return rejeter(id, utilisateur, commentaire);
    }

    @Transactional
    public DecisionDePrelevement rejeter(Long id, Utilisateur utilisateur, String commentaire) {
        log.info("Rejet de la décision de prélèvement {} par {}", id, utilisateur.getEmail());

        DecisionDePrelevement decision = decisionDePrelevementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Décision de prélèvement introuvable"));

        String ancienStatut = decision.getStatut().name();
        decision.setStatut(Statut.REJETE);
        decision.setDateModification(LocalDateTime.now());

        DecisionDePrelevement decisionRejetee = decisionDePrelevementRepo.save(decision);

        historiqueService.enregistrerAction(
                "DECISION_PRELEVEMENT",
                id,
                "REJET",
                utilisateur,
                null, // ancienEtat
                null, // nouveauEtat
                ancienStatut, // ancienStatut
                decision.getStatut().name(), // nouveauStatut
                "Décision rejetée: " + commentaire);

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.DECISION_PRELEVEMENT,
                utilisateur,
                "REJETE",
                commentaire);

        if (decision.getCreePar() != null) {
            notificationService.notifierValidation("DECISION_PRELEVEMENT", id, utilisateur,
                    decision.getCreePar(), "rejetée", commentaire);
        }

        return decisionRejetee;
    }

    public List<DecisionDePrelevement> listerParEntreprise(Entreprise entreprise) {
        return decisionDePrelevementRepo.findByEntreprise(entreprise);
    }

    /**
     * Liste toutes les décisions de prélèvement d'une entreprise par son ID
     */
    public List<DecisionDePrelevement> listerParEntrepriseId(Long entrepriseId) {
        // Récupérer l'entreprise par son ID
        Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise introuvable avec ID: " + entrepriseId));
        return decisionDePrelevementRepo.findByEntreprise(entreprise);
    }

    public Optional<DecisionDePrelevement> trouverParId(Long id) {
        return decisionDePrelevementRepo.findById(id);
    }

    public Optional<DecisionDePrelevement> trouverParIdDTO(Long id) {
        return decisionDePrelevementRepo.findById(id);
    }

    private Utilisateur trouverResponsableDansTransaction(Long entrepriseId, String nomEntreprise) {
        try {
            // Rechercher le responsable dans l'entreprise
            Optional<Entreprise> entrepriseOpt = entrepriseRepo.findById(entrepriseId);
            if (entrepriseOpt.isPresent()) {
                Optional<Utilisateur> responsableOpt = utilisateurRepo.findByEntrepriseAndRole(entrepriseOpt.get(),
                        kafofond.entity.Role.RESPONSABLE);
                if (responsableOpt.isPresent()) {
                    return responsableOpt.get();
                }
            }

            // Si aucun responsable trouvé, rechercher dans l'entreprise par nom
            Optional<Entreprise> entrepriseParNomOpt = entrepriseRepo.findByNom(nomEntreprise);
            if (entrepriseParNomOpt.isPresent()) {
                Optional<Utilisateur> responsableOpt = utilisateurRepo.findByEntrepriseAndRole(
                        entrepriseParNomOpt.get(),
                        kafofond.entity.Role.RESPONSABLE);
                if (responsableOpt.isPresent()) {
                    return responsableOpt.get();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du responsable : {}", e.getMessage());
            return null;
        }
    }

    private Utilisateur trouverDirecteurDansTransaction(Long entrepriseId, String nomEntreprise) {
        try {
            // Rechercher le directeur dans l'entreprise
            Optional<Entreprise> entrepriseOpt = entrepriseRepo.findById(entrepriseId);
            if (entrepriseOpt.isPresent()) {
                Optional<Utilisateur> directeurOpt = utilisateurRepo.findByEntrepriseAndRole(entrepriseOpt.get(),
                        kafofond.entity.Role.DIRECTEUR);
                if (directeurOpt.isPresent()) {
                    return directeurOpt.get();
                }
            }

            // Si aucun directeur trouvé, rechercher dans l'entreprise par nom
            Optional<Entreprise> entrepriseParNomOpt = entrepriseRepo.findByNom(nomEntreprise);
            if (entrepriseParNomOpt.isPresent()) {
                Optional<Utilisateur> directeurOpt = utilisateurRepo.findByEntrepriseAndRole(entrepriseParNomOpt.get(),
                        kafofond.entity.Role.DIRECTEUR);
                if (directeurOpt.isPresent()) {
                    return directeurOpt.get();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du directeur : {}", e.getMessage());
            return null;
        }
    }
}