package kafofond.service;

import kafofond.dto.EntrepriseCreateDTO;
import kafofond.dto.EntrepriseDTO;
import kafofond.entity.Entreprise;
import kafofond.entity.Role;
import kafofond.entity.Utilisateur;
import kafofond.exception.OperationNonAutoriseeException;
import kafofond.exception.RessourceNonTrouveeException;
import kafofond.mapper.EntrepriseMapper;
import kafofond.repository.EntrepriseRepo;
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
public class EntrepriseService {

    private final EntrepriseRepo entrepriseRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final HistoriqueService historiqueService;
    private final EntrepriseMapper entrepriseMapper;

    /**
     * Crée une nouvelle entreprise (Super Admin uniquement)
     */
    @Transactional
    public EntrepriseDTO creerEntreprise(EntrepriseDTO entrepriseDTO, String emailSuperAdmin) {
        log.info("Création d'une entreprise par {}", emailSuperAdmin);

        Utilisateur superAdmin = utilisateurRepo.findByEmail(emailSuperAdmin)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        // Vérification du rôle
        if (superAdmin.getRole() != Role.SUPER_ADMIN) {
            throw new OperationNonAutoriseeException("Seul le Super Admin peut créer des entreprises");
        }

        // Vérification des doublons
        if (entrepriseRepo.existsByNom(entrepriseDTO.getNom())) {
            throw new IllegalArgumentException("Une entreprise avec ce nom existe déjà");
        }

        if (entrepriseDTO.getEmail() != null && entrepriseRepo.existsByEmail(entrepriseDTO.getEmail())) {
            throw new IllegalArgumentException("Une entreprise avec cet email existe déjà");
        }

        // Préparation de l'entité
        Entreprise entreprise = entrepriseMapper.toEntity(entrepriseDTO);
        entreprise.setDateCreation(LocalDate.now().atStartOfDay());
        entreprise.setEtat(Boolean.TRUE); // actif par défaut

        Entreprise entrepriseCreee = entrepriseRepo.save(entreprise);

        // Enregistrement historique
        historiqueService.enregistrerAction(
                "ENTREPRISE",
                entrepriseCreee.getId(),
                "CREATION",
                superAdmin,
                null,
                "ACTIF",
                null,
                null,
                "Création de l'entreprise " + entrepriseCreee.getNom());

        return entrepriseMapper.toDTO(entrepriseCreee);
    }

    /**
     * Crée une nouvelle entreprise à partir d'un DTO simplifié (Super Admin
     * uniquement)
     */
    @Transactional
    public EntrepriseDTO creerEntrepriseFromSimpleDTO(EntrepriseCreateDTO entrepriseCreateDTO, String emailSuperAdmin) {
        log.info("Création d'une entreprise par {}", emailSuperAdmin);

        Utilisateur superAdmin = utilisateurRepo.findByEmail(emailSuperAdmin)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        // Vérification du rôle
        if (superAdmin.getRole() != Role.SUPER_ADMIN) {
            throw new OperationNonAutoriseeException("Seul le Super Admin peut créer des entreprises");
        }

        // Vérification des doublons
        if (entrepriseRepo.existsByNom(entrepriseCreateDTO.getNom())) {
            throw new IllegalArgumentException("Une entreprise avec ce nom existe déjà");
        }

        if (entrepriseCreateDTO.getEmail() != null && entrepriseRepo.existsByEmail(entrepriseCreateDTO.getEmail())) {
            throw new IllegalArgumentException("Une entreprise avec cet email existe déjà");
        }

        // Conversion du DTO simplifié vers le DTO complet
        EntrepriseDTO entrepriseDTO = EntrepriseDTO.builder()
                .nom(entrepriseCreateDTO.getNom())
                .domaine(entrepriseCreateDTO.getDomaine())
                .adresse(entrepriseCreateDTO.getAdresse())
                .telephone(entrepriseCreateDTO.getTelephone())
                .email(entrepriseCreateDTO.getEmail())
                .build();

        // Préparation de l'entité
        Entreprise entreprise = entrepriseMapper.toEntity(entrepriseDTO);
        entreprise.setDateCreation(LocalDate.now().atStartOfDay());
        entreprise.setEtat(Boolean.TRUE); // actif par défaut

        Entreprise entrepriseCreee = entrepriseRepo.save(entreprise);

        // Enregistrement historique
        historiqueService.enregistrerAction(
                "ENTREPRISE",
                entrepriseCreee.getId(),
                "CREATION",
                superAdmin,
                null,
                "ACTIF",
                null,
                null,
                "Création de l'entreprise " + entrepriseCreee.getNom());

        return entrepriseMapper.toDTO(entrepriseCreee);
    }

    /**
     * Modifie une entreprise existante (Super Admin uniquement)
     */
    @Transactional
    public EntrepriseDTO modifierEntreprise(Long id, EntrepriseDTO entrepriseDTO, String emailUtilisateur) {
        log.info("Modification de l'entreprise {} par {}", id, emailUtilisateur);

        Utilisateur utilisateur = utilisateurRepo.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        // Vérification du rôle
        if (utilisateur.getRole() != Role.SUPER_ADMIN && utilisateur.getRole() != Role.ADMIN) {
            throw new OperationNonAutoriseeException("Seul le Super Admin et l'Admin peuvent modifier des entreprises");
        }

        // Si c'est un admin, vérifier qu'il modifie son entreprise
        if (utilisateur.getRole() == Role.ADMIN) {
            if (utilisateur.getEntreprise() == null || !utilisateur.getEntreprise().getId().equals(id)) {
                throw new OperationNonAutoriseeException("Un admin ne peut modifier que son entreprise");
            }
        }

        Entreprise entreprise = entrepriseRepo.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Entreprise introuvable"));

        String ancienEtat = Boolean.TRUE.equals(entreprise.getEtat()) ? "ACTIF" : "INACTIF";

        // Vérification des doublons (exclure l'entreprise actuelle)
        if (entrepriseDTO.getNom() != null && !entreprise.getNom().equals(entrepriseDTO.getNom())
                && entrepriseRepo.existsByNom(entrepriseDTO.getNom())) {
            throw new IllegalArgumentException("Une entreprise avec ce nom existe déjà");
        }

        if (entrepriseDTO.getEmail() != null && !entreprise.getEmail().equals(entrepriseDTO.getEmail())
                && entrepriseRepo.existsByEmail(entrepriseDTO.getEmail())) {            throw new IllegalArgumentException("Une entreprise avec cet email existe déjà");

        }

        // Mise à jour des champs (on vérifie les nulls pour éviter d'écraser
        // involontairement)
        if (entrepriseDTO.getNom() != null)
            entreprise.setNom(entrepriseDTO.getNom());
        if (entrepriseDTO.getDomaine() != null)
            entreprise.setDomaine(entrepriseDTO.getDomaine());
        if (entrepriseDTO.getAdresse() != null)
            entreprise.setAdresse(entrepriseDTO.getAdresse());
        if (entrepriseDTO.getTelephone() != null)
            entreprise.setTelephone(entrepriseDTO.getTelephone());
        if (entrepriseDTO.getEmail() != null)
            entreprise.setEmail(entrepriseDTO.getEmail());
        if (entrepriseDTO.getEtat() != null)
            entreprise.setEtat(entrepriseDTO.getEtat());

        Entreprise entrepriseModifiee = entrepriseRepo.save(entreprise);

        // Enregistrement historique
        historiqueService.enregistrerAction(
                "ENTREPRISE",
                id,
                "MODIFICATION",
                utilisateur,
                ancienEtat,
                Boolean.TRUE.equals(entreprise.getEtat()) ? "ACTIF" : "INACTIF",
                null,
                null,
                "Modification des informations de l'entreprise");

        return entrepriseMapper.toDTO(entrepriseModifiee);
    }

    /**
     * Liste toutes les entreprises (Super Admin uniquement)
     */
    public List<EntrepriseDTO> listerToutesEntreprises(String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        if (utilisateur.getRole() != Role.SUPER_ADMIN) {
            throw new OperationNonAutoriseeException("Seul le Super Admin peut lister toutes les entreprises");
        }

        List<Entreprise> entreprises = entrepriseRepo.findAll();
        return entrepriseMapper.toDTOList(entreprises);
    }

    /**
     * Trouve une entreprise par ID
     */
    public Optional<EntrepriseDTO> trouverParId(Long id) {
        return entrepriseRepo.findById(id)
                .map(entrepriseMapper::toDTO);
    }

    /**
     * Trouve une entreprise par nom
     */
    public Optional<EntrepriseDTO> trouverParNom(String nom) {
        return entrepriseRepo.findByNom(nom)
                .map(entrepriseMapper::toDTO);
    }

    /**
     * Active/désactive une entreprise
     */
    @Transactional
    public EntrepriseDTO changerEtatEntreprise(Long id, boolean etat, String emailSuperAdmin) {
        Utilisateur superAdmin = utilisateurRepo.findByEmail(emailSuperAdmin)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        if (superAdmin.getRole() != Role.SUPER_ADMIN) {
            throw new OperationNonAutoriseeException("Seul le Super Admin peut changer l'état d'une entreprise");
        }

        Entreprise entreprise = entrepriseRepo.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Entreprise introuvable"));

        String ancienEtat = Boolean.TRUE.equals(entreprise.getEtat()) ? "ACTIF" : "INACTIF";
        entreprise.setEtat(etat);
        Entreprise entrepriseModifiee = entrepriseRepo.save(entreprise);

        // Enregistrement historique
        historiqueService.enregistrerAction(
                "ENTREPRISE",
                id,
                etat ? "ACTIVATION" : "DESACTIVATION",
                superAdmin,
                ancienEtat,
                etat ? "ACTIF" : "INACTIF",
                null,
                null,
                etat ? "Activation de l'entreprise" : "Désactivation de l'entreprise");

        return entrepriseMapper.toDTO(entrepriseModifiee);
    }

    /**
     * Vérifie si une entreprise appartient à un utilisateur
     */
    public boolean appartientAUtilisateur(Long entrepriseId, String emailUtilisateur) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        // Un admin peut modifier son entreprise
        if (utilisateur.getRole() == Role.ADMIN && utilisateur.getEntreprise() != null) {
            return utilisateur.getEntreprise().getId().equals(entrepriseId);
        }

        // Un super admin peut modifier toutes les entreprises
        if (utilisateur.getRole() == Role.SUPER_ADMIN) {
            return true;
        }

        return false;
    }
}
