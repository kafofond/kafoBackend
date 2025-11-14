package kafofond.dto;

import java.util.List;

public interface GestionnaireChartDatasetsDTO {

    List<Integer> getLignesCredit();

    List<Integer> getFichesBesoin();

    List<Integer> getDemandesAchat();
}