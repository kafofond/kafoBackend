package kafofond.controller;

import kafofond.service.FileStorageService; // Importez votre service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;

/**
 * Controller pour la gestion de l'upload de fichiers
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Gère l'upload de fichiers (MultipartFile) pour tout document nécessitant une pièce jointe.
     * Accessible à tous les rôles qui peuvent créer ou modifier des documents.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_TRESORERIE', 'ROLE_GESTIONNAIRE', 'ROLE_COMPTABLE')")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        // Le nom du paramètre ("file") doit correspondre à celui utilisé dans le Flutter MultipartRequest.

        // 1. Validation de base du fichier (non vide)
        if (file.isEmpty()) {
            log.error("Tentative d'upload d'un fichier vide.");
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier ne peut pas être vide."));
        }

        try {
            // 2. Déléguer la logique de stockage au service
            String fileUrl = fileStorageService.storeFile(file);

            // 3. Réponse succès
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl); // URL à stocker dans ficheDeBesoin.urlFichierJoint
            response.put("fileName", file.getOriginalFilename());
            response.put("message", "Fichier uploadé avec succès.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Utilisé pour les erreurs de validation spécifiques (ex: type de fichier non autorisé)
            log.error("Erreur de validation de fichier: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Erreur générique de stockage (ex: problème de disque)
            log.error("Erreur lors de l'upload du fichier : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Échec de l'upload: " + e.getMessage()));
        }
    }
}
