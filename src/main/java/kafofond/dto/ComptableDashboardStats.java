package kafofond.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComptableDashboardStats {
    private int totalDemandesAchat;
    private int totalBonsCommande;
    private int totalOrdresPaiement;
    private int demandesEnAttente;
    private int bonsEnAttente;
    private int ordresEnAttente;
    private double pourcentageDemandesTraitees;
    private double pourcentageBonsValides;
}