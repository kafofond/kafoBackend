package kafofond.controller;

import kafofond.dto.DsiDashboardStats;
import kafofond.entity.Utilisateur;
import kafofond.service.StatistiqueService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;
    private final UtilisateurService utilisateurService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getStatistiquesDashboard() {
        Map<String, Object> statistiques = new HashMap<>();

        // Statistiques des entreprises
        statistiques.put("totalEntreprises", statistiqueService.getTotalEntreprises());
        statistiques.put("entreprisesActives", statistiqueService.getEntreprisesActives());
        statistiques.put("pourcentageEntreprisesActives",
                Math.round(statistiqueService.getPourcentageEntreprisesActives() * 100.0) / 100.0);

        // Statistiques des utilisateurs
        statistiques.put("totalUtilisateurs", statistiqueService.getTotalUtilisateurs());
        statistiques.put("utilisateursActifs", statistiqueService.getUtilisateursActifs());
        statistiques.put("pourcentageUtilisateursActifs",
                Math.round(statistiqueService.getPourcentageUtilisateursActifs() * 100.0) / 100.0);

        // Statistiques des documents
        statistiques.put("totalDocuments", statistiqueService.getTotalDocuments());

        return ResponseEntity.ok(statistiques);
    }

    // Nouvel endpoint pour le dashboard DSI
    @GetMapping("/dsi/dashboard")
    public ResponseEntity<DsiDashboardStats> getStatistiquesDashboardDSI(Authentication authentication) {
        try {
            // Récupérer l'utilisateur authentifié
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            DsiDashboardStats stats = new DsiDashboardStats();

            // Total users = utilisateurs actifs de l'entreprise
            int totalUsers = Math.toIntExact(
                    statistiqueService.getUtilisateursActifsParEntreprise(utilisateur.getEntreprise().getId()));
            stats.setTotalUsers(totalUsers);

            // Disabled users = total utilisateurs - utilisateurs actifs de l'entreprise
            int totalUtilisateurs = Math.toIntExact(
                    statistiqueService.getTotalUtilisateursParEntreprise(utilisateur.getEntreprise().getId()));
            int disabledUsers = totalUtilisateurs - totalUsers;
            stats.setDisabledUsers(disabledUsers);

            // Shared documents = total documents de l'entreprise
            int sharedDocuments = Math
                    .toIntExact(statistiqueService.getTotalDocumentsParEntreprise(utilisateur.getEntreprise().getId()));
            stats.setSharedDocuments(sharedDocuments);

            // Active users percentage
            double activeUsersPercentage = totalUtilisateurs > 0 ? (double) totalUsers / totalUtilisateurs * 100 : 0;
            stats.setActiveUsersPercentage(Math.round(activeUsersPercentage * 100.0) / 100.0);

            // Disabled users percentage
            double disabledUsersPercentage = totalUtilisateurs > 0 ? (double) disabledUsers / totalUtilisateurs * 100
                    : 0;
            stats.setDisabledUsersPercentage(Math.round(disabledUsersPercentage * 100.0) / 100.0);

            // Documents percentage (par rapport à un seuil, par exemple 1000)
            double documentsPercentage = Math.min(100.0, (double) sharedDocuments / 1000 * 100);
            stats.setDocumentsPercentage(Math.round(documentsPercentage * 100.0) / 100.0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // En cas d'erreur, retourner des statistiques par défaut
            DsiDashboardStats defaultStats = new DsiDashboardStats();
            defaultStats.setTotalUsers(0);
            defaultStats.setDisabledUsers(0);
            defaultStats.setSharedDocuments(0);
            defaultStats.setActiveUsersPercentage(0.0);
            defaultStats.setDisabledUsersPercentage(0.0);
            defaultStats.setDocumentsPercentage(0.0);
            return ResponseEntity.ok(defaultStats);
        }
    }

    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> getStatistiquesDocuments() {
        Map<String, Object> statistiques = new HashMap<>();

        // Comptage détaillé des documents
        statistiques.put("totalFichesBesoin", statistiqueService.getTotalFichesBesoin());
        statistiques.put("totalDemandesAchat", statistiqueService.getTotalDemandesAchat());
        statistiques.put("totalBonsCommande", statistiqueService.getTotalBonsCommande());
        statistiques.put("totalAttestationsServiceFait", statistiqueService.getTotalAttestationsServiceFait());
        statistiques.put("totalDecisionsPrelevement", statistiqueService.getTotalDecisionsPrelevement());
        statistiques.put("totalOrdresPaiement", statistiqueService.getTotalOrdresPaiement());
        statistiques.put("totalBudgets", statistiqueService.getTotalBudgets());
        statistiques.put("totalLignesCredit", statistiqueService.getTotalLignesCredit());
        statistiques.put("totalDocuments", statistiqueService.getTotalDocuments());

        return ResponseEntity.ok(statistiques);
    }

    @GetMapping("/chart")
    public ResponseEntity<Map<String, Object>> getStatistiquesChart(@RequestParam String periode) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> datasets = new HashMap<>();

        // Récupérer les vraies données
        List<String> labels = statistiqueService.getLabelsParPeriode(periode);
        List<Integer> utilisateurs = statistiqueService.getUtilisateursParPeriode(periode);
        List<Integer> entreprises = statistiqueService.getEntreprisesParPeriode(periode);
        List<Integer> documents = statistiqueService.getDocumentsParPeriode(periode);

        datasets.put("utilisateurs", utilisateurs);
        datasets.put("entreprises", entreprises);
        datasets.put("documents", documents);

        response.put("labels", labels);
        response.put("datasets", datasets);

        return ResponseEntity.ok(response);
    }

    // Nouvel endpoint pour les graphiques DSI
    @GetMapping("/dsi/chart")
    public ResponseEntity<Map<String, Object>> getStatistiquesChartDSI(@RequestParam String periode,
            Authentication authentication) {
        try {
            // Récupérer l'utilisateur authentifié
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> datasets = new HashMap<>();

            // Récupérer les vraies données pour l'entreprise
            List<String> labels = statistiqueService.getLabelsParPeriode(periode);
            List<Integer> utilisateurs = statistiqueService.getUtilisateursParPeriodeEtEntreprise(periode,
                    utilisateur.getEntreprise().getId());
            List<Integer> documents = statistiqueService.getDocumentsParPeriodeEtEntreprise(periode,
                    utilisateur.getEntreprise().getId());

            datasets.put("utilisateurs", utilisateurs);
            datasets.put("documents", documents);

            response.put("labels", labels);
            response.put("datasets", datasets);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // En cas d'erreur, retourner des données vides
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> datasets = new HashMap<>();
            datasets.put("utilisateurs", new int[0]);
            datasets.put("documents", new int[0]);
            response.put("labels", new String[0]);
            response.put("datasets", datasets);
            return ResponseEntity.ok(response);
        }
    }
}