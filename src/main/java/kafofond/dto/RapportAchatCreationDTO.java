package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour créer un rapport d'achat")
public class RapportAchatCreationDTO {

    @Schema(description = "Nom du rapport d'achat", example = "Rapport achat fournitures")
    private String nom;

    @Schema(description = "ID de la fiche de besoin", example = "1")
    private Long ficheBesoinId;

    @Schema(description = "ID de la demande d'achat", example = "2")
    private Long demandeAchatId;

    @Schema(description = "ID du bon de commande", example = "3")
    private Long bonCommandeId;

    @Schema(description = "ID de l'attestation de service fait", example = "4")
    private Long attestationServiceFaitId;

    @Schema(description = "ID de la décision de prélèvement", example = "5")
    private Long decisionPrelevementId;

    @Schema(description = "ID de l'ordre de paiement", example = "6")
    private Long ordrePaiementId;
}