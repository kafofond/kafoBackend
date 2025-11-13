package kafofond.service;

import kafofond.dto.ResponsableDashboardStatsDTO;
import kafofond.dto.ResponsableChartDataDTO;
import kafofond.repository.LigneCreditRepo;
import kafofond.repository.OrdreDePaiementRepo;
import kafofond.repository.BudgetRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResponsableStatistiquesService {

    private final LigneCreditRepo ligneCreditRepo;
    private final OrdreDePaiementRepo ordreDePaiementRepo;
    private final BudgetRepo budgetRepo;

    // 📊 Statistiques principales (cartes)
    public ResponsableDashboardStatsDTO getDashboardStats(Long entrepriseId) {
        // Récupérer les crédits affectés (montant total des lignes de crédits valides et actives 
        // de tous les budgets valides et actifs de l'entreprise)
        double creditsAffectes = ligneCreditRepo.sumMontantAllouerByEntrepriseIdAndStatut(entrepriseId);
        
        // Récupérer les crédits utilisés (montant total des dépenses validées/approuvées 
        // des lignes de crédits actives et valides)
        double creditsUtilises = ordreDePaiementRepo.sumMontantTotalByEntrepriseId(entrepriseId);
        
        // Calculer les crédits restants
        double creditsRestants = creditsAffectes - creditsUtilises;
        
        // Calculer les pourcentages
        double pourcentageBudget = 100.0;
        double pourcentageUtilisation = creditsAffectes > 0 ? (creditsUtilises / creditsAffectes) * 100 : 0;
        double pourcentageRestant = 100 - pourcentageUtilisation;

        return ResponsableDashboardStatsDTO.builder()
                .creditsAffectes(creditsAffectes)
                .creditsUtilises(creditsUtilises)
                .creditsRestants(creditsRestants)
                .pourcentageBudget(pourcentageBudget)
                .pourcentageUtilisation(pourcentageUtilisation)
                .pourcentageRestant(pourcentageRestant)
                .build();
    }

    // 📈 Données pour le graphique
    public ResponsableChartDataDTO getChartData(String periode, Long entrepriseId) {
        List<String> labels = generateLabels(periode);
        
        // Récupérer les données réelles selon la période
        List<Double> creditsAffectes = new ArrayList<>();
        List<Double> creditsUtilises = new ArrayList<>();
        List<Double> creditsRestants = new ArrayList<>();
        
        LocalDate now = LocalDate.now();
        
        switch (periode) {
            case "jour":
                // Données par heure (simulées pour l'exemple)
                for (int i = 0; i < labels.size(); i++) {
                    creditsAffectes.add(getCreditsAffectesParJour(now, i, entrepriseId));
                    creditsUtilises.add(getCreditsUtilisesParJour(now, i, entrepriseId));
                    creditsRestants.add(creditsAffectes.get(i) - creditsUtilises.get(i));
                }
                break;
            case "semaine":
                // Données par jour de la semaine
                for (int i = 0; i < labels.size(); i++) {
                    creditsAffectes.add(getCreditsAffectesParSemaine(now, i, entrepriseId));
                    creditsUtilises.add(getCreditsUtilisesParSemaine(now, i, entrepriseId));
                    creditsRestants.add(creditsAffectes.get(i) - creditsUtilises.get(i));
                }
                break;
            case "mois":
                // Données par semaine du mois
                for (int i = 0; i < labels.size(); i++) {
                    creditsAffectes.add(getCreditsAffectesParMois(now, i, entrepriseId));
                    creditsUtilises.add(getCreditsUtilisesParMois(now, i, entrepriseId));
                    creditsRestants.add(creditsAffectes.get(i) - creditsUtilises.get(i));
                }
                break;
            default:
                // Par défaut, données pour la semaine
                for (int i = 0; i < labels.size(); i++) {
                    creditsAffectes.add(getCreditsAffectesParSemaine(now, i, entrepriseId));
                    creditsUtilises.add(getCreditsUtilisesParSemaine(now, i, entrepriseId));
                    creditsRestants.add(creditsAffectes.get(i) - creditsUtilises.get(i));
                }
                break;
        }

        Map<String, List<Double>> datasets = new HashMap<>();
        datasets.put("creditsAffectes", creditsAffectes);
        datasets.put("creditsUtilises", creditsUtilises);
        datasets.put("creditsRestants", creditsRestants);

        return ResponsableChartDataDTO.builder()
                .labels(labels)
                .datasets(datasets)
                .build();
    }

    private List<String> generateLabels(String periode) {
        switch (periode) {
            case "jour":
                return List.of("06h", "08h", "10h", "12h", "14h", "16h", "18h", "20h");
            case "mois":
                return List.of("Sem 1", "Sem 2", "Sem 3", "Sem 4");
            default:
                return List.of("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim");
        }
    }
    
    // Méthodes pour récupérer les données réelles (à implémenter selon les besoins)
    private double getCreditsAffectesParJour(LocalDate date, int heure, Long entrepriseId) {
        // Implémentation pour récupérer les crédits affectés par heure
        // Cette méthode devrait faire une requête dans la base de données
        // Utilisation de la méthode existante
        return ligneCreditRepo.sumMontantAllouerByDateAndHeure(date, heure);
    }
    
    private double getCreditsUtilisesParJour(LocalDate date, int heure, Long entrepriseId) {
        // Implémentation pour récupérer les crédits utilisés par heure
        return ordreDePaiementRepo.sumMontantTotalByEntrepriseIdAndDateAndHeure(entrepriseId, date, heure);
    }
    
    private double getCreditsAffectesParSemaine(LocalDate date, int jour, Long entrepriseId) {
        // Implémentation pour récupérer les crédits affectés par jour de la semaine
        LocalDate jourSpecifique = date.minusDays(6 - jour); // Obtenir le jour spécifique de la semaine
        return ligneCreditRepo.sumMontantAllouerByDate(jourSpecifique);
    }
    
    private double getCreditsUtilisesParSemaine(LocalDate date, int jour, Long entrepriseId) {
        // Implémentation pour récupérer les crédits utilisés par jour de la semaine
        LocalDate jourSpecifique = date.minusDays(6 - jour); // Obtenir le jour spécifique de la semaine
        return ordreDePaiementRepo.sumMontantTotalByEntrepriseIdAndDate(entrepriseId, jourSpecifique);
    }
    
    private double getCreditsAffectesParMois(LocalDate date, int semaine, Long entrepriseId) {
        // Implémentation pour récupérer les crédits affectés par semaine du mois
        LocalDate debutSemaine = date.with(TemporalAdjusters.firstDayOfMonth())
                .plusWeeks(semaine);
        LocalDate finSemaine = debutSemaine.plusDays(6);
        // Pour le moment, nous retournons une valeur par défaut car il n'y a pas de méthode entre deux dates
        return 0.0;
    }
    
    private double getCreditsUtilisesParMois(LocalDate date, int semaine, Long entrepriseId) {
        // Implémentation pour récupérer les crédits utilisés par semaine du mois
        LocalDate debutSemaine = date.with(TemporalAdjusters.firstDayOfMonth())
                .plusWeeks(semaine);
        LocalDate finSemaine = debutSemaine.plusDays(6);
        // Pour le moment, nous retournons une valeur par défaut car il n'y a pas de méthode entre deux dates
        return 0.0;
    }
}