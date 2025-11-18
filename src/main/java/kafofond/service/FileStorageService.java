package kafofond.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;

@Service
public class FileStorageService {

    // 💡 Définissez ce répertoire dans votre application.properties/yml :
    // file.upload-dir=./uploads
    @Value("${file.upload-dir}")
    private String uploadDir;

    // Extensions autorisées
    private final List<String> allowedExtensions = Arrays.asList(
            "pdf", "doc", "docx", "jpg", "jpeg", "png", "xlsx", "xls"
    );

    // Taille maximale autorisée (5 Mo)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Crée le répertoire s'il n'existe pas
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire d'upload : " + uploadDir, ex);
        }
    }

    /**
     * Stocke le fichier sur le disque et renvoie l'URL d'accès (chemin relatif).
     */
    public String storeFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 1. Validation de la taille et de l'extension
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La taille du fichier ne doit pas dépasser 5 Mo.");
        }

        String fileExtension = getFileExtension(originalFileName);
        if (fileExtension == null || !allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Types permis : " + allowedExtensions);
        }

        // 2. Générer un nom de fichier unique pour éviter les conflits
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

        // 3. Copier le fichier binaire vers la destination
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 4. Renvoie le chemin d'accès (qui sera stocké dans la DB)
        // NOTE: Si vous utilisez un stockage cloud (S3, Azure Blob), c'est ici que l'URL publique sera retournée.
        return "/uploads/" + uniqueFileName; // Chemin relatif pour l'accès web
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return null;
    }
}