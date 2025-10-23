package kafofond.mapper;

import kafofond.dto.DesignationDTO;
import kafofond.entity.Designation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour Designation
 */
@Component
public class DesignationMapper {

    public DesignationDTO toDTO(Designation designation) {
        if (designation == null) return null;

        return DesignationDTO.builder()
                .id(designation.getId())
                .produit(designation.getProduit())
                .quantite(designation.getQuantite())
                .prixUnitaire(designation.getPrixUnitaire())
                .montantTotal(designation.getMontantTotal())
                .date(designation.getDate())
                .ficheBesoinId(designation.getFicheDeBesoin() != null ? designation.getFicheDeBesoin().getId() : null)
                .build();
    }

    public Designation toEntity(DesignationDTO dto) {
        if (dto == null) return null;

        return Designation.builder()
                .id(dto.getId())
                .produit(dto.getProduit())
                .quantite(dto.getQuantite())
                .prixUnitaire(dto.getPrixUnitaire())
                .montantTotal(dto.getMontantTotal())
                .date(dto.getDate())
                .build();
    }

    public List<DesignationDTO> toDTOList(List<Designation> designations) {
        if (designations == null) return null;
        return designations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Designation> toEntityList(List<DesignationDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
