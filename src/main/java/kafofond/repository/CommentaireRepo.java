package kafofond.repository;

import kafofond.entity.Commentaire;
import kafofond.entity.TypeDocument;
import kafofond.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepo extends JpaRepository<Commentaire, Long> {

    // Tous les commentaires pour un document donné (par ID et type)
    List<Commentaire> findByDocumentIdAndTypeDocument(Long documentId, TypeDocument typeDocument);

    // Tous les commentaires par auteur
    List<Commentaire> findByAuteur(Utilisateur auteur);

}
