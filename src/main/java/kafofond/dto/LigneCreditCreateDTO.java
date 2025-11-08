package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "DTO simplifié pour la création d'une ligne de crédit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCreditCreateDTO {

    @Schema(description = "Intitulé de la ligne de crédit", example = "Ligne de crédit équipements")
    private String intituleLigne;

    @Schema(description = "Description de la ligne de crédit", example = "Ligne de crédit pour l'achat d'équipements informatiques")
    private String description;

    @Schema(description = "Montant alloué à la ligne de crédit", example = "5000000")
    private double montantAllouer;

    @Schema(description = "ID du budget associé", example = "1")
    private Long budgetId;
}