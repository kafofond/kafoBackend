package kafofond.mapper;

import kafofond.dto.TableValidationDTO;
import kafofond.entity.TableValidation;
import kafofond.entity.TypeDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour TableValidation (remplace CommentaireMapper)
 */
@Component
public class TableValidationMapper {

    public TableValidationDTO toDTO(TableValidation validation) {
        if (validation == null) return null;

        return TableValidationDTO.builder()
                .id(validation.getId())
                .validateurId(validation.getValidateurId())
                .validateurNomComplet(validation.getValidateur() != null ? 
                    validation.getValidateur().getPrenom() + " " + validation.getValidateur().getNom() : null)
                .validateurEmail(validation.getValidateur() != null ? 
                    validation.getValidateur().getEmail() : null)
                .commentaire(validation.getCommentaire())
                .statut(validation.getStatut())
                .idDocument(validation.getIdDocument())
                .typeDocument(validation.getTypeDocument() != null ? validation.getTypeDocument().name() : null)
                .dateValidation(validation.getDateValidation())
                .build();
    }

    public TableValidation toEntity(TableValidationDTO dto) {
        if (dto == null) return null;

        return TableValidation.builder()
                .id(dto.getId())
                .validateurId(dto.getValidateurId())
                .commentaire(dto.getCommentaire())
                .statut(dto.getStatut())
                .idDocument(dto.getIdDocument())
                .typeDocument(dto.getTypeDocument() != null ? TypeDocument.valueOf(dto.getTypeDocument()) : null)
                .dateValidation(dto.getDateValidation())
                .build();
    }

    public List<TableValidationDTO> toDTOList(List<TableValidation> validations) {
        if (validations == null) return null;
        return validations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TableValidation> toEntityList(List<TableValidationDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}