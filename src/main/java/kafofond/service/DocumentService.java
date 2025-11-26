package kafofond.service;

import kafofond.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service de génération de documents (PDF, Excel)
 * Génère les documents pour les entités validées
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @Value("${reporting.output.path:reports/}")
    private String reportsPath;

    /**
     * Génère le PDF d'un bon de commande
     */
    public String genererBonCommandePdf(BonDeCommande bon) throws IOException {
        log.info("Génération du PDF pour le bon de commande {}", bon.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("bon_commande_%d_%s.pdf", 
                    bon.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfBonCommande(bon);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/bon-commande/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le bon de commande {}: {}", bon.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'un budget
     */
    public String genererBudgetPdf(Budget budget) throws IOException {
        log.info("Génération du PDF pour le budget {}", budget.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("budget_%d_%s.pdf", 
                    budget.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfBudget(budget);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/budget/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le budget {}: {}", budget.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'une fiche de besoin
     */
    public String genererFicheDeBesoinPdf(FicheDeBesoin fiche) throws IOException {
        log.info("Génération du PDF pour la fiche de besoin {}", fiche.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("fiche_besoin_%d_%s.pdf", 
                    fiche.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfFicheBesoin(fiche);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/fiche-besoin/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la fiche de besoin {}: {}", fiche.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'une demande d'achat
     */
    public String genererDemandeAchatPdf(DemandeDAchat demande) throws IOException {
        log.info("Génération du PDF pour la demande d'achat {}", demande.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("demande_achat_%d_%s.pdf", 
                    demande.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfDemandeAchat(demande);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/demande-achat/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la demande d'achat {}: {}", demande.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'une attestation de service fait
     */
    public String genererAttestationServicePdf(AttestationDeServiceFait attestation) throws IOException {
        log.info("Génération du PDF pour l'attestation de service fait {}", attestation.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("attestation_service_%d_%s.pdf", 
                    attestation.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfAttestationService(attestation);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/attestation-service/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'attestation de service fait {}: {}", attestation.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'une décision de prélèvement
     */
    public String genererDecisionPrelevementPdf(DecisionDePrelevement decision) throws IOException {
        log.info("Génération du PDF pour la décision de prélèvement {}", decision.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("decision_prelevement_%d_%s.pdf", 
                    decision.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfDecisionPrelevement(decision);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/decision-prelevement/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la décision de prélèvement {}: {}", decision.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'un ordre de paiement
     */
    public String genererOrdrePaiementPdf(OrdreDePaiement ordre) throws IOException {
        log.info("Génération du PDF pour l'ordre de paiement {}", ordre.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("ordre_paiement_%d_%s.pdf", 
                    ordre.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfOrdrePaiement(ordre);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/ordre-paiement/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'ordre de paiement {}: {}", ordre.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF d'une ligne de crédit
     */
    public String genererLigneCreditPdf(LigneCredit ligne) throws IOException {
        log.info("Génération du PDF pour la ligne de crédit {}", ligne.getId());
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("ligne_credit_%d_%s.pdf", 
                    ligne.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText
            byte[] pdfBytes = genererPdfLigneCredit(ligne);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/ligne-credit/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la ligne de crédit {}: {}", ligne.getId(), e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour un bon de commande avec iText
     */
    private byte[] genererPdfBonCommande(BonDeCommande bon) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("BON DE COMMANDE")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (bon.getEntreprise() != null && bon.getEntreprise().getNom() != null) {
                document.add(new Paragraph(bon.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (bon.getCode() != null) {
                table.addCell("Code");
                table.addCell(bon.getCode());
            }
            
            table.addCell("Fournisseur");
            table.addCell(bon.getFournisseur() != null ? bon.getFournisseur() : "N/A");
            
            table.addCell("Description");
            table.addCell(bon.getDescription() != null ? bon.getDescription() : "N/A");
            
            table.addCell("Montant Total");
            table.addCell(String.format("%.2f FCFA", bon.getMontantTotal()));
            
            table.addCell("Service Bénéficiaire");
            table.addCell(bon.getServiceBeneficiaire() != null ? bon.getServiceBeneficiaire() : "N/A");
            
            table.addCell("Mode de Paiement");
            table.addCell(bon.getModePaiement() != null ? bon.getModePaiement() : "N/A");
            
            if (bon.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(bon.getDateCreation().toString());
            }
            
            if (bon.getDelaiPaiement() != null) {
                table.addCell("Délai de Paiement");
                table.addCell(bon.getDelaiPaiement().toString());
            }
            
            if (bon.getDateExecution() != null) {
                table.addCell("Date d'Exécution");
                table.addCell(bon.getDateExecution().toString());
            }
            
            if (bon.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(bon.getStatut().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (bon.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((bon.getCreePar().getPrenom() != null ? bon.getCreePar().getPrenom() : "") + " " + (bon.getCreePar().getNom() != null ? bon.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(bon.getCreePar().getRole() != null ? bon.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le bon de commande {}", bon.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour un budget avec iText
     */
    private byte[] genererPdfBudget(Budget budget) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("BUDGET")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (budget.getEntreprise() != null && budget.getEntreprise().getNom() != null) {
                document.add(new Paragraph(budget.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (budget.getCode() != null) {
                table.addCell("Code");
                table.addCell(budget.getCode());
            }
            
            table.addCell("Intitulé");
            table.addCell(budget.getIntituleBudget() != null ? budget.getIntituleBudget() : "N/A");
            
            table.addCell("Description");
            table.addCell(budget.getDescription() != null ? budget.getDescription() : "N/A");
            
            table.addCell("Montant");
            table.addCell(String.format("%.2f FCFA", budget.getMontantBudget()));
            
            if (budget.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(budget.getStatut().toString());
            }
            
            if (budget.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(budget.getDateCreation().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (budget.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((budget.getCreePar().getPrenom() != null ? budget.getCreePar().getPrenom() : "") + " " + (budget.getCreePar().getNom() != null ? budget.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(budget.getCreePar().getRole() != null ? budget.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le budget {}", budget.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une fiche de besoin avec iText
     */
    private byte[] genererPdfFicheBesoin(FicheDeBesoin fiche) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("FICHE DE BESOIN")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (fiche.getEntreprise() != null && fiche.getEntreprise().getNom() != null) {
                document.add(new Paragraph(fiche.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (fiche.getCode() != null) {
                table.addCell("Code");
                table.addCell(fiche.getCode());
            }
            
            table.addCell("Service Bénéficiaire");
            table.addCell(fiche.getServiceBeneficiaire() != null ? fiche.getServiceBeneficiaire() : "N/A");
            
            table.addCell("Objet");
            table.addCell(fiche.getObjet() != null ? fiche.getObjet() : "N/A");
            
            table.addCell("Description");
            table.addCell(fiche.getDescription() != null ? fiche.getDescription() : "N/A");
            
            table.addCell("Montant Estimé");
            table.addCell(String.format("%.2f FCFA", fiche.getMontantEstime()));
            
            if (fiche.getDateAttendu() != null) {
                table.addCell("Date Attendue");
                table.addCell(fiche.getDateAttendu().toString());
            }
            
            if (fiche.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(fiche.getDateCreation().toString());
            }
            
            if (fiche.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(fiche.getStatut().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (fiche.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((fiche.getCreePar().getPrenom() != null ? fiche.getCreePar().getPrenom() : "") + " " + (fiche.getCreePar().getNom() != null ? fiche.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(fiche.getCreePar().getRole() != null ? fiche.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la fiche de besoin {}", fiche.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une demande d'achat avec iText
     */
    private byte[] genererPdfDemandeAchat(DemandeDAchat demande) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("DEMANDE D'ACHAT")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (demande.getEntreprise() != null && demande.getEntreprise().getNom() != null) {
                document.add(new Paragraph(demande.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (demande.getCode() != null) {
                table.addCell("Code");
                table.addCell(demande.getCode());
            }
            
            table.addCell("Référence Besoin");
            table.addCell(demande.getReferenceBesoin() != null ? demande.getReferenceBesoin() : "N/A");
            
            table.addCell("Description");
            table.addCell(demande.getDescription() != null ? demande.getDescription() : "N/A");
            
            table.addCell("Fournisseur");
            table.addCell(demande.getFournisseur() != null ? demande.getFournisseur() : "N/A");
            
            table.addCell("Montant Total");
            table.addCell(String.format("%.2f FCFA", demande.getMontantTotal()));
            
            table.addCell("Service Bénéficiaire");
            table.addCell(demande.getServiceBeneficiaire() != null ? demande.getServiceBeneficiaire() : "N/A");
            
            if (demande.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(demande.getDateCreation().toString());
            }
            
            if (demande.getDateAttendu() != null) {
                table.addCell("Date Attendue");
                table.addCell(demande.getDateAttendu().toString());
            }
            
            if (demande.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(demande.getStatut().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (demande.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((demande.getCreePar().getPrenom() != null ? demande.getCreePar().getPrenom() : "") + " " + (demande.getCreePar().getNom() != null ? demande.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(demande.getCreePar().getRole() != null ? demande.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la demande d'achat {}", demande.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une attestation de service fait avec iText
     */
    private byte[] genererPdfAttestationService(AttestationDeServiceFait attestation) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("ATTESTATION DE SERVICE FAIT")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (attestation.getEntreprise() != null && attestation.getEntreprise().getNom() != null) {
                document.add(new Paragraph(attestation.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (attestation.getCode() != null) {
                table.addCell("Code");
                table.addCell(attestation.getCode());
            }
            
            table.addCell("Référence Bon Commande");
            table.addCell(attestation.getReferenceBonCommande() != null ? attestation.getReferenceBonCommande() : "N/A");
            
            table.addCell("Fournisseur");
            table.addCell(attestation.getFournisseur() != null ? attestation.getFournisseur() : "N/A");
            
            table.addCell("Titre");
            table.addCell(attestation.getTitre() != null ? attestation.getTitre() : "N/A");
            
            table.addCell("Constat");
            table.addCell(attestation.getConstat() != null ? attestation.getConstat() : "N/A");
            
            if (attestation.getDateLivraison() != null) {
                table.addCell("Date de Livraison");
                table.addCell(attestation.getDateLivraison().toString());
            }
            
            if (attestation.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(attestation.getDateCreation().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (attestation.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((attestation.getCreePar().getPrenom() != null ? attestation.getCreePar().getPrenom() : "") + " " + (attestation.getCreePar().getNom() != null ? attestation.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(attestation.getCreePar().getRole() != null ? attestation.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'attestation de service fait {}", attestation.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une décision de prélèvement avec iText
     */
    private byte[] genererPdfDecisionPrelevement(DecisionDePrelevement decision) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("DÉCISION DE PRÉLÈVEMENT")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (decision.getEntreprise() != null && decision.getEntreprise().getNom() != null) {
                document.add(new Paragraph(decision.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (decision.getCode() != null) {
                table.addCell("Code");
                table.addCell(decision.getCode());
            }
            
            table.addCell("Référence Attestation");
            table.addCell(decision.getReferenceAttestation() != null ? decision.getReferenceAttestation() : "N/A");
            
            table.addCell("Montant");
            table.addCell(String.format("%.2f FCFA", decision.getMontant()));
            
            table.addCell("Compte Origine");
            table.addCell(decision.getCompteOrigine() != null ? decision.getCompteOrigine() : "N/A");
            
            table.addCell("Compte Destinataire");
            table.addCell(decision.getCompteDestinataire() != null ? decision.getCompteDestinataire() : "N/A");
            
            table.addCell("Motif Prélèvement");
            table.addCell(decision.getMotifPrelevement() != null ? decision.getMotifPrelevement() : "N/A");
            
            if (decision.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(decision.getDateCreation().toString());
            }
            
            if (decision.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(decision.getStatut().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (decision.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((decision.getCreePar().getPrenom() != null ? decision.getCreePar().getPrenom() : "") + " " + (decision.getCreePar().getNom() != null ? decision.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(decision.getCreePar().getRole() != null ? decision.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la décision de prélèvement {}", decision.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour un ordre de paiement avec iText
     */
    private byte[] genererPdfOrdrePaiement(OrdreDePaiement ordre) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("ORDRE DE PAIEMENT")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise
            if (ordre.getEntreprise() != null && ordre.getEntreprise().getNom() != null) {
                document.add(new Paragraph(ordre.getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (ordre.getCode() != null) {
                table.addCell("Code");
                table.addCell(ordre.getCode());
            }
            
            table.addCell("Référence Décision Prélèvement");
            table.addCell(ordre.getReferenceDecisionPrelevement() != null ? ordre.getReferenceDecisionPrelevement() : "N/A");
            
            table.addCell("Montant");
            table.addCell(String.format("%.2f FCFA", ordre.getMontant()));
            
            table.addCell("Description");
            table.addCell(ordre.getDescription() != null ? ordre.getDescription() : "N/A");
            
            table.addCell("Compte Origine");
            table.addCell(ordre.getCompteOrigine() != null ? ordre.getCompteOrigine() : "N/A");
            
            table.addCell("Compte Destinataire");
            table.addCell(ordre.getCompteDestinataire() != null ? ordre.getCompteDestinataire() : "N/A");
            
            if (ordre.getDateExecution() != null) {
                table.addCell("Date d'Exécution");
                table.addCell(ordre.getDateExecution().toString());
            }
            
            if (ordre.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(ordre.getDateCreation().toString());
            }
            
            if (ordre.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(ordre.getStatut().toString());
            }
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (ordre.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((ordre.getCreePar().getPrenom() != null ? ordre.getCreePar().getPrenom() : "") + " " + (ordre.getCreePar().getNom() != null ? ordre.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(ordre.getCreePar().getRole() != null ? ordre.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'ordre de paiement {}", ordre.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une ligne de crédit avec iText
     */
    private byte[] genererPdfLigneCredit(LigneCredit ligne) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            document.add(new Paragraph("LIGNE DE CRÉDIT")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Nom de l'entreprise (à partir du budget)
            if (ligne.getBudget() != null && ligne.getBudget().getEntreprise() != null) {
                document.add(new Paragraph(ligne.getBudget().getEntreprise().getNom())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30));
            }

            // Informations principales (sans ID)
            float[] columnWidths = {2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
            
            if (ligne.getCode() != null) {
                table.addCell("Code");
                table.addCell(ligne.getCode());
            }
            
            table.addCell("Intitulé Ligne");
            table.addCell(ligne.getIntituleLigne() != null ? ligne.getIntituleLigne() : "N/A");
            
            table.addCell("Description");
            table.addCell(ligne.getDescription() != null ? ligne.getDescription() : "N/A");
            
            table.addCell("Montant Alloué");
            table.addCell(String.format("%.2f FCFA", ligne.getMontantAllouer()));
            
            table.addCell("Montant Engagé");
            table.addCell(String.format("%.2f FCFA", ligne.getMontantEngager()));
            
            table.addCell("Montant Restant");
            table.addCell(String.format("%.2f FCFA", ligne.getMontantRestant()));
            
            if (ligne.getDateCreation() != null) {
                table.addCell("Date de Création");
                table.addCell(ligne.getDateCreation().toString());
            }
            
            if (ligne.getDateDebut() != null) {
                table.addCell("Date de Début");
                table.addCell(ligne.getDateDebut().toString());
            }
            
            if (ligne.getDateFin() != null) {
                table.addCell("Date de Fin");
                table.addCell(ligne.getDateFin().toString());
            }
            
            if (ligne.getStatut() != null) {
                table.addCell("Statut");
                table.addCell(ligne.getStatut().toString());
            }
            
            table.addCell("État");
            table.addCell(ligne.isEtat() ? "Actif" : "Inactif");
            
            document.add(table);
            
            document.add(new Paragraph(" ")
                    .setMarginTop(40));
            
            // Section signature
            document.add(new Paragraph("Signature")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(30)
                    .setMarginBottom(20));
            
            // Informations sur la personne ayant validé/approuvé
            if (ligne.getCreePar() != null) {
                document.add(new Paragraph("Document traité par :"));
                document.add(new Paragraph((ligne.getCreePar().getPrenom() != null ? ligne.getCreePar().getPrenom() : "") + " " + (ligne.getCreePar().getNom() != null ? ligne.getCreePar().getNom() : ""))
                        .setBold());
                document.add(new Paragraph(ligne.getCreePar().getRole() != null ? ligne.getCreePar().getRole().toString() : "")
                        .setMarginBottom(30));
            }
            
            // Pied de page
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la ligne de crédit {}", ligne.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une liste de documents avec iText
     */
    public String genererListeDocumentsPdf(TypeDocument typeDocument, List<?> documents, String nomFichier) throws IOException {
        log.info("Génération du PDF pour une liste de {} documents de type {}", documents.size(), typeDocument);
        
        try {
            // Créer le dossier s'il n'existe pas
            Path reportsDir = Paths.get(reportsPath);
            if (!Files.exists(reportsDir)) {
                log.info("Création du dossier de rapports: {}", reportsDir.toAbsolutePath());
                Files.createDirectories(reportsDir);
            }
            
            // Nom du fichier
            String fileName = String.format("liste_%s_%s.pdf", 
                    nomFichier, 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path filePath = reportsDir.resolve(fileName);
            log.info("Chemin du fichier PDF: {}", filePath.toAbsolutePath());
            
            // Générer le PDF avec iText selon le type de document
            byte[] pdfBytes = genererPdfListeDocuments(typeDocument, documents);
            log.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            
            // Écrire le fichier
            Files.write(filePath, pdfBytes);
            log.info("Fichier PDF écrit avec succès");
            
            String urlPdf = "/api/rapports/liste-" + typeDocument.name().toLowerCase().replace("_", "-") + "/" + fileName;
            log.info("PDF généré : {}", urlPdf);
            
            return urlPdf;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la liste de documents de type {}: {}", typeDocument, e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le PDF pour une liste de documents avec iText
     */
    private byte[] genererPdfListeDocuments(TypeDocument typeDocument, List<?> documents) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre principal
            String titre = "LISTE DES " + typeDocument.name().replace("_", " ");
            document.add(new Paragraph(titre)
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Date de génération
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30));

            // Générer le tableau selon le type de document
            Table table = genererTableauDocuments(typeDocument, documents);
            document.add(table);
            
            // Pied de page
            document.add(new Paragraph("Total des documents : " + documents.size())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour la liste de documents de type {}", typeDocument, e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Génère le tableau de documents selon le type
     */
    private Table genererTableauDocuments(TypeDocument typeDocument, List<?> documents) {
        switch (typeDocument) {
            case DEMANDE_ACHAT:
                return genererTableauDemandesAchat(documents);
            case FICHE_BESOIN:
                return genererTableauFichesBesoin(documents);
            case BUDGET:
                return genererTableauBudgets(documents);
            case BON_COMMANDE:
                return genererTableauBonsCommande(documents);
            case ATTESTATION_SERVICE_FAIT:
                return genererTableauAttestationsService(documents);
            case DECISION_PRELEVEMENT:
                return genererTableauDecisionsPrelevement(documents);
            case ORDRE_PAIEMENT:
                return genererTableauOrdresPaiement(documents);
            case LIGNE_CREDIT:
                return genererTableauLignesCredit(documents);
            default:
                throw new IllegalArgumentException("Type de document non supporté: " + typeDocument);
        }
    }

    /**
     * Génère le tableau pour les demandes d'achat
     */
    private Table genererTableauDemandesAchat(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Fournisseur").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant Total").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            DemandeDAchat demande = (DemandeDAchat) obj;
            table.addCell(new Cell().add(new Paragraph(demande.getCode() != null ? demande.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(demande.getDescription() != null ? demande.getDescription() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(demande.getFournisseur() != null ? demande.getFournisseur() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(demande.getMontantTotal() != 0 ? String.format("%,.0f FCFA", demande.getMontantTotal()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(demande.getStatut() != null ? demande.getStatut().name() : "N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les fiches de besoin
     */
    private Table genererTableauFichesBesoin(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Objet").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Service Bénéficiaire").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant Estimé").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            FicheDeBesoin fiche = (FicheDeBesoin) obj;
            table.addCell(new Cell().add(new Paragraph(fiche.getCode() != null ? fiche.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(fiche.getObjet() != null ? fiche.getObjet() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(fiche.getServiceBeneficiaire() != null ? fiche.getServiceBeneficiaire() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(fiche.getMontantEstime() != 0 ? String.format("%,.0f FCFA", fiche.getMontantEstime()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(fiche.getStatut() != null ? fiche.getStatut().name() : "N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les budgets
     */
    private Table genererTableauBudgets(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Intitulé").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Année").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant Alloué").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            Budget budget = (Budget) obj;
            table.addCell(new Cell().add(new Paragraph(budget.getCode() != null ? budget.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(budget.getIntituleBudget() != null ? budget.getIntituleBudget() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(budget.getDateDebut() != null ? String.valueOf(budget.getDateDebut().getYear()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(budget.getMontantBudget() != 0 ? String.format("%,.0f FCFA", budget.getMontantBudget()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(budget.getStatut() != null ? budget.getStatut().name() : "N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les bons de commande
     */
    private Table genererTableauBonsCommande(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Fournisseur").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Demande d'Achat").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant Total").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            BonDeCommande bon = (BonDeCommande) obj;
            table.addCell(new Cell().add(new Paragraph(bon.getCode() != null ? bon.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(bon.getFournisseur() != null ? bon.getFournisseur() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(bon.getDemandeDAchat() != null && bon.getDemandeDAchat().getCode() != null ? bon.getDemandeDAchat().getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(bon.getMontantTotal() != 0 ? String.format("%,.0f FCFA", bon.getMontantTotal()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(bon.getStatut() != null ? bon.getStatut().name() : "N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les attestations de service fait
     */
    private Table genererTableauAttestationsService(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Bon de Commande").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date Service").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            AttestationDeServiceFait attestation = (AttestationDeServiceFait) obj;
            table.addCell(new Cell().add(new Paragraph(attestation.getCode() != null ? attestation.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(attestation.getBonDeCommande() != null && attestation.getBonDeCommande().getCode() != null ? attestation.getBonDeCommande().getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(attestation.getDateLivraison() != null ? attestation.getDateLivraison().toString() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(attestation.getBonDeCommande() != null && attestation.getBonDeCommande().getMontantTotal() != 0 ? String.format("%,.0f FCFA", attestation.getBonDeCommande().getMontantTotal()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph("N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les décisions de prélèvement
     */
    private Table genererTableauDecisionsPrelevement(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Attestation").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date Prélèvement").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            DecisionDePrelevement decision = (DecisionDePrelevement) obj;
            table.addCell(new Cell().add(new Paragraph(decision.getCode() != null ? decision.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(decision.getAttestationDeServiceFait() != null && decision.getAttestationDeServiceFait().getCode() != null ? decision.getAttestationDeServiceFait().getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(decision.getDateCreation() != null ? decision.getDateCreation().toLocalDate().toString() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(decision.getMontant() != 0 ? String.format("%,.0f FCFA", decision.getMontant()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(decision.getStatut() != null ? decision.getStatut().name() : "N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les ordres de paiement
     */
    private Table genererTableauOrdresPaiement(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Décision Prélèvement").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date Émission").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            OrdreDePaiement ordre = (OrdreDePaiement) obj;
            table.addCell(new Cell().add(new Paragraph(ordre.getCode() != null ? ordre.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ordre.getDecisionDePrelevement() != null && ordre.getDecisionDePrelevement().getCode() != null ? ordre.getDecisionDePrelevement().getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ordre.getDateCreation() != null ? ordre.getDateCreation().toLocalDate().toString() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ordre.getMontant() != 0 ? String.format("%,.0f FCFA", ordre.getMontant()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ordre.getStatut() != null ? ordre.getStatut().name() : "N/A")));
        }
        
        return table;
    }

    /**
     * Génère le tableau pour les lignes de crédit
     */
    private Table genererTableauLignesCredit(List<?> documents) {
        float[] columnWidths = {1, 3, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Code").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Budget").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant Alloué").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant Utilisé").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        
        // Données du tableau
        for (Object obj : documents) {
            LigneCredit ligne = (LigneCredit) obj;
            table.addCell(new Cell().add(new Paragraph(ligne.getCode() != null ? ligne.getCode() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ligne.getBudget() != null && ligne.getBudget().getIntituleBudget() != null ? ligne.getBudget().getIntituleBudget() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ligne.getMontantAllouer() != 0 ? String.format("%,.0f FCFA", ligne.getMontantAllouer()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ligne.getMontantEngager() != 0 ? String.format("%,.0f FCFA", ligne.getMontantEngager()) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(ligne.getStatut() != null ? ligne.getStatut().name() : "N/A")));
        }
        
        return table;
    }
}