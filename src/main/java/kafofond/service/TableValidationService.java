package kafofond.service;

import kafofond.entity.TableValidation;
import kafofond.entity.TypeDocument;
import kafofond.entity.Utilisateur;
import kafofond.entity.Entreprise;
import kafofond.repository.TableValidationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion de la table de validation
 * Enregistre toutes les validations/rejets/approbations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TableValidationService {

    private final TableValidationRepo tableValidationRepo;

    /**
     * Enregistre une validation (VALIDE, REJETE, APPROUVE, etc.)
     */
    @Transactional
    public TableValidation enregistrerValidation(
            Long idDocument,
            TypeDocument typeDocument,
            Utilisateur validateur,
            String statut,
            String commentaire
    ) {
        log.info("Enregistrement de validation {} pour {} #{} par {}",
                statut, typeDocument, idDocument, validateur.getEmail());

        TableValidation validation = TableValidation.builder()
                .validateurId(validateur.getId())
                .commentaire(commentaire)
                .statut(statut)
                .idDocument(idDocument)
                .typeDocument(typeDocument)
                .dateValidation(LocalDateTime.now())
                .build();

        return tableValidationRepo.save(validation);
    }

    /**
     * Enregistre la création d'un document
     */
    @Transactional
    public TableValidation enregistrerCreation(
            Long idDocument,
            TypeDocument typeDocument,
            Utilisateur createur
    ) {
        log.info("Enregistrement de la création du {} #{} par {}",
                typeDocument, idDocument, createur.getEmail());

        TableValidation validation = TableValidation.builder()
                .validateurId(createur.getId())
                .statut("CREATION")
                .idDocument(idDocument)
                .typeDocument(typeDocument)
                .dateValidation(LocalDateTime.now())
                .build();

        return tableValidationRepo.save(validation);
    }

    /**
     * Récupère toutes les validations d'un document
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsDocument(Long idDocument, TypeDocument typeDocument) {
        log.info("Consultation des validations pour {} #{}", typeDocument, idDocument);
        return tableValidationRepo.findByIdDocumentAndTypeDocument(idDocument, typeDocument);
    }

    /**
     * Récupère toutes les validations d'un validateur
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsValidateur(Long validateurId) {
        log.info("Consultation des validations du validateur #{}", validateurId);
        return tableValidationRepo.findByValidateurId(validateurId);
    }

    /**
     * Récupère toutes les validations par type de document
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsParType(TypeDocument typeDocument) {
        log.info("Consultation des validations pour le type {}", typeDocument);
        return tableValidationRepo.findByTypeDocument(typeDocument);
    }

    /**
     * Récupère toutes les validations par statut
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsParStatut(String statut) {
        log.info("Consultation des validations avec le statut {}", statut);
        return tableValidationRepo.findByStatut(statut);
    }

    /**
     * Récupère toutes les validations d'une entreprise
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsEntreprise(Entreprise entreprise) {
        log.info("Consultation des validations pour l'entreprise {}", entreprise.getNom());
        return tableValidationRepo.findByEntreprise(entreprise);
    }

    /**
     * Récupère toutes les validations d'une entreprise par son ID
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsEntrepriseParId(Long entrepriseId) {
        log.info("Consultation des validations pour l'entreprise #{}", entrepriseId);
        return tableValidationRepo.findByEntrepriseId(entrepriseId);
    }

    /**
     * Récupère toutes les validations d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<TableValidation> consulterValidationsUtilisateur(Long utilisateurId) {
        log.info("Consultation des validations de l'utilisateur #{}", utilisateurId);
        return tableValidationRepo.findByUtilisateurId(utilisateurId);
    }
}