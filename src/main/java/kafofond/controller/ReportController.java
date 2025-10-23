package kafofond.controller;

import kafofond.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rapports", description = "Endpoints pour accéder aux rapports générés")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final DocumentService documentService;

    @GetMapping("/bon-commande/{filename:.+}")
    @Operation(summary = "Télécharger un rapport de bon de commande")
    public ResponseEntity<Resource> getBonCommandeReport(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("reports").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'accès au rapport: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/budget/{filename:.+}")
    @Operation(summary = "Télécharger un rapport de budget")
    public ResponseEntity<Resource> getBudgetReport(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("reports").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'accès au rapport: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
