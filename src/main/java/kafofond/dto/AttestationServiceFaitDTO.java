package kafofond.dto;

import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * DTO pour les attestations de service fait
 * Représente une attestation de service fait dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Attestation de service fait avec toutes ses informations")
public class AttestationServiceFaitDTO {
    
    @Schema(description = "Identifiant unique de l'attestation", example = "1")
    private Long id;
    
    @Schema(description = "Code unique de l'attestation", example = "ASF-0001-01-2025")
    private String code;
    
    @Schema(description = "Référence du bon de commande", example = "BC-2024-001")
    private String referenceBonCommande;
    
    @Schema(description = "Nom du fournisseur", example = "Tech Solutions")
    private String fournisseur;
    
    @Schema(description = "Titre de l'attestation", example = "Attestation de livraison")
    private String titre;
    
    @Schema(description = "Constat de l'attestation", example = "Livraison conforme aux spécifications")
    private String constat;
    
    @Schema(description = "Date de livraison", example = "2024-01-15")
    private LocalDate dateLivraison;
    
    @Schema(description = "Date de création", example = "2024-01-15")
    private LocalDate dateCreation;
    
    @Schema(description = "URL du fichier joint", example = "/uploads/asf/ASF-2024-001.pdf")
    private String urlFichierJoint;
    
    @Schema(description = "Nom complet du créateur", example = "Aïssata Konaté")
    private String createurNom;
    
    @Schema(description = "Email du créateur", example = "tresorerie@tresor.ml")
    private String createurEmail;
    
    @Schema(description = "Nom de l'entreprise", example = "Trésor")
    private String entrepriseNom;
    
    @Schema(description = "ID du bon de commande associé", example = "1")
    private Long bonDeCommandeId;
}