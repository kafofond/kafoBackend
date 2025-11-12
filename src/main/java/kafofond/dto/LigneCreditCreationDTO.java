package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour créer une ligne de crédit")
public class LigneCreditCreationDTO {

    @Schema(description = "Intitulé de la ligne de crédit", example = "Ligne pour fournitures")
    private String intituleLigne;

    @Schema(description = "Description détaillée de la ligne de crédit", example = "Ligne de crédit pour les fournitures de bureau")
    private String description;

    @Schema(description = "Montant alloué à la ligne de crédit", example = "5000.0")
    private double montantAllouer;

    @Schema(description = "Date de début de la ligne de crédit", example = "2025-01-01")
    private LocalDate dateDebut;

    @Schema(description = "Date de fin de la ligne de crédit", example = "2025-12-31")
    private LocalDate dateFin;

    @Schema(description = "ID du budget associé", example = "1")
    private Long budgetId;
}