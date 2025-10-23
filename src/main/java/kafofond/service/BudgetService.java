package kafofond.service;

import kafofond.entity.Budget;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import kafofond.repository.BudgetRepo;
import kafofond.repository.UtilisateurRepo;
import kafofond.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des budgets
 * Implémente le workflow : EN_COURS → VALIDÉ/REJETÉ par le Directeur
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepo budgetRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final DocumentService documentService;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final CodeGeneratorService codeGeneratorService;
    private final TableValidationService tableValidationService;
    private final UtilisateurService utilisateurService;

    /**
     * Crée un nouveau budget (Responsable ou Directeur)
     */
    @Transactional
    public Budget creer(Budget budget, Utilisateur createur) {
        log.info("Création d'un budget par {}", createur.getEmail());
        
        // Forcer le chargement de l'entreprise
        if (createur.getEntreprise() != null) {
            createur.getEntreprise().getNom();
        }

        if (createur.getRole() != kafofond.entity.Role.RESPONSABLE &&
                createur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seuls les Responsables et Directeurs peuvent créer des budgets");
        }

        budget.setCreePar(createur);
        budget.setEntreprise(createur.getEntreprise());
        budget.setStatut(Statut.EN_COURS);
        budget.setEtat(false); // Inactif jusqu'à validation
        budget.setDateCreation(LocalDate.now());
        budget.setDateModification(LocalDateTime.now());

        Budget budgetCree = budgetRepo.save(budget);
        
        // Générer le code unique automatiquement
        String code = codeGeneratorService.generateBudgetCode(budgetCree.getId(), budgetCree.getDateCreation());
        budgetCree.setCode(code);
        budgetCree = budgetRepo.save(budgetCree);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BUDGET",
                budgetCree.getId(),
                "CREATION",
                createur,
                null,                       // ancienEtat
                "INACTIF",                  // nouveauEtat (etat = false)
                null,                       // ancienStatut
                Statut.EN_COURS.name(),     // nouveauStatut
                "Créé par " + createur.getRole().name()
        );

        // Notifier le Directeur si ce n'est pas lui qui crée
        if (createur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            Utilisateur directeur = trouverDirecteur(createur.getEntreprise());
            if (directeur != null) {
                notificationService.notifierModification("BUDGET", budgetCree.getId(),
                        createur, directeur, "créé");
            }
        }

        return budgetCree;
    }

    /**
     * Crée un budget et retourne le DTO
     */
    @Transactional
    public kafofond.dto.BudgetDTO creerDTO(Budget budget, String emailCreateur) {
        // Récupérer le créateur dans la transaction
        Utilisateur createur = utilisateurService.trouverParEmail(emailCreateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        Budget budgetCree = creer(budget, createur);
        return kafofond.dto.BudgetDTO.fromEntity(budgetCree);
    }

    /**
     * Modifie un budget existant
     */
    @Transactional
    public Budget modifier(Long id, Budget budgetModifie, Utilisateur modificateur) {
        log.info("Modification du budget {} par {}", id, modificateur.getEmail());
        
        // Forcer le chargement de l'entreprise
        if (modificateur.getEntreprise() != null) {
            modificateur.getEntreprise().getNom();
        }

        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));

        if (modificateur.getRole() != kafofond.entity.Role.RESPONSABLE &&
                modificateur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seuls les Responsables et Directeurs peuvent modifier des budgets");
        }

        Statut ancienStatut = budget.getStatut();

        // Mettre à jour les champs
        budget.setIntituleBudget(budgetModifie.getIntituleBudget());
        budget.setDescription(budgetModifie.getDescription());
        budget.setMontantBudget(budgetModifie.getMontantBudget());
        budget.setDateModification(LocalDateTime.now());

        if (budget.getStatut() == Statut.VALIDE) {
            budget.setStatut(Statut.EN_COURS);
            budget.setEtat(false);
        }

        Budget budgetModifiee = budgetRepo.save(budget);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BUDGET",
                id,
                "MODIFICATION",
                modificateur,
                budget.getEtat() != null && budget.getEtat() ? "ACTIF" : "INACTIF",      // ancienEtat
                budget.getEtat() != null && budget.getEtat() ? "ACTIF" : "INACTIF",      // nouveauEtat
                ancienStatut != null ? ancienStatut.name() : null,  // ancienStatut
                budget.getStatut() != null ? budget.getStatut().name() : null,  // nouveauStatut
                "Modification du budget"
        );

        // Notifier le Directeur si ce n'est pas lui qui modifie
        if (modificateur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            Utilisateur directeur = trouverDirecteur(modificateur.getEntreprise());
            if (directeur != null) {
                notificationService.notifierModification("BUDGET", id,
                        modificateur, directeur, "modifié");
            }
        }

        return budgetModifiee;
    }

    @Transactional
    public kafofond.dto.BudgetDTO modifierDTO(Long id, Budget budgetModifie, String emailModificateur) {
        Utilisateur modificateur = utilisateurService.trouverParEmail(emailModificateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Budget budgetModif = modifier(id, budgetModifie, modificateur);
        return kafofond.dto.BudgetDTO.fromEntity(budgetModif);
    }

    /**
     * Valide un budget (Directeur uniquement)
     */
    @Transactional
    public Budget valider(Long id, Utilisateur directeur) {
        log.info("Validation du budget {} par {}", id, directeur.getEmail());
        
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }

        if (directeur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Directeur peut valider un budget");
        }

        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));

        Statut ancienStatut = budget.getStatut();
        budget.setStatut(Statut.VALIDE);
        budget.setEtat(true);
        budget.setDateModification(LocalDateTime.now());

        Budget budgetValide = budgetRepo.save(budget);

        // Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "BUDGET",
                id,
                "VALIDATION",
                directeur,
                "INACTIF",                  // ancienEtat (avant validation)
                "ACTIF",                    // nouveauEtat (après validation)
                ancienStatut != null ? ancienStatut.name() : null,  // ancienStatut
                Statut.VALIDE.name(),       // nouveauStatut
                "Budget validé"
        );
        
        // Enregistrer dans TableValidation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.BUDGET,
                directeur,
                "VALIDE",
                null
        );

        if (budget.getCreePar() != null) {
            notificationService.notifierValidation("BUDGET", id, directeur,
                    budget.getCreePar(), "validé", null);
        }

        return budgetValide;
    }

    @Transactional
    public kafofond.dto.BudgetDTO validerDTO(Long id, String emailDirecteur) {
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Budget budgetValide = valider(id, directeur);
        return kafofond.dto.BudgetDTO.fromEntity(budgetValide);
    }

    /**
     * Rejette un budget avec commentaire obligatoire (Directeur uniquement)
     */
    @Transactional
    public Budget rejeter(Long id, Utilisateur directeur, String commentaire) {
        log.info("Rejet du budget {} par {}", id, directeur.getEmail());
        
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }

        if (directeur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Directeur peut rejeter un budget");
        }

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
        }

        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));

        Statut ancienStatut = budget.getStatut();
        budget.setStatut(Statut.REJETE);
        budget.setEtat(false);
        budget.setDateModification(LocalDateTime.now());

        Budget budgetRejete = budgetRepo.save(budget);

        historiqueService.enregistrerAction(
                "BUDGET",
                id,
                "REJET",
                directeur,
                "INACTIF",                  // ancienEtat
                "INACTIF",                  // nouveauEtat (reste inactif)
                ancienStatut != null ? ancienStatut.name() : null,  // ancienStatut
                Statut.REJETE.name(),       // nouveauStatut
                commentaire
        );
        
        // Enregistrer dans TableValidation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.BUDGET,
                directeur,
                "REJETE",
                commentaire
        );

        if (budget.getCreePar() != null) {
            notificationService.notifierValidation("BUDGET", id, directeur,
                    budget.getCreePar(), "rejeté", commentaire);
        }

        return budgetRejete;
    }

    @Transactional
    public kafofond.dto.BudgetDTO rejeterDTO(Long id, String emailDirecteur, String commentaire) {
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Budget budgetRejete = rejeter(id, directeur, commentaire);
        return kafofond.dto.BudgetDTO.fromEntity(budgetRejete);
    }

    /**
     * Active un budget (Directeur uniquement)
     */
    @Transactional
    public Budget activer(Long id, Utilisateur directeur) {
        log.info("Activation du budget {} par {}", id, directeur.getEmail());
        
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }

        if (directeur.getRole() != kafofond.entity.Role.DIRECTEUR) {
            throw new IllegalArgumentException("Seul le Directeur peut activer un budget");
        }

        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));

        budget.setEtat(true);
        budget.setDateModification(LocalDateTime.now());

        Budget budgetActive = budgetRepo.save(budget);

        historiqueService.enregistrerAction(
                "BUDGET",
                id,
                "ACTIVATION",
                directeur,
                "INACTIF",                  // ancienEtat
                "ACTIF",                    // nouveauEtat
                null,                       // ancienStatut
                null,                       // nouveauStatut
                "Budget activé"
        );

        return budgetActive;
    }

    @Transactional
    public kafofond.dto.BudgetDTO activerDTO(Long id, String emailDirecteur) {
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Budget budgetActive = activer(id, directeur);
        return kafofond.dto.BudgetDTO.fromEntity(budgetActive);
    }

    /**
     * Désactive un budget (Directeur ou Responsable)
     */
    @Transactional
    public Budget desactiver(Long id, Utilisateur utilisateur) {
        log.info("Désactivation du budget {} par {}", id, utilisateur.getEmail());
        
        if (utilisateur.getEntreprise() != null) {
            utilisateur.getEntreprise().getNom();
        }

        if (utilisateur.getRole() != kafofond.entity.Role.DIRECTEUR &&
                utilisateur.getRole() != kafofond.entity.Role.RESPONSABLE) {
            throw new IllegalArgumentException("Seuls les Directeurs et Responsables peuvent désactiver un budget");
        }

        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));

        budget.setEtat(false);
        budget.setDateModification(LocalDateTime.now());

        Budget budgetDesactive = budgetRepo.save(budget);

        historiqueService.enregistrerAction(
                "BUDGET",
                id,
                "DESACTIVATION",
                utilisateur,
                "ACTIF",                    // ancienEtat
                "INACTIF",                  // nouveauEtat
                null,                       // ancienStatut
                null,                       // nouveauStatut
                "Budget désactivé"
        );

        return budgetDesactive;
    }

    @Transactional
    public kafofond.dto.BudgetDTO desactiverDTO(Long id, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Budget budgetDesactive = desactiver(id, utilisateur);
        return kafofond.dto.BudgetDTO.fromEntity(budgetDesactive);
    }

    /**
     * Trouve le directeur d'une entreprise
     */
    private Utilisateur trouverDirecteur(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("directeur@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    /**
     * Liste tous les budgets d'une entreprise
     */
    public List<Budget> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return budgetRepo.findByEntreprise(entreprise);
    }

    /**
     * Liste tous les budgets d'une entreprise et retourne les DTO
     */
    @Transactional(readOnly = true)
    public List<kafofond.dto.BudgetDTO> listerParEntrepriseDTO(String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        List<Budget> budgets = budgetRepo.findByEntreprise(utilisateur.getEntreprise());
        
        // Initialiser les entités pour éviter les problèmes de proxy
        for (Budget budget : budgets) {
            // Forcer le chargement des relations lazy
            if (budget.getCreePar() != null) {
                budget.getCreePar().getId(); // Force l'initialisation du proxy
                budget.getCreePar().getNom();
                budget.getCreePar().getPrenom();
                budget.getCreePar().getEmail();
                if (budget.getCreePar().getEntreprise() != null) {
                    budget.getCreePar().getEntreprise().getId(); // Force l'initialisation du proxy
                }
            }
            if (budget.getEntreprise() != null) {
                budget.getEntreprise().getId(); // Force l'initialisation du proxy
                budget.getEntreprise().getNom();
            }
        }
        
        return budgets.stream()
                .map(kafofond.dto.BudgetDTO::fromEntity)
                .toList();
    }

    /**
     * Trouve un budget par ID
     */
    public Optional<Budget> trouverParId(Long id) {
        return budgetRepo.findById(id);
    }

    /**
     * Trouve un budget par ID et retourne le DTO (avec chargement dans la transaction)
     */
    @Transactional(readOnly = true)
    public Optional<kafofond.dto.BudgetDTO> trouverParIdDTO(Long id) {
        return budgetRepo.findById(id)
                .map(budget -> {
                    // Forcer le chargement des relations lazy
                    if (budget.getCreePar() != null) {
                        budget.getCreePar().getNom();
                        if (budget.getCreePar().getEntreprise() != null) {
                            budget.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (budget.getEntreprise() != null) {
                        budget.getEntreprise().getNom();
                    }
                    return kafofond.dto.BudgetDTO.fromEntity(budget);
                });
    }

    /**
     * Génère le PDF d'un budget
     */
    public String genererPdf(Long id) {
        log.info("Génération du PDF pour le budget {}", id);

        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));

        try {
            String urlPdf = documentService.genererBudgetPdf(budget);
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le budget {} : {}", id, e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
    
    /**
     * Trouve un budget par ID avec initialisation des relations
     * Utilisé pour éviter les problèmes de lazy loading
     */
    @Transactional(readOnly = true)
    public Optional<Budget> trouverParIdAvecRelations(Long id) {
        return budgetRepo.findById(id)
                .map(budget -> {
                    // Initialiser les relations pour éviter les problèmes de lazy loading
                    if (budget.getCreePar() != null) {
                        budget.getCreePar().getNom();
                        budget.getCreePar().getPrenom();
                        budget.getCreePar().getEmail();
                        if (budget.getCreePar().getEntreprise() != null) {
                            budget.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (budget.getEntreprise() != null) {
                        budget.getEntreprise().getNom();
                    }
                    if (budget.getLignesCredits() != null) {
                        budget.getLignesCredits().size(); // Force le chargement de la liste
                        budget.getLignesCredits().forEach(ligne -> {
                            ligne.getIntituleLigne();
                        });
                    }
                    return budget;
                });
    }
}
