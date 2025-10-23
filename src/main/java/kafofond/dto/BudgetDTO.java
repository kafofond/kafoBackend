package kafofond.dto;

import kafofond.entity.Budget;
import kafofond.entity.Statut;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Budget avec toutes ses informations")
public class BudgetDTO {

    private Long id;
    private String code;
    private String intituleBudget;
    private String description;
    private double montantBudget;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Statut statut;
    private boolean actif;
    private String createurNom;
    private String createurEmail;
    private String entrepriseNom;

    public static BudgetDTO fromEntity(Budget budget) {
        if (budget == null) return null;

        return BudgetDTO.builder()
                .id(budget.getId())
                .code(budget.getCode())
                .intituleBudget(budget.getIntituleBudget())
                .description(budget.getDescription())
                .montantBudget(budget.getMontantBudget())
                .dateCreation(budget.getDateCreation())
                .dateModification(budget.getDateModification())
                .dateDebut(budget.getDateDebut())
                .dateFin(budget.getDateFin())
                .statut(budget.getStatut())
                .actif(budget.getEtat() != null ? budget.getEtat() : false)
                .createurNom(budget.getCreePar() != null ?
                        budget.getCreePar().getPrenom() + " " + budget.getCreePar().getNom() : null)
                .createurEmail(budget.getCreePar() != null ? budget.getCreePar().getEmail() : null)
                .entrepriseNom(budget.getEntreprise() != null ? budget.getEntreprise().getNom() : null)
                .build();
    }
}
