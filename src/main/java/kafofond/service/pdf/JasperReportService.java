package kafofond.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service générique pour la génération de PDF avec JasperReports
 * 
 * ✅ UTILISATION :
 * 
 * 1. Créer un fichier .jrxml dans src/main/resources/reports/
 * 2. Le compiler en .jasper (ou laisser le service le faire automatiquement)
 * 3. Appeler generatePdf() avec :
 *    - templateName : nom du fichier sans extension (ex: "fiche_besoin")
 *    - data : liste d'objets à afficher dans le rapport
 *    - params : paramètres supplémentaires (titre, logo, etc.)
 * 
 * 📌 EXEMPLE D'UTILISATION :
 * 
 * <pre>
 * {@code
 * // 1. Préparer les données
 * List<FicheBesoin> fiches = Arrays.asList(fiche);
 * 
 * // 2. Préparer les paramètres
 * Map<String, Object> params = new HashMap<>();
 * params.put("TITRE", "Fiche de Besoin #" + fiche.getId());
 * params.put("ENTREPRISE", fiche.getEntreprise().getNom());
 * params.put("DATE_GENERATION", LocalDate.now().toString());
 * 
 * // 3. Générer le PDF
 * byte[] pdfBytes = jasperReportService.generatePdf("fiche_besoin", fiches, params);
 * }
 * </pre>
 * 
 * 📂 STRUCTURE DES FICHIERS :
 * 
 * src/main/resources/reports/
 *   ├── fiche_besoin.jrxml        (template source)
 *   ├── fiche_besoin.jasper       (compilé, optionnel)
 *   ├── demande_achat.jrxml
 *   ├── bon_commande.jrxml
 *   └── ... autres templates
 * 
 * 🎨 CRÉATION D'UN TEMPLATE JRXML :
 * 
 * Utiliser Jaspersoft Studio (gratuit) ou créer manuellement :
 * - Définir les champs du bean : <field name="objet" class="java.lang.String"/>
 * - Créer des sections : title, pageHeader, columnHeader, detail, pageFooter
 * - Ajouter des textFields : <textField><textFieldExpression>$F{objet}</textFieldExpression></textField>
 * 
 * ⚠️ NOTES IMPORTANTES :
 * - Les templates doivent être dans le classpath (src/main/resources/reports)
 * - Les noms de champs dans le .jrxml doivent correspondre aux getters des entités
 * - Utiliser JRBeanCollectionDataSource pour passer des listes d'objets Java
 * - Les paramètres sont préfixés par $P{NOM_PARAM} dans le template
 * - Les champs sont préfixés par $F{nomChamp} dans le template
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JasperReportService {

    /**
     * Génère un PDF à partir d'un template JasperReports
     * 
     * @param templateName Nom du template sans extension (ex: "fiche_besoin")
     * @param data Liste d'objets à afficher dans le rapport (peut être null si on utilise seulement des paramètres)
     * @param params Paramètres additionnels (titre, date, logo, etc.)
     * @return Tableau de bytes représentant le PDF généré
     * @throws Exception Si erreur lors de la génération
     */
    public byte[] generatePdf(String templateName, List<?> data, Map<String, Object> params) throws Exception {
        log.info("Génération du PDF avec le template: {}", templateName);

        // 1. Charger le template JRXML depuis le classpath
        String jrxmlPath = "reports/" + templateName + ".jrxml";
        InputStream jrxmlInputStream = new ClassPathResource(jrxmlPath).getInputStream();

        // 2. Compiler le template JRXML en JasperReport
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);

        // 3. Préparer les paramètres (ajouter des paramètres par défaut si nécessaire)
        Map<String, Object> parameters = params != null ? new HashMap<>(params) : new HashMap<>();
        
        // Ajouter des paramètres par défaut si non fournis
        parameters.putIfAbsent("REPORT_TITLE", "Document KafoFond");
        parameters.putIfAbsent("GENERATED_BY", "Système KafoFond");

        // 4. Créer la source de données
        JRBeanCollectionDataSource dataSource = data != null 
            ? new JRBeanCollectionDataSource(data) 
            : new JRBeanCollectionDataSource(List.of(new Object())); // Source vide si pas de données

        // 5. Remplir le rapport avec les données
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 6. Exporter le rapport en PDF
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, pdfOutputStream);

        log.info("PDF généré avec succès. Taille: {} bytes", pdfOutputStream.size());

        return pdfOutputStream.toByteArray();
    }

    /**
     * Génère un PDF à partir d'un template précompilé (.jasper)
     * Méthode alternative plus rapide si le template est déjà compilé
     * 
     * @param jasperPath Chemin vers le fichier .jasper compilé
     * @param data Liste d'objets pour le rapport
     * @param params Paramètres additionnels
     * @return Tableau de bytes représentant le PDF généré
     * @throws Exception Si erreur lors de la génération
     */
    public byte[] generatePdfFromCompiledTemplate(String jasperPath, List<?> data, Map<String, Object> params) throws Exception {
        log.info("Génération du PDF avec le template compilé: {}", jasperPath);

        // Charger le template compilé
        InputStream jasperInputStream = new ClassPathResource(jasperPath).getInputStream();

        // Préparer les paramètres
        Map<String, Object> parameters = params != null ? new HashMap<>(params) : new HashMap<>();
        parameters.putIfAbsent("REPORT_TITLE", "Document KafoFond");

        // Créer la source de données
        JRBeanCollectionDataSource dataSource = data != null 
            ? new JRBeanCollectionDataSource(data) 
            : new JRBeanCollectionDataSource(List.of(new Object()));

        // Remplir le rapport
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperInputStream, parameters, dataSource);

        // Exporter en PDF
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, pdfOutputStream);

        log.info("PDF généré avec succès depuis template compilé. Taille: {} bytes", pdfOutputStream.size());

        return pdfOutputStream.toByteArray();
    }

    /**
     * Génère un PDF sans données (seulement avec des paramètres)
     * Utile pour des documents statiques ou des formulaires vides
     * 
     * @param templateName Nom du template
     * @param params Paramètres du rapport
     * @return Tableau de bytes représentant le PDF
     * @throws Exception Si erreur lors de la génération
     */
    public byte[] generatePdfWithoutData(String templateName, Map<String, Object> params) throws Exception {
        return generatePdf(templateName, null, params);
    }
}
