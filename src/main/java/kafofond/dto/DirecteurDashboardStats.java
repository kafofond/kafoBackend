package kafofond.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirecteurDashboardStats {
    private long totalBudget;
    private long totalLignesCredit;
    private long totalDepenses;
    private long budgetEnCours;
    private long lignesCreditEnAttente;
    private long depensesEnAttente;
    private int budgetPercentage;
    private int lignesCreditPercentage;
    private int depensesPercentage;
}