package kafofond.service;

import kafofond.dto.SeuilValidationDTO;
import kafofond.entity.Entreprise;
import kafofond.entity.Role;
import kafofond.entity.SeuilValidation;
import kafofond.entity.Utilisateur;
import kafofond.mapper.SeuilValidationMapper;
import kafofond.repository.SeuilValidationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des seuils de validation
 * Permet au Directeur de configurer les seuils par entreprise
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeuilValidationService {

    private final SeuilValidationRepo seuilValidationRepo;
    private final SeuilValidationMapper seuilValidationMapper;
    private final HistoriqueService historiqueService;
    private final UtilisateurService utilisateurService;

    /**
     * Configure un seuil de validation (Directeur uniquement)
     */
    @Transactional
    public SeuilValidation configurerSeuil(SeuilValidation seuil, Utilisateur directeur) {
        log.info("Configuration d'un seuil de validation par {}", directeur.getEmail());
        
        // Forcer le chargement de l'entreprise du directeur pendant la transaction
        Entreprise entreprise = directeur.getEntreprise();
        if (entreprise != null) {
            entreprise.getNom(); // Accès pour initialiser le proxy
        }
        
        // Vérifier les droits
        if (directeur.getRole() != Role.DIRECTEUR && directeur.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul le Directeur ou Super Admin peut configurer les seuils de validation");
        }
        
        // Vérifier qu'il n'y a pas déjà un seuil actif
        SeuilValidation ancienSeuil = seuilValidationRepo.findByEntrepriseAndActif(entreprise, true)
                .orElse(null);
        
        if (ancienSeuil != null) {
            throw new IllegalArgumentException("Un seuil est déjà actif. Veuillez le désactiver avant d'en créer un nouveau.");
        }
        
        // Créer le nouveau seuil
        seuil.setEntreprise(entreprise);
        seuil.setActif(true);
        
        SeuilValidation saved = seuilValidationRepo.save(seuil);
        
        historiqueService.enregistrerAction("SEUIL_VALIDATION", saved.getId(), "CREATION", 
                directeur, null, "ACTIF", null, null, 
                String.format("Création seuil de validation: %.2f", saved.getMontantSeuil()));
        
        return saved;
    }

    /**
     * Configure un seuil et retourne le DTO (évite le lazy loading)
     */
    @Transactional
    public SeuilValidationDTO configurerSeuilDTO(SeuilValidation seuil, String emailDirecteur) {
        // Récupérer le directeur dans la transaction
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Forcer le chargement de l'entreprise
        Entreprise entreprise = directeur.getEntreprise();
        if (entreprise != null) {
            entreprise.getNom();
        }
        
        SeuilValidation saved = configurerSeuil(seuil, directeur);
        return seuilValidationMapper.toDTO(saved);
    }

    /**
     * Obtient le seuil de validation actif pour une entreprise
     */
    @Transactional(readOnly = true)
    public SeuilValidation obtenirSeuilActif(Entreprise entreprise) {
        return seuilValidationRepo.findByEntrepriseAndActif(entreprise, true)
                .orElse(null);
    }

    /**
     * Obtient le seuil actif en DTO
     */
    @Transactional(readOnly = true)
    public SeuilValidationDTO obtenirSeuilActifDTO(Entreprise entreprise) {
        SeuilValidation seuil = seuilValidationRepo.findByEntrepriseAndActif(entreprise, true)
                .orElse(null);
        return seuil != null ? seuilValidationMapper.toDTO(seuil) : null;
    }

    /**
     * Liste tous les seuils d'une entreprise
     */
    @Transactional(readOnly = true)
    public List<SeuilValidationDTO> listerSeuilsParEntreprise(Entreprise entreprise) {
        return seuilValidationRepo.findAll().stream()
                .filter(s -> s.getEntreprise() != null && s.getEntreprise().getId().equals(entreprise.getId()))
                .map(seuilValidationMapper::toDTO)
                .toList();
    }

    /**
     * Obtient un seuil par ID
     */
    @Transactional(readOnly = true)
    public Optional<SeuilValidation> trouverParId(Long id) {
        return seuilValidationRepo.findById(id);
    }

    /**
     * Obtient un seuil par ID en DTO
     */
    @Transactional(readOnly = true)
    public Optional<SeuilValidationDTO> trouverParIdDTO(Long id) {
        return seuilValidationRepo.findById(id)
                .map(seuilValidationMapper::toDTO);
    }

    /**
     * Modifie un seuil existant
     */
    @Transactional
    public SeuilValidation modifierSeuil(Long id, SeuilValidation modification, Utilisateur directeur) {
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        if (directeur.getRole() != Role.DIRECTEUR && directeur.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul le Directeur ou Super Admin peut modifier les seuils");
        }

        SeuilValidation seuil = seuilValidationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seuil introuvable"));

        double ancienMontant = seuil.getMontantSeuil();
        seuil.setMontantSeuil(modification.getMontantSeuil());

        SeuilValidation updated = seuilValidationRepo.save(seuil);

        historiqueService.enregistrerAction("SEUIL_VALIDATION", id, "MODIFICATION", 
                directeur, null, null, null, null,
                String.format("Modification seuil: %.2f -> %.2f", ancienMontant, updated.getMontantSeuil()));

        return updated;
    }

    /**
     * Modifie un seuil et retourne le DTO
     */
    @Transactional
    public SeuilValidationDTO modifierSeuilDTO(Long id, SeuilValidation modification, String emailDirecteur) {
        // Récupérer le directeur dans la transaction
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        SeuilValidation updated = modifierSeuil(id, modification, directeur);
        return seuilValidationMapper.toDTO(updated);
    }

    /**
     * Active un seuil (désactive automatiquement les autres)
     */
    @Transactional
    public SeuilValidation activerSeuil(Long id, Utilisateur directeur) {
        // Forcer le chargement de l'entreprise
        Entreprise entreprise = directeur.getEntreprise();
        if (entreprise != null) {
            entreprise.getNom();
        }
        
        if (directeur.getRole() != Role.DIRECTEUR && directeur.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul le Directeur ou Super Admin peut activer les seuils");
        }

        SeuilValidation seuil = seuilValidationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seuil introuvable"));

        if (seuil.isActif()) {
            throw new IllegalArgumentException("Ce seuil est déjà actif");
        }

        // Désactiver tous les autres seuils actifs de l'entreprise
        SeuilValidation ancienSeuilActif = seuilValidationRepo.findByEntrepriseAndActif(entreprise, true)
                .orElse(null);

        if (ancienSeuilActif != null) {
            ancienSeuilActif.setActif(false);
            seuilValidationRepo.save(ancienSeuilActif);
            
            historiqueService.enregistrerAction("SEUIL_VALIDATION", ancienSeuilActif.getId(), "DESACTIVATION", 
                    directeur, "ACTIF", "INACTIF", null, null, "Désactivation automatique");
        }

        // Activer le nouveau seuil
        seuil.setActif(true);
        SeuilValidation updated = seuilValidationRepo.save(seuil);

        historiqueService.enregistrerAction("SEUIL_VALIDATION", id, "ACTIVATION", 
                directeur, "INACTIF", "ACTIF", null, null, "Activation seuil");

        return updated;
    }

    /**
     * Active un seuil et retourne le DTO
     */
    @Transactional
    public SeuilValidationDTO activerSeuilDTO(Long id, String emailDirecteur) {
        // Récupérer le directeur dans la transaction
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        SeuilValidation updated = activerSeuil(id, directeur);
        return seuilValidationMapper.toDTO(updated);
    }

    /**
     * Désactive un seuil
     */
    @Transactional
    public SeuilValidation desactiverSeuil(Long id, Utilisateur directeur) {
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        if (directeur.getRole() != Role.DIRECTEUR && directeur.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul le Directeur ou Super Admin peut désactiver les seuils");
        }

        SeuilValidation seuil = seuilValidationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seuil introuvable"));

        if (!seuil.isActif()) {
            throw new IllegalArgumentException("Ce seuil est déjà inactif");
        }

        seuil.setActif(false);
        SeuilValidation updated = seuilValidationRepo.save(seuil);

        historiqueService.enregistrerAction("SEUIL_VALIDATION", id, "DESACTIVATION", 
                directeur, "ACTIF", "INACTIF", null, null, "Désactivation seuil");

        return updated;
    }

    /**
     * Désactive un seuil et retourne le DTO
     */
    @Transactional
    public SeuilValidationDTO desactiverSeuilDTO(Long id, String emailDirecteur) {
        // Récupérer le directeur dans la transaction
        Utilisateur directeur = utilisateurService.trouverParEmail(emailDirecteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Forcer le chargement de l'entreprise
        if (directeur.getEntreprise() != null) {
            directeur.getEntreprise().getNom();
        }
        
        SeuilValidation updated = desactiverSeuil(id, directeur);
        return seuilValidationMapper.toDTO(updated);
    }
}
