package kafofond.dto;

import kafofond.entity.HistoriqueAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour l'historique des actions
 * Représente une action dans l'historique
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueDTO {
    
    private Long id;
    private String typeDocument;
    private Long idDocument;
    private String documentCode; // Nouveau champ pour le code du document
    private String action;
    private String ancienStatut;
    private String nouveauStatut;
    private String ancienEtat;
    private String nouveauEtat;
    private LocalDateTime dateAction;
    private Long utilisateurId;
    private String utilisateurNomComplet; // Nom de l'utilisateur qui a effectué l'action
    private String utilisateurEmail;
    private String utilisateurConcerneNom; // Nom de l'utilisateur concerné par l'action (pour les actions de type UTILISATEUR)
    private Long entrepriseId;
    private String entrepriseNom;
    
    /**
     * Convertit une entité HistoriqueAction en DTO
     * Cette méthode doit être appelée dans un contexte transactionnel
     */
    public static HistoriqueDTO fromEntity(HistoriqueAction historique) {
        HistoriqueDTO dto = HistoriqueDTO.builder()
                .id(historique.getId())
                .typeDocument(historique.getTypeDocument())
                .idDocument(historique.getIdDocument())
                .action(historique.getAction())
                .ancienStatut(historique.getAncienStatut())
                .nouveauStatut(historique.getNouveauStatut())
                .ancienEtat(historique.getAncienEtat())
                .nouveauEtat(historique.getNouveauEtat())
                .dateAction(historique.getDateAction())
                .build();
        
        // Gérer l'utilisateur de manière sécurisée
        if (historique.getUtilisateur() != null) {
            dto.setUtilisateurId(historique.getUtilisateur().getId());
            dto.setUtilisateurEmail(historique.getUtilisateur().getEmail());
            dto.setUtilisateurNomComplet(
                historique.getUtilisateur().getPrenom() + " " + historique.getUtilisateur().getNom()
            );
            // Pour les actions sur les utilisateurs, on gardera le nom de l'utilisateur concerné dans utilisateurConcerneNom
        }
        
        // Gérer l'entreprise de manière sécurisée
        if (historique.getEntreprise() != null) {
            dto.setEntrepriseId(historique.getEntreprise().getId());
            // On ne tente pas d'accéder au nom ici pour éviter les problèmes de proxy
        }
        
        return dto;
    }
}