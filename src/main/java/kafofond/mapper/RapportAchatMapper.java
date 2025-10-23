package kafofond.mapper;

import kafofond.dto.RapportAchatDTO;
import kafofond.entity.RapportAchat;
import org.springframework.stereotype.Component;

/**
 * Mapper pour RapportAchat (anciennement PieceJustificativeMapper)
 */
@Component
public class RapportAchatMapper {

    public RapportAchatDTO toDTO(RapportAchat rapport) {
        if (rapport == null) return null;

        return RapportAchatDTO.builder()
                .id(rapport.getId())
                .nom(rapport.getNom())
                .ficheBesoin(rapport.getFicheBesoin())
                .demandeAchat(rapport.getDemandeAchat())
                .bonCommande(rapport.getBonCommande())
                .attestationServiceFait(rapport.getAttestationServiceFait())
                .decisionPrelevement(rapport.getDecisionPrelevement())
                .ordrePaiement(rapport.getOrdrePaiement())
                .dateAjout(rapport.getDateAjout())
                .entrepriseNom(rapport.getEntreprise() != null ? rapport.getEntreprise().getNom() : null)
                .build();
    }

    public RapportAchat toEntity(RapportAchatDTO dto) {
        if (dto == null) return null;

        return RapportAchat.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .ficheBesoin(dto.getFicheBesoin())
                .demandeAchat(dto.getDemandeAchat())
                .bonCommande(dto.getBonCommande())
                .attestationServiceFait(dto.getAttestationServiceFait())
                .decisionPrelevement(dto.getDecisionPrelevement())
                .ordrePaiement(dto.getOrdrePaiement())
                .dateAjout(dto.getDateAjout())
                .build();
    }
}
