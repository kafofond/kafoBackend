package kafofond.service;

import kafofond.entity.Budget;
import kafofond.entity.DemandeDAchat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération de fichiers Excel
 * Génère les rapports Excel pour les documents
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    @Value("${reporting.output.path:reports/}")
    private String reportsPath;

    /**
     * Génère un fichier Excel pour un budget
     */
    public String genererBudgetExcel(Budget budget) throws IOException {
        log.info("Génération du fichier Excel pour le budget {}", budget.getId());
        
        // Créer le dossier s'il n'existe pas
        Path reportsDir = Paths.get(reportsPath);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }
        
        // Nom du fichier
        String fileName = String.format("budget_%d_%s.xlsx", 
                budget.getId(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        Path filePath = reportsDir.resolve(fileName);
        
        // Créer le workbook Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Budget");
            
            // Style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Créer les en-têtes
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Champ", "Valeur"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Remplir les données
            int rowNum = 1;
            String[][] data = {
                {"ID", budget.getId().toString()},
                {"Intitulé", budget.getIntituleBudget()},
                {"Description", budget.getDescription()},
                {"Montant", String.valueOf(budget.getMontantBudget()) + " FCFA"},
                {"Statut", budget.getStatut().name()},
                {"Date de création", budget.getDateCreation().toString()},
                {"Entreprise", budget.getEntreprise().getNom()},
                {"Créé par", budget.getCreePar().getPrenom() + " " + budget.getCreePar().getNom()}
            };
            
            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData[i]);
                }
            }
            
            // Ajuster la largeur des colonnes
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
            // Écrire le fichier
            try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
                workbook.write(fileOut);
            }
        }
        
        String urlExcel = "/api/rapports/budget/" + fileName;
        log.info("Fichier Excel généré : {}", urlExcel);
        
        return urlExcel;
    }

    /**
     * Génère un fichier Excel pour une demande d'achat
     */
    public String genererDemandeAchatExcel(DemandeDAchat demande) throws IOException {
        log.info("Génération du fichier Excel pour la demande d'achat {}", demande.getId());
        
        // Créer le dossier s'il n'existe pas
        Path reportsDir = Paths.get(reportsPath);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }
        
        // Nom du fichier
        String fileName = String.format("demande_achat_%d_%s.xlsx", 
                demande.getId(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        Path filePath = reportsDir.resolve(fileName);
        
        // Créer le workbook Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Demande d'Achat");
            
            // Style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Créer les en-têtes
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Champ", "Valeur"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Remplir les données
            int rowNum = 1;
            String[][] data = {
                {"ID", demande.getId().toString()},
                {"Référence besoin", demande.getReferenceBesoin()},
                {"Description", demande.getDescription()},
                {"Fournisseur", demande.getFournisseur()},
                {"Montant total", String.valueOf(demande.getMontantTotal()) + " FCFA"},
                {"Service bénéficiaire", demande.getServiceBeneficiaire()},
                {"Date de création", demande.getDateCreation().toString()},
                {"Date attendue", demande.getDateAttendu().toString()},
                {"Statut", demande.getStatut().name()},
                {"Entreprise", demande.getEntreprise().getNom()},
                {"Créé par", demande.getCreePar().getPrenom() + " " + demande.getCreePar().getNom()}
            };
            
            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData[i]);
                }
            }
            
            // Ajuster la largeur des colonnes
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
            // Écrire le fichier
            try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
                workbook.write(fileOut);
            }
        }
        
        String urlExcel = "/api/rapports/demande-achat/" + fileName;
        log.info("Fichier Excel généré : {}", urlExcel);
        
        return urlExcel;
    }
}
