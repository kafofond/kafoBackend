package kafofond.service;

import kafofond.entity.Notification;
import kafofond.entity.Utilisateur;
import kafofond.repository.NotificationRepo;
import kafofond.repository.AttestationDeServiceFaitRepo;
import kafofond.repository.BonDeCommandeRepo;
import kafofond.repository.DemandeDAchatRepo;
import kafofond.service.CodeGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

/**
 * Service de gestion des notifications
 * Gère l'envoi de notifications système et d'emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final JavaMailSender mailSender;
    private final AttestationDeServiceFaitRepo attestationDeServiceFaitRepo;
    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final DemandeDAchatRepo demandeDAchatRepo;
    private final CodeGeneratorService codeGeneratorService;

    /**
     * Crée une notification système pour un utilisateur
     */
    public Notification creerNotification(Utilisateur destinataire, String titre, String message, 
                                        String typeDocument, Long idDocument) {
        log.info("Création d'une notification pour {} : {}", destinataire.getEmail(), titre);
        
        Notification notification = Notification.builder()
                .titre(titre)
                .message(message)
                .etat(false)  // Non lue par défaut
                .transmission(null)  // Pas d'email par défaut
                .dateEnvoi(LocalDateTime.now())
                .destinataire(destinataire)
                .idDocument(idDocument)
                .build();
        
        return notificationRepo.save(notification);
    }

    /**
     * Envoie un email simple à un destinataire
     */
    public void envoyerEmail(String destinataire, String sujet, String corps) {
        try {
            log.info("Envoi d'un email à {} : {}", destinataire, sujet);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(corps);
            
            mailSender.send(message);
            log.info("Email envoyé avec succès à {}", destinataire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {} : {}", destinataire, e.getMessage());
        }
    }

    /**
     * Envoie un email HTML à un destinataire
     */
    public void envoyerEmailHtml(String destinataire, String sujet, String htmlContent) {
        try {
            log.info("Envoi d'un email HTML à {} : {}", destinataire, sujet);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(htmlContent, true); // true active le HTML
            
            mailSender.send(mimeMessage);
            log.info("Email HTML envoyé avec succès à {}", destinataire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email HTML à {} : {}", destinataire, e.getMessage());
        }
    }

    /**
     * Récupère le code d'un document selon son type et son ID
     */
    private String getCodeDocument(String typeDocument, Long idDocument) {
        if (idDocument == null) {
            return "N/A";
        }
        
        try {
            switch (typeDocument.toUpperCase()) {
                case "ATTESTATION_SERVICE_FAIT":
                    return attestationDeServiceFaitRepo.findById(idDocument)
                            .map(attestation -> attestation.getCode())
                            .orElse("ASF-" + String.format("%04d", idDocument));
                case "BON_COMMANDE":
                    return bonDeCommandeRepo.findById(idDocument)
                            .map(bon -> bon.getCode())
                            .orElse("BC-" + String.format("%04d", idDocument));
                case "DEMANDE_ACHAT":
                    return demandeDAchatRepo.findById(idDocument)
                            .map(demande -> demande.getCode())
                            .orElse("DA-" + String.format("%04d", idDocument));
                default:
                    return typeDocument + "-" + String.format("%04d", idDocument);
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer le code pour {} #{} : {}", typeDocument, idDocument, e.getMessage());
            return typeDocument + "-" + String.format("%04d", idDocument);
        }
    }

    /**
     * Formate un message avec du HTML pour les emails
     */
    private String formaterMessageHtml(String titre, String message) {
        return "<html>" +
               "<body>" +
               "<h2 style='color: blue; font-weight: bold;'>" + titre + "</h2>" +
               "<p>" + message.replace("\n", "<br>") + "</p>" +
               "<hr>" +
               "<p><i>Ceci est une notification automatique de KafoFond.</i></p>" +
               "</body>" +
               "</html>";
    }

    /**
     * Notifie automatiquement le supérieur hiérarchique lors d'une modification
     */
    public void notifierModification(String typeDocument, Long idDocument, Utilisateur createur, 
                                   Utilisateur superieur, String action) {
        String codeDocument = getCodeDocument(typeDocument, idDocument);
        String titre = String.format("Document %s - %s", typeDocument, action);
        String message = String.format("L'utilisateur %s %s a %s le document %s #%s. " +
                "Veuillez le valider ou le rejeter.", 
                createur.getPrenom(), createur.getNom(), action.toLowerCase(), typeDocument, codeDocument);
        
        // Créer notification système
        Notification notification = creerNotification(superieur, titre, message, typeDocument, idDocument);
        
        // Envoyer email et mettre à jour le statut de transmission
        try {
            String htmlContent = formaterMessageHtml(titre, message);
            envoyerEmailHtml(superieur.getEmail(), titre, htmlContent);
            notification.setTransmission(true);  // Email envoyé avec succès
        } catch (Exception e) {
            notification.setTransmission(false);  // Échec d'envoi
            log.error("Échec d'envoi email pour notification {} : {}", notification.getId(), e.getMessage());
        }
        notificationRepo.save(notification);
    }

    /**
     * Notifie le créateur lors d'une validation/rejet
     */
    public void notifierValidation(String typeDocument, Long idDocument, Utilisateur validateur, 
                                 Utilisateur createur, String action, String commentaire) {
        String codeDocument = getCodeDocument(typeDocument, idDocument);
        String titre = String.format("Document %s - %s", typeDocument, action);
        String message = String.format("Votre document %s #%s a été %s par %s %s.", 
                typeDocument, codeDocument, action.toLowerCase(), 
                validateur.getPrenom(), validateur.getNom());
        
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            message += String.format(" Commentaire : %s", commentaire);
        }
        
        // Créer notification système
        Notification notification = creerNotification(createur, titre, message, typeDocument, idDocument);
        
        // Envoyer email et mettre à jour le statut de transmission
        try {
            String htmlContent = formaterMessageHtml(titre, message);
            envoyerEmailHtml(createur.getEmail(), titre, htmlContent);
            notification.setTransmission(true);  // Email envoyé avec succès
        } catch (Exception e) {
            notification.setTransmission(false);  // Échec d'envoi
            log.error("Échec d'envoi email pour notification {} : {}", notification.getId(), e.getMessage());
        }
        notificationRepo.save(notification);
    }
}