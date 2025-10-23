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
public class CommentaireDTO {
    private Long id;
    private String contenu;
    private LocalDateTime dateCreation;
    private String dateCreationFormatee;
    private String auteurNom;
    private String auteurEmail;
}
