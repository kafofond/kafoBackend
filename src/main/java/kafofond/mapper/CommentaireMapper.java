package kafofond.mapper;

import kafofond.dto.CommentaireDTO;
import kafofond.dto.CommentaireSimplifieDTO;
import kafofond.entity.Commentaire;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class CommentaireMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMMM-yyyy, HH:mm", new Locale("fr"));

    /**
     * Convertit une entité Commentaire en DTO
     */
    public CommentaireDTO toDTO(Commentaire commentaire) {
        if (commentaire == null) {
            return null;
        }

        return CommentaireDTO.builder()
                .id(commentaire.getId())
                .contenu(commentaire.getContenu())
                .dateCreation(commentaire.getDateCreation())
                .dateCreationFormatee(commentaire.getDateCreation() != null
                        ? commentaire.getDateCreation().format(DATE_FORMATTER)
                        : null)
                .auteurNom(commentaire.getAuteur() != null
                        ? commentaire.getAuteur().getPrenom() + " " + commentaire.getAuteur().getNom()
                        : null)
                .build();
    }

    /**
     * Convertit une entité Commentaire en CommentaireSimplifieDTO
     */
    public CommentaireSimplifieDTO toSimplifieDTO(Commentaire commentaire) {
        if (commentaire == null) {
            return null;
        }

        return CommentaireSimplifieDTO.builder()
                .contenu(commentaire.getContenu())
                .dateCreation(commentaire.getDateCreation())
                .dateCreationFormatee(commentaire.getDateCreation() != null
                        ? commentaire.getDateCreation().format(DATE_FORMATTER)
                        : null)
                .auteurNom(commentaire.getAuteur() != null
                        ? commentaire.getAuteur().getPrenom() + " " + commentaire.getAuteur().getNom()
                        : null)
                .build();
    }

    /**
     * Convertit un DTO en entité Commentaire
     */
    public Commentaire toEntity(CommentaireDTO dto) {
        if (dto == null) {
            return null;
        }

        return Commentaire.builder()
                .id(dto.getId())
                .contenu(dto.getContenu())
                .dateCreation(dto.getDateCreation())
                .build();
    }
}