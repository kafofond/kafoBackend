package kafofond.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsableDashboardStatsDTO {
    private double creditsAffectes;
    private double creditsUtilises;
    private double creditsRestants;
    private double pourcentageBudget;
    private double pourcentageUtilisation;
    private double pourcentageRestant;
}