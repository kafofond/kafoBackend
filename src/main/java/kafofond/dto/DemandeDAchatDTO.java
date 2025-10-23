package kafofond.dto;

import kafofond.entity.DemandeDAchat;
import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour les demandes d'achat
 * Représente une demande d'achat dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeDAchatDTO {
    
    private Long id;
    private String code;
    private String description;
    private String fournisseur;
    private double montantTotal;
    private String serviceBeneficiaire;
    private LocalDate dateCreation;
    private LocalDate dateAttendu;
    private Statut statut;
    private String urlFichierJoint;
    private String createurNom;
    private String createurEmail;
    private String entrepriseNom;
    
    // Référence à la fiche de besoin
    private Long ficheBesoinId;
    private String ficheBesoinCode; // Pour affichage
    
    // Commentaires associés
    private List<CommentaireSimplifieDTO> commentaires;

    public static DemandeDAchatDTO fromEntity(DemandeDAchat demande) {
        if (demande == null) return null;

        return DemandeDAchatDTO.builder()
                .id(demande.getId())
                .code(demande.getCode())
                .description(demande.getDescription())
                .fournisseur(demande.getFournisseur())
                .montantTotal(demande.getMontantTotal())
                .serviceBeneficiaire(demande.getServiceBeneficiaire())
                .dateCreation(demande.getDateCreation())
                .dateAttendu(demande.getDateAttendu())
                .statut(demande.getStatut())
                .urlFichierJoint(demande.getUrlFichierJoint())
                .createurNom(demande.getCreePar() != null ?
                        demande.getCreePar().getPrenom() + " " + demande.getCreePar().getNom() : null)
                .createurEmail(demande.getCreePar() != null ? demande.getCreePar().getEmail() : null)
                .entrepriseNom(demande.getEntreprise() != null ? demande.getEntreprise().getNom() : null)
                .ficheBesoinId(demande.getFicheDeBesoin() != null ? demande.getFicheDeBesoin().getId() : null)
                .ficheBesoinCode(demande.getFicheDeBesoin() != null ? demande.getFicheDeBesoin().getCode() : null)
                .build();
    }
}