package kafofond.service;

import kafofond.entity.Commentaire;
import kafofond.entity.TypeDocument;
import kafofond.entity.Utilisateur;
import kafofond.repository.CommentaireRepo;
import kafofond.repository.UtilisateurRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentaireService {

    private final CommentaireRepo commentaireRepo;
    private final UtilisateurRepo utilisateurRepo;
    private final HistoriqueService historiqueService;

    /**
     * Crée un nouveau commentaire pour un document
     */
    @Transactional
    public Commentaire creerCommentaire(Long documentId, TypeDocument typeDocument, String contenu, Utilisateur auteur) {
        log.info("Création d'un commentaire pour le document {} de type {} par {}", documentId, typeDocument, auteur.getEmail());

        Commentaire commentaire = Commentaire.builder()
                .documentId(documentId)
                .typeDocument(typeDocument)
                .contenu(contenu)
                .auteur(auteur)
                .dateCreation(LocalDateTime.now())
                .build();

        Commentaire commentaireCree = commentaireRepo.save(commentaire);

        // Historique
        historiqueService.enregistrerAction(
                "COMMENTAIRE",
                commentaireCree.getId(),
                "CREATION",
                auteur,
                null,
                null,
                contenu,
                null,
                null
        );

        return commentaireCree;
    }

    /**
     * Modifie un commentaire existant
     */
    @Transactional
    public Commentaire modifierCommentaire(Long id, String nouveauContenu, Utilisateur modificateur) {
        log.info("Modification du commentaire {} par {}", id, modificateur.getEmail());

        Commentaire commentaire = commentaireRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));

        // Vérification des droits
        if (!commentaire.getAuteur().getId().equals(modificateur.getId()) &&
                modificateur.getRole() != kafofond.entity.Role.ADMIN &&
                modificateur.getRole() != kafofond.entity.Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Vous ne pouvez modifier que vos propres commentaires");
        }

        String ancienContenu = commentaire.getContenu();
        commentaire.setContenu(nouveauContenu);

        Commentaire commentaireModifie = commentaireRepo.save(commentaire);

        // Historique
        historiqueService.enregistrerAction(
                "COMMENTAIRE",
                id,
                "MODIFICATION",
                modificateur,
                ancienContenu,
                nouveauContenu,
                nouveauContenu,
                null,
                null
        );

        return commentaireModifie;
    }

    /**
     * Supprime un commentaire
     */
    @Transactional
    public void supprimerCommentaire(Long id, Utilisateur utilisateur) {
        log.info("Suppression du commentaire {} par {}", id, utilisateur.getEmail());

        Commentaire commentaire = commentaireRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));

        // Vérification des droits
        if (!commentaire.getAuteur().getId().equals(utilisateur.getId()) &&
                utilisateur.getRole() != kafofond.entity.Role.ADMIN &&
                utilisateur.getRole() != kafofond.entity.Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Vous ne pouvez supprimer que vos propres commentaires");
        }

        commentaireRepo.delete(commentaire);

        // Historique
        historiqueService.enregistrerAction(
                "COMMENTAIRE",
                id,
                "SUPPRESSION",
                utilisateur,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Liste les commentaires pour un document donné
     */
    public List<Commentaire> getCommentairesByDocument(Long documentId, TypeDocument typeDocument) {
        log.info("Liste des commentaires pour le document {} de type {}", documentId, typeDocument);
        return commentaireRepo.findByDocumentIdAndTypeDocument(documentId, typeDocument);
    }

    /**
     * Trouve un commentaire par ID
     */
    public Optional<Commentaire> trouverParId(Long id) {
        return commentaireRepo.findById(id);
    }

    /**
     * Liste les commentaires par auteur
     */
    public List<Commentaire> getCommentairesByAuteur(Utilisateur auteur) {
        return commentaireRepo.findByAuteur(auteur);
    }
}
