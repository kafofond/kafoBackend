package kafofond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireSimplifieDTO {
    private String contenu;
    private LocalDateTime dateCreation;
    private String dateCreationFormatee;
    private String auteurNom;
}