package kafofond.controller;

import kafofond.entity.*;
import kafofond.repository.*;
import kafofond.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.Map;

/**
 * Contrôleur unifié pour la génération de tous les documents de l'application
 * 
 * 🎯 ENDPOINTS PRINCIPAUX :
 * - GET /api/documents/bon-commande/{id}/pdf
 * - GET /api/documents/budget/{id}/pdf
 * - GET /api/documents/fiche-besoin/{id}/pdf
 * - GET /api/documents/demande-achat/{id}/pdf
 * - GET /api/documents/attestation-service/{id}/pdf
 * - GET /api/documents/decision-prelevement/{id}/pdf
 * - GET /api/documents/ordre-paiement/{id}/pdf
 * - GET /api/documents/ligne-credit/{id}/pdf
 * 
 * Ce contrôleur centralise la génération de tous les documents PDF de
 * l'application.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Génération de Documents Unifiée", description = "Endpoints unifiés pour générer des documents PDF pour toutes les entités")
@SecurityRequirement(name = "bearerAuth")
public class DocumentGenerationController {

    private final BonDeCommandeRepo bonDeCommandeRepo;
    private final BudgetRepo budgetRepo;
    private final FicheBesoinRepo ficheBesoinRepo;
    private final DemandeDAchatRepo demandeDAchatRepo;
    private final AttestationDeServiceFaitRepo attestationRepo;
    private final DecisionDePrelevementRepo decisionRepo;
    private final OrdreDePaiementRepo ordreRepo;
    private final LigneCreditRepo ligneCreditRepo;

    private final BonDeCommandeService bonDeCommandeService;
    private final BudgetService budgetService;
    private final FicheBesoinService ficheBesoinService;
    private final DemandeDAchatService demandeDAchatService;
    private final AttestationServiceFaitService attestationServiceFaitService;
    private final DecisionPrelevementService decisionPrelevementService;
    private final OrdreDePaiementService ordreDePaiementService;

    private final DocumentService documentService;
    private final UtilisateurService utilisateurService;

    /**
     * Génère un PDF pour un bon de commande spécifique
     */
    @GetMapping("/bon-commande/{id}/pdf")
    @Operation(summary = "Générer un PDF d'un bon de commande", description = "Génère un document PDF professionnel contenant toutes les informations du bon de commande")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
            @ApiResponse(responseCode = "404", description = "Bon de commande introuvable"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération du PDF")
    })
    public ResponseEntity<?> genererPdfBonDeCommande(
            @Parameter(description = "ID du bon de commande") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Génération du PDF pour bon de commande ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer le bon de commande avec initialisation des relations
            BonDeCommande bon = bonDeCommandeService.getBonDeCommandeById(id);

            if (!bon.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererBonCommandePdf(bon);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bon_commande_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour bon de commande ID: {}. Taille: {} bytes", id, pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour bon de commande ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/bon-commande/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'un bon de commande
     */
    @GetMapping("/bon-commande/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'un bon de commande", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfBonDeCommande(
            @Parameter(description = "ID du bon de commande") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Téléchargement du PDF pour bon de commande ID: {}", id);

            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            BonDeCommande bon = bonDeCommandeRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bon de commande introuvable"));

            if (!bon.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererBonCommandePdf(bon);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "bon_commande_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour bon de commande ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/bon-commande/" + id + "/pdf/download",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Génère un PDF pour un budget spécifique
     */
    @GetMapping("/budget/{id}/pdf")
    @Operation(summary = "Générer un PDF d'un budget", description = "Génère un document PDF professionnel contenant toutes les informations du budget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
            @ApiResponse(responseCode = "404", description = "Budget introuvable"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération du PDF")
    })
    public ResponseEntity<?> genererPdfBudget(
            @Parameter(description = "ID du budget") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour budget ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer le budget avec initialisation des relations
            Budget budget = budgetService.trouverParIdAvecRelations(id)
                    .orElseThrow(() -> new RuntimeException("Budget introuvable"));

            if (!budget.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererBudgetPdf(budget);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "budget_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour budget ID: {}. Taille: {} bytes", id, pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour budget ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/budget/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'un budget
     */
    @GetMapping("/budget/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'un budget", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfBudget(
            @Parameter(description = "ID du budget") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Budget budget = budgetRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Budget introuvable"));

            if (!budget.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererBudgetPdf(budget);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "budget_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour budget", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Génère un PDF pour une fiche de besoin spécifique
     */
    @GetMapping("/fiche-besoin/{id}/pdf")
    @Operation(summary = "Générer un PDF d'une fiche de besoin", description = "Génère un document PDF professionnel contenant toutes les informations de la fiche de besoin")
    public ResponseEntity<?> genererPdfFicheBesoin(
            @Parameter(description = "ID de la fiche de besoin") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour fiche de besoin ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer la fiche de besoin avec initialisation des relations
            FicheDeBesoin fiche = ficheBesoinService.trouverParIdAvecRelations(id)
                    .orElseThrow(() -> new RuntimeException("Fiche de besoin introuvable"));

            if (!fiche.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererFicheDeBesoinPdf(fiche);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "fiche_besoin_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour fiche de besoin ID: {}. Taille: {} bytes", id, pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour fiche de besoin ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/fiche-besoin/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'une fiche de besoin
     */
    @GetMapping("/fiche-besoin/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'une fiche de besoin", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfFicheBesoin(
            @Parameter(description = "ID de la fiche de besoin") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            FicheDeBesoin fiche = ficheBesoinRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fiche de besoin introuvable"));

            if (!fiche.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererFicheDeBesoinPdf(fiche);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "fiche_besoin_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour fiche de besoin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Génère un PDF pour une demande d'achat spécifique
     */
    @GetMapping("/demande-achat/{id}/pdf")
    @Operation(summary = "Générer un PDF d'une demande d'achat", description = "Génère un document PDF professionnel contenant toutes les informations de la demande d'achat")
    public ResponseEntity<?> genererPdfDemandeAchat(
            @Parameter(description = "ID de la demande d'achat") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour demande d'achat ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer la demande d'achat avec initialisation des relations
            DemandeDAchat demande = demandeDAchatService.trouverParIdAvecRelations(id)
                    .orElseThrow(() -> new RuntimeException("Demande d'achat introuvable"));

            if (!demande.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererDemandeAchatPdf(demande);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "demande_achat_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour demande d'achat ID: {}. Taille: {} bytes", id, pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour demande d'achat ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/demande-achat/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'une demande d'achat
     */
    @GetMapping("/demande-achat/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'une demande d'achat", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfDemandeAchat(
            @Parameter(description = "ID de la demande d'achat") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            DemandeDAchat demande = demandeDAchatRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Demande d'achat introuvable"));

            if (!demande.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererDemandeAchatPdf(demande);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "demande_achat_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour demande d'achat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Génère un PDF pour une attestation de service fait spécifique
     */
    @GetMapping("/attestation-service/{id}/pdf")
    @Operation(summary = "Générer un PDF d'une attestation de service fait", description = "Génère un document PDF professionnel contenant toutes les informations de l'attestation de service fait")
    public ResponseEntity<?> genererPdfAttestationService(
            @Parameter(description = "ID de l'attestation de service fait") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour attestation de service fait ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer l'attestation avec initialisation des relations
            AttestationDeServiceFait attestation = attestationServiceFaitService.trouverParIdAvecRelations(id)
                    .orElseThrow(() -> new RuntimeException("Attestation de service fait introuvable"));

            if (!attestation.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererAttestationServicePdf(attestation);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "attestation_service_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour attestation de service fait ID: {}. Taille: {} bytes", id,
                    pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour attestation de service fait ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/attestation-service/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'une attestation de service fait
     */
    @GetMapping("/attestation-service/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'une attestation de service fait", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfAttestationService(
            @Parameter(description = "ID de l'attestation de service fait") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            AttestationDeServiceFait attestation = attestationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Attestation de service fait introuvable"));

            if (!attestation.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererAttestationServicePdf(attestation);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "attestation_service_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour attestation de service fait", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Génère un PDF pour une décision de prélèvement spécifique
     */
    @GetMapping("/decision-prelevement/{id}/pdf")
    @Operation(summary = "Générer un PDF d'une décision de prélèvement", description = "Génère un document PDF professionnel contenant toutes les informations de la décision de prélèvement")
    public ResponseEntity<?> genererPdfDecisionPrelevement(
            @Parameter(description = "ID de la décision de prélèvement") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour décision de prélèvement ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer la décision avec initialisation des relations
            DecisionDePrelevement decision = decisionPrelevementService.trouverParId(id)
                    .orElseThrow(() -> new RuntimeException("Décision de prélèvement introuvable"));

            if (!decision.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererDecisionPrelevementPdf(decision);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la génération du PDF: " + e.getMessage()));
        }
    }

    /**
     * Télécharge le PDF d'une décision de prélèvement
     */
    @GetMapping("/decision-prelevement/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'une décision de prélèvement", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfDecisionPrelevement(
            @Parameter(description = "ID de la décision de prélèvement") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            DecisionDePrelevement decision = decisionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Décision de prélèvement introuvable"));

            if (!decision.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererDecisionPrelevementPdf(decision);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "decision_prelevement_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour décision de prélèvement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Génère un PDF pour un ordre de paiement spécifique
     */
    @GetMapping("/ordre-paiement/{id}/pdf")
    @Operation(summary = "Générer un PDF d'un ordre de paiement", description = "Génère un document PDF professionnel contenant toutes les informations de l'ordre de paiement")
    public ResponseEntity<?> genererPdfOrdrePaiement(
            @Parameter(description = "ID de l'ordre de paiement") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour ordre de paiement ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer l'ordre avec initialisation des relations
            OrdreDePaiement ordre = ordreDePaiementService.trouverParIdAvecRelations(id)
                    .orElseThrow(() -> new RuntimeException("Ordre de paiement introuvable"));

            if (!ordre.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererOrdrePaiementPdf(ordre);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "ordre_paiement_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour ordre de paiement ID: {}. Taille: {} bytes", id, pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour ordre de paiement ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/ordre-paiement/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'un ordre de paiement
     */
    @GetMapping("/ordre-paiement/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'un ordre de paiement", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfOrdrePaiement(
            @Parameter(description = "ID de l'ordre de paiement") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            OrdreDePaiement ordre = ordreRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ordre de paiement introuvable"));

            if (!ordre.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererOrdrePaiementPdf(ordre);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ordre_paiement_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour ordre de paiement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Génère un PDF pour une ligne de crédit spécifique
     */
    @GetMapping("/ligne-credit/{id}/pdf")
    @Operation(summary = "Générer un PDF d'une ligne de crédit", description = "Génère un document PDF professionnel contenant toutes les informations de la ligne de crédit")
    public ResponseEntity<?> genererPdfLigneCredit(
            @Parameter(description = "ID de la ligne de crédit") @PathVariable Long id,
            Authentication auth) {

        try {
            log.info("Demande de génération PDF pour ligne de crédit ID: {}", id);

            // Vérifier l'authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer la ligne de crédit avec initialisation des relations
            LigneCredit ligne = ligneCreditRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ligne de crédit introuvable"));

            if (!ligne.getBudget().getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererLigneCreditPdf(ligne);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer la réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "ligne_credit_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF généré avec succès pour ligne de crédit ID: {}. Taille: {} bytes", id, pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour ligne de crédit ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la génération du PDF: " + e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.LocalDateTime.now().toString(),
                            "path", "/api/documents/ligne-credit/" + id + "/pdf",
                            "details", "Veuillez contacter l'administrateur"));
        }
    }

    /**
     * Télécharge le PDF d'une ligne de crédit
     */
    @GetMapping("/ligne-credit/{id}/pdf/download")
    @Operation(summary = "Télécharger le PDF d'une ligne de crédit", description = "Force le téléchargement du PDF au lieu de l'affichage dans le navigateur")
    public ResponseEntity<?> telechargerPdfLigneCredit(
            @Parameter(description = "ID de la ligne de crédit") @PathVariable Long id,
            Authentication auth) {

        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            LigneCredit ligne = ligneCreditRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ligne de crédit introuvable"));

            if (!ligne.getBudget().getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès interdit"));
            }

            // Générer le PDF
            String urlPdf = documentService.genererLigneCreditPdf(ligne);

            // Lire le fichier PDF généré
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);

            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ligne_credit_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF pour ligne de crédit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }
}