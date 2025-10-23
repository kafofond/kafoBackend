package kafofond.service;

import kafofond.dto.UtilisateurDTO;
import kafofond.entity.Entreprise;
import kafofond.entity.Role;
import kafofond.entity.Utilisateur;
import kafofond.mapper.UtilisateurMapper;
import kafofond.repository.EntrepriseRepo;
import kafofond.repository.UtilisateurRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurService {

    private final UtilisateurRepo utilisateurRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;
    private final UtilisateurMapper utilisateurMapper;

    @Transactional
    public Utilisateur creerUtilisateur(Utilisateur user, Utilisateur admin) {
        if (admin.getRole() != Role.SUPER_ADMIN && admin.getRole() != Role.ADMIN && admin.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seuls les Super Admins, Admins et Directeurs peuvent créer des utilisateurs");

        utilisateurRepo.findByEmail(user.getEmail())
                .ifPresent(u -> { throw new IllegalArgumentException("Email déjà utilisé"); });

        user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
        
        // Logique différente selon le rôle
        if (admin.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN : doit spécifier l'entreprise cible
            if (user.getEntreprise() == null || user.getEntreprise().getId() == null) {
                throw new IllegalArgumentException("Le Super Admin doit spécifier l'entreprise de l'utilisateur");
            }
            // Vérifier que l'entreprise existe
            Entreprise entreprise = entrepriseRepo.findById(user.getEntreprise().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Entreprise introuvable"));
            user.setEntreprise(entreprise);
        } else {
            // ADMIN/DIRECTEUR : hérite automatiquement de leur entreprise
            user.setEntreprise(admin.getEntreprise());
        }
        
        user.setEtat(true);

        Utilisateur saved = utilisateurRepo.save(user);
        historiqueService.enregistrerAction("UTILISATEUR", saved.getId(), "CREATION", admin, null,
                saved.isEtat() ? "ACTIF" : "INACTIF", null, null, "Création utilisateur");

        try {
            notificationService.envoyerEmail(saved.getEmail(), "Compte créé",
                    String.format("Bonjour %s %s, votre compte a été créé avec le rôle %s",
                            saved.getPrenom(), saved.getNom(), saved.getRole()));
        } catch (Exception e) { log.warn(e.getMessage()); }

        return saved;
    }

    @Transactional
    public Utilisateur modifierUtilisateur(Long id, Utilisateur modif, Utilisateur admin) {
        if (admin.getRole() != Role.SUPER_ADMIN && admin.getRole() != Role.ADMIN && admin.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seuls Super Admins, Admins et Directeurs peuvent modifier");

        Utilisateur user = utilisateurRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Vérifier les permissions d'entreprise
        if (admin.getRole() != Role.SUPER_ADMIN) {
            // ADMIN et DIRECTEUR ne peuvent modifier que les utilisateurs de leur entreprise
            if (!user.getEntreprise().getId().equals(admin.getEntreprise().getId())) {
                throw new IllegalArgumentException("Vous ne pouvez modifier que les utilisateurs de votre entreprise");
            }
        }

        Role ancienRole = user.getRole();
        boolean ancienEtat = user.isEtat();

        user.setNom(modif.getNom());
        user.setPrenom(modif.getPrenom());
        user.setEmail(modif.getEmail());
        user.setDepartement(modif.getDepartement());
        user.setRole(modif.getRole());
        if (modif.getMotDePasse() != null && !modif.getMotDePasse().trim().isEmpty())
            user.setMotDePasse(passwordEncoder.encode(modif.getMotDePasse()));

        Utilisateur updated = utilisateurRepo.save(user);

        historiqueService.enregistrerAction("UTILISATEUR", id, "MODIFICATION", admin,
                ancienEtat ? "ACTIF" : "INACTIF",
                updated.isEtat() ? "ACTIF" : "INACTIF", null, null,
                ancienRole != modif.getRole() ? String.format("Rôle %s -> %s", ancienRole, modif.getRole()) : null);

        return updated;
    }

    @Transactional
    public UtilisateurDTO modifierUtilisateurDTO(Long id, Utilisateur modif, Utilisateur admin) {
        Utilisateur updated = modifierUtilisateur(id, modif, admin);
        return utilisateurMapper.toDTO(updated);
    }

    @Transactional
    public UtilisateurDTO modifierMotDePasseUtilisateur(Long id, String nouveauMotDePasse, Utilisateur admin) {
        // Vérifier les permissions
        if (admin.getRole() != Role.SUPER_ADMIN && admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Seuls les SUPER_ADMIN et ADMIN peuvent modifier les mots de passe");
        }
        
        Utilisateur utilisateur = utilisateurRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Si c'est un admin, vérifier qu'il modifie un utilisateur de son entreprise
        if (admin.getRole() == Role.ADMIN) {
            if (!utilisateur.getEntreprise().getId().equals(admin.getEntreprise().getId())) {
                throw new IllegalArgumentException("Vous ne pouvez modifier que les utilisateurs de votre entreprise");
            }
        }
        
        // Modifier le mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        Utilisateur utilisateurModifie = utilisateurRepo.save(utilisateur);
        
        historiqueService.enregistrerAction("UTILISATEUR", id, "MODIFICATION_MDP", admin,
                null, null, null, null, "Modification du mot de passe");
        
        return utilisateurMapper.toDTO(utilisateurModifie);
    }

    @Transactional
    public Utilisateur desactiverUtilisateur(Long id, Utilisateur admin) {
        if (admin.getRole() != Role.SUPER_ADMIN && admin.getRole() != Role.ADMIN && admin.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seuls Super Admins, Admins et Directeurs peuvent désactiver");

        Utilisateur user = utilisateurRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Vérifier les permissions d'entreprise
        if (admin.getRole() != Role.SUPER_ADMIN) {
            // ADMIN et DIRECTEUR ne peuvent désactiver que les utilisateurs de leur entreprise
            if (!user.getEntreprise().getId().equals(admin.getEntreprise().getId())) {
                throw new IllegalArgumentException("Vous ne pouvez désactiver que les utilisateurs de votre entreprise");
            }
        }

        user.setEtat(false);
        Utilisateur updated = utilisateurRepo.save(user);

        historiqueService.enregistrerAction("UTILISATEUR", id, "DESACTIVATION", admin, "ACTIF", "INACTIF", null, null, "Utilisateur désactivé");
        return updated;
    }

    @Transactional
    public UtilisateurDTO desactiverUtilisateurDTO(Long id, Utilisateur admin) {
        Utilisateur updated = desactiverUtilisateur(id, admin);
        return utilisateurMapper.toDTO(updated);
    }

    @Transactional
    public Utilisateur reactiverUtilisateur(Long id, Utilisateur admin) {
        if (admin.getRole() != Role.SUPER_ADMIN && admin.getRole() != Role.ADMIN && admin.getRole() != Role.DIRECTEUR)
            throw new IllegalArgumentException("Seuls Super Admins, Admins et Directeurs peuvent réactiver");

        Utilisateur user = utilisateurRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Vérifier les permissions d'entreprise
        if (admin.getRole() != Role.SUPER_ADMIN) {
            // ADMIN et DIRECTEUR ne peuvent réactiver que les utilisateurs de leur entreprise
            if (!user.getEntreprise().getId().equals(admin.getEntreprise().getId())) {
                throw new IllegalArgumentException("Vous ne pouvez réactiver que les utilisateurs de votre entreprise");
            }
        }

        user.setEtat(true);
        Utilisateur updated = utilisateurRepo.save(user);

        historiqueService.enregistrerAction("UTILISATEUR", id, "REACTIVATION", admin, "INACTIF", "ACTIF", null, null, "Utilisateur réactivé");
        return updated;
    }

    @Transactional
    public UtilisateurDTO reactiverUtilisateurDTO(Long id, Utilisateur admin) {
        Utilisateur updated = reactiverUtilisateur(id, admin);
        return utilisateurMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listerParEntreprise(Entreprise entreprise) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entreprise.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerParEntrepriseDTO(Entreprise entreprise) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entreprise.getId()))
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listerActifsParEntreprise(Entreprise entreprise) {
        return listerParEntreprise(entreprise).stream()
                .filter(Utilisateur::isEtat)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerActifsParEntrepriseDTO(Entreprise entreprise) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entreprise.getId()))
                .filter(Utilisateur::isEtat)
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerTousActifsDTO() {
        return utilisateurRepo.findAll().stream()
                .filter(Utilisateur::isEtat)
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerInactifsParEntrepriseDTO(Entreprise entreprise) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entreprise.getId()))
                .filter(u -> !u.isEtat())
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerTousInactifsDTO() {
        return utilisateurRepo.findAll().stream()
                .filter(u -> !u.isEtat())
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerTousUtilisateursDTO() {
        return utilisateurRepo.findAll().stream()
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerParRoleDTO(Role role) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerParRoleEtEntrepriseDTO(Role role, Entreprise entreprise) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getRole() == role)
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entreprise.getId()))
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerParEntrepriseIdDTO(Long entrepriseId) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entrepriseId))
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerActifsParEntrepriseIdDTO(Long entrepriseId) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entrepriseId))
                .filter(Utilisateur::isEtat)
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerInactifsParEntrepriseIdDTO(Long entrepriseId) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entrepriseId))
                .filter(u -> !u.isEtat())
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerParRoleEtEntrepriseIdDTO(Role role, Long entrepriseId) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> u.getRole() == role)
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entrepriseId))
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> trouverParId(Long id) {
        return utilisateurRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UtilisateurDTO> trouverParIdDTO(Long id) {
        Optional<Utilisateur> utilisateur = utilisateurRepo.findById(id);
        // Le mapping se fait dans la transaction, donc l'entreprise sera accessible
        return utilisateur.map(utilisateurMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public String getNomEntrepriseParId(Long entrepriseId) {
        return entrepriseRepo.findById(entrepriseId)
                .map(Entreprise::getNom)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> trouverParEmail(String email) {
        return utilisateurRepo.findByEmail(email);
    }
    
    @Transactional(readOnly = true)
    public Optional<Utilisateur> trouverParEmailAvecEntreprise(String email) {
        return utilisateurRepo.findByEmailWithEntreprise(email);
    }

    @Transactional(readOnly = true)
    public Optional<UtilisateurDTO> trouverParEmailDTO(String email) {
        Optional<Utilisateur> utilisateur = utilisateurRepo.findByEmailWithEntreprise(email);
        // Le mapping se fait dans la transaction, donc l'entreprise sera accessible
        return utilisateur.map(utilisateurMapper::toDTO);
    }
}