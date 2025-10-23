package kafofond.controller;

import kafofond.config.jwt.JwtTokenProvider;
import kafofond.security.payload.*;
import kafofond.service.CustomUserDetailsService;
import kafofond.service.UtilisateurService;
import kafofond.repository.UtilisateurRepo;
import kafofond.dto.ReinitialisationMotDePasseDTO;
import kafofond.service.ReinitialisationMotDePasseService;
import kafofond.entity.Utilisateur;
import kafofond.entity.Role;
import kafofond.entity.Entreprise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller d'authentification
 * Gère la connexion et l'inscription des utilisateurs
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Endpoints pour l'authentification et l'inscription des utilisateurs")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UtilisateurRepo utilisateurRepo;
    private final UtilisateurService utilisateurService;
    private final ReinitialisationMotDePasseService reinitialisationMotDePasseService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Connexion d'un utilisateur
     */
    @Operation(summary = "Connexion utilisateur", 
               description = "Authentifie un utilisateur avec son email et mot de passe. Retourne un token JWT valide.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou utilisateur introuvable"),
        @ApiResponse(responseCode = "401", description = "Mot de passe incorrect ou compte désactivé")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            log.info("Tentative de connexion pour l'utilisateur {}", request.getEmail());
            
            Utilisateur user = utilisateurRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Vérifier si l'utilisateur est actif
            if (!user.isEtat()) {
                throw new RuntimeException("Compte désactivé");
            }

            // Vérifier le mot de passe
            if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {
                throw new RuntimeException("Mot de passe ou Email invalide");
            }

            // Générer le token JWT
            String token = jwtTokenProvider.generateToken(
                    user.getEmail(), 
                    user.getRole().name(), 
                    user.getId(), 
                    user.getEntreprise().getId()
            );

            JwtResponse response = new JwtResponse(token, "Bearer", user.getRole().name(), user.getEmail());
            
            log.info("Connexion réussie pour l'utilisateur {}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la connexion : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Inscription d'un nouvel utilisateur (Admin/SuperAdmin uniquement)
     */
    @Operation(summary = "Inscription utilisateur", 
               description = "Crée un nouvel utilisateur. Accessible uniquement aux Admin et Super Admin. Nécessite la confirmation du mot de passe.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - droits insuffisants")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request, Authentication authentication) {
        try {
            log.info("Tentative d'inscription pour l'utilisateur {}", request.getEmail());
            
            // Vérifier que les deux mots de passe correspondent
            if (request.getMotDePasse() == null || request.getConfirmationMotDePasse() == null || 
                !request.getMotDePasse().equals(request.getConfirmationMotDePasse())) {
                throw new RuntimeException("Les mots de passe ne correspondent pas");
            }
            
            // Vérifier que l'utilisateur connecté a les droits
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("Authentification requise");
            }
            
            String userRole = authentication.getAuthorities().iterator().next().getAuthority();
            if (!userRole.equals("ROLE_ADMIN") && !userRole.equals("ROLE_DIRECTEUR") && !userRole.equals("ROLE_SUPER_ADMIN")) {
                throw new RuntimeException("Vous n'êtes pas autorisé à créer des utilisateurs");
            }
            
            // Récupérer l'utilisateur connecté
            Utilisateur admin = utilisateurRepo.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
            
            // Créer le nouvel utilisateur
            Utilisateur newUser = Utilisateur.builder()
                    .nom(request.getNom())
                    .prenom(request.getPrenom())
                    .email(request.getEmail())
                    .motDePasse(passwordEncoder.encode(request.getMotDePasse())) // Encoder le mot de passe
                    .departement(request.getDepartement())
                    .role(Role.valueOf(request.getRole()))
                    .etat(true)
                    .build();
            
            // Si l'administrateur est un SUPER_ADMIN et qu'un ID d'entreprise est fourni, l'associer
            if (admin.getRole() == Role.SUPER_ADMIN && request.getEntrepriseId() != null) {
                Entreprise entreprise = new Entreprise();
                entreprise.setId(request.getEntrepriseId());
                newUser.setEntreprise(entreprise);
            }
            
            Utilisateur userCreated = utilisateurService.creerUtilisateur(newUser, admin);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur créé avec succès");
            response.put("userId", userCreated.getId().toString());
            
            log.info("Inscription réussie pour l'utilisateur {}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'inscription : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Réinitialisation de mot de passe - Demande de code
     */
    @Operation(summary = "Demander un code de réinitialisation",
               description = "Envoie un code de réinitialisation de mot de passe par email. Nécessite l'email de l'utilisateur.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Code envoyé avec succès"),
        @ApiResponse(responseCode = "400", description = "Email invalide ou utilisateur introuvable"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de l'envoi de l'email")
    })
    @PostMapping("/reinitialiser-mot-de-passe/demander")
    public ResponseEntity<?> demanderReinitialisationMotDePasse(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email requis");
            }
            
            reinitialisationMotDePasseService.demanderReinitialisation(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Code de réinitialisation envoyé par email");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la demande de réinitialisation de mot de passe : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Réinitialisation de mot de passe - Réinitialiser avec code
     */
    @Operation(summary = "Réinitialiser le mot de passe",
               description = "Réinitialise le mot de passe avec le code reçu par email. Nécessite le code, le nouveau mot de passe et sa confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
        @ApiResponse(responseCode = "400", description = "Code invalide ou données incorrectes"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<?> reinitialiserMotDePasse(@RequestBody ReinitialisationMotDePasseDTO dto) {
        try {
            reinitialisationMotDePasseService.reinitialiserMotDePasse(dto);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe réinitialisé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation de mot de passe : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Vérification du token JWT
     */
    @Operation(summary = "Vérification token", 
               description = "Vérifie la validité du token JWT et retourne les informations de l'utilisateur connecté.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token valide"),
        @ApiResponse(responseCode = "401", description = "Token invalide ou expiré")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("email", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
    }
}
