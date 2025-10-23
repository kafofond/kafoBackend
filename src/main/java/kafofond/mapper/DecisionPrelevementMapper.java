package kafofond.mapper;

import kafofond.dto.DecisionPrelevementDTO;
import kafofond.entity.DecisionDePrelevement;
import org.springframework.stereotype.Component;

/**
 * Mapper pour l'entité DecisionDePrelevement
 * Convertit entre entité et DTO
 */
@Component
public class DecisionPrelevementMapper {

    /**
     * Convertit une entité DecisionDePrelevement en DTO
     */
    public DecisionPrelevementDTO toDTO(DecisionDePrelevement decision) {
        if (decision == null) {
            return null;
        }
        
        return DecisionPrelevementDTO.builder()
                .id(decision.getId())
                .code(decision.getCode())
                .montant(decision.getMontant())
                .compteOrigine(decision.getCompteOrigine())
                .compteDestinataire(decision.getCompteDestinataire())
                .motifPrelevement(decision.getMotifPrelevement())
                .dateCreation(decision.getDateCreation())
                .dateModification(decision.getDateModification())
                .statut(decision.getStatut())
                .createurNom(decision.getCreePar() != null ? 
                    decision.getCreePar().getPrenom() + " " + decision.getCreePar().getNom() : null)
                .createurEmail(decision.getCreePar() != null ? decision.getCreePar().getEmail() : null)
                .entrepriseNom(decision.getEntreprise() != null ? decision.getEntreprise().getNom() : null)
                .attestationId(decision.getAttestationDeServiceFait() != null ? 
                    decision.getAttestationDeServiceFait().getId() : null)
                .referenceAttestation(decision.getAttestationDeServiceFait() != null ? 
                    decision.getAttestationDeServiceFait().getCode() : null)
                .build();
    }

    /**
     * Convertit un DTO en entité DecisionDePrelevement
     */
    public DecisionDePrelevement toEntity(DecisionPrelevementDTO decisionDTO) {
        if (decisionDTO == null) {
            return null;
        }
        
        return DecisionDePrelevement.builder()
                .id(decisionDTO.getId())
                .code(decisionDTO.getCode())
                .referenceAttestation(decisionDTO.getReferenceAttestation())
                .montant(decisionDTO.getMontant())
                .compteOrigine(decisionDTO.getCompteOrigine())
                .compteDestinataire(decisionDTO.getCompteDestinataire())
                .motifPrelevement(decisionDTO.getMotifPrelevement())
                .dateCreation(decisionDTO.getDateCreation())
                .dateModification(decisionDTO.getDateModification())
                .statut(decisionDTO.getStatut())
                .build();
    }
}