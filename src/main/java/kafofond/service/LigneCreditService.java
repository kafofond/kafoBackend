package kafofond.service;

import kafofond.dto.LigneCreditDTO;
import kafofond.entity.*;
import kafofond.mapper.LigneCreditMapper;
import kafofond.repository.CommentaireRepo;
import kafofond.repository.LigneCreditRepo;
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
public class LigneCreditService {

    private final LigneCreditRepo ligneCreditRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final CommentaireRepo commentaireRepo;
    private final LigneCreditMapper mapper;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final UtilisateurService utilisateurService;
    private final CodeGeneratorService codeGeneratorService;
    private final BudgetService budgetService;
    private final TableValidationService tableValidationService;

    @Transactional
    public LigneCredit creer(LigneCredit ligne, Utilisateur createur) {
        // Forcer le chargement de l'entreprise
        if (createur.getEntreprise() != null) {
            createur.getEntreprise().getNom();
        }
        
        if (createur.getRole() != Role.RESPONSABLE && createur.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seuls le Responsable et le Directeur peuvent créer des lignes de crédit");

        ligne.setCreePar(createur);
        ligne.setStatut(Statut.EN_COURS);
        ligne.setEtat(false);
        ligne.setDateCreation(LocalDate.now());

        LigneCredit ligneCreee = ligneCreditRepo.save(ligne);
        
        // Générer le code unique automatiquement
        String code = codeGeneratorService.generateLigneCreditCode(ligneCreee.getId(), ligneCreee.getDateCreation());
        ligneCreee.setCode(code);
        ligneCreee = ligneCreditRepo.save(ligneCreee);

        historiqueService.enregistrerAction(
                "LIGNE_CREDIT",
                ligneCreee.getId(),
                "CREATION",
                createur,
                null,                       // ancienEtat
                "INACTIF",                  // nouveauEtat (etat = false)
                null,                       // ancienStatut
                ligneCreee.getStatut().name(),  // nouveauStatut
                "Ligne de crédit créée"
        );

        Utilisateur directeur = trouverDirecteur(createur.getEntreprise());
        if (directeur != null)
            notificationService.notifierModification("LIGNE_CREDIT", ligneCreee.getId(), createur, directeur, "créée");

        return ligneCreee;
    }

    @Transactional
    public LigneCredit modifier(Long id, LigneCredit ligneModifiee, Utilisateur modificateur) {
        // Forcer le chargement de l'entreprise
        if (modificateur.getEntreprise() != null) {
            modificateur.getEntreprise().getNom();
        }
        
        LigneCredit ligne = ligneCreditRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));

        if (modificateur.getRole() != Role.RESPONSABLE && modificateur.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seuls le Responsable et le Directeur peuvent modifier des lignes de crédit");

        boolean ancienEtat = ligne.isEtat();
        String ancienStatut = ligne.getStatut().name();

        ligne.setIntituleLigne(ligneModifiee.getIntituleLigne());
        ligne.setDescription(ligneModifiee.getDescription());
        ligne.setMontantAllouer(ligneModifiee.getMontantAllouer());
        
        // Si un budget est spécifié dans la ligne modifiée, l'associer
        if (ligneModifiee.getBudget() != null && ligneModifiee.getBudget().getId() != null) {
            Budget budget = budgetService.trouverParId(ligneModifiee.getBudget().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));
            ligne.setBudget(budget);
        }

        if (ligne.getStatut() == Statut.VALIDE)
            ligne.setStatut(Statut.EN_COURS);

        LigneCredit ligneModifie = ligneCreditRepo.save(ligne);

        historiqueService.enregistrerAction(
                "LIGNE_CREDIT",
                id,
                "MODIFICATION",
                modificateur,
                ancienEtat ? "ACTIF" : "INACTIF",       // ancienEtat
                ligneModifie.isEtat() ? "ACTIF" : "INACTIF",  // nouveauEtat
                ancienStatut,                           // ancienStatut
                ligneModifie.getStatut().name(),        // nouveauStatut
                "Ligne de crédit modifiée"
        );

        Utilisateur directeur = trouverDirecteur(modificateur.getEntreprise());
        if (directeur != null)
            notificationService.notifierModification("LIGNE_CREDIT", id, modificateur, directeur, "modifiée");

        return ligneModifie;
    }

    @Transactional
    public LigneCredit valider(Long id, Utilisateur directeur) {
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        if (directeur.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seul le Directeur peut valider une ligne de crédit");

        LigneCredit ligne = ligneCreditRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));

        boolean ancienActif = ligne.isEtat();
        String ancienStatut = ligne.getStatut().name();

        ligne.setStatut(Statut.VALIDE);
        ligne.setEtat(true);

        LigneCredit ligneValidee = ligneCreditRepo.save(ligne);

        historiqueService.enregistrerAction(
                "LIGNE_CREDIT",
                id,
                "VALIDATION",
                directeur,
                ancienActif ? "ACTIF" : "INACTIF",      // ancienEtat
                ligneValidee.isEtat() ? "ACTIF" : "INACTIF",  // nouveauEtat
                ancienStatut,                           // ancienStatut
                ligneValidee.getStatut().name(),        // nouveauStatut
                "Ligne validée"
        );
        
        // Enregistrer dans TableValidation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.LIGNE_CREDIT,
                directeur,
                "VALIDE",
                null
        );

        if (ligne.getCreePar() != null)
            notificationService.notifierValidation("LIGNE_CREDIT", id, directeur, ligne.getCreePar(), "validée", null);

        return ligneValidee;
    }

    @Transactional
    public LigneCredit rejeter(Long id, Utilisateur directeur, String commentaire) {
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        if (directeur.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seul le Directeur peut rejeter une ligne de crédit");

        if (commentaire == null || commentaire.trim().isEmpty())
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");

        LigneCredit ligne = ligneCreditRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));

        boolean ancienActif = ligne.isEtat();
        String ancienStatut = ligne.getStatut().name();

        ligne.setStatut(Statut.REJETE);
        ligne.setEtat(false);

        LigneCredit ligneRejetee = ligneCreditRepo.save(ligne);

        commentaireRepo.save(Commentaire.builder()
                .contenu(commentaire)
                .dateCreation(LocalDate.now().atStartOfDay())
                .auteur(directeur)
                .typeDocument(TypeDocument.LIGNE_CREDIT)
                .documentId(ligne.getId())
                .build()
        );

        historiqueService.enregistrerAction(
                "LIGNE_CREDIT",
                id,
                "REJET",
                directeur,
                ancienActif ? "ACTIF" : "INACTIF",      // ancienEtat
                ligneRejetee.isEtat() ? "ACTIF" : "INACTIF",  // nouveauEtat
                ancienStatut,                           // ancienStatut
                ligneRejetee.getStatut().name(),        // nouveauStatut
                commentaire
        );
        
        // Enregistrer dans TableValidation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.LIGNE_CREDIT,
                directeur,
                "REJETE",
                commentaire
        );

        if (ligne.getCreePar() != null)
            notificationService.notifierValidation("LIGNE_CREDIT", id, directeur, ligne.getCreePar(), "rejetée", commentaire);

        return ligneRejetee;
    }

    @Transactional
    public LigneCredit activer(Long id, Utilisateur directeur) {
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        LigneCredit ligne = ligneCreditRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));

        boolean ancienActif = ligne.isEtat();
        String ancienStatut = ligne.getStatut().name();

        ligne.setEtat(true);
        LigneCredit ligneActivee = ligneCreditRepo.save(ligne);

        historiqueService.enregistrerAction(
                "LIGNE_CREDIT",
                id,
                "ACTIVATION",
                directeur,
                ancienActif ? "ACTIF" : "INACTIF",      // ancienEtat
                ligneActivee.isEtat() ? "ACTIF" : "INACTIF",  // nouveauEtat
                ancienStatut,                           // ancienStatut
                ligneActivee.getStatut().name(),        // nouveauStatut
                "Ligne activée"
        );

        return ligneActivee;
    }

    @Transactional
    public LigneCredit desactiver(Long id, Utilisateur utilisateur) {
        // Forcer le chargement de l'entreprise
        if (utilisateur.getEntreprise() != null) {
            utilisateur.getEntreprise().getNom();
        }
        
        LigneCredit ligne = ligneCreditRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));

        boolean ancienActif = ligne.isEtat();
        String ancienStatut = ligne.getStatut().name();

        ligne.setEtat(false);
        LigneCredit ligneDesactivee = ligneCreditRepo.save(ligne);

        historiqueService.enregistrerAction(
                "LIGNE_CREDIT",
                id,
                "DESACTIVATION",
                utilisateur,
                ancienActif ? "ACTIF" : "INACTIF",      // ancienEtat
                ligneDesactivee.isEtat() ? "ACTIF" : "INACTIF",  // nouveauEtat
                ancienStatut,                           // ancienStatut
                ligneDesactivee.getStatut().name(),     // nouveauStatut
                "Ligne désactivée"
        );

        return ligneDesactivee;
    }

    private Utilisateur trouverDirecteur(Entreprise entreprise) {
        return utilisateurRepo.findByEmail(
                "directeur@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com"
        ).orElse(null);
    }

    public List<LigneCredit> listerParEntreprise(Entreprise entreprise) {
        return ligneCreditRepo.findByBudgetEntreprise(entreprise);
    }

    public Optional<LigneCredit> trouverParId(Long id) {
        return ligneCreditRepo.findById(id);
    }

    /**
     * Récupère une ligne de crédit avec ses commentaires
     */
    @Transactional(readOnly = true)
    public LigneCreditDTO getLigneCreditAvecCommentaires(Long id) {
        LigneCredit ligne = ligneCreditRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de crédit introuvable"));
        
        // Forcer le chargement des relations lazy
        if (ligne.getCreePar() != null) {
            ligne.getCreePar().getNom();
            if (ligne.getCreePar().getEntreprise() != null) {
                ligne.getCreePar().getEntreprise().getNom();
            }
        }
        if (ligne.getBudget() != null) {
            ligne.getBudget().getIntituleBudget();
            if (ligne.getBudget().getEntreprise() != null) {
                ligne.getBudget().getEntreprise().getNom();
            }
        }

        List<Commentaire> commentaires = getCommentaires(ligne);

        return mapper.toDTO(ligne, commentaires);
    }

    /**
     * Récupère tous les commentaires d'une ligne de crédit
     */
    public List<Commentaire> getCommentaires(LigneCredit ligneCredit) {
        log.info("Récupération des commentaires pour la ligne de crédit {}", ligneCredit.getId());
        return commentaireRepo.findByDocumentIdAndTypeDocument(
                ligneCredit.getId(),
                TypeDocument.LIGNE_CREDIT

        );
    }
    
    // ========== MÉTHODES DTO ==========
    
    /**
     * Crée une ligne de crédit et retourne le DTO
     */
    @Transactional
    public LigneCreditDTO creerDTO(LigneCredit ligne, String emailCreateur) {
        Utilisateur createur = utilisateurService.trouverParEmail(emailCreateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Si un budget est spécifié, le récupérer et l'associer
        if (ligne.getBudget() != null && ligne.getBudget().getId() != null) {
            Budget budget = budgetService.trouverParId(ligne.getBudget().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));
            ligne.setBudget(budget);
        }
        
        LigneCredit ligneCreee = creer(ligne, createur);
        List<Commentaire> commentaires = getCommentaires(ligneCreee);
        return mapper.toDTO(ligneCreee, commentaires);
    }
    
    /**
     * Modifie une ligne de crédit et retourne le DTO
     */
    @Transactional
    public LigneCreditDTO modifierDTO(Long id, LigneCredit ligneModifiee, String emailModificateur) {
        Utilisateur modificateur = utilisateurService.trouverParEmail(emailModificateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Si un budget est spécifié dans la ligne modifiée, le récupérer et l'associer
        if (ligneModifiee.getBudget() != null && ligneModifiee.getBudget().getId() != null) {
            Budget budget = budgetService.trouverParId(ligneModifiee.getBudget().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));
            ligneModifiee.setBudget(budget);
        }
        
        LigneCredit ligneModif = modifier(id, ligneModifiee, modificateur);
        List<Commentaire> commentaires = getCommentaires(ligneModif);
        return mapper.toDTO(ligneModif, commentaires);
    }
    
    /**
     * Valide une ligne de crédit et retourne le DTO
     */
    @Transactional
    public LigneCreditDTO validerDTO(Long id, String emailDirecteur) {
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        LigneCredit ligneValidee = valider(id, directeur);
        List<Commentaire> commentaires = getCommentaires(ligneValidee);
        return mapper.toDTO(ligneValidee, commentaires);
    }
    
    /**
     * Rejette une ligne de crédit et retourne le DTO
     */
    @Transactional
    public LigneCreditDTO rejeterDTO(Long id, String emailDirecteur, String commentaire) {
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        LigneCredit ligneRejetee = rejeter(id, directeur, commentaire);
        List<Commentaire> commentaires = getCommentaires(ligneRejetee);
        return mapper.toDTO(ligneRejetee, commentaires);
    }
    
    /**
     * Active une ligne de crédit et retourne le DTO
     */
    @Transactional
    public LigneCreditDTO activerDTO(Long id, String emailDirecteur) {
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        LigneCredit ligneActivee = activer(id, directeur);
        List<Commentaire> commentaires = getCommentaires(ligneActivee);
        return mapper.toDTO(ligneActivee, commentaires);
    }
    
    /**
     * Désactive une ligne de crédit et retourne le DTO
     */
    @Transactional
    public LigneCreditDTO desactiverDTO(Long id, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        LigneCredit ligneDesactivee = desactiver(id, utilisateur);
        List<Commentaire> commentaires = getCommentaires(ligneDesactivee);
        return mapper.toDTO(ligneDesactivee, commentaires);
    }
    
    /**
     * Liste toutes les lignes de crédit d'une entreprise et retourne les DTOs
     */
    @Transactional(readOnly = true)
    public List<LigneCreditDTO> listerParEntrepriseDTO(String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        List<LigneCredit> lignes = listerParEntreprise(utilisateur.getEntreprise());
        return lignes.stream()
                .map(l -> {
                    // Forcer le chargement des relations
                    if (l.getCreePar() != null) {
                        l.getCreePar().getNom();
                        if (l.getCreePar().getEntreprise() != null) {
                            l.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (l.getBudget() != null) {
                        l.getBudget().getIntituleBudget();
                        if (l.getBudget().getEntreprise() != null) {
                            l.getBudget().getEntreprise().getNom();
                        }
                    }
                    List<Commentaire> commentaires = getCommentaires(l);
                    return mapper.toDTO(l, commentaires);
                })
                .toList();
    }
    
    /**
     * Liste les lignes de crédit par budget
     */
    @Transactional(readOnly = true)
    public List<LigneCreditDTO> listerParBudgetDTO(Long budgetId, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        Budget budget = budgetService.trouverParId(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget introuvable"));
        
        List<LigneCredit> lignes = ligneCreditRepo.findByBudget(budget);
        return lignes.stream()
                .map(l -> {
                    // Forcer le chargement des relations
                    if (l.getCreePar() != null) {
                        l.getCreePar().getNom();
                        if (l.getCreePar().getEntreprise() != null) {
                            l.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (l.getBudget() != null) {
                        l.getBudget().getIntituleBudget();
                        if (l.getBudget().getEntreprise() != null) {
                            l.getBudget().getEntreprise().getNom();
                        }
                    }
                    List<Commentaire> commentaires = getCommentaires(l);
                    return mapper.toDTO(l, commentaires);
                })
                .toList();
    }
    
    /**
     * Liste les lignes de crédit par statut
     */
    @Transactional(readOnly = true)
    public List<LigneCreditDTO> listerParStatutDTO(Statut statut, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Filtrer par entreprise de l'utilisateur et statut
        List<LigneCredit> lignes = ligneCreditRepo.findByBudgetEntrepriseAndStatut(
                utilisateur.getEntreprise(), statut);
        
        return lignes.stream()
                .map(l -> {
                    // Forcer le chargement des relations
                    if (l.getCreePar() != null) {
                        l.getCreePar().getNom();
                        if (l.getCreePar().getEntreprise() != null) {
                            l.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (l.getBudget() != null) {
                        l.getBudget().getIntituleBudget();
                        if (l.getBudget().getEntreprise() != null) {
                            l.getBudget().getEntreprise().getNom();
                        }
                    }
                    List<Commentaire> commentaires = getCommentaires(l);
                    return mapper.toDTO(l, commentaires);
                })
                .toList();
    }
    
    /**
     * Liste les lignes de crédit par état (actif/inactif)
     */
    @Transactional(readOnly = true)
    public List<LigneCreditDTO> listerParEtatDTO(boolean actif, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Filtrer par entreprise de l'utilisateur et état
        List<LigneCredit> lignes = ligneCreditRepo.findByBudgetEntreprise(utilisateur.getEntreprise())
                .stream()
                .filter(l -> l.isEtat() == actif)
                .toList();
        
        return lignes.stream()
                .map(l -> {
                    // Forcer le chargement des relations
                    if (l.getCreePar() != null) {
                        l.getCreePar().getNom();
                        if (l.getCreePar().getEntreprise() != null) {
                            l.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (l.getBudget() != null) {
                        l.getBudget().getIntituleBudget();
                        if (l.getBudget().getEntreprise() != null) {
                            l.getBudget().getEntreprise().getNom();
                        }
                    }
                    List<Commentaire> commentaires = getCommentaires(l);
                    return mapper.toDTO(l, commentaires);
                })
                .toList();
    }
    
    /**
     * Liste les lignes de crédit actives
     */
    @Transactional(readOnly = true)
    public List<LigneCreditDTO> listerActivesDTO(String emailUtilisateur) {
        return listerParEtatDTO(true, emailUtilisateur);
    }
    
    /**
     * Liste les lignes de crédit inactives
     */
    @Transactional(readOnly = true)
    public List<LigneCreditDTO> listerInactivesDTO(String emailUtilisateur) {
        return listerParEtatDTO(false, emailUtilisateur);
    }
}
