package kafofond.dto;

import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour les décisions de prélèvement
 * Représente une décision de prélèvement dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Décision de prélèvement avec toutes ses informations")
public class DecisionPrelevementDTO {
    
    @Schema(description = "Identifiant unique de la décision", example = "1")
    private Long id;
    
    @Schema(description = "Code unique de la décision", example = "DP-0001-01-2025")
    private String code;
    
    @Schema(description = "Référence de l'attestation de service fait", example = "ASF-2024-001")
    private String referenceAttestation;
    
    @Schema(description = "Montant en FCFA", example = "2500000.0")
    private double montant;
    
    @Schema(description = "Compte d'origine", example = "Compte Principal")
    private String compteOrigine;
    
    @Schema(description = "Compte destinataire", example = "Tech Solutions")
    private String compteDestinataire;
    
    @Schema(description = "Motif du prélèvement", example = "Paiement facture")
    private String motifPrelevement;
    
    @Schema(description = "Date de création", example = "2024-01-15")
    private LocalDate dateCreation;
    
    @Schema(description = "Date de dernière modification", example = "2024-01-15T10:30:00")
    private LocalDateTime dateModification;
    
    @Schema(description = "Statut de la décision", example = "EN_COURS")
    private Statut statut;
    
    @Schema(description = "Nom complet du créateur", example = "Fatou Coulibaly")
    private String createurNom;
    
    @Schema(description = "Email du créateur", example = "comptable@tresor.ml")
    private String createurEmail;
    
    @Schema(description = "Nom de l'entreprise", example = "Trésor")
    private String entrepriseNom;
    
    @Schema(description = "ID de l'attestation de service fait associée", example = "1")
    private Long attestationId;
}