package kafofond.service;

import kafofond.entity.OrdreDePaiement;
import kafofond.entity.DecisionDePrelevement;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import kafofond.repository.OrdreDePaiementRepo;
import kafofond.repository.UtilisateurRepo;
import kafofond.repository.LigneCreditRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des ordres de paiement
 * Workflow : EN_COURS → VALIDÉ/APPROUVÉ → REJETÉ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdreDePaiementService {

    private final OrdreDePaiementRepo ordreDePaiementRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final ValidationService validationService;
    private final LigneCreditRepo ligneCreditRepo;
    private final CodeGeneratorService codeGeneratorService;
    private final TableValidationService tableValidationService;

    /**
     * Crée un nouvel ordre de paiement (Comptable uniquement)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public OrdreDePaiement creerDTO(OrdreDePaiement ordre, String emailComptable) {
        log.info("Création d'un ordre de paiement par {}", emailComptable);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur comptable = utilisateurRepo.findByEmail(emailComptable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (comptable.getEntreprise() != null) {
            comptable.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return creer(ordre, comptable);
    }

    /**
     * Crée un nouvel ordre de paiement (Comptable uniquement)
     */
    @Transactional
    public OrdreDePaiement creer(OrdreDePaiement ordre, Utilisateur comptable) {
        log.info("Création d'un ordre de paiement par {}", comptable.getEmail());

        if (comptable.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut créer des ordres de paiement");
        }

        ordre.setCreePar(comptable);
        ordre.setEntreprise(comptable.getEntreprise());
        ordre.setStatut(Statut.EN_COURS);
        ordre.setDateCreation(LocalDate.now());
        ordre.setDateModification(LocalDateTime.now());

        // Sauvegarder d'abord pour obtenir l'ID
        OrdreDePaiement ordreCree = ordreDePaiementRepo.save(ordre);
        
        // Générer le code automatiquement
        String code = codeGeneratorService.generateOrdrePaiementCode(ordreCree.getId(), ordreCree.getDateCreation());
        ordreCree.setCode(code);
        ordreCree = ordreDePaiementRepo.save(ordreCree);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "ORDRE_PAIEMENT",
                ordreCree.getId(),
                "CREATION",
                comptable,
                null,                       // ancienEtat
                Statut.EN_COURS.name(),     // nouveauEtat
                null,                       // ancienStatut
                Statut.EN_COURS.name(),     // nouveauStatut
                "Créé par " + comptable.getRole().name()
        );

        // Vérifier le seuil et notifier le bon validateur
        boolean depasseSeuil = validationService.verifierSeuilValidation(ordre.getMontant(), comptable.getEntreprise());

        if (depasseSeuil) {
            Utilisateur directeur = trouverDirecteur(comptable.getEntreprise());
            if (directeur != null) {
                notificationService.notifierModification("ORDRE_PAIEMENT", ordreCree.getId(),
                        comptable, directeur, "créé (montant >= seuil)");
            }
        } else {
            Utilisateur responsable = trouverResponsable(comptable.getEntreprise());
            if (responsable != null) {
                notificationService.notifierModification("ORDRE_PAIEMENT", ordreCree.getId(),
                        comptable, responsable, "créé");
            }
        }

        return ordreCree;
    }

    /**
     * Valide un ordre de paiement (Responsable uniquement)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public OrdreDePaiement validerDTO(Long id, String emailResponsable) {
        log.info("Validation de l'ordre de paiement {} par {}", id, emailResponsable);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur responsable = utilisateurRepo.findByEmail(emailResponsable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (responsable.getEntreprise() != null) {
            responsable.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return valider(id, responsable);
    }

    /**
     * Valide un ordre de paiement (Responsable uniquement)
     */
    @Transactional
    public OrdreDePaiement valider(Long id, Utilisateur responsable) {
        log.info("Validation de l'ordre de paiement {} par {}", id, responsable.getEmail());

        if (responsable.getRole() != kafofond.entity.Role.RESPONSABLE) {
            throw new IllegalArgumentException("Seul le Responsable peut valider un ordre de paiement");
        }

        OrdreDePaiement ordre = ordreDePaiementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de paiement introuvable"));

        boolean depasseSeuil = validationService.verifierSeuilValidation(ordre.getMontant(), responsable.getEntreprise());
        if (depasseSeuil) {
            throw new IllegalArgumentException("Ce montant nécessite l'approbation du Directeur");
        }

        Statut ancienStatut = ordre.getStatut();
        ordre.setStatut(Statut.VALIDE);
        ordre.setDateModification(LocalDateTime.now());

        OrdreDePaiement ordreValide = ordreDePaiementRepo.save(ordre);

        // Ajuster le restant de la ligne de crédit (si liée) après validation (montant payé)
        if (ordreValide.getLigneCredit() != null) {
            var ligne = ligneCreditRepo.findById(ordreValide.getLigneCredit().getId()).orElse(null);
            if (ligne != null) {
                // Le montant engagé reste inchangé ici; on recalcule juste le restant par sécurité
                double restant = Math.max(0d, ligne.getMontantAllouer() - Math.max(0d, ligne.getMontantEngager()));
                ligne.setMontantRestant(restant);
                ligneCreditRepo.save(ligne);

                historiqueService.enregistrerAction(
                        "LIGNE_CREDIT",
                        ligne.getId(),
                        "MISE_A_JOUR_MONTANTS",
                        responsable,
                        null,
                        null,
                        null,
                        null,
                        String.format("Restant=%.2f après validation OP #%d", restant, ordreValide.getId())
                );
            }
        }

        historiqueService.enregistrerAction(
                "ORDRE_PAIEMENT",
                id,
                "VALIDATION",
                responsable,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.VALIDE.name(),
                null,
                null,
                "Ordre validé par Responsable"
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.ORDRE_PAIEMENT,
                responsable,
                "VALIDE",
                "Ordre validé par Responsable"
        );

        if (ordre.getCreePar() != null) {
            notificationService.notifierValidation("ORDRE_PAIEMENT", id, responsable,
                    ordre.getCreePar(), "validé", null);
        }

        return ordreValide;
    }

    /**
     * Approuve un ordre de paiement (Directeur uniquement)
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public OrdreDePaiement approuverDTO(Long id, String emailDirecteur) {
        log.info("Approbation de l'ordre de paiement {} par {}", id, emailDirecteur);

        // Récupérer l'utilisateur dans la transaction pour éviter les problèmes de lazy loading
        Utilisateur directeur = utilisateurRepo.findByEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Forcer le chargement de l'entreprise pour éviter les problèmes de lazy loading
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom(); // Force le chargement de l'entreprise
        }

        return approuver(id, directeur);
    }

    /**
     * Approuve un ordre de paiement (Directeur uniquement)
     */
    @Transactional
    public OrdreDePaiement approuver(Long id, Utilisateur directeur) {
        log.info("Approbation de l'ordre de paiement {} par {}", id, directeur.getEmail());

        if (directeur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Directeur peut approuver un ordre de paiement");
        }

        OrdreDePaiement ordre = ordreDePaiementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de paiement introuvable"));

        boolean depasseSeuil = validationService.verifierSeuilValidation(ordre.getMontant(), directeur.getEntreprise());
        if (!depasseSeuil) {
            throw new IllegalArgumentException("Ce montant peut être validé par le Responsable");
        }

        Statut ancienStatut = ordre.getStatut();
        ordre.setStatut(Statut.APPROUVE);
        ordre.setDateModification(LocalDateTime.now());

        OrdreDePaiement ordreApprouve = ordreDePaiementRepo.save(ordre);

        // Ajuster le restant de la ligne de crédit (si liée) après approbation
        if (ordreApprouve.getLigneCredit() != null) {
            var ligne = ligneCreditRepo.findById(ordreApprouve.getLigneCredit().getId()).orElse(null);
            if (ligne != null) {
                double restant = Math.max(0d, ligne.getMontantAllouer() - Math.max(0d, ligne.getMontantEngager()));
                ligne.setMontantRestant(restant);
                ligneCreditRepo.save(ligne);

                historiqueService.enregistrerAction(
                        "LIGNE_CREDIT",
                        ligne.getId(),
                        "MISE_A_JOUR_MONTANTS",
                        directeur,
                        null,
                        null,
                        null,
                        null,
                        String.format("Restant=%.2f après approbation OP #%d", restant, ordreApprouve.getId())
                );
            }
        }

        historiqueService.enregistrerAction(
                "ORDRE_PAIEMENT",
                id,
                "APPROBATION",
                directeur,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.APPROUVE.name(),
                null,
                null,
                "Ordre approuvé par Directeur"
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.ORDRE_PAIEMENT,
                directeur,
                "APPROUVE",
                "Ordre approuvé par Directeur"
        );

        if (ordre.getCreePar() != null) {
            notificationService.notifierValidation("ORDRE_PAIEMENT", id, directeur,
                    ordre.getCreePar(), "approuvé", null);
        }

        return ordreApprouve;
    }

    /**
     * Rejette un ordre de paiement
     * Cette méthode est appelée depuis le contrôleur avec l'email de l'utilisateur
     */
    @Transactional
    public OrdreDePaiement rejeterDTO(Long id, String emailValidateur, String commentaire) {
        log.info("Rejet de l'ordre de paiement {} par {}", id, emailValidateur);

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
     * Rejette un ordre de paiement
     */
    @Transactional
    public OrdreDePaiement rejeter(Long id, Utilisateur validateur, String commentaire) {
        log.info("Rejet de l'ordre de paiement {} par {}", id, validateur.getEmail());

        if (validateur.getRole() != kafofond.entity.Role.RESPONSABLE &&
                validateur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seuls le Responsable et le Directeur peuvent rejeter un ordre de paiement");
        }

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
        }

        OrdreDePaiement ordre = ordreDePaiementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de paiement introuvable"));

        Statut ancienStatut = ordre.getStatut();
        ordre.setStatut(Statut.REJETE);
        ordre.setDateModification(LocalDateTime.now());

        OrdreDePaiement ordreRejete = ordreDePaiementRepo.save(ordre);

        historiqueService.enregistrerAction(
                "ORDRE_PAIEMENT",
                id,
                "REJET",
                validateur,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.REJETE.name(),
                null,
                null,
                commentaire
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.ORDRE_PAIEMENT,
                validateur,
                "REJETE",
                commentaire
        );

        if (ordre.getCreePar() != null) {
            notificationService.notifierValidation("ORDRE_PAIEMENT", id, validateur,
                    ordre.getCreePar(), "rejeté", commentaire);
        }

        return ordreRejete;
    }

    private Utilisateur trouverDirecteur(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("directeur@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    private Utilisateur trouverResponsable(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("responsable@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    public List<OrdreDePaiement> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return ordreDePaiementRepo.findByEntreprise(entreprise);
    }

    public Optional<OrdreDePaiement> trouverParId(Long id) {
        return ordreDePaiementRepo.findById(id);
    }
    
    /**
     * Trouve un ordre de paiement par ID avec initialisation des relations
     * Utilisé pour éviter les problèmes de lazy loading
     */
    @Transactional(readOnly = true)
    public Optional<OrdreDePaiement> trouverParIdAvecRelations(Long id) {
        return ordreDePaiementRepo.findById(id)
                .map(ordre -> {
                    // Initialiser les relations pour éviter les problèmes de lazy loading
                    if (ordre.getCreePar() != null) {
                        ordre.getCreePar().getNom();
                        ordre.getCreePar().getPrenom();
                        ordre.getCreePar().getEmail();
                        if (ordre.getCreePar().getEntreprise() != null) {
                            ordre.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (ordre.getEntreprise() != null) {
                        ordre.getEntreprise().getNom();
                    }
                    if (ordre.getDecisionDePrelevement() != null) {
                        ordre.getDecisionDePrelevement().getId();
                    }
                    if (ordre.getLigneCredit() != null) {
                        ordre.getLigneCredit().getId();
                    }
                    return ordre;
                });
    }

    public List<OrdreDePaiement> listerParDecision(DecisionDePrelevement decision) {
        return ordreDePaiementRepo.findByDecisionDePrelevement(decision);
    }
}