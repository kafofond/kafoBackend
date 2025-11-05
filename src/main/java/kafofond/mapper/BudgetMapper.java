package kafofond.mapper;

import kafofond.dto.BudgetDTO;
import kafofond.entity.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetDTO toDTO(Budget budget) {
        return BudgetDTO.fromEntity(budget);
    }

    public Budget toEntity(BudgetDTO dto) {
        if (dto == null) return null;

        return Budget.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .intituleBudget(dto.getIntituleBudget())
                .description(dto.getDescription())
                .montantBudget(dto.getMontantBudget())
                .dateCreation(dto.getDateCreation().atStartOfDay())
                .dateModification(dto.getDateModification())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .statut(dto.getStatut())
                .etat(dto.isActif())
                .build();
    }
}
