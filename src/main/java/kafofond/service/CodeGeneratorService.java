package kafofond.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération de codes uniques pour les documents
 * 
 * Format des codes : [PREFIXE]-[NUMERO]-[MOIS]-[ANNEE]
 * 
 * Exemples :
 * - Budget : BUD-0001-11-2025
 * - Ligne de crédit : LC-0035-11-2025
 * - Fiche de besoin : FB-0035-11-2025
 * - Demande d'achat : DA-0042-11-2025
 * - Bon de commande : BC-0018-11-2025
 * - Attestation de service fait : ASF-0018-11-2025
 * - Décision de prélèvement : DP-0012-11-2025
 * - Ordre de paiement : OP-0012-11-2025
 * 
 * Le numéro est incrémenté automatiquement pour chaque type de document
 */
@Service
@Slf4j
public class CodeGeneratorService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Génère un code unique pour un document
     * 
     * @param prefix Préfixe du code (BUD, LC, FB, DA, BC, ASF, DP, OP)
     * @param sequenceNumber Numéro de séquence (généré par la base de données)
     * @param date Date de création du document
     * @return Code formaté (ex: FB-0035-11-2025)
     */
    public String generateCode(String prefix, Long sequenceNumber, LocalDate date) {
        if (sequenceNumber == null || date == null) {
            log.warn("Impossible de générer le code : sequenceNumber ou date null");
            return null;
        }

        String month = date.format(MONTH_FORMATTER);
        String year = date.format(YEAR_FORMATTER);
        String formattedNumber = String.format("%04d", sequenceNumber);

        String code = String.format("%s-%s-%s-%s", prefix, formattedNumber, month, year);
        
        log.debug("Code généré : {}", code);
        return code;
    }

    /**
     * Génère un code pour un Budget
     * Format: BUD-0001-11-2025
     */
    public String generateBudgetCode(Long id, LocalDate dateCreation) {
        return generateCode("BUD", id, dateCreation);
    }

    /**
     * Génère un code pour une Ligne de Crédit
     * Format: LC-0035-11-2025
     */
    public String generateLigneCreditCode(Long id, LocalDate dateCreation) {
        return generateCode("LC", id, dateCreation);
    }

    /**
     * Génère un code pour une Fiche de Besoin
     * Format: FB-0035-11-2025
     */
    public String generateFicheBesoinCode(Long id, LocalDate dateCreation) {
        return generateCode("FB", id, dateCreation);
    }

    /**
     * Génère un code pour une Demande d'Achat
     * Format: DA-0042-11-2025
     */
    public String generateDemandeAchatCode(Long id, LocalDate dateCreation) {
        return generateCode("DA", id, dateCreation);
    }

    /**
     * Génère un code pour un Bon de Commande
     * Format: BC-0018-11-2025
     */
    public String generateBonCommandeCode(Long id, LocalDate dateCreation) {
        return generateCode("BC", id, dateCreation);
    }

    /**
     * Génère un code pour une Attestation de Service Fait
     * Format: ASF-0018-11-2025
     */
    public String generateAttestationServiceFaitCode(Long id, LocalDate dateCreation) {
        return generateCode("ASF", id, dateCreation);
    }

    /**
     * Génère un code pour une Décision de Prélèvement
     * Format: DP-0012-11-2025
     */
    public String generateDecisionPrelevementCode(Long id, LocalDate dateCreation) {
        return generateCode("DP", id, dateCreation);
    }

    /**
     * Génère un code pour un Ordre de Paiement
     * Format: OP-0012-11-2025
     */
    public String generateOrdrePaiementCode(Long id, LocalDate dateCreation) {
        return generateCode("OP", id, dateCreation);
    }

    /**
     * Extrait l'ID depuis un code
     * Utile pour rechercher un document par son code
     * 
     * @param code Code du document (ex: FB-0035-11-2025)
     * @return ID extrait (ex: 35)
     */
    public Long extractIdFromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        try {
            String[] parts = code.split("-");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException e) {
            log.error("Impossible d'extraire l'ID du code : {}", code, e);
        }

        return null;
    }

    /**
     * Extrait le préfixe depuis un code
     * 
     * @param code Code du document (ex: FB-0035-11-2025)
     * @return Préfixe (ex: FB)
     */
    public String extractPrefixFromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        String[] parts = code.split("-");
        return parts.length > 0 ? parts[0] : null;
    }

    /**
     * Valide un code
     * 
     * @param code Code à valider
     * @return true si le code est valide, false sinon
     */
    public boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }

        String[] parts = code.split("-");
        
        // Format attendu : PREFIX-NNNN-MM-YYYY (4 parties)
        if (parts.length != 4) {
            return false;
        }

        // Vérifier que le numéro est numérique et de 4 chiffres
        if (!parts[1].matches("\\d{4}")) {
            return false;
        }

        // Vérifier que le mois est numérique et entre 01-12
        if (!parts[2].matches("(0[1-9]|1[0-2])")) {
            return false;
        }

        // Vérifier que l'année est numérique et de 4 chiffres
        if (!parts[3].matches("\\d{4}")) {
            return false;
        }

        return true;
    }
}
