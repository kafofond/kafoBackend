package kafofond.mapper;

import kafofond.dto.SeuilValidationDTO;
import kafofond.entity.SeuilValidation;
import org.springframework.stereotype.Component;

@Component
public class SeuilValidationMapper {

    public SeuilValidationDTO toDTO(SeuilValidation seuil) {
        if (seuil == null) return null;

        return SeuilValidationDTO.builder()
                .id(seuil.getId())
                .montantSeuil(seuil.getMontantSeuil())
                .dateCreation(seuil.getDateCreation())
                .actif(seuil.isActif())
                .entrepriseNom(seuil.getEntreprise() != null ? seuil.getEntreprise().getNom() : null)
                .build();
    }

    public SeuilValidation toEntity(SeuilValidationDTO dto) {
        if (dto == null) return null;

        return SeuilValidation.builder()
                .id(dto.getId())
                .montantSeuil(dto.getMontantSeuil())
                .dateCreation(dto.getDateCreation())
                .actif(dto.isActif())
                .build();
    }
}
