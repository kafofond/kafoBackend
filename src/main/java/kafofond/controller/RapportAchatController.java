package kafofond.controller;

import kafofond.dto.RapportAchatDTO;
import kafofond.entity.RapportAchat;
import kafofond.entity.Utilisateur;
import kafofond.mapper.RapportAchatMapper;
import kafofond.service.RapportAchatService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller pour RapportAchat (anciennement PieceJustificativeController)
 */
@RestController
@RequestMapping("/api/rapports-achat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rapports d'Achat", description = "Gestion des rapports d'achat (pièces justificatives)")
@SecurityRequirement(name = "bearerAuth")
public class RapportAchatController {

    private final RapportAchatService rapportService;
    private final UtilisateurService utilisateurService;
    private final RapportAchatMapper mapper;

    @PostMapping
    @Operation(summary = "Créer un rapport d'achat")
    public ResponseEntity<?> creer(@RequestBody RapportAchatDTO dto, Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            RapportAchat rapport = mapper.toEntity(dto);
            RapportAchat rapportCree = rapportService.creer(rapport, user);
            return ResponseEntity.ok(mapper.toDTO(rapportCree));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping
    @Operation(summary = "Lister tous les rapports d'achat d'une entreprise")
    public ResponseEntity<?> lister(Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<RapportAchatDTO> list = rapportService.listerParEntreprise(user.getEntreprise())
                    .stream().map(mapper::toDTO).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", list.size());
            response.put("rapports", list);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un rapport d'achat")
    public ResponseEntity<?> obtenir(@PathVariable Long id, Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Optional<RapportAchat> rapport = rapportService.trouverParId(id);
            
            // Vérifier que le rapport appartient à l'entreprise de l'utilisateur
            if (rapport.isEmpty() || !rapport.get().getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(mapper.toDTO(rapport.get()));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/document")
    @Operation(summary = "Lister les rapports par document")
    public ResponseEntity<?> listerParDocument(@RequestParam String document, Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Filtrer les rapports par entreprise de l'utilisateur
            List<RapportAchatDTO> list = rapportService.listerParDocumentEtEntreprise(document, user.getEntreprise())
                    .stream().map(mapper::toDTO).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", list.size());
            response.put("rapports", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}