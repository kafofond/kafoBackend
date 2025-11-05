package kafofond.controller;

import kafofond.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}