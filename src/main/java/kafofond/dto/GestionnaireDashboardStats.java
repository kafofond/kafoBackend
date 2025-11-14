package kafofond.dto;

public interface GestionnaireDashboardStats {

    int getTotalLignesCredit();

    int getLignesCreditEnCours();

    int getTotalFichesBesoin();

    int getFichesBesoinEnAttente();

    int getTotalDemandesAchat();

    int getDemandesAchatEnAttente();

    double getPourcentageLignesCreditTraitees();

    double getPourcentageFichesBesoinTraitees();

    double getPourcentageDemandesAchatTraitees();
}