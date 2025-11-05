package kafofond.controller;

import kafofond.dto.UtilisateurCreateDTO;
import kafofond.dto.UtilisateurDTO;
import kafofond.entity.Entreprise;
import kafofond.entity.Role;
import kafofond.entity.Utilisateur;
import kafofond.mapper.UtilisateurMapper;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des utilisateurs
 * Endpoints : CRUD utilisateurs (Admin et Directeur uniquement)
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Liste tous les utilisateurs de l'entreprise
     * 
     * @param entrepriseId (optionnel) ID de l'entreprise à filtrer (SUPER_ADMIN
     *                     uniquement)
     */
    @GetMapping
    public ResponseEntity<?> listerUtilisateurs(
            @RequestParam(required = false) Long entrepriseId,
            Authentication authentication) {
        try {
            log.info("Liste des utilisateurs demandée par {} (entrepriseId: {})", authentication.getName(),
                    entrepriseId);

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            log.info("Utilisateur connecté: {} avec le rôle: {}", utilisateur.getEmail(), utilisateur.getRole());

            List<UtilisateurDTO> utilisateursDTO;

            if (entrepriseId != null) {
                // Filtrage par entreprise spécifique
                log.info(
                        "Vérification du rôle pour filtrage par entreprise. Rôle de l'utilisateur: {}, Rôle requis: {}",
                        utilisateur.getRole(), Role.SUPER_ADMIN);
                if (utilisateur.getRole() != Role.SUPER_ADMIN) {
                    log.warn(
                            "Accès refusé: l'utilisateur {} avec le rôle {} n'est pas autorisé à filtrer par entreprise",
                            utilisateur.getEmail(), utilisateur.getRole());
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Seul le SUPER_ADMIN peut filtrer par entreprise");
                    return ResponseEntity.status(403).body(error);
                }
                utilisateursDTO = utilisateurService.listerParEntrepriseIdDTO(entrepriseId);
            } else if (utilisateur.getRole() == Role.SUPER_ADMIN) {
                // SUPER_ADMIN sans filtre: tous les utilisateurs
                utilisateursDTO = utilisateurService.listerTousUtilisateursDTO();
            } else {
                // ADMIN/DIRECTEUR: seulement leur entreprise
                utilisateursDTO = utilisateurService.listerParEntrepriseDTO(utilisateur.getEntreprise());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("utilisateurs", utilisateursDTO);
            response.put("total", utilisateursDTO.size());
            if (entrepriseId != null) {
                response.put("entrepriseId", entrepriseId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Crée un nouvel utilisateur
     */
    @PostMapping
    public ResponseEntity<?> creerUtilisateur(@RequestBody UtilisateurCreateDTO utilisateurCreateDTO,
            Authentication authentication) {
        try {
            log.info("Création d'un utilisateur par {}", authentication.getName());
            log.info("Rôles de l'utilisateur authentifié: {}", authentication.getAuthorities());

            // Vérifier si l'utilisateur a les autorisations nécessaires directement avec
            // Spring Security
            boolean hasPermission = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                            auth.getAuthority().equals("ROLE_ADMIN") ||
                            auth.getAuthority().equals("ROLE_DIRECTEUR"));

            if (!hasPermission) {
                log.warn("Accès refusé pour la création d'utilisateur: {}", authentication.getName());
                Map<String, String> error = new HashMap<>();
                error.put("message", "Vous n'avez pas les autorisations nécessaires pour créer un utilisateur");
                return ResponseEntity.status(403).body(error);
            }

            // Récupérer l'administrateur
            Utilisateur admin = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            log.info("Rôle de l'administrateur: {}", admin.getRole());

            // Créer l'utilisateur avec le service dédié
            UtilisateurDTO utilisateurCreeDTO = utilisateurService.creerUtilisateurFromSimpleDTO(utilisateurCreateDTO,
                    admin);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur créé avec succès");
            response.put("utilisateur", utilisateurCreeDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création de l'utilisateur : {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifie un utilisateur existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierUtilisateur(@PathVariable Long id, @RequestBody UtilisateurDTO utilisateurDTO,
            Authentication authentication) {
        try {
            log.info("Modification de l'utilisateur {} par {}", id, authentication.getName());

            Utilisateur admin = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Utilisateur utilisateurModifie = utilisateurMapper.toEntity(utilisateurDTO);
            UtilisateurDTO utilisateurMisAJourDTO = utilisateurService.modifierUtilisateurDTO(id, utilisateurModifie,
                    admin);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur modifié avec succès");
            response.put("utilisateur", utilisateurMisAJourDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'utilisateur : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Désactive un utilisateur
     */
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverUtilisateur(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Désactivation de l'utilisateur {} par {}", id, authentication.getName());

            Utilisateur admin = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            UtilisateurDTO utilisateurDesactiveDTO = utilisateurService.desactiverUtilisateurDTO(id, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur désactivé avec succès");
            response.put("utilisateur", utilisateurDesactiveDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la désactivation de l'utilisateur : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Réactive un utilisateur
     */
    @PostMapping("/{id}/reactiver")
    public ResponseEntity<?> reactiverUtilisateur(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Réactivation de l'utilisateur {} par {}", id, authentication.getName());

            Utilisateur admin = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            UtilisateurDTO utilisateurReactiveDTO = utilisateurService.reactiverUtilisateurDTO(id, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur réactivé avec succès");
            response.put("utilisateur", utilisateurReactiveDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la réactivation de l'utilisateur : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'un utilisateur
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirUtilisateur(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Détails de l'utilisateur {} demandés par {}", id, authentication.getName());

            return utilisateurService.trouverParIdDTO(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste tous les utilisateurs (SUPER_ADMIN uniquement)
     */
    @GetMapping("/tous")
    public ResponseEntity<?> listerTousUtilisateurs(Authentication authentication) {
        try {
            log.info("Liste de tous les utilisateurs demandée par {}", authentication.getName());

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Seul le SUPER_ADMIN peut lister tous les utilisateurs
            if (utilisateur.getRole() != Role.SUPER_ADMIN) {
                // Les admins peuvent lister les utilisateurs de leur entreprise
                if (utilisateur.getRole() == Role.ADMIN) {
                    List<UtilisateurDTO> utilisateursDTO = utilisateurService
                            .listerParEntrepriseDTO(utilisateur.getEntreprise());

                    Map<String, Object> response = new HashMap<>();
                    response.put("utilisateurs", utilisateursDTO);
                    response.put("total", utilisateursDTO.size());
                    response.put("entrepriseId", utilisateur.getEntreprise().getId());

                    return ResponseEntity.ok(response);
                }

                Map<String, String> error = new HashMap<>();
                error.put("message", "Seul le SUPER_ADMIN peut lister tous les utilisateurs");
                return ResponseEntity.status(403).body(error);
            }

            List<UtilisateurDTO> utilisateursDTO = utilisateurService.listerTousUtilisateursDTO();

            Map<String, Object> response = new HashMap<>();
            response.put("utilisateurs", utilisateursDTO);
            response.put("total", utilisateursDTO.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de tous les utilisateurs : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste les utilisateurs actifs
     * 
     * @param entrepriseId (optionnel) ID de l'entreprise à filtrer (SUPER_ADMIN
     *                     uniquement)
     * @param tous         (optionnel) Si true, liste tous les actifs de
     *                     l'application (SUPER_ADMIN uniquement)
     */
    @GetMapping("/actifs")
    public ResponseEntity<?> listerUtilisateursActifs(
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false, defaultValue = "false") Boolean tous,
            Authentication authentication) {
        try {
            log.info("Liste des utilisateurs actifs demandée par {} (entrepriseId: {}, tous: {})",
                    authentication.getName(), entrepriseId, tous);

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<UtilisateurDTO> utilisateursDTO;

            if (entrepriseId != null) {
                // Filtrage par entreprise spécifique
                if (utilisateur.getRole() != Role.SUPER_ADMIN) {
                    // Les admins peuvent filtrer par entreprise s'il s'agit de leur entreprise
                    if (utilisateur.getRole() == Role.ADMIN
                            && utilisateur.getEntreprise().getId().equals(entrepriseId)) {
                        utilisateursDTO = utilisateurService.listerActifsParEntrepriseIdDTO(entrepriseId);
                    } else {
                        Map<String, String> error = new HashMap<>();
                        error.put("message", "Seul le SUPER_ADMIN peut filtrer par entreprise");
                        return ResponseEntity.status(403).body(error);
                    }
                } else {
                    utilisateursDTO = utilisateurService.listerActifsParEntrepriseIdDTO(entrepriseId);
                }
            } else if (tous && utilisateur.getRole() == Role.SUPER_ADMIN) {
                // SUPER_ADMIN: tous les actifs de toute l'application
                utilisateursDTO = utilisateurService.listerTousActifsDTO();
            } else {
                // Par défaut: entreprise de l'utilisateur connecté
                utilisateursDTO = utilisateurService.listerActifsParEntrepriseDTO(utilisateur.getEntreprise());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("utilisateurs", utilisateursDTO);
            response.put("total", utilisateursDTO.size());
            if (entrepriseId != null) {
                response.put("entrepriseId", entrepriseId);
            }
            if (tous && entrepriseId == null) {
                response.put("scope", "global");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs actifs : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste les utilisateurs inactifs
     * 
     * @param entrepriseId (optionnel) ID de l'entreprise à filtrer (SUPER_ADMIN
     *                     uniquement)
     * @param tous         (optionnel) Si true, liste tous les inactifs de
     *                     l'application (SUPER_ADMIN uniquement)
     */
    @GetMapping("/inactifs")
    public ResponseEntity<?> listerUtilisateursInactifs(
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false, defaultValue = "false") Boolean tous,
            Authentication authentication) {
        try {
            log.info("Liste des utilisateurs inactifs demandée par {} (entrepriseId: {}, tous: {})",
                    authentication.getName(), entrepriseId, tous);

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<UtilisateurDTO> utilisateursDTO;

            if (entrepriseId != null) {
                // Filtrage par entreprise spécifique
                if (utilisateur.getRole() != Role.SUPER_ADMIN) {
                    // Les admins peuvent filtrer par entreprise s'il s'agit de leur entreprise
                    if (utilisateur.getRole() == Role.ADMIN
                            && utilisateur.getEntreprise().getId().equals(entrepriseId)) {
                        utilisateursDTO = utilisateurService.listerInactifsParEntrepriseIdDTO(entrepriseId);
                    } else {
                        Map<String, String> error = new HashMap<>();
                        error.put("message", "Seul le SUPER_ADMIN peut filtrer par entreprise");
                        return ResponseEntity.status(403).body(error);
                    }
                } else {
                    utilisateursDTO = utilisateurService.listerInactifsParEntrepriseIdDTO(entrepriseId);
                }
            } else if (tous && utilisateur.getRole() == Role.SUPER_ADMIN) {
                // SUPER_ADMIN: tous les inactifs de toute l'application
                utilisateursDTO = utilisateurService.listerTousInactifsDTO();
            } else {
                // Par défaut: entreprise de l'utilisateur connecté
                utilisateursDTO = utilisateurService.listerInactifsParEntrepriseDTO(utilisateur.getEntreprise());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("utilisateurs", utilisateursDTO);
            response.put("total", utilisateursDTO.size());
            if (entrepriseId != null) {
                response.put("entrepriseId", entrepriseId);
            }
            if (tous && entrepriseId == null) {
                response.put("scope", "global");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs inactifs : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste les utilisateurs par rôle
     * 
     * @param role         Le rôle à filtrer
     * @param entrepriseId (optionnel) ID de l'entreprise à filtrer (SUPER_ADMIN
     *                     uniquement)
     */
    @GetMapping("/par-role/{role}")
    public ResponseEntity<?> listerParRole(
            @PathVariable String role,
            @RequestParam(required = false) Long entrepriseId,
            Authentication authentication) {
        try {
            log.info("Liste des utilisateurs avec le rôle {} demandée par {} (entrepriseId: {})",
                    role, authentication.getName(), entrepriseId);

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Role roleEnum;
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Rôle invalide: " + role);
                return ResponseEntity.badRequest().body(error);
            }

            List<UtilisateurDTO> utilisateursDTO;

            if (entrepriseId != null) {
                // Filtrage par entreprise spécifique
                if (utilisateur.getRole() != Role.SUPER_ADMIN) {
                    // Les admins peuvent filtrer par rôle dans leur entreprise
                    if (utilisateur.getRole() == Role.ADMIN
                            && utilisateur.getEntreprise().getId().equals(entrepriseId)) {
                        utilisateursDTO = utilisateurService.listerParRoleEtEntrepriseDTO(roleEnum,
                                utilisateur.getEntreprise());
                    } else {
                        Map<String, String> error = new HashMap<>();
                        error.put("message", "Seul le SUPER_ADMIN peut filtrer par entreprise");
                        return ResponseEntity.status(403).body(error);
                    }
                } else {
                    utilisateursDTO = utilisateurService.listerParRoleEtEntrepriseIdDTO(roleEnum, entrepriseId);
                }
            } else if (utilisateur.getRole() == Role.SUPER_ADMIN) {
                // SUPER_ADMIN sans filtre: tous les utilisateurs avec ce rôle
                utilisateursDTO = utilisateurService.listerParRoleDTO(roleEnum);
            } else {
                // ADMIN/DIRECTEUR: seulement leur entreprise
                utilisateursDTO = utilisateurService.listerParRoleEtEntrepriseDTO(roleEnum,
                        utilisateur.getEntreprise());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("utilisateurs", utilisateursDTO);
            response.put("role", role);
            response.put("total", utilisateursDTO.size());
            if (entrepriseId != null) {
                response.put("entrepriseId", entrepriseId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs par rôle : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails de l'utilisateur connecté
     */
    @GetMapping("/moi")
    public ResponseEntity<?> obtenirMonProfil(Authentication authentication) {
        try {
            log.info("Profil de l'utilisateur demandé par {}", authentication.getName());

            return utilisateurService.trouverParEmailDTO(authentication.getName())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération du profil : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifie le profil de l'utilisateur connecté
     */
    @PutMapping("/moi")
    public ResponseEntity<?> modifierMonProfil(@RequestBody UtilisateurDTO utilisateurDTO,
            Authentication authentication) {
        try {
            log.info("Modification du profil par {}", authentication.getName());
            log.info("Données reçues: {}", utilisateurDTO);

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            log.info("Utilisateur trouvé: {}", utilisateur);

            // Seul l'utilisateur peut modifier son propre profil
            if (!utilisateur.getEmail().equals(authentication.getName())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Vous ne pouvez modifier que votre propre profil");
                return ResponseEntity.status(403).body(error);
            }

            // Empêcher la modification du rôle et de l'entreprise
            utilisateurDTO.setRole(utilisateur.getRole());
            if (utilisateur.getEntreprise() != null) {
                utilisateurDTO.setEntrepriseId(utilisateur.getEntreprise().getId());
                // Récupérer le nom de l'entreprise de manière transactionnelle
                String nomEntreprise = utilisateurService.getNomEntrepriseParId(utilisateur.getEntreprise().getId());
                utilisateurDTO.setEntrepriseNom(nomEntreprise);
            }

            Utilisateur utilisateurModifie = utilisateurMapper.toEntity(utilisateurDTO);
            UtilisateurDTO utilisateurMisAJourDTO = utilisateurService.modifierUtilisateurDTO(utilisateur.getId(),
                    utilisateurModifie, utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil modifié avec succès");
            response.put("utilisateur", utilisateurMisAJourDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la modification du profil : {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Change le mot de passe de l'utilisateur connecté
     */
    @PostMapping("/moi/changer-mot-de-passe")
    public ResponseEntity<?> changerMotDePasse(@RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            log.info("Changement de mot de passe par {}", authentication.getName());

            String ancienMotDePasse = request.get("ancienMotDePasse");
            String nouveauMotDePasse = request.get("nouveauMotDePasse");

            if (ancienMotDePasse == null || nouveauMotDePasse == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "L'ancien et le nouveau mot de passe sont requis");
                return ResponseEntity.badRequest().body(error);
            }

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "L'ancien mot de passe est incorrect");
                return ResponseEntity.badRequest().body(error);
            }

            // Mettre à jour le mot de passe
            utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
            utilisateurService.modifierUtilisateur(utilisateur.getId(), utilisateur, utilisateur);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe changé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifier le mot de passe d'un utilisateur (ADMIN/SUPER_ADMIN uniquement)
     */
    @PostMapping("/{id}/modifier-mot-de-passe")
    public ResponseEntity<?> modifierMotDePasseUtilisateur(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            log.info("Modification du mot de passe de l'utilisateur {} par {}", id, authentication.getName());

            Utilisateur admin = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Vérifier les permissions
            if (admin.getRole() != Role.SUPER_ADMIN && admin.getRole() != Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Seuls les SUPER_ADMIN et ADMIN peuvent modifier les mots de passe");
                return ResponseEntity.status(403).body(error);
            }

            String nouveauMotDePasse = request.get("nouveauMotDePasse");
            if (nouveauMotDePasse == null || nouveauMotDePasse.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Nouveau mot de passe requis");
                return ResponseEntity.badRequest().body(error);
            }

            // Si c'est un admin, vérifier qu'il modifie un utilisateur de son entreprise
            if (admin.getRole() == Role.ADMIN) {
                Utilisateur utilisateurAModifier = utilisateurService.trouverParId(id)
                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                if (!utilisateurAModifier.getEntreprise().getId().equals(admin.getEntreprise().getId())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Vous ne pouvez modifier que les utilisateurs de votre entreprise");
                    return ResponseEntity.status(403).body(error);
                }
            }

            // Modifier le mot de passe
            UtilisateurDTO utilisateurModifie = utilisateurService.modifierMotDePasseUtilisateur(id, nouveauMotDePasse,
                    admin);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mot de passe modifié avec succès");
            response.put("utilisateur", utilisateurModifie);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la modification du mot de passe : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
