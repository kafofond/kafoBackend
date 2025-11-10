package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour modifier un bon de commande")
public class BonDeCommandeModificationDTO {

    @Schema(description = "Fournisseur du bon de commande", example = "Fournisseur ABC")
    private String fournisseur;

    @Schema(description = "Description du bon de commande", example = "Fournitures de bureau")
    private String description;

    @Schema(description = "Montant total du bon de commande", example = "1500.0")
    private double montantTotal;

    @Schema(description = "Service bénéficiaire", example = "Service Finance")
    private String serviceBeneficiaire;

    @Schema(description = "Mode de paiement", example = "Virement bancaire")
    private String modePaiement;

    @Schema(description = "Délai de paiement", example = "2025-12-31")
    private LocalDate delaiPaiement;
}