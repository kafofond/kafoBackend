package kafofond.mapper;

import kafofond.dto.BonDeCommandeDTO;
import kafofond.entity.BonDeCommande;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper pour l'entité BonDeCommande
 * Convertit entre entité et DTO
 */
@Component
public class BonDeCommandeMapper {

    /**
     * Convertit une entité BonDeCommande en DTO
     * Cette méthode doit être appelée dans un contexte où les relations sont initialisées
     */
    public BonDeCommandeDTO toDTO(BonDeCommande bon) {
        if (bon == null) {
            return null;
        }
        
        BonDeCommandeDTO.BonDeCommandeDTOBuilder builder = BonDeCommandeDTO.builder()
                .id(bon.getId())
                .code(bon.getCode())
                .fournisseur(bon.getFournisseur())
                .description(bon.getDescription())
                // Suppression des champs quantite et prixUnitaire inutiles
                .montantTotal(bon.getMontantTotal())
                .serviceBeneficiaire(bon.getServiceBeneficiaire())
                .modePaiement(bon.getModePaiement())
                .dateCreation(LocalDate.from(bon.getDateCreation()))
                .delaiPaiement(bon.getDelaiPaiement())
                .dateExecution(bon.getDateExecution())
                .statut(bon.getStatut())
                .urlPdf(bon.getUrlPdf())
                .demandeAchatId(bon.getDemandeDAchat() != null ? bon.getDemandeDAchat().getId() : null);
        
        // Gérer le créateur de manière sécurisée
        if (bon.getCreePar() != null) {
            builder.createurNom(bon.getCreePar().getPrenom() + " " + bon.getCreePar().getNom());
            builder.createurEmail(bon.getCreePar().getEmail());
        }
        
        // Gérer l'entreprise de manière sécurisée
        if (bon.getEntreprise() != null) {
            builder.entrepriseNom(bon.getEntreprise().getNom());
        }
        
        return builder.build();
    }

    /**
     * Convertit un DTO en entité BonDeCommande
     */
    public BonDeCommande toEntity(BonDeCommandeDTO bonDTO) {
        if (bonDTO == null) {
            return null;
        }
        
        return BonDeCommande.builder()
                .id(bonDTO.getId())
                .code(bonDTO.getCode())
                .fournisseur(bonDTO.getFournisseur())
                .description(bonDTO.getDescription())
                // Suppression des champs quantite et prixUnitaire inutiles
                .montantTotal(bonDTO.getMontantTotal())
                .serviceBeneficiaire(bonDTO.getServiceBeneficiaire())
                .modePaiement(bonDTO.getModePaiement())
                .dateCreation(bonDTO.getDateCreation().atStartOfDay())
                .delaiPaiement(bonDTO.getDelaiPaiement())
                .dateExecution(bonDTO.getDateExecution())
                .statut(bonDTO.getStatut())
                .urlPdf(bonDTO.getUrlPdf())
                .build();
    }
}