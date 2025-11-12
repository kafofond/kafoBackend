package kafofond.service;

import kafofond.entity.AttestationDeServiceFait;
import kafofond.entity.Utilisateur;
import kafofond.entity.Statut;
import kafofond.entity.BonDeCommande;
import kafofond.repository.AttestationDeServiceFaitRepo;
import kafofond.repository.UtilisateurRepo;
import kafofond.repository.BonDeCommandeRepo;
import kafofond.repository.EntrepriseRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des attestations de service fait
 * Implémente le workflow : EN_COURS → VALIDÉ par Gestionnaire → APPROUVÉ par
 * Comptable
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttestationServiceFaitService {

    private final AttestationDeServiceFaitRepo attestationDeServiceFaitRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final TableValidationService tableValidationService;
    private final CodeGeneratorService codeGeneratorService;

    /**
     * Crée une nouvelle attestation de service fait (Trésorerie et Gestionnaire)
     */
    @Transactional
    public AttestationDeServiceFait creer(AttestationDeServiceFait attestation, Utilisateur utilisateur) {
        log.info("Création d'une attestation de service fait par {}", utilisateur.getEmail());

        // Autoriser la création par Trésorerie et Gestionnaire
        if (utilisateur.getRole() != kafofond.entity.Role.TRESORERIE &&
                utilisateur.getRole() != kafofond.entity.Role.GESTIONNAIRE) {
            throw new IllegalArgumentException(
                    "Seule la Trésorerie et le Gestionnaire peuvent créer des attestations de service fait");
        }

        attestation.setCreePar(utilisateur);
        attestation.setEntreprise(utilisateur.getEntreprise());
        attestation.setDateCreation(LocalDateTime.now());

        // Si un bon de commande est lié, le récupérer avec son entreprise pour éviter
        // les problèmes de proxy
        if (attestation.getBonDeCommande() != null && attestation.getBonDeCommande().getId() != null) {
            Optional<BonDeCommande> bonDeCommandeOpt = bonDeCommandeRepo
                    .findByIdWithEntreprise(attestation.getBonDeCommande().getId());
            if (bonDeCommandeOpt.isPresent()) {
                BonDeCommande bonDeCommande = bonDeCommandeOpt.get();
                attestation.setBonDeCommande(bonDeCommande);
                // S'assurer que l'entreprise de l'attestation correspond à celle du bon de
                // commande
                attestation.setEntreprise(bonDeCommande.getEntreprise());
                
                // Mettre à jour la référence du bon de commande si elle n'est pas déjà définie
                if (attestation.getReferenceBonCommande() == null || attestation.getReferenceBonCommande().isEmpty()) {
                    attestation.setReferenceBonCommande(bonDeCommande.getCode());
                }
            }
        }

        // Sauvegarder d'abord pour obtenir l'ID
        AttestationDeServiceFait attestationTemp = attestationDeServiceFaitRepo.save(attestation);
        
        // Générer le code automatiquement si ce n'est pas déjà fait
        if (attestationTemp.getCode() == null || attestationTemp.getCode().isEmpty()) {
            String code = codeGeneratorService.generateAttestationServiceFaitCode(attestationTemp.getId(), LocalDate.now());
            attestationTemp.setCode(code);
        }
        
        AttestationDeServiceFait attestationCreee = attestationDeServiceFaitRepo.save(attestationTemp);

        // Historique
        historiqueService.enregistrerAction(
                "ATTESTATION_SERVICE_FAIT",
                attestationCreee.getId(),
                "CREATION",
                utilisateur,
                null, // ancienEtat (pas utilisé)
                null, // nouveauEtat (pas utilisé)
                null, // ancienStatut
                null, // nouveauStatut (pas de statut)
                "Créée par " + utilisateur.getRole() // commentaire
        );

        // Enregistrer dans la table de validation
        tableValidationService.enregistrerCreation(
                attestationCreee.getId(),
                kafofond.entity.TypeDocument.ATTESTATION_SERVICE_FAIT,
                utilisateur
        );

        // Notifier le Gestionnaire
        Utilisateur gestionnaire = trouverGestionnaire(utilisateur.getEntreprise());
        if (gestionnaire != null) {
            notificationService.notifierModification("ATTESTATION_SERVICE_FAIT", attestationCreee.getId(),
                    utilisateur, gestionnaire, "créée");
        }

        return attestationCreee;
    }

    /**
     * Liste toutes les attestations de service fait d'une entreprise
     */
    public List<AttestationDeServiceFait> listerParEntreprise(kafofond.entity.Entreprise entreprise) {
        return attestationDeServiceFaitRepo.findByEntreprise(entreprise);
    }
    
    /**
     * Liste toutes les attestations de service fait d'une entreprise par son ID
     */
    public List<AttestationDeServiceFait> listerParEntrepriseId(Long entrepriseId) {
        // Récupérer l'entreprise par son ID
        kafofond.entity.Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise introuvable avec ID: " + entrepriseId));
        return attestationDeServiceFaitRepo.findByEntreprise(entreprise);
    }

    /**
     * Trouve une attestation de service fait par ID
     */
    public Optional<AttestationDeServiceFait> trouverParId(Long id) {
        return attestationDeServiceFaitRepo.findById(id);
    }

    /**
     * Trouve une attestation de service fait par ID avec initialisation des
     * relations
     * Utilisé pour éviter les problèmes de lazy loading
     */
    @Transactional(readOnly = true)
    public Optional<AttestationDeServiceFait> trouverParIdAvecRelations(Long id) {
        return attestationDeServiceFaitRepo.findById(id)
                .map(attestation -> {
                    // Initialiser les relations pour éviter les problèmes de lazy loading
                    if (attestation.getCreePar() != null) {
                        attestation.getCreePar().getNom();
                        attestation.getCreePar().getPrenom();
                        attestation.getCreePar().getEmail();
                        if (attestation.getCreePar().getEntreprise() != null) {
                            attestation.getCreePar().getEntreprise().getNom();
                        }
                    }
                    if (attestation.getEntreprise() != null) {
                        attestation.getEntreprise().getNom();
                    }
                    if (attestation.getBonDeCommande() != null) {
                        attestation.getBonDeCommande().getId();
                        if (attestation.getBonDeCommande().getEntreprise() != null) {
                            attestation.getBonDeCommande().getEntreprise().getNom();
                        }
                    }
                    return attestation;
                });
    }

    /**
     * Trouve le gestionnaire d'une entreprise
     */
    private Utilisateur trouverGestionnaire(kafofond.entity.Entreprise entreprise) {
        // Éviter les problèmes de proxy en utilisant le nom de l'entreprise de manière
        // sécurisée
        try {
            String nomEntreprise = entreprise.getNom();
            return utilisateurRepo.findByEmail("gestionnaire@" + nomEntreprise.toLowerCase().replace(" ", "") + ".com")
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Impossible d'accéder à l'entreprise pour trouver le gestionnaire: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Trouve le comptable d'une entreprise
     */
    private Utilisateur trouverComptable(kafofond.entity.Entreprise entreprise) {
        // Éviter les problèmes de proxy en utilisant le nom de l'entreprise de manière
        // sécurisée
        try {
            String nomEntreprise = entreprise.getNom();
            return utilisateurRepo.findByEmail("comptable@" + nomEntreprise.toLowerCase().replace(" ", "") + ".com")
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Impossible d'accéder à l'entreprise pour trouver le comptable: {}", e.getMessage());
            return null;
        }
    }
}