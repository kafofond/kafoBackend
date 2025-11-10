package kafofond.mapper;

import kafofond.dto.CommentaireDTO;
import kafofond.dto.LigneCreditDTO;
import kafofond.entity.Commentaire;
import kafofond.entity.LigneCredit;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LigneCreditMapper {

    public LigneCreditDTO toDTO(LigneCredit ligne, List<Commentaire> commentaires) {
        List<CommentaireDTO> commentaireDTOs = commentaires.stream()
                .map(c -> CommentaireDTO.builder()
                        .id(c.getId())
                        .contenu(c.getContenu())
                        .dateCreation(c.getDateCreation())
                        .auteurNom(c.getAuteur() != null ? c.getAuteur().getPrenom() + " " + c.getAuteur().getNom() : null)
                        .auteurEmail(c.getAuteur() != null ? c.getAuteur().getEmail() : null)
                        .build())
                .collect(Collectors.toList());

        return LigneCreditDTO.builder()
                .id(ligne.getId())
                .code(ligne.getCode())
                .intituleLigne(ligne.getIntituleLigne())
                .description(ligne.getDescription())
                .montantAllouer(ligne.getMontantAllouer())
                .montantEngager(ligne.getMontantEngager())
                .montantRestant(ligne.getMontantRestant())
                .dateCreation(ligne.getDateCreation().toLocalDate())
                .dateModification(ligne.getDateModification())
                .dateDebut(ligne.getDateDebut())
                .dateFin(ligne.getDateFin())
                .statut(ligne.getStatut())
                .actif(ligne.isEtat())
                .budgetId(ligne.getBudget() != null ? ligne.getBudget().getId() : null)
                .createurNom(ligne.getCreePar() != null ? ligne.getCreePar().getPrenom() + " " + ligne.getCreePar().getNom() : null)
                .createurEmail(ligne.getCreePar() != null ? ligne.getCreePar().getEmail() : null)
                .entrepriseNom(ligne.getBudget() != null && ligne.getBudget().getEntreprise() != null ? ligne.getBudget().getEntreprise().getNom() : null)
                .commentaires(commentaireDTOs)
                .build();
    }

    public LigneCredit toEntity(LigneCreditDTO dto) {
        if (dto == null) return null;

        LigneCredit.LigneCreditBuilder builder = LigneCredit.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .intituleLigne(dto.getIntituleLigne())
                .description(dto.getDescription())
                .montantAllouer(dto.getMontantAllouer())
                .montantEngager(dto.getMontantEngager())
                .montantRestant(dto.getMontantRestant())
                .dateCreation(dto.getDateCreation().atStartOfDay())
                .dateModification(dto.getDateModification())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .statut(dto.getStatut())
                .etat(dto.isActif());
        
        // Si un budgetId est fourni, créer un objet Budget avec cet ID
        if (dto.getBudgetId() != null) {
            kafofond.entity.Budget budget = kafofond.entity.Budget.builder()
                    .id(dto.getBudgetId())
                    .build();
            builder.budget(budget);
        }
        
        return builder.build();
    }
}