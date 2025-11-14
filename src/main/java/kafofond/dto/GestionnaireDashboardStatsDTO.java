package kafofond.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GestionnaireDashboardStatsDTO implements GestionnaireDashboardStats {
    
    private int totalLignesCredit;
    private int lignesCreditEnCours;
    private int totalFichesBesoin;
    private int fichesBesoinEnAttente;
    private int totalDemandesAchat;
    private int demandesAchatEnAttente;
    private double pourcentageLignesCreditTraitees;
    private double pourcentageFichesBesoinTraitees;
    private double pourcentageDemandesAchatTraitees;
}