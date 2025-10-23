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
            if (bon.getEntreprise() != null) {
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
            table.addCell(bon.getFournisseur());
            
            table.addCell("Description");
            table.addCell(bon.getDescription());
            
            table.addCell("Montant Total");
            table.addCell(String.format("%.2f FCFA", bon.getMontantTotal()));
            
            table.addCell("Service Bénéficiaire");
            table.addCell(bon.getServiceBeneficiaire());
            
            table.addCell("Mode de Paiement");
            table.addCell(bon.getModePaiement());
            
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
                document.add(new Paragraph(bon.getCreePar().getPrenom() + " " + bon.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(bon.getCreePar().getRole().toString())
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
            if (budget.getEntreprise() != null) {
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
            table.addCell(budget.getIntituleBudget());
            
            table.addCell("Description");
            table.addCell(budget.getDescription());
            
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
                document.add(new Paragraph(budget.getCreePar().getPrenom() + " " + budget.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(budget.getCreePar().getRole().toString())
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
            if (fiche.getEntreprise() != null) {
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
            table.addCell(fiche.getServiceBeneficiaire());
            
            table.addCell("Objet");
            table.addCell(fiche.getObjet());
            
            table.addCell("Description");
            table.addCell(fiche.getDescription());
            
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
                document.add(new Paragraph(fiche.getCreePar().getPrenom() + " " + fiche.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(fiche.getCreePar().getRole().toString())
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
            if (demande.getEntreprise() != null) {
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
            table.addCell(demande.getReferenceBesoin());
            
            table.addCell("Description");
            table.addCell(demande.getDescription());
            
            table.addCell("Fournisseur");
            table.addCell(demande.getFournisseur());
            
            table.addCell("Montant Total");
            table.addCell(String.format("%.2f FCFA", demande.getMontantTotal()));
            
            table.addCell("Service Bénéficiaire");
            table.addCell(demande.getServiceBeneficiaire());
            
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
                document.add(new Paragraph(demande.getCreePar().getPrenom() + " " + demande.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(demande.getCreePar().getRole().toString())
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
            if (attestation.getEntreprise() != null) {
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
            table.addCell(attestation.getReferenceBonCommande());
            
            table.addCell("Fournisseur");
            table.addCell(attestation.getFournisseur());
            
            table.addCell("Titre");
            table.addCell(attestation.getTitre());
            
            table.addCell("Constat");
            table.addCell(attestation.getConstat());
            
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
                document.add(new Paragraph(attestation.getCreePar().getPrenom() + " " + attestation.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(attestation.getCreePar().getRole().toString())
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
            if (decision.getEntreprise() != null) {
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
            table.addCell(decision.getReferenceAttestation());
            
            table.addCell("Montant");
            table.addCell(String.format("%.2f FCFA", decision.getMontant()));
            
            table.addCell("Compte Origine");
            table.addCell(decision.getCompteOrigine());
            
            table.addCell("Compte Destinataire");
            table.addCell(decision.getCompteDestinataire());
            
            table.addCell("Motif Prélèvement");
            table.addCell(decision.getMotifPrelevement());
            
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
                document.add(new Paragraph(decision.getCreePar().getPrenom() + " " + decision.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(decision.getCreePar().getRole().toString())
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
            if (ordre.getEntreprise() != null) {
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
            table.addCell(ordre.getReferenceDecisionPrelevement());
            
            table.addCell("Montant");
            table.addCell(String.format("%.2f FCFA", ordre.getMontant()));
            
            table.addCell("Description");
            table.addCell(ordre.getDescription());
            
            table.addCell("Compte Origine");
            table.addCell(ordre.getCompteOrigine());
            
            table.addCell("Compte Destinataire");
            table.addCell(ordre.getCompteDestinataire());
            
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
                document.add(new Paragraph(ordre.getCreePar().getPrenom() + " " + ordre.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(ordre.getCreePar().getRole().toString())
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
            table.addCell(ligne.getIntituleLigne());
            
            table.addCell("Description");
            table.addCell(ligne.getDescription());
            
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
                document.add(new Paragraph(ligne.getCreePar().getPrenom() + " " + ligne.getCreePar().getNom())
                        .setBold());
                document.add(new Paragraph(ligne.getCreePar().getRole().toString())
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
}