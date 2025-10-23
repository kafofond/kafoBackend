# ⚙️ CONFIGURATION JASPERREPORTS - KAFOFOND

## 📝 Configuration dans application.properties

Ajoutez ces configurations optionnelles pour optimiser JasperReports :

```properties
# ========================================
# Configuration JasperReports (Optionnel)
# ========================================

# Répertoire des templates JasperReports
jasper.reports.path=classpath:reports/

# Cache des templates compilés (améliore les performances)
jasper.reports.cache.enabled=true
jasper.reports.cache.size=50

# Timeout pour la génération de PDF (en secondes)
jasper.reports.timeout=30

# Format de date par défaut dans les rapports
jasper.reports.date.format=dd/MM/yyyy

# Encodage des caractères
jasper.reports.encoding=UTF-8

# Activer/Désactiver la génération de PDF
jasper.reports.enabled=true

# Niveau de log pour JasperReports
logging.level.net.sf.jasperreports=WARN
```

---

## 🔧 Configuration Bean Spring (Optionnel)

Si vous souhaitez personnaliser davantage, créez une classe de configuration :

**Fichier :** `src/main/java/kafofond/config/JasperReportsConfig.java`

```java
package kafofond.config;

import net.sf.jasperreports.engine.JasperReport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration pour JasperReports
 * Optimise les performances en cachant les templates compilés
 */
@Configuration
@EnableCaching
public class JasperReportsConfig {

    /**
     * Cache des templates JasperReports compilés
     * Évite de recompiler à chaque génération de PDF
     */
    @Bean
    public Map<String, JasperReport> jasperReportCache() {
        return new HashMap<>();
    }

    /**
     * Configuration des paramètres par défaut pour tous les rapports
     */
    @Bean
    public Map<String, Object> defaultJasperParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_LOCALE", new java.util.Locale("fr", "FR"));
        params.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone("Africa/Bamako"));
        params.put("IS_IGNORE_PAGINATION", Boolean.FALSE);
        return params;
    }
}
```

---

## 🚀 Service Optimisé avec Cache

Pour améliorer les performances, créez un service avec cache :

**Fichier :** `src/main/java/kafofond/service/pdf/CachedJasperReportService.java`

```java
package kafofond.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

/**
 * Service JasperReports avec cache des templates compilés
 * Améliore les performances en évitant de recompiler les templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CachedJasperReportService {

    /**
     * Compile et met en cache un template JasperReports
     * Le template compilé est réutilisé pour toutes les générations futures
     */
    @Cacheable(value = "jasperTemplates", key = "#templateName")
    public JasperReport getCompiledTemplate(String templateName) throws Exception {
        log.info("Compilation du template: {}", templateName);
        
        String jrxmlPath = "reports/" + templateName + ".jrxml";
        InputStream jrxmlInputStream = new ClassPathResource(jrxmlPath).getInputStream();
        
        JasperReport compiledReport = JasperCompileManager.compileReport(jrxmlInputStream);
        
        log.info("Template compilé et mis en cache: {}", templateName);
        return compiledReport;
    }
}
```

**Configuration du cache dans application.properties :**

```properties
# Configuration du cache Spring
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=100,expireAfterAccess=1h
spring.cache.cache-names=jasperTemplates
```

**Dépendance Maven pour le cache :**

```xml
<!-- Cache Caffeine (optionnel mais recommandé) -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

---

## 🎨 Configuration des Polices (Fonts)

Pour utiliser des polices personnalisées dans les PDFs :

### 1. Créer un fichier de configuration des polices

**Fichier :** `src/main/resources/jasperreports_extension.properties`

```properties
# Extension JasperReports pour les polices
net.sf.jasperreports.extension.registry.factory.fonts=net.sf.jasperreports.engine.fonts.SimpleFontExtensionRegistryFactory
net.sf.jasperreports.extension.simple.font.families.myfontfamily=fonts/fonts.xml
```

### 2. Définir les polices

**Fichier :** `src/main/resources/fonts/fonts.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<fontFamilies>
    <fontFamily name="DejaVu Sans">
        <normal>fonts/DejaVuSans.ttf</normal>
        <bold>fonts/DejaVuSans-Bold.ttf</bold>
        <italic>fonts/DejaVuSans-Oblique.ttf</italic>
        <boldItalic>fonts/DejaVuSans-BoldOblique.ttf</boldItalic>
        <pdfEncoding>Identity-H</pdfEncoding>
        <pdfEmbedded>true</pdfEmbedded>
    </fontFamily>
</fontFamilies>
```

### 3. Utiliser dans le template

```xml
<textField>
    <reportElement x="0" y="0" width="200" height="30"/>
    <textElement>
        <font fontName="DejaVu Sans" size="12" isBold="true"/>
    </textElement>
    <textFieldExpression><![CDATA[$F{objet}]]></textFieldExpression>
</textField>
```

---

## 🔒 Configuration de Sécurité

Pour restreindre la génération de PDF :

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            // Seuls certains rôles peuvent générer des PDFs
            .requestMatchers("/api/**/pdf").hasAnyRole("ADMIN", "COMPTABLE", "DIRECTEUR")
            // ...
        );
        return http.build();
    }
}
```

---

## 📊 Configuration des Logs

Pour déboguer JasperReports :

**application.properties :**

```properties
# Logs détaillés pour JasperReports (dev uniquement)
logging.level.net.sf.jasperreports=DEBUG
logging.level.kafofond.service.pdf=DEBUG

# Logs de performance
logging.level.net.sf.jasperreports.engine.fill=TRACE
```

---

## 🌍 Configuration Multilingue

Pour supporter plusieurs langues :

```java
// Dans le service
Map<String, Object> params = new HashMap<>();

// Français
params.put("REPORT_LOCALE", new Locale("fr", "FR"));
params.put("CURRENCY_SYMBOL", "FCFA");

// Anglais
params.put("REPORT_LOCALE", new Locale("en", "US"));
params.put("CURRENCY_SYMBOL", "$");
```

**Dans le template :**

```xml
<parameter name="REPORT_LOCALE" class="java.util.Locale"/>
<parameter name="CURRENCY_SYMBOL" class="java.lang.String"/>

<textField>
    <textFieldExpression>
        <![CDATA[$F{montant} + " " + $P{CURRENCY_SYMBOL}]]>
    </textFieldExpression>
</textField>
```

---

## 💾 Configuration du Stockage des PDFs

Pour sauvegarder les PDFs générés :

```properties
# Répertoire de sauvegarde des PDFs
pdf.storage.path=/var/kafofond/pdfs/
pdf.storage.enabled=true
pdf.storage.retention.days=90
```

**Service de stockage :**

```java
@Service
@RequiredArgsConstructor
public class PdfStorageService {
    
    @Value("${pdf.storage.path}")
    private String storagePath;
    
    @Value("${pdf.storage.enabled:false}")
    private boolean storageEnabled;
    
    public void savePdf(byte[] pdfBytes, String filename) throws IOException {
        if (!storageEnabled) {
            return;
        }
        
        Path path = Paths.get(storagePath, filename);
        Files.createDirectories(path.getParent());
        Files.write(path, pdfBytes);
        
        log.info("PDF sauvegardé: {}", path);
    }
}
```

---

## 🔄 Configuration de la Génération Asynchrone

Pour ne pas bloquer le thread principal :

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "pdfTaskExecutor")
    public Executor pdfTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("pdf-gen-");
        executor.initialize();
        return executor;
    }
}
```

**Service asynchrone :**

```java
@Service
@RequiredArgsConstructor
public class AsyncPdfService {
    
    private final JasperReportService jasperService;
    
    @Async("pdfTaskExecutor")
    public CompletableFuture<byte[]> generatePdfAsync(
            String template, 
            List<?> data, 
            Map<String, Object> params) {
        
        try {
            byte[] pdf = jasperService.generatePdf(template, data, params);
            return CompletableFuture.completedFuture(pdf);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

---

## 📧 Configuration pour l'Envoi par Email

Pour envoyer les PDFs par email automatiquement :

```java
@Service
@RequiredArgsConstructor
public class PdfEmailService {
    
    private final JavaMailSender mailSender;
    private final JasperReportService jasperService;
    
    public void sendPdfByEmail(
            String recipientEmail, 
            String subject,
            String templateName,
            List<?> data,
            Map<String, Object> params) throws Exception {
        
        // Générer le PDF
        byte[] pdfBytes = jasperService.generatePdf(templateName, data, params);
        
        // Créer le message email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText("Veuillez trouver le document PDF en pièce jointe.");
        
        // Attacher le PDF
        helper.addAttachment(templateName + ".pdf", new ByteArrayResource(pdfBytes));
        
        // Envoyer
        mailSender.send(message);
        
        log.info("PDF envoyé par email à: {}", recipientEmail);
    }
}
```

---

## 🎯 Variables d'Environnement Recommandées

Pour la production :

```bash
# Environment variables
JASPER_CACHE_ENABLED=true
JASPER_CACHE_SIZE=100
JASPER_TIMEOUT=30
JASPER_LOG_LEVEL=WARN
PDF_STORAGE_PATH=/var/kafofond/pdfs/
PDF_STORAGE_ENABLED=true
```

---

## ✅ CHECKLIST DE CONFIGURATION

Configuration de base :
- [x] Dépendances Maven ajoutées
- [x] Service JasperReportService créé
- [x] Templates dans src/main/resources/reports/

Configuration optionnelle recommandée :
- [ ] Cache des templates activé
- [ ] Logs configurés
- [ ] Polices personnalisées ajoutées
- [ ] Stockage des PDFs configuré
- [ ] Génération asynchrone activée
- [ ] Envoi par email configuré

---

**💡 Conseil :** Commencez avec la configuration de base, puis ajoutez progressivement les optimisations selon vos besoins.
