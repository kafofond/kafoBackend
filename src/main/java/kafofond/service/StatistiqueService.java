package kafofond.service;

import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import kafofond.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final EntrepriseRepo entrepriseRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final FicheBesoinRepo ficheBesoinRepo;
    private final DemandeDAchatRepo demandeDAchatRepo;
    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final AttestationDeServiceFaitRepo attestationServiceFaitRepo;
    private final DecisionDePrelevementRepo decisionPrelevementRepo;
    private final OrdreDePaiementRepo ordreDePaiementRepo;
    private final BudgetRepo budgetRepo;
    private final LigneCreditRepo ligneCreditRepo;

    // Statistiques des entreprises
    public long getTotalEntreprises() {
        return entrepriseRepo.count();
    }

    public long getEntreprisesActives() {
        return entrepriseRepo.countByEtatTrue();
    }

    public long getEntreprisesInactives() {
        return entrepriseRepo.countByEtatFalse();
    }

    public double getPourcentageEntreprisesActives() {
        long total = getTotalEntreprises();
        if (total == 0)
            return 0.0;
        return (getEntreprisesActives() * 100.0) / total;
    }

    // Statistiques des utilisateurs
    public long getTotalUtilisateurs() {
        return utilisateurRepo.count();
    }

    public long getUtilisateursActifs() {
        return utilisateurRepo.countByEtatTrue();
    }

    public long getUtilisateursInactifs() {
        return utilisateurRepo.countByEtatFalse();
    }

    public double getPourcentageUtilisateursActifs() {
        long total = getTotalUtilisateurs();
        if (total == 0)
            return 0.0;
        return (getUtilisateursActifs() * 100.0) / total;
    }

    // Statistiques des utilisateurs par entreprise
    public long getTotalUtilisateursParEntreprise(Long entrepriseId) {
        return utilisateurRepo.countByEntrepriseId(entrepriseId);
    }

    public long getUtilisateursActifsParEntreprise(Long entrepriseId) {
        return utilisateurRepo.countByEntrepriseIdAndEtatTrue(entrepriseId);
    }

    // Statistiques des documents
    public long getTotalFichesBesoin() {
        return ficheBesoinRepo.count();
    }

    public long getTotalDemandesAchat() {
        return demandeDAchatRepo.count();
    }

    public long getTotalBonsCommande() {
        return bonDeCommandeRepo.count();
    }

    public long getTotalAttestationsServiceFait() {
        return attestationServiceFaitRepo.count();
    }

    public long getTotalDecisionsPrelevement() {
        return decisionPrelevementRepo.count();
    }

    public long getTotalOrdresPaiement() {
        return ordreDePaiementRepo.count();
    }

    public long getTotalBudgets() {
        return budgetRepo.count();
    }

    public long getTotalLignesCredit() {
        return ligneCreditRepo.count();
    }

    public long getTotalDocuments() {
        return getTotalFichesBesoin() + getTotalDemandesAchat() + getTotalBonsCommande() +
                getTotalAttestationsServiceFait() + getTotalDecisionsPrelevement() + getTotalOrdresPaiement() +
                getTotalBudgets() + getTotalLignesCredit();
    }

    // Statistiques des documents par entreprise
    public long getTotalDocumentsParEntreprise(Long entrepriseId) {
        return ficheBesoinRepo.countByEntrepriseId(entrepriseId) +
                demandeDAchatRepo.countByEntrepriseId(entrepriseId) +
                bonDeCommandeRepo.countByEntrepriseId(entrepriseId) +
                attestationServiceFaitRepo.countByEntrepriseId(entrepriseId) +
                decisionPrelevementRepo.countByEntrepriseId(entrepriseId) +
                ordreDePaiementRepo.countByEntrepriseId(entrepriseId) +
                budgetRepo.countByEntrepriseId(entrepriseId) +
                ligneCreditRepo.countByEntrepriseId(entrepriseId);
    }

    // Statistiques par période
    public List<Integer> getUtilisateursParPeriode(String periode) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        // Correction: éviter de créer une heure de 24h
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);
                        long count = utilisateurRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = utilisateurRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);
                        long count = utilisateurRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = utilisateurRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            System.err.println("Erreur dans getUtilisateursParPeriode: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    // Statistiques par période et par entreprise
    public List<Integer> getUtilisateursParPeriodeEtEntreprise(String periode, Long entrepriseId) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        // Correction: éviter de créer une heure de 24h
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);
                        long count = utilisateurRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = utilisateurRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);
                        long count = utilisateurRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = utilisateurRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            System.err.println("Erreur dans getUtilisateursParPeriodeEtEntreprise: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    public List<Integer> getEntreprisesParPeriode(String periode) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        // Correction: éviter de créer une heure de 24h
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);
                        long count = entrepriseRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = entrepriseRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);
                        long count = entrepriseRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = entrepriseRepo.countByDateCreationBetween(start, end);
                        resultats.add((int) count);
                    }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            System.err.println("Erreur dans getEntreprisesParPeriode: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    public List<Integer> getDocumentsParPeriode(String periode) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        // Correction: éviter de créer une heure de 24h
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);

                        // Compter tous les types de documents
                        long countFiches = ficheBesoinRepo.countByDateCreationBetween(start, end);
                        long countDemandes = demandeDAchatRepo.countByDateCreationBetween(start, end);
                        long countBons = bonDeCommandeRepo.countByDateCreationBetween(start, end);
                        long countAttestations = attestationServiceFaitRepo.countByDateCreationBetween(start, end);
                        long countDecisions = decisionPrelevementRepo.countByDateCreationBetween(start, end);
                        long countOrdres = ordreDePaiementRepo.countByDateCreationBetween(start, end);
                        long countBudgets = budgetRepo.countByDateCreationBetween(start, end);
                        long countLignes = ligneCreditRepo.countByDateCreationBetween(start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);

                        // Compter tous les types de documents
                        long countFiches = ficheBesoinRepo.countByDateCreationBetween(start, end);
                        long countDemandes = demandeDAchatRepo.countByDateCreationBetween(start, end);
                        long countBons = bonDeCommandeRepo.countByDateCreationBetween(start, end);
                        long countAttestations = attestationServiceFaitRepo.countByDateCreationBetween(start, end);
                        long countDecisions = decisionPrelevementRepo.countByDateCreationBetween(start, end);
                        long countOrdres = ordreDePaiementRepo.countByDateCreationBetween(start, end);
                        long countBudgets = budgetRepo.countByDateCreationBetween(start, end);
                        long countLignes = ligneCreditRepo.countByDateCreationBetween(start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);

                        // Compter tous les types de documents
                        long countFiches = ficheBesoinRepo.countByDateCreationBetween(start, end);
                        long countDemandes = demandeDAchatRepo.countByDateCreationBetween(start, end);
                        long countBons = bonDeCommandeRepo.countByDateCreationBetween(start, end);
                        long countAttestations = attestationServiceFaitRepo.countByDateCreationBetween(start, end);
                        long countDecisions = decisionPrelevementRepo.countByDateCreationBetween(start, end);
                        long countOrdres = ordreDePaiementRepo.countByDateCreationBetween(start, end);
                        long countBudgets = budgetRepo.countByDateCreationBetween(start, end);
                        long countLignes = ligneCreditRepo.countByDateCreationBetween(start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);

                        // Compter tous les types de documents
                        long countFiches = ficheBesoinRepo.countByDateCreationBetween(start, end);
                        long countDemandes = demandeDAchatRepo.countByDateCreationBetween(start, end);
                        long countBons = bonDeCommandeRepo.countByDateCreationBetween(start, end);
                        long countAttestations = attestationServiceFaitRepo.countByDateCreationBetween(start, end);
                        long countDecisions = decisionPrelevementRepo.countByDateCreationBetween(start, end);
                        long countOrdres = ordreDePaiementRepo.countByDateCreationBetween(start, end);
                        long countBudgets = budgetRepo.countByDateCreationBetween(start, end);
                        long countLignes = ligneCreditRepo.countByDateCreationBetween(start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            System.err.println("Erreur dans getDocumentsParPeriode: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    // Statistiques des documents par période et par entreprise
    public List<Integer> getDocumentsParPeriodeEtEntreprise(String periode, Long entrepriseId) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        // Correction: éviter de créer une heure de 24h
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);

                        // Compter tous les types de documents pour l'entreprise
                        long countFiches = ficheBesoinRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countDemandes = demandeDAchatRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBons = bonDeCommandeRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countAttestations = attestationServiceFaitRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countDecisions = decisionPrelevementRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countOrdres = ordreDePaiementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBudgets = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        long countLignes = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);

                        // Compter tous les types de documents pour l'entreprise
                        long countFiches = ficheBesoinRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countDemandes = demandeDAchatRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBons = bonDeCommandeRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countAttestations = attestationServiceFaitRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countDecisions = decisionPrelevementRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countOrdres = ordreDePaiementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBudgets = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        long countLignes = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);

                        // Compter tous les types de documents pour l'entreprise
                        long countFiches = ficheBesoinRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countDemandes = demandeDAchatRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBons = bonDeCommandeRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countAttestations = attestationServiceFaitRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countDecisions = decisionPrelevementRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countOrdres = ordreDePaiementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBudgets = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        long countLignes = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);

                        // Compter tous les types de documents pour l'entreprise
                        long countFiches = ficheBesoinRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countDemandes = demandeDAchatRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBons = bonDeCommandeRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countAttestations = attestationServiceFaitRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countDecisions = decisionPrelevementRepo
                                .countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        long countOrdres = ordreDePaiementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        long countBudgets = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        long countLignes = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);

                        long totalCount = countFiches + countDemandes + countBons + countAttestations +
                                countDecisions + countOrdres + countBudgets + countLignes;
                        resultats.add((int) totalCount);
                    }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            System.err.println("Erreur dans getDocumentsParPeriodeEtEntreprise: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    public List<String> getLabelsParPeriode(String periode) {
        List<String> labels = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Labels par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        labels.add(String.format("%02dh", i));
                    }
                    break;

                case "semaine":
                    // Labels par jour pour cette semaine
                    String[] jours = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };
                    for (int i = 6; i >= 0; i--) {
                        int dayOfWeek = now.minusDays(i).getDayOfWeek().getValue();
                        labels.add(jours[dayOfWeek - 1]);
                    }
                    break;

                case "mois":
                    // Labels par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        labels.add("Sem " + (4 - i));
                    }
                    break;

                default:
                    // Par défaut, labels par jour pour la semaine
                    String[] joursDefaut = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };
                    for (int i = 6; i >= 0; i--) {
                        int dayOfWeek = now.minusDays(i).getDayOfWeek().getValue();
                        labels.add(joursDefaut[dayOfWeek - 1]);
                    }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            System.err.println("Erreur dans getLabelsParPeriode: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return labels;
    }

    // Méthodes pour les statistiques du directeur
    public long getTotalBudgetByEntrepriseId(Long entrepriseId) {
        return budgetRepo.countByEntrepriseId(entrepriseId);
    }

    public long getTotalLignesCreditByEntrepriseId(Long entrepriseId) {
        return ligneCreditRepo.countByEntrepriseId(entrepriseId);
    }

    public long getTotalDepensesByEntrepriseId(Long entrepriseId) {
        return decisionPrelevementRepo.countByEntrepriseId(entrepriseId);
    }

    public long getBudgetEnCoursByEntrepriseId(Long entrepriseId) {
        return budgetRepo.countByEntrepriseIdAndStatutEnCours(entrepriseId);
    }

    public long getLignesCreditEnAttenteByEntrepriseId(Long entrepriseId) {
        return ligneCreditRepo.countByEntrepriseIdAndStatutEnAttente(entrepriseId);
    }

    public long getDepensesEnAttenteByEntrepriseId(Long entrepriseId) {
        return decisionPrelevementRepo.countByEntrepriseIdAndStatutEnAttente(entrepriseId);
    }

    // Méthodes pour les graphiques du directeur
    public List<Integer> getBudgetsValidesParPeriodeEtEntreprise(String periode, Long entrepriseId) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);
                        long count = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);
                        long count = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        resultats.add((int) count);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = budgetRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start, end);
                        resultats.add((int) count);
                    }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans getBudgetsValidesParPeriodeEtEntreprise: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    public List<Integer> getLignesCreditValideesParPeriodeEtEntreprise(String periode, Long entrepriseId) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);
                        long count = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);
                        long count = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = ligneCreditRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId, start,
                                end);
                        resultats.add((int) count);
                    }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans getLignesCreditValideesParPeriodeEtEntreprise: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }

    public List<Integer> getDepensesValideesParPeriodeEtEntreprise(String periode, Long entrepriseId) {
        List<Integer> resultats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            switch (periode.toLowerCase()) {
                case "jour":
                    // Données par heure pour aujourd'hui
                    for (int i = 0; i < 24; i += 2) { // Toutes les 2 heures
                        LocalDateTime start = now.with(LocalTime.of(i, 0));
                        LocalDateTime end = (i + 2 < 24) ? now.with(LocalTime.of(i + 2, 0)) : now.with(LocalTime.MAX);
                        long count = decisionPrelevementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "semaine":
                    // Données par jour pour cette semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = decisionPrelevementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        resultats.add((int) count);
                    }
                    break;

                case "mois":
                    // Données par semaine pour ce mois (4 semaines)
                    for (int i = 3; i >= 0; i--) {
                        LocalDateTime start = now.minusWeeks(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusWeeks(i).with(LocalTime.MAX);
                        long count = decisionPrelevementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        resultats.add((int) count);
                    }
                    break;

                default:
                    // Par défaut, données par jour pour la semaine
                    for (int i = 6; i >= 0; i--) {
                        LocalDateTime start = now.minusDays(i).with(LocalTime.MIDNIGHT);
                        LocalDateTime end = now.minusDays(i).with(LocalTime.MAX);
                        long count = decisionPrelevementRepo.countByEntrepriseIdAndDateCreationBetween(entrepriseId,
                                start, end);
                        resultats.add((int) count);
                    }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans getDepensesValideesParPeriodeEtEntreprise: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return resultats;
    }
}