package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour créer un ordre de paiement")
public class OrdreDePaiementCreationDTO {

    @Schema(description = "Montant de l'ordre de paiement", example = "1500.0")
    private double montant;

    @Schema(description = "Description de l'ordre de paiement", example = "Paiement fournitures")
    private String description;

    @Schema(description = "Compte d'origine", example = "COMPTE-ORIGINE-001")
    private String compteOrigine;

    @Schema(description = "Compte destinataire", example = "COMPTE-DEST-001")
    private String compteDestinataire;

    @Schema(description = "ID de la décision de prélèvement", example = "1")
    private Long decisionId;
}