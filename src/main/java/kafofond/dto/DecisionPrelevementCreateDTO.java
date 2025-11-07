package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "DTO simplifié pour la création d'une décision de prélèvement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionPrelevementCreateDTO {

    @Schema(description = "ID de l'attestation de service fait", example = "1")
    private Long attestationId;

    @Schema(description = "Montant de la décision de prélèvement", example = "5000000")
    private double montant;

    @Schema(description = "Compte bancaire d'origine", example = "CI001-254789632541")
    private String compteOrigine;

    @Schema(description = "Compte bancaire destinataire", example = "BF002-123456789012")
    private String compteDestinataire;

    @Schema(description = "Motif du prélèvement", example = "Paiement facture")
    private String motifPrelevement;
}