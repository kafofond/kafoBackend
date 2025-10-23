package kafofond.mapper;

import kafofond.dto.OrdreDePaiementDTO;
import kafofond.entity.OrdreDePaiement;
import org.springframework.stereotype.Component;

/**
 * Mapper pour l'entité OrdreDePaiement
 * Convertit entre entité et DTO
 */
@Component
public class OrdreDePaiementMapper {

    /**
     * Convertit une entité OrdreDePaiement en DTO
     */
    public OrdreDePaiementDTO toDTO(OrdreDePaiement ordre) {
        if (ordre == null) {
            return null;
        }
        
        return OrdreDePaiementDTO.builder()
                .id(ordre.getId())
                .code(ordre.getCode())
                .referenceDecisionPrelevement(ordre.getReferenceDecisionPrelevement())
                .montant(ordre.getMontant())
                .description(ordre.getDescription())
                .compteOrigine(ordre.getCompteOrigine())
                .compteDestinataire(ordre.getCompteDestinataire())
                .dateExecution(ordre.getDateExecution())
                .dateCreation(ordre.getDateCreation())
                .dateModification(ordre.getDateModification())
                .statut(ordre.getStatut())
                .createurNom(ordre.getCreePar() != null ? 
                    ordre.getCreePar().getPrenom() + " " + ordre.getCreePar().getNom() : null)
                .createurEmail(ordre.getCreePar() != null ? ordre.getCreePar().getEmail() : null)
                .entrepriseNom(ordre.getEntreprise() != null ? ordre.getEntreprise().getNom() : null)
                .decisionId(ordre.getDecisionDePrelevement() != null ? ordre.getDecisionDePrelevement().getId() : null)
                .build();
    }

    /**
     * Convertit un DTO en entité OrdreDePaiement
     */
    public OrdreDePaiement toEntity(OrdreDePaiementDTO ordreDTO) {
        if (ordreDTO == null) {
            return null;
        }
        
        return OrdreDePaiement.builder()
                .id(ordreDTO.getId())
                .code(ordreDTO.getCode())
                .referenceDecisionPrelevement(ordreDTO.getReferenceDecisionPrelevement())
                .montant(ordreDTO.getMontant())
                .description(ordreDTO.getDescription())
                .compteOrigine(ordreDTO.getCompteOrigine())
                .compteDestinataire(ordreDTO.getCompteDestinataire())
                .dateExecution(ordreDTO.getDateExecution())
                .dateCreation(ordreDTO.getDateCreation())
                .dateModification(ordreDTO.getDateModification())
                .statut(ordreDTO.getStatut())
                .build();
    }
}