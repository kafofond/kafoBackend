package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour créer un rapport d'achat")
public class RapportAchatCreationDTO {

    @Schema(description = "Nom du rapport d'achat", example = "Rapport Achat Informatique")
    private String nom;

    @Schema(description = "Code de la fiche de besoin", example = "FB-0001-11-2025")
    private String ficheBesoin;

    @Schema(description = "Code de la demande d'achat", example = "DA-0001-11-2025")
    private String demandeAchat;

    @Schema(description = "Code du bon de commande", example = "BC-0001-11-2025")
    private String bonCommande;

    @Schema(description = "Code de l'attestation de service fait", example = "ASF-0001-11-2025")
    private String attestationServiceFait;

    @Schema(description = "Code de la décision de prélèvement", example = "DP-0001-11-2025")
    private String decisionPrelevement;

    @Schema(description = "Code de l'ordre de paiement", example = "OP-0001-11-2025")
    private String ordrePaiement;
}