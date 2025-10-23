package kafofond.service;

import kafofond.dto.ReinitialisationMotDePasseDTO;
import kafofond.entity.Utilisateur;
import kafofond.repository.UtilisateurRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReinitialisationMotDePasseService {

    private final UtilisateurRepo utilisateurRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    
    // Stockage temporaire des codes de réinitialisation avec email et timestamp (en production, utiliser Redis ou une base de données)
    private final Map<String, CodeReinitialisation> codesReinitialisation = new HashMap<>();
    
    /**
     * Demande de réinitialisation de mot de passe
     */
    public void demanderReinitialisation(String email) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        // Générer un code aléatoire
        String code = genererCodeAleatoire();
        CodeReinitialisation codeReinit = new CodeReinitialisation(code, email, LocalDateTime.now());
        codesReinitialisation.put(code, codeReinit);
        
        // Envoyer le code par email avec une durée de validité
        try {
            String message = String.format(
                "Votre code de réinitialisation est : %s\n\n" +
                "Ce code est valable pendant 10 minutes.\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.",
                code
            );
            notificationService.envoyerEmail(email, "Réinitialisation de mot de passe", message);
            log.info("Code de réinitialisation envoyé à {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du code de réinitialisation : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi du code de réinitialisation");
        }
    }
    
    /**
     * Réinitialiser le mot de passe avec le code
     */
    @Transactional
    public void reinitialiserMotDePasse(ReinitialisationMotDePasseDTO dto) {
        String codeSaisi = dto.getCode();
        String nouveauMotDePasse = dto.getNouveauMotDePasse();
        String confirmationMotDePasse = dto.getConfirmationMotDePasse();
        
        // Vérifier que les deux mots de passe correspondent
        if (nouveauMotDePasse == null || confirmationMotDePasse == null || 
            !nouveauMotDePasse.equals(confirmationMotDePasse)) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        
        // Vérifier si le code est valide et non expiré
        CodeReinitialisation codeStocke = codesReinitialisation.get(codeSaisi);
        if (codeStocke == null) {
            throw new RuntimeException("Code de réinitialisation invalide ou expiré");
        }
        
        // Vérifier si le code est expiré (10 minutes)
        if (codeStocke.getTimestamp().isBefore(LocalDateTime.now().minusMinutes(10))) {
            codesReinitialisation.remove(codeSaisi); // Supprimer le code expiré
            throw new RuntimeException("Code de réinitialisation expiré");
        }
        
        // Mettre à jour le mot de passe
        String email = codeStocke.getEmail();
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepo.save(utilisateur);
        
        // Supprimer le code utilisé
        codesReinitialisation.remove(codeSaisi);
        
        log.info("Mot de passe réinitialisé pour l'utilisateur {}", email);
    }
    
    /**
     * Générer un code aléatoire de 6 chiffres
     */
    private String genererCodeAleatoire() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * Classe interne pour stocker le code avec son email et timestamp
     */
    private static class CodeReinitialisation {
        private final String code;
        private final String email;
        private final LocalDateTime timestamp;
        
        public CodeReinitialisation(String code, String email, LocalDateTime timestamp) {
            this.code = code;
            this.email = email;
            this.timestamp = timestamp;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getEmail() {
            return email;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}