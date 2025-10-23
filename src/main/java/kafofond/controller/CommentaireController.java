package kafofond.controller;

import kafofond.dto.CommentaireDTO;
import kafofond.dto.CommentaireSimplifieDTO;
import kafofond.entity.Commentaire;
import kafofond.entity.TypeDocument;
import kafofond.entity.Utilisateur;
import kafofond.mapper.CommentaireMapper;
import kafofond.service.CommentaireService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des commentaires
 */
@RestController
@RequestMapping("/api/commentaires")
@RequiredArgsConstructor
@Slf4j
public class CommentaireController {

    private final CommentaireService commentaireService;
    private final UtilisateurService utilisateurService;
    private final CommentaireMapper commentaireMapper;

    /**
     * Crée un nouveau commentaire
     */
    @PostMapping
    public ResponseEntity<?> creerCommentaire(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            log.info("Création d'un commentaire par {}", authentication.getName());

            Long documentId = Long.valueOf(request.get("documentId"));
            TypeDocument typeDocument = TypeDocument.valueOf(request.get("typeDocument"));
            String contenu = request.get("contenu");

            Utilisateur auteur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Commentaire commentaire = commentaireService.creerCommentaire(documentId, typeDocument, contenu, auteur);
            CommentaireSimplifieDTO dto = commentaireMapper.toSimplifieDTO(commentaire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Commentaire créé avec succès");
            response.put("commentaire", dto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création du commentaire : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifie un commentaire existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierCommentaire(@PathVariable Long id, @RequestBody Map<String, String> request,
                                               Authentication authentication) {
        try {
            log.info("Modification du commentaire {} par {}", id, authentication.getName());

            String contenu = request.get("contenu");

            Utilisateur modificateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Commentaire commentaire = commentaireService.modifierCommentaire(id, contenu, modificateur);
            CommentaireSimplifieDTO dto = commentaireMapper.toSimplifieDTO(commentaire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Commentaire modifié avec succès");
            response.put("commentaire", dto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la modification du commentaire : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Supprime un commentaire
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerCommentaire(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Suppression du commentaire {} par {}", id, authentication.getName());

            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            commentaireService.supprimerCommentaire(id, utilisateur);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Commentaire supprimé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la suppression du commentaire : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste les commentaires pour un document donné
     */
    @GetMapping
    public ResponseEntity<?> listerCommentaires(@RequestParam Long documentId,
                                              @RequestParam TypeDocument typeDocument,
                                              Authentication authentication) {
        try {
            log.info("Liste des commentaires pour le document {} de type {} demandée par {}",
                    documentId, typeDocument, authentication.getName());

            List<CommentaireSimplifieDTO> commentaires = commentaireService.getCommentairesByDocument(documentId, typeDocument)
                    .stream()
                    .map(commentaireMapper::toSimplifieDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("commentaires", commentaires);
            response.put("total", commentaires.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des commentaires : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Trouve un commentaire par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> trouverCommentaire(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Recherche du commentaire {} demandée par {}", id, authentication.getName());

            return commentaireService.trouverParId(id)
                    .map(commentaire -> {
                        CommentaireSimplifieDTO dto = commentaireMapper.toSimplifieDTO(commentaire);
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Erreur lors de la recherche du commentaire : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}