package kafofond.controller;

import kafofond.dto.ComptableDashboardStats;
import kafofond.dto.ComptableChartDataDTO;
import kafofond.dto.DirecteurDashboardStats;
import kafofond.dto.DsiDashboardStats;
import kafofond.dto.ResponsableDashboardStatsDTO;
import kafofond.dto.ResponsableChartDataDTO;
import kafofond.entity.Role;
import kafofond.entity.Utilisateur;
import kafofond.service.StatistiqueService;
import kafofond.service.UtilisateurService;
import kafofond.service.ResponsableStatistiquesService;
import kafofond.repository.LigneCreditRepo;
import kafofond.repository.OrdreDePaiementRepo;
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
        private final ResponsableStatistiquesService responsableStatistiquesService;
        private final LigneCreditRepo ligneCreditRepo;
        private final OrdreDePaiementRepo ordreDePaiementRepo;

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
                                        statistiqueService.getUtilisateursActifsParEntreprise(
                                                        utilisateur.getEntreprise().getId()));
                        stats.setTotalUsers(totalUsers);

                        // Disabled users = total utilisateurs - utilisateurs actifs de l'entreprise
                        int totalUtilisateurs = Math.toIntExact(
                                        statistiqueService.getTotalUtilisateursParEntreprise(
                                                        utilisateur.getEntreprise().getId()));
                        int disabledUsers = totalUtilisateurs - totalUsers;
                        stats.setDisabledUsers(disabledUsers);

                        // Shared documents = total documents de l'entreprise
                        int sharedDocuments = Math
                                        .toIntExact(statistiqueService.getTotalDocumentsParEntreprise(
                                                        utilisateur.getEntreprise().getId()));
                        stats.setSharedDocuments(sharedDocuments);

                        // Active users percentage
                        double activeUsersPercentage = totalUtilisateurs > 0
                                        ? (double) totalUsers / totalUtilisateurs * 100
                                        : 0;
                        stats.setActiveUsersPercentage(Math.round(activeUsersPercentage * 100.0) / 100.0);

                        // Disabled users percentage
                        double disabledUsersPercentage = totalUtilisateurs > 0
                                        ? (double) disabledUsers / totalUtilisateurs * 100
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

        // Nouvel endpoint pour le dashboard directeur
        @GetMapping("/directeur/dashboard")
        public ResponseEntity<DirecteurDashboardStats> getStatistiquesDashboardDirecteur(
                        Authentication authentication) {
                try {
                        // Récupérer l'utilisateur authentifié
                        Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                        // Vérifier que l'utilisateur a le rôle DIRECTEUR
                        if (utilisateur.getRole() != Role.DIRECTEUR) {
                                return ResponseEntity.status(403).build();
                        }

                        DirecteurDashboardStats stats = new DirecteurDashboardStats();

                        Long entrepriseId = utilisateur.getEntreprise().getId();

                        // Récupérer les statistiques
                        long totalBudget = statistiqueService.getTotalBudgetByEntrepriseId(entrepriseId);
                        long totalLignesCredit = statistiqueService.getTotalLignesCreditByEntrepriseId(entrepriseId);
                        long totalDepenses = statistiqueService.getTotalDepensesByEntrepriseId(entrepriseId);
                        long budgetEnCours = statistiqueService.getBudgetEnCoursByEntrepriseId(entrepriseId);
                        long lignesCreditEnAttente = statistiqueService
                                        .getLignesCreditEnAttenteByEntrepriseId(entrepriseId);
                        long depensesEnAttente = statistiqueService.getDepensesEnAttenteByEntrepriseId(entrepriseId);

                        // Configurer les statistiques de base
                        stats.setTotalBudget(totalBudget);
                        stats.setTotalLignesCredit(totalLignesCredit);
                        stats.setTotalDepenses(totalDepenses);
                        stats.setBudgetEnCours(budgetEnCours);
                        stats.setLignesCreditEnAttente(lignesCreditEnAttente);
                        stats.setDepensesEnAttente(depensesEnAttente);

                        // Calculer les pourcentages (valeurs fictives pour l'exemple)
                        stats.setBudgetPercentage(95);
                        stats.setLignesCreditPercentage(95);
                        stats.setDepensesPercentage(82);

                        return ResponseEntity.ok(stats);
                } catch (Exception e) {
                        // En cas d'erreur, retourner des statistiques par défaut
                        DirecteurDashboardStats defaultStats = new DirecteurDashboardStats();
                        defaultStats.setTotalBudget(0L);
                        defaultStats.setTotalLignesCredit(0L);
                        defaultStats.setTotalDepenses(0L);
                        defaultStats.setBudgetEnCours(0L);
                        defaultStats.setLignesCreditEnAttente(0L);
                        defaultStats.setDepensesEnAttente(0L);
                        defaultStats.setBudgetPercentage(0);
                        defaultStats.setLignesCreditPercentage(0);
                        defaultStats.setDepensesPercentage(0);
                        return ResponseEntity.ok(defaultStats);
                }
        }

        // Nouvel endpoint pour les graphiques directeur
        @GetMapping("/directeur/chart")
        public ResponseEntity<Map<String, Object>> getStatistiquesChartDirecteur(
                        @RequestParam String periode,
                        Authentication authentication) {
                try {
                        // Récupérer l'utilisateur authentifié
                        Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                        // Vérifier que l'utilisateur a le rôle DIRECTEUR
                        if (utilisateur.getRole() != Role.DIRECTEUR) {
                                Map<String, Object> error = new HashMap<>();
                                error.put("message",
                                                "Accès refusé. Seuls les directeurs peuvent accéder à ces statistiques.");
                                return ResponseEntity.status(403).body(error);
                        }

                        Map<String, Object> response = new HashMap<>();
                        Map<String, Object> datasets = new HashMap<>();

                        Long entrepriseId = utilisateur.getEntreprise().getId();

                        // Récupérer les données pour les graphiques
                        List<String> labels = statistiqueService.getLabelsParPeriode(periode);
                        List<Integer> budgets = statistiqueService.getBudgetsValidesParPeriodeEtEntreprise(periode,
                                        entrepriseId);
                        List<Integer> lignesCredit = statistiqueService
                                        .getLignesCreditValideesParPeriodeEtEntreprise(periode, entrepriseId);
                        List<Integer> depenses = statistiqueService.getDepensesValideesParPeriodeEtEntreprise(periode,
                                        entrepriseId);

                        datasets.put("budgets", budgets);
                        datasets.put("lignesCredit", lignesCredit);
                        datasets.put("depenses", depenses);

                        response.put("labels", labels);
                        response.put("datasets", datasets);

                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        // En cas d'erreur, retourner des données vides
                        Map<String, Object> response = new HashMap<>();
                        Map<String, Object> datasets = new HashMap<>();
                        datasets.put("budgets", new int[0]);
                        datasets.put("lignesCredit", new int[0]);
                        datasets.put("depenses", new int[0]);
                        response.put("labels", new String[0]);
                        response.put("datasets", datasets);
                        return ResponseEntity.ok(response);
                }
        }
        
        // Nouveaux endpoints pour le dashboard responsable
        @GetMapping("/responsable/dashboard")
        public ResponseEntity<ResponsableDashboardStatsDTO> getStatistiquesDashboardResponsable(
                        Authentication authentication) {
                try {
                        // Récupérer l'utilisateur authentifié
                        Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                        // Vérifier que l'utilisateur a le rôle RESPONSABLE
                        if (utilisateur.getRole() != Role.RESPONSABLE) {
                                return ResponseEntity.status(403).build();
                        }

                        Long entrepriseId = utilisateur.getEntreprise().getId();
                        
                        // Récupérer les statistiques dynamiques du service
                        ResponsableDashboardStatsDTO stats = responsableStatistiquesService.getDashboardStats(entrepriseId);

                        return ResponseEntity.ok(stats);
                } catch (Exception e) {
                        // En cas d'erreur, retourner des statistiques par défaut
                        ResponsableDashboardStatsDTO defaultStats = ResponsableDashboardStatsDTO.builder()
                                        .creditsAffectes(0.0)
                                        .creditsUtilises(0.0)
                                        .creditsRestants(0.0)
                                        .pourcentageBudget(0.0)
                                        .pourcentageUtilisation(0.0)
                                        .pourcentageRestant(0.0)
                                        .build();
                        return ResponseEntity.ok(defaultStats);
                }
        }

        // Nouvel endpoint pour les graphiques responsable
        @GetMapping("/responsable/chart")
        public ResponseEntity<ResponsableChartDataDTO> getStatistiquesChartResponsable(
                        @RequestParam(defaultValue = "semaine") String periode,
                        Authentication authentication) {
                try {
                        // Récupérer l'utilisateur authentifié
                        Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                        // Vérifier que l'utilisateur a le rôle RESPONSABLE
                        if (utilisateur.getRole() != Role.RESPONSABLE) {
                                return ResponseEntity.status(403).build();
                        }

                        Long entrepriseId = utilisateur.getEntreprise().getId();
                        
                        // Récupérer les données pour les graphiques
                        ResponsableChartDataDTO chartData = responsableStatistiquesService.getChartData(periode, entrepriseId);

                        return ResponseEntity.ok(chartData);
                } catch (Exception e) {
                        // En cas d'erreur, retourner des données vides
                        ResponsableChartDataDTO emptyData = ResponsableChartDataDTO.builder()
                                        .labels(List.of())
                                        .datasets(Map.of())
                                        .build();
                        return ResponseEntity.ok(emptyData);
                }
        }

        // Nouvel endpoint pour le dashboard comptable
        @GetMapping("/comptable/dashboard")
        public ResponseEntity<ComptableDashboardStats> getStatistiquesDashboardComptable(
                        Authentication authentication) {
                try {
                        // Récupérer l'utilisateur authentifié
                        Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                        // Vérifier que l'utilisateur a le rôle COMPTABLE
                        if (utilisateur.getRole() != Role.COMPTABLE) {
                                return ResponseEntity.status(403).build();
                        }

                        Long entrepriseId = utilisateur.getEntreprise().getId();

                        ComptableDashboardStats stats = new ComptableDashboardStats();

                        // Récupérer les statistiques
                        int totalDemandesAchat = Math.toIntExact(statistiqueService.getTotalDemandesAchatByEntrepriseId(entrepriseId));
                        int totalBonsCommande = Math.toIntExact(statistiqueService.getTotalBonsCommandeByEntrepriseId(entrepriseId));
                        int totalOrdresPaiement = Math.toIntExact(statistiqueService.getTotalOrdresPaiementByEntrepriseId(entrepriseId));
                        
                        int demandesEnAttente = Math.toIntExact(statistiqueService.getDemandesAchatEnAttenteByEntrepriseId(entrepriseId));
                        int bonsEnAttente = Math.toIntExact(statistiqueService.getBonsCommandeEnAttenteByEntrepriseId(entrepriseId));
                        int ordresEnAttente = Math.toIntExact(statistiqueService.getOrdresPaiementEnAttenteByEntrepriseId(entrepriseId));
                        
                        // Calculer les pourcentages
                        double pourcentageDemandesTraitees = totalDemandesAchat > 0 
                                ? ((double) (totalDemandesAchat - demandesEnAttente) / totalDemandesAchat) * 100 
                                : 0.0;
                                
                        double pourcentageBonsValides = totalBonsCommande > 0 
                                ? ((double) (totalBonsCommande - bonsEnAttente) / totalBonsCommande) * 100 
                                : 0.0;

                        // Configurer les statistiques
                        stats.setTotalDemandesAchat(totalDemandesAchat);
                        stats.setTotalBonsCommande(totalBonsCommande);
                        stats.setTotalOrdresPaiement(totalOrdresPaiement);
                        stats.setDemandesEnAttente(demandesEnAttente);
                        stats.setBonsEnAttente(bonsEnAttente);
                        stats.setOrdresEnAttente(ordresEnAttente);
                        stats.setPourcentageDemandesTraitees(Math.round(pourcentageDemandesTraitees * 100.0) / 100.0);
                        stats.setPourcentageBonsValides(Math.round(pourcentageBonsValides * 100.0) / 100.0);

                        return ResponseEntity.ok(stats);
                } catch (Exception e) {
                        // En cas d'erreur, retourner des statistiques par défaut
                        ComptableDashboardStats defaultStats = new ComptableDashboardStats();
                        defaultStats.setTotalDemandesAchat(0);
                        defaultStats.setTotalBonsCommande(0);
                        defaultStats.setTotalOrdresPaiement(0);
                        defaultStats.setDemandesEnAttente(0);
                        defaultStats.setBonsEnAttente(0);
                        defaultStats.setOrdresEnAttente(0);
                        defaultStats.setPourcentageDemandesTraitees(0.0);
                        defaultStats.setPourcentageBonsValides(0.0);
                        return ResponseEntity.ok(defaultStats);
                }
        }

        // Nouvel endpoint pour les graphiques comptable
        @GetMapping("/comptable/chart")
        public ResponseEntity<ComptableChartDataDTO> getStatistiquesChartComptable(
                        @RequestParam(defaultValue = "semaine") String periode,
                        Authentication authentication) {
                try {
                        // Récupérer l'utilisateur authentifié
                        Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                        // Vérifier que l'utilisateur a le rôle COMPTABLE
                        if (utilisateur.getRole() != Role.COMPTABLE) {
                                return ResponseEntity.status(403).build();
                        }

                        Long entrepriseId = utilisateur.getEntreprise().getId();
                        
                        // Récupérer les données pour les graphiques
                        List<String> labels = statistiqueService.getLabelsParPeriode(periode);
                        List<Integer> demandesAchat = statistiqueService.getDemandesAchatParPeriodeEtEntreprise(periode, entrepriseId);
                        List<Integer> bonsCommande = statistiqueService.getBonsCommandeParPeriodeEtEntreprise(periode, entrepriseId);
                        List<Integer> ordresPaiement = statistiqueService.getOrdresPaiementParPeriodeEtEntreprise(periode, entrepriseId);
                        
                        Map<String, List<Integer>> datasets = new HashMap<>();
                        datasets.put("demandesAchat", demandesAchat);
                        datasets.put("bonsCommande", bonsCommande);
                        datasets.put("ordresPaiement", ordresPaiement);

                        ComptableChartDataDTO chartData = ComptableChartDataDTO.builder()
                                        .labels(labels)
                                        .datasets(datasets)
                                        .build();

                        return ResponseEntity.ok(chartData);
                } catch (Exception e) {
                        // En cas d'erreur, retourner des données vides
                        ComptableChartDataDTO emptyData = ComptableChartDataDTO.builder()
                                        .labels(List.of())
                                        .datasets(Map.of())
                                        .build();
                        return ResponseEntity.ok(emptyData);
                }
        }
}