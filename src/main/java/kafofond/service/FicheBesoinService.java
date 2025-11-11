package kafofond.service;

import kafofond.entity.FicheDeBesoin;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import kafofond.repository.FicheBesoinRepo;
import kafofond.repository.UtilisateurRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des fiches de besoin
 * Implémente le workflow : EN_COURS → VALIDÉ par Gestionnaire → APPROUVÉ par Comptable
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FicheBesoinService {

    private final FicheBesoinRepo ficheBesoinRepo;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final TableValidationService tableValidationService;
    private final CodeGeneratorService codeGeneratorService;
    private final UtilisateurService utilisateurService;
    private final UtilisateurRepo utilisateurRepo;
    private final CommentaireService commentaireService;

    @Transactional
    public FicheDeBesoin creer(FicheDeBesoin fiche, Utilisateur utilisateur) {
        log.info("Création d'une fiche de besoin par {}", utilisateur.getEmail());

        // Autoriser la création par Trésorerie et Gestionnaire
        if (utilisateur.getRole() != kafofond.entity.Role.TRESORERIE && 
            utilisateur.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException("Seule la Trésorerie et le Gestionnaire peuvent créer des fiches de besoin");
        }

        fiche.setCreePar(utilisateur);
        fiche.setEntreprise(utilisateur.getEntreprise());
        fiche.setStatut(Statut.EN_COURS);
        fiche.setDateCreation(LocalDate.now().atStartOfDay());
        
        // Calculer le montant total à partir des désignations
        if (fiche.getDesignations() != null && !fiche.getDesignations().isEmpty()) {
            double montantTotal = fiche.getDesignations().stream()
                    .mapToDouble(d -> d.getMontantTotal())
                    .sum();
            fiche.setMontantEstime(montantTotal);
            
            // Associer les désignations à la fiche
            fiche.getDesignations().forEach(d -> {
                d.setFicheDeBesoin(fiche);
                d.setDate(LocalDate.now());
            });
        }
        
        // Le champ quantité n'est plus utilisé (déprécié)
        fiche.setQuantite(0);

        FicheDeBesoin ficheCreee = ficheBesoinRepo.save(fiche);
        
        // Générer le code unique automatiquement
        String code = codeGeneratorService.generateFicheBesoinCode(ficheCreee.getId(), LocalDate.from(ficheCreee.getDateCreation()));
        ficheCreee.setCode(code);
        ficheCreee = ficheBesoinRepo.save(ficheCreee);

        // Historique
        historiqueService.enregistrerAction(
                "FICHE_BESOIN",
                ficheCreee.getId(),
                "CREATION",
                utilisateur,
                null,                       // ancienEtat
                null,                       // nouveauEtat
                null,                       // ancienStatut
                Statut.EN_COURS.name(),     // nouveauStatut
                "Créée par " + utilisateur.getRole()
        );

        Utilisateur gestionnaire = trouverGestionnaire(utilisateur.getEntreprise());
        if (gestionnaire != null) {
            notificationService.notifierModification("FICHE_BESOIN", ficheCreee.getId(),
                    utilisateur, gestionnaire, "créée");
        }

        return ficheCreee;
    }

    @Transactional
    public kafofond.dto.FicheBesoinDTO creerDTO(FicheDeBesoin fiche, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        FicheDeBesoin created = creer(fiche, utilisateur);
        
        // Forcer le chargement des relations lazy
        if (created.getDesignations() != null && !created.getDesignations().isEmpty()) {
            created.getDesignations().size();
            created.getDesignations().forEach(d -> d.getProduit());
        }
        if (created.getCreePar() != null) {
            created.getCreePar().getNom();
        }
        if (created.getEntreprise() != null) {
            created.getEntreprise().getNom();
        }
        
        // Mapper vers DTO dans la transaction
        return kafofond.dto.FicheBesoinDTO.builder()
                .id(created.getId())
                .code(created.getCode())
                .serviceBeneficiaire(created.getServiceBeneficiaire())
                .objet(created.getObjet())
                .description(created.getDescription())
                .montantEstime(created.getMontantEstime())
                .dateAttendu(created.getDateAttendu())
                .dateCreation(LocalDate.from(created.getDateCreation()))
                .statut(created.getStatut())
                .urlFichierJoint(created.getUrlFichierJoint())
                .createurNom(created.getCreePar() != null ? 
                    created.getCreePar().getPrenom() + " " + created.getCreePar().getNom() : null)
                .createurEmail(created.getCreePar() != null ? created.getCreePar().getEmail() : null)
                .entrepriseNom(created.getEntreprise() != null ? created.getEntreprise().getNom() : null)
                .designations(created.getDesignations() != null && !created.getDesignations().isEmpty() ?
                        created.getDesignations().stream()
                                .map(d -> kafofond.dto.DesignationDTO.builder()
                                        .id(d.getId())
                                        .produit(d.getProduit())
                                        .quantite(d.getQuantite())
                                        .prixUnitaire(d.getPrixUnitaire())
                                        .montantTotal(d.getMontantTotal())
                                        .date(d.getDate())
                                        .ficheBesoinId(created.getId())
                                        .build())
                                .collect(java.util.stream.Collectors.toList())
                        : null)
                .build();
    }

    @Transactional
    public FicheDeBesoin modifier(Long id, FicheDeBesoin ficheModifiee, Utilisateur modificateur) {
        log.info("Modification de la fiche de besoin {} par {}", id, modificateur.getEmail());
        
        // Forcer le chargement de l'entreprise
        if (modificateur.getEntreprise() != null) {
            modificateur.getEntreprise().getNom();
        }

        FicheDeBesoin fiche = ficheBesoinRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiche de besoin introuvable"));

        // Autoriser la modification par Trésorerie et Gestionnaire
        if (modificateur.getRole() != kafofond.entity.Role.TRESORERIE && 
            modificateur.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException("Seule la Trésorerie et le Gestionnaire peuvent modifier des fiches de besoin");
        }

        Statut ancienStatut = fiche.getStatut();

        fiche.setServiceBeneficiaire(ficheModifiee.getServiceBeneficiaire());
        fiche.setObjet(ficheModifiee.getObjet());
        fiche.setDescription(ficheModifiee.getDescription());
        fiche.setDateAttendu(ficheModifiee.getDateAttendu());
        fiche.setUrlFichierJoint(ficheModifiee.getUrlFichierJoint());
        
        // Calculer le montant total à partir des désignations
        if (ficheModifiee.getDesignations() != null && !ficheModifiee.getDesignations().isEmpty()) {
            double montantTotal = ficheModifiee.getDesignations().stream()
                    .mapToDouble(d -> d.getMontantTotal())
                    .sum();
            fiche.setMontantEstime(montantTotal);
            
            // Supprimer les anciennes désignations
            fiche.getDesignations().clear();
            
            // Ajouter les nouvelles désignations
            ficheModifiee.getDesignations().forEach(d -> {
                d.setFicheDeBesoin(fiche);
                d.setDate(LocalDate.now());
                fiche.getDesignations().add(d);
            });
        } else {
            fiche.setMontantEstime(ficheModifiee.getMontantEstime());
        }
        
        // Le champ quantité reste à 0 (déprécié)
        fiche.setQuantite(0);

        if (fiche.getStatut() == Statut.VALIDE || fiche.getStatut() == Statut.APPROUVE) {
            fiche.setStatut(Statut.EN_COURS);
        }

        FicheDeBesoin ficheModifie = ficheBesoinRepo.save(fiche);

        // Historique
        historiqueService.enregistrerAction(
                "FICHE_BESOIN",
                id,
                "MODIFICATION",
                modificateur,
                null,                                           // ancienEtat
                null,                                           // nouveauEtat
                ancienStatut != null ? ancienStatut.name() : null,  // ancienStatut
                fiche.getStatut().name(),                           // nouveauStatut
                "Fiche modifiée par " + modificateur.getRole()
        );

        Utilisateur gestionnaire = trouverGestionnaire(modificateur.getEntreprise());
        if (gestionnaire != null) {
            notificationService.notifierModification("FICHE_BESOIN", id,
                    modificateur, gestionnaire, "modifiée");
        }

        return ficheModifie;
    }

    @Transactional
    public FicheDeBesoin valider(Long id, Utilisateur gestionnaire) {
        log.info("Validation de la fiche de besoin {} par {}", id, gestionnaire.getEmail());

        if (gestionnaire.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException("Seul le Gestionnaire peut valider une fiche de besoin");
        }

        FicheDeBesoin fiche = ficheBesoinRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiche de besoin introuvable"));

        Statut ancienStatut = fiche.getStatut();
        fiche.setStatut(Statut.VALIDE);

        FicheDeBesoin ficheValidee = ficheBesoinRepo.save(fiche);

        // Historique
        historiqueService.enregistrerAction(
                "FICHE_BESOIN",
                id,
                "VALIDATION",
                gestionnaire,
                null,
                null,
                ancienStatut != null ? ancienStatut.name() : null,
                Statut.VALIDE.name(),
                "Validée par Gestionnaire"
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.FICHE_BESOIN,
                gestionnaire,
                "VALIDE",
                "Validée par Gestionnaire"
        );

        if (fiche.getCreePar() != null) {
            notificationService.notifierValidation("FICHE_BESOIN", id, gestionnaire,
                    fiche.getCreePar(), "validée", null);
        }

        Utilisateur comptable = trouverComptable(gestionnaire.getEntreprise());
        if (comptable != null) {
            notificationService.notifierModification("FICHE_BESOIN", id,
                    gestionnaire, comptable, "validée");
        }

        return ficheValidee;
    }

    @Transactional
    public kafofond.dto.FicheBesoinDTO validerDTO(Long id, String emailGestionnaire) {
        Utilisateur gestionnaire = utilisateurService.trouverParEmail(emailGestionnaire)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        FicheDeBesoin validated = valider(id, gestionnaire);
        
        // Forcer le chargement des relations lazy
        if (validated.getDesignations() != null && !validated.getDesignations().isEmpty()) {
            validated.getDesignations().size();
            validated.getDesignations().forEach(d -> d.getProduit());
        }
        if (validated.getCreePar() != null) {
            validated.getCreePar().getNom();
        }
        if (validated.getEntreprise() != null) {
            validated.getEntreprise().getNom();
        }
        
        // Mapper vers DTO dans la transaction
        return kafofond.dto.FicheBesoinDTO.builder()
                .id(validated.getId())
                .code(validated.getCode())
                .serviceBeneficiaire(validated.getServiceBeneficiaire())
                .objet(validated.getObjet())
                .description(validated.getDescription())
                .montantEstime(validated.getMontantEstime())
                .dateAttendu(validated.getDateAttendu())
                .dateCreation(LocalDate.from(validated.getDateCreation()))
                .statut(validated.getStatut())
                .createurNom(validated.getCreePar() != null ? 
                    validated.getCreePar().getPrenom() + " " + validated.getCreePar().getNom() : null)
                .createurEmail(validated.getCreePar() != null ? validated.getCreePar().getEmail() : null)
                .entrepriseNom(validated.getEntreprise() != null ? validated.getEntreprise().getNom() : null)
                .designations(validated.getDesignations() != null && !validated.getDesignations().isEmpty() ?
                        validated.getDesignations().stream()
                                .map(d -> kafofond.dto.DesignationDTO.builder()
                                        .id(d.getId())
                                        .produit(d.getProduit())
                                        .quantite(d.getQuantite())
                                        .prixUnitaire(d.getPrixUnitaire())
                                        .montantTotal(d.getMontantTotal())
                                        .date(d.getDate())
                                        .ficheBesoinId(validated.getId())
                                        .build())
                                .collect(java.util.stream.Collectors.toList())
                        : null)
                .build();
    }

    @Transactional
    public FicheDeBesoin approuver(Long id, Utilisateur comptable) {
        log.info("Approbation de la fiche de besoin {} par {}", id, comptable.getEmail());

        if (comptable.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seul le Comptable peut approuver une fiche de besoin");
        }

        FicheDeBesoin fiche = ficheBesoinRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiche de besoin introuvable"));

        Statut ancienStatut = fiche.getStatut();
        fiche.setStatut(Statut.APPROUVE);

        FicheDeBesoin ficheApprouvee = ficheBesoinRepo.save(fiche);

        historiqueService.enregistrerAction(
                "FICHE_BESOIN",
                id,
                "APPROBATION",
                comptable,
                null,                                           // ancienEtat
                null,                                           // nouveauEtat
                ancienStatut != null ? ancienStatut.name() : null,  // ancienStatut
                Statut.APPROUVE.name(),                             // nouveauStatut
                "Approuvée par Comptable"
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.FICHE_BESOIN,
                comptable,
                "APPROUVE",
                "Approuvée par Comptable"
        );

        if (fiche.getCreePar() != null) {
            notificationService.notifierValidation("FICHE_BESOIN", id, comptable,
                    fiche.getCreePar(), "approuvée", null);
        }

        return ficheApprouvee;
    }

    @Transactional
    public kafofond.dto.FicheBesoinDTO approuverDTO(Long id, String emailComptable) {
        Utilisateur comptable = utilisateurService.trouverParEmail(emailComptable)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        FicheDeBesoin approved = approuver(id, comptable);
        
        // Forcer le chargement des relations lazy
        if (approved.getDesignations() != null && !approved.getDesignations().isEmpty()) {
            approved.getDesignations().size();
            approved.getDesignations().forEach(d -> d.getProduit());
        }
        if (approved.getCreePar() != null) {
            approved.getCreePar().getNom();
        }
        if (approved.getEntreprise() != null) {
            approved.getEntreprise().getNom();
        }
        
        // Mapper vers DTO dans la transaction
        return kafofond.dto.FicheBesoinDTO.builder()
                .id(approved.getId())
                .code(approved.getCode())
                .serviceBeneficiaire(approved.getServiceBeneficiaire())
                .objet(approved.getObjet())
                .description(approved.getDescription())
                .montantEstime(approved.getMontantEstime())
                .dateAttendu(approved.getDateAttendu())
                .dateCreation(LocalDate.from(approved.getDateCreation()))
                .statut(approved.getStatut())
                .createurNom(approved.getCreePar() != null ? 
                    approved.getCreePar().getPrenom() + " " + approved.getCreePar().getNom() : null)
                .createurEmail(approved.getCreePar() != null ? approved.getCreePar().getEmail() : null)
                .entrepriseNom(approved.getEntreprise() != null ? approved.getEntreprise().getNom() : null)
                .designations(approved.getDesignations() != null && !approved.getDesignations().isEmpty() ?
                        approved.getDesignations().stream()
                                .map(d -> kafofond.dto.DesignationDTO.builder()
                                        .id(d.getId())
                                        .produit(d.getProduit())
                                        .quantite(d.getQuantite())
                                        .prixUnitaire(d.getPrixUnitaire())
                                        .montantTotal(d.getMontantTotal())
                                        .date(d.getDate())
                                        .ficheBesoinId(approved.getId())
                                        .build())
                                .collect(java.util.stream.Collectors.toList())
                        : null)
                .build();
    }

    @Transactional
    public FicheDeBesoin rejeter(Long id, Utilisateur validateur, String commentaire) {
        log.info("Rejet de la fiche de besoin {} par {}", id, validateur.getEmail());

        if (validateur.getRole() != kafofond.entity.Role.GESTIONNAIRE &&
                validateur.getRole() != kafofond.entity.Role.COMPTABLE) {
            throw new IllegalArgumentException("Seuls le Gestionnaire et le Comptable peuvent rejeter une fiche de besoin");
        }

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
        }

        FicheDeBesoin fiche = ficheBesoinRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiche de besoin introuvable"));

        Statut ancienStatut = fiche.getStatut();
        fiche.setStatut(Statut.REJETE);

        FicheDeBesoin ficheRejetee = ficheBesoinRepo.save(fiche);

        // L'enregistrement du commentaire se fait maintenant dans la table de validation
        // commentaireService.creerCommentaire(
        //         id,
        //         kafofond.entity.TypeDocument.FICHE_BESOIN,
        //         commentaire,
        //         validateur
        // );

        historiqueService.enregistrerAction(
                "FICHE_BESOIN",
                id,
                "REJET",
                validateur,
                null,                                           // ancienEtat
                null,                                           // nouveauEtat
                ancienStatut != null ? ancienStatut.name() : null,  // ancienStatut
                Statut.REJETE.name(),                               // nouveauStatut
                commentaire
        );

        // Enregistrement dans la table de validation
        tableValidationService.enregistrerValidation(
                id,
                kafofond.entity.TypeDocument.FICHE_BESOIN,
                validateur,
                "REJETE",
                commentaire
        );

        if (fiche.getCreePar() != null) {
            notificationService.notifierValidation("FICHE_BESOIN", id, validateur,
                    fiche.getCreePar(), "rejetée", commentaire);
        }

        return ficheRejetee;
    }

    @Transactional
    public kafofond.dto.FicheBesoinDTO rejeterDTO(Long id, String emailValidateur, String commentaire) {
        Utilisateur validateur = utilisateurService.trouverParEmail(emailValidateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        FicheDeBesoin rejected = rejeter(id, validateur, commentaire);
        
        // Forcer le chargement des relations lazy
        if (rejected.getDesignations() != null && !rejected.getDesignations().isEmpty()) {
            rejected.getDesignations().size();
            rejected.getDesignations().forEach(d -> d.getProduit());
        }
        if (rejected.getCreePar() != null) {
            rejected.getCreePar().getNom();
        }
        if (rejected.getEntreprise() != null) {
            rejected.getEntreprise().getNom();
        }
        
        // Mapper vers DTO dans la transaction
        return kafofond.dto.FicheBesoinDTO.builder()
                .id(rejected.getId())
                .code(rejected.getCode())
                .serviceBeneficiaire(rejected.getServiceBeneficiaire())
                .objet(rejected.getObjet())
                .description(rejected.getDescription())
                .montantEstime(rejected.getMontantEstime())
                .dateAttendu(rejected.getDateAttendu())
                .dateCreation(LocalDate.from(rejected.getDateCreation()))
                .statut(rejected.getStatut())
                .createurNom(rejected.getCreePar() != null ? 
                    rejected.getCreePar().getPrenom() + " " + rejected.getCreePar().getNom() : null)
                .createurEmail(rejected.getCreePar() != null ? rejected.getCreePar().getEmail() : null)
                .entrepriseNom(rejected.getEntreprise() != null ? rejected.getEntreprise().getNom() : null)
                .designations(rejected.getDesignations() != null && !rejected.getDesignations().isEmpty() ?
                        rejected.getDesignations().stream()
                                .map(d -> kafofond.dto.DesignationDTO.builder()
                                        .id(d.getId())
                                        .produit(d.getProduit())
                                        .quantite(d.getQuantite())
                                        .prixUnitaire(d.getPrixUnitaire())
                                        .montantTotal(d.getMontantTotal())
                                        .date(d.getDate())
                                        .ficheBesoinId(rejected.getId())
                                        .build())
                                .collect(java.util.stream.Collectors.toList())
                        : null)
                .build();
    }

    private Utilisateur trouverGestionnaire(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("gestionnaire@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    private Utilisateur trouverComptable(kafofond.entity.Entreprise entreprise) {
        return utilisateurRepo.findByEmail("comptable@" + entreprise.getNom().toLowerCase().replace(" ", "") + ".com")
                .orElse(null);
    }

    /**
     * Liste toutes les fiches de besoin d'une entreprise
     */
    public List<FicheDeBesoin> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return ficheBesoinRepo.findByEntreprise(entreprise);
    }
    
    /**
     * Liste toutes les fiches de besoin d'une entreprise et retourne les DTOs
     */
    @Transactional(readOnly = true)
    public List<kafofond.dto.FicheBesoinDTO> listerParEntrepriseDTO(kafofond.entity.Entreprise entreprise) {
        List<FicheDeBesoin> fiches = ficheBesoinRepo.findByEntreprise(entreprise);
        return fiches.stream()
                .map(fiche -> {
                    // Forcer le chargement des relations lazy
                    if (fiche.getDesignations() != null && !fiche.getDesignations().isEmpty()) {
                        fiche.getDesignations().size();
                        fiche.getDesignations().forEach(d -> d.getProduit());
                    }
                    if (fiche.getCreePar() != null) {
                        fiche.getCreePar().getNom();
                    }
                    if (fiche.getEntreprise() != null) {
                        fiche.getEntreprise().getNom();
                    }
                    
                    // Mapper vers DTO dans la transaction
                    return kafofond.dto.FicheBesoinDTO.builder()
                            .id(fiche.getId())
                            .code(fiche.getCode())
                            .serviceBeneficiaire(fiche.getServiceBeneficiaire())
                            .objet(fiche.getObjet())
                            .description(fiche.getDescription())
                            .montantEstime(fiche.getMontantEstime())
                            .dateAttendu(fiche.getDateAttendu())
                            .dateCreation(LocalDate.from(fiche.getDateCreation()))
                            .statut(fiche.getStatut())
                            .urlFichierJoint(fiche.getUrlFichierJoint())
                            .createurNom(fiche.getCreePar() != null ? 
                                fiche.getCreePar().getPrenom() + " " + fiche.getCreePar().getNom() : null)
                            .createurEmail(fiche.getCreePar() != null ? fiche.getCreePar().getEmail() : null)
                            .entrepriseNom(fiche.getEntreprise() != null ? fiche.getEntreprise().getNom() : null)
                            .designations(fiche.getDesignations() != null && !fiche.getDesignations().isEmpty() ?
                                    fiche.getDesignations().stream()
                                            .map(d -> kafofond.dto.DesignationDTO.builder()
                                                    .id(d.getId())
                                                    .produit(d.getProduit())
                                                    .quantite(d.getQuantite())
                                                    .prixUnitaire(d.getPrixUnitaire())
                                                    .montantTotal(d.getMontantTotal())
                                                    .date(d.getDate())
                                                    .ficheBesoinId(fiche.getId())
                                                    .build())
                                            .collect(java.util.stream.Collectors.toList())
                                    : null)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<FicheDeBesoin> trouverParId(Long id) {
        return ficheBesoinRepo.findById(id);
    }
    
    /**
     * Trouve une fiche de besoin par ID et retourne le DTO (avec chargement dans la transaction)
     */
    @Transactional(readOnly = true)
    public Optional<kafofond.dto.FicheBesoinDTO> trouverParIdDTO(Long id) {
        return ficheBesoinRepo.findById(id)
                .map(fiche -> {
                    // Forcer le chargement des relations lazy
                    if (fiche.getDesignations() != null && !fiche.getDesignations().isEmpty()) {
                        fiche.getDesignations().size(); // Force le chargement
                        fiche.getDesignations().forEach(d -> {
                            d.getProduit(); // Force le chargement de chaque désignation
                        });
                    }
                    if (fiche.getCreePar() != null) {
                        fiche.getCreePar().getNom();
                    }
                    if (fiche.getEntreprise() != null) {
                        fiche.getEntreprise().getNom();
                    }
                    
                    // Le mapping se fait dans la transaction
                    return kafofond.dto.FicheBesoinDTO.builder()
                            .id(fiche.getId())
                            .code(fiche.getCode())
                            .serviceBeneficiaire(fiche.getServiceBeneficiaire())
                            .objet(fiche.getObjet())
                            .description(fiche.getDescription())
                            .montantEstime(fiche.getMontantEstime())
                            .dateAttendu(fiche.getDateAttendu())
                            .dateCreation(LocalDate.from(fiche.getDateCreation()))
                            .statut(fiche.getStatut())
                            .urlFichierJoint(fiche.getUrlFichierJoint())
                            .createurNom(fiche.getCreePar() != null ? 
                                fiche.getCreePar().getPrenom() + " " + fiche.getCreePar().getNom() : null)
                            .createurEmail(fiche.getCreePar() != null ? fiche.getCreePar().getEmail() : null)
                            .entrepriseNom(fiche.getEntreprise() != null ? fiche.getEntreprise().getNom() : null)
                            .designations(fiche.getDesignations() != null && !fiche.getDesignations().isEmpty() ?
                                    fiche.getDesignations().stream()
                                            .map(d -> kafofond.dto.DesignationDTO.builder()
                                                    .id(d.getId())
                                                    .produit(d.getProduit())
                                                    .quantite(d.getQuantite())
                                                    .prixUnitaire(d.getPrixUnitaire())
                                                    .montantTotal(d.getMontantTotal())
                                                    .date(d.getDate())
                                                    .ficheBesoinId(fiche.getId())
                                                    .build())
                                            .collect(java.util.stream.Collectors.toList())
                                    : null)
                            .build();
                });
    }
    
    /**
     * Modifie une fiche de besoin et retourne le DTO
     */
    @Transactional
    public kafofond.dto.FicheBesoinDTO modifierDTO(Long id, FicheDeBesoin ficheModifiee, String emailModificateur) {
        Utilisateur modificateur = utilisateurService.trouverParEmail(emailModificateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        FicheDeBesoin ficheModif = modifier(id, ficheModifiee, modificateur);
        
        // Forcer le chargement des désignations
        if (ficheModif.getDesignations() != null && !ficheModif.getDesignations().isEmpty()) {
            ficheModif.getDesignations().size();
            ficheModif.getDesignations().forEach(d -> d.getProduit());
        }
        
        // Mapper vers DTO dans la transaction
        return kafofond.dto.FicheBesoinDTO.builder()
                .id(ficheModif.getId())
                .code(ficheModif.getCode())
                .serviceBeneficiaire(ficheModif.getServiceBeneficiaire())
                .objet(ficheModif.getObjet())
                .description(ficheModif.getDescription())
                .montantEstime(ficheModif.getMontantEstime())
                .dateAttendu(ficheModif.getDateAttendu())
                .dateCreation(LocalDate.from(ficheModif.getDateCreation()))
                .statut(ficheModif.getStatut())
                .urlFichierJoint(ficheModif.getUrlFichierJoint())
                .createurNom(ficheModif.getCreePar() != null ? 
                    ficheModif.getCreePar().getPrenom() + " " + ficheModif.getCreePar().getNom() : null)
                .createurEmail(ficheModif.getCreePar() != null ? ficheModif.getCreePar().getEmail() : null)
                .entrepriseNom(ficheModif.getEntreprise() != null ? ficheModif.getEntreprise().getNom() : null)
                .designations(ficheModif.getDesignations() != null && !ficheModif.getDesignations().isEmpty() ?
                        ficheModif.getDesignations().stream()
                                .map(d -> kafofond.dto.DesignationDTO.builder()
                                        .id(d.getId())
                                        .produit(d.getProduit())
                                        .quantite(d.getQuantite())
                                        .prixUnitaire(d.getPrixUnitaire())
                                        .montantTotal(d.getMontantTotal())
                                        .date(d.getDate())
                                        .ficheBesoinId(ficheModif.getId())
                                        .build())
                                .collect(java.util.stream.Collectors.toList())
                        : null)
                .build();
    }
    
    /**
     * Trouve une fiche de besoin par ID avec initialisation des relations
     * Utilisé pour éviter les problèmes de lazy loading
     */
    @Transactional(readOnly = true)
    public Optional<FicheDeBesoin> trouverParIdAvecRelations(Long id) {
        return ficheBesoinRepo.findById(id)
                .map(fiche -> {
                    // Initialiser les relations pour éviter les problèmes de lazy loading
                    if (fiche.getDesignations() != null && !fiche.getDesignations().isEmpty()) {
                        fiche.getDesignations().size(); // Force le chargement
                        fiche.getDesignations().forEach(d -> {
                            d.getProduit(); // Force le chargement de chaque désignation
                        });
                    }
                    if (fiche.getCreePar() != null) {
                        fiche.getCreePar().getNom();
                        fiche.getCreePar().getPrenom();
                        fiche.getCreePar().getEmail();
                        if (fiche.getCreePar().getEntreprise() != null) {
                            fiche.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (fiche.getEntreprise() != null) {
                        fiche.getEntreprise().getNom();
                    }
                    if (fiche.getDemandeDAchat() != null) {
                        fiche.getDemandeDAchat().getId();
                    }
                    return fiche;
                });
    }
}