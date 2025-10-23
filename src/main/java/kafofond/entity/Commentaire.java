package kafofond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commentaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contenu;

    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur auteur;

    // Identifie le document commenté
    @Column(nullable = false)
    private Long documentId;

    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

}
