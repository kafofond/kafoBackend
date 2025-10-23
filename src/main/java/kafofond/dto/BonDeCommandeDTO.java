package kafofond.dto;

import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour les bons de commande
 * Représente un bon de commande dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonDeCommandeDTO {
    
    private Long id;
    private String code;
    private String fournisseur;
    private String description;
    // Suppression des champs quantite et prixUnitaire inutiles
    private double montantTotal;
    private String serviceBeneficiaire;
    private String modePaiement;
    private LocalDate dateCreation;
    private LocalDate delaiPaiement;
    private LocalDate dateExecution;
    private Statut statut;
    private String urlPdf;
    private String createurNom;
    private String createurEmail;
    private String entrepriseNom;
    private Long demandeAchatId;
    private List<CommentaireDTO> commentaires;
}