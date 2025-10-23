package kafofond.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * DTO pour Designation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Désignation d'un produit ou service dans une fiche de besoin")
public class DesignationDTO {

    private Long id;

    @Schema(description = "Nom du produit ou service demandé", example = "Ordinateur portable HP")
    private String produit;

    @Schema(description = "Quantité demandée", example = "5")
    private int quantite;

    @Schema(description = "Prix unitaire", example = "500000.0")
    private double prixUnitaire;

    @Schema(description = "Montant total (quantité × prix unitaire)", example = "2500000.0")
    private double montantTotal;

    @Schema(description = "Date de la désignation")
    private LocalDate date;

    @Schema(description = "ID de la fiche de besoin associée")
    private Long ficheBesoinId;
}
