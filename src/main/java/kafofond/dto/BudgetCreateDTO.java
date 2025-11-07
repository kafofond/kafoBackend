package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO simplifié pour la création d'un budget")
public class BudgetCreateDTO {

    @Schema(description = "Intitulé du budget", example = "Budget 2025")
    private String intituleBudget;

    @Schema(description = "Description du budget", example = "Budget annuel pour les achats")
    private String description;

    @Schema(description = "Montant total du budget", example = "50000")
    private double montantBudget;

    @Schema(description = "Date de début du budget", example = "2025-01-01")
    private LocalDate dateDebut;

    @Schema(description = "Date de fin du budget", example = "2025-12-31")
    private LocalDate dateFin;
}