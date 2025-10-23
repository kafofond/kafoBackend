# 📄 GUIDE D'UTILISATION JASPERREPORTS - KAFOFOND

## 🎯 OBJECTIF

Ce guide explique comment utiliser JasperReports intégré dans le projet KafoFond pour générer des documents PDF professionnels (fiches de besoin, demandes d'achat, bons de commande, etc.).

---

## ✅ INSTALLATION COMPLÉTÉE

### Dépendances Maven ajoutées

```xml
<!-- JasperReports -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.21.0</version>
</dependency>
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports-fonts</artifactId>
    <version>6.21.0</version>
</dependency>
```

### Fichiers créés

```
kafofond/
├── src/main/java/kafofond/
│   ├── service/pdf/
│   │   └── JasperReportService.java        ✅ Service générique
│   └── controller/
│       └── FicheBesoinPdfController.java   ✅ Contrôleur exemple
│
└── src/main/resources/
    └── reports/
        └── fiche_besoin.jrxml              ✅ Template exemple
```

---

## 🚀 UTILISATION RAPIDE

### 1️⃣ Test immédiat avec l'endpoint créé

```bash
# Via Swagger (recommandé)
1. Allez sur http://localhost:8080/swagger-ui/index.html
2. Authentifiez-vous avec votre JWT
3. Testez l'endpoint : GET /api/fiche-besoin/{id}/pdf

# Via curl
curl -X GET "http://localhost:8080/api/fiche-besoin/1/pdf" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT" \
  --output fiche_besoin.pdf

# Via navigateur (avec authentification)
http://localhost:8080/api/fiche-besoin/1/pdf
```

### 2️⃣ Télécharger le PDF au lieu de l'afficher

```bash
# Force le téléchargement
GET /api/fiche-besoin/{id}/pdf/download

# Générer toutes les fiches d'une entreprise
GET /api/fiche-besoin/pdf/toutes

# Filtrer par statut
GET /api/fiche-besoin/pdf/toutes?statut=EN_ATTENTE
```

---

## 📚 GUIDE COMPLET

### Étape 1 : Créer un template JRXML

#### Option A : Utiliser Jaspersoft Studio (recommandé)

1. **Télécharger Jaspersoft Studio** (gratuit)
   - Site : https://community.jaspersoft.com/project/jaspersoft-studio
   - Version Community Edition suffisante

2. **Créer un nouveau rapport**
   ```
   File → New → Jasper Report
   Choisir : Blank A4
   ```

3. **Définir les champs (Fields)**
   ```
   Clic droit → Dataset and Query
   Ajouter des champs correspondant à votre entité Java :
   
   Exemple pour FicheBesoin :
   - id (java.lang.Long)
   - serviceBeneficiaire (java.lang.String)
   - objet (java.lang.String)
   - description (java.lang.String)
   - quantite (java.lang.Integer)
   - montantEstime (java.lang.Double)
   - dateCreation (java.time.LocalDate)
   - statut (kafofond.entity.Statut)
   ```

4. **Définir les paramètres (Parameters)**
   ```
   Pour les valeurs globales :
   - TITRE (java.lang.String)
   - ENTREPRISE (java.lang.String)
   - DATE_GENERATION (java.lang.String)
   ```

5. **Designer le rapport**
   - Glisser-déposer des textFields depuis la palette
   - Utiliser $F{nomChamp} pour les champs
   - Utiliser $P{NOM_PARAM} pour les paramètres

6. **Sauvegarder**
   ```
   Enregistrer dans : src/main/resources/reports/
   Nom : nom_template.jrxml
   ```

#### Option B : Créer manuellement le JRXML

Voir l'exemple complet dans `fiche_besoin.jrxml`

---

### Étape 2 : Créer un service ou utiliser JasperReportService

Le service `JasperReportService` est déjà créé et prêt à l'emploi.

**Signature de la méthode principale :**

```java
public byte[] generatePdf(
    String templateName,    // Nom du template sans extension
    List<?> data,          // Liste d'objets à afficher
    Map<String, Object> params  // Paramètres additionnels
) throws Exception
```

**Exemple d'utilisation dans un service :**

```java
@Service
@RequiredArgsConstructor
public class DemandeAchatService {
    
    private final JasperReportService jasperReportService;
    private final DemandeDAchatRepo demandeRepo;
    
    public byte[] genererPdfDemandeAchat(Long id) throws Exception {
        // 1. Récupérer les données
        DemandeDAchat demande = demandeRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        
        List<DemandeDAchat> demandes = Arrays.asList(demande);
        
        // 2. Préparer les paramètres
        Map<String, Object> params = new HashMap<>();
        params.put("TITRE", "DEMANDE D'ACHAT #" + demande.getId());
        params.put("ENTREPRISE", demande.getEntreprise().getNom());
        params.put("DATE_GENERATION", LocalDate.now().toString());
        
        // 3. Générer le PDF
        return jasperReportService.generatePdf("demande_achat", demandes, params);
    }
}
```

---

### Étape 3 : Créer un endpoint dans un contrôleur

**Modèle de contrôleur (inspiré de FicheBesoinPdfController) :**

```java
@RestController
@RequestMapping("/api/demandes-achat")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DemandeAchatPdfController {

    private final DemandeDAchatRepo demandeRepo;
    private final JasperReportService jasperReportService;
    private final UtilisateurService utilisateurService;

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer un PDF d'une demande d'achat")
    public ResponseEntity<?> genererPdf(
            @PathVariable Long id,
            Authentication auth) {
        
        try {
            // 1. Authentification
            Utilisateur user = utilisateurService.trouverParEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // 2. Récupérer la demande
            DemandeDAchat demande = demandeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

            // 3. Vérifier les droits d'accès
            if (!demande.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Accès interdit"));
            }

            // 4. Préparer les données
            List<DemandeDAchat> demandes = Arrays.asList(demande);
            
            Map<String, Object> params = new HashMap<>();
            params.put("TITRE", "DEMANDE D'ACHAT #" + demande.getId());
            params.put("ENTREPRISE", demande.getEntreprise().getNom());
            params.put("DATE_GENERATION", LocalDate.now().toString());

            // 5. Générer le PDF
            byte[] pdfBytes = jasperReportService.generatePdf(
                "demande_achat",  // Nom du template
                demandes,         // Données
                params            // Paramètres
            );

            // 6. Retourner le PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "demande_achat_" + id + ".pdf");

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur génération PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }
}
```

---

## 🎨 PERSONNALISATION DU TEMPLATE

### Sections principales d'un template JRXML

```xml
<jasperReport>
    <!-- TITLE : En-tête du document (apparaît une seule fois) -->
    <title>
        <band height="100">
            <!-- Logo, titre principal, informations entreprise -->
        </band>
    </title>
    
    <!-- PAGE HEADER : En-tête de chaque page -->
    <pageHeader>
        <band height="50">
            <!-- Informations répétées sur chaque page -->
        </band>
    </pageHeader>
    
    <!-- COLUMN HEADER : En-têtes de colonnes -->
    <columnHeader>
        <band height="30">
            <!-- Titres des colonnes (ID, Nom, Prix, etc.) -->
        </band>
    </columnHeader>
    
    <!-- DETAIL : Lignes de données (répété pour chaque élément) -->
    <detail>
        <band height="40">
            <!-- Affichage des données : $F{nomChamp} -->
        </band>
    </detail>
    
    <!-- COLUMN FOOTER : Pied de colonnes -->
    <columnFooter>
        <band height="20">
            <!-- Totaux partiels, etc. -->
        </band>
    </columnFooter>
    
    <!-- PAGE FOOTER : Pied de chaque page -->
    <pageFooter>
        <band height="30">
            <!-- Numéro de page, date, etc. -->
        </band>
    </pageFooter>
    
    <!-- SUMMARY : Pied du document (fin du rapport) -->
    <summary>
        <band height="50">
            <!-- Totaux, signatures, etc. -->
        </band>
    </summary>
</jasperReport>
```

### Exemples d'expressions JasperReports

```xml
<!-- Afficher un champ -->
<textField>
    <textFieldExpression><![CDATA[$F{objet}]]></textFieldExpression>
</textField>

<!-- Afficher un paramètre -->
<textField>
    <textFieldExpression><![CDATA[$P{TITRE}]]></textFieldExpression>
</textField>

<!-- Formater un nombre -->
<textField pattern="#,##0.00">
    <textFieldExpression><![CDATA[$F{montantEstime}]]></textFieldExpression>
</textField>

<!-- Formater une date -->
<textField pattern="dd/MM/yyyy">
    <textFieldExpression><![CDATA[$F{dateCreation}]]></textFieldExpression>
</textField>

<!-- Concaténation -->
<textFieldExpression>
    <![CDATA["Total : " + $V{TOTAL_MONTANT} + " FCFA"]]>
</textFieldExpression>

<!-- Condition ternaire -->
<textFieldExpression>
    <![CDATA[$F{statut}.equals("APPROUVE") ? "✓ Approuvé" : "En attente"]]>
</textFieldExpression>

<!-- Numéro de page -->
<textFieldExpression>
    <![CDATA["Page " + $V{PAGE_NUMBER} + " sur " + $V{PAGE_COUNT}]]>
</textFieldExpression>

<!-- Variable calculée (somme) -->
<variable name="TOTAL_MONTANT" class="java.lang.Double" calculation="Sum">
    <variableExpression><![CDATA[$F{montantEstime}]]></variableExpression>
</variable>
```

---

## 🧩 CRÉER DES TEMPLATES POUR D'AUTRES ENTITÉS

### Pour DemandeDAchat

1. **Créer le template** : `src/main/resources/reports/demande_achat.jrxml`

2. **Définir les champs** :
```xml
<field name="id" class="java.lang.Long"/>
<field name="referenceBesoin" class="java.lang.String"/>
<field name="fournisseur" class="java.lang.String"/>
<field name="quantite" class="java.lang.Integer"/>
<field name="prixUnitaire" class="java.lang.Double"/>
<field name="montantTotal" class="java.lang.Double"/>
<field name="statut" class="kafofond.entity.Statut"/>
```

3. **Créer le contrôleur** (voir exemple ci-dessus)

### Pour BonDeCommande

1. **Template** : `bon_commande.jrxml`

2. **Champs spécifiques** :
```xml
<field name="id" class="java.lang.Long"/>
<field name="fournisseur" class="java.lang.String"/>
<field name="montantTotal" class="java.lang.Double"/>
<field name="modePaiement" class="java.lang.String"/>
<field name="delaiPaiement" class="java.time.LocalDate"/>
<field name="statut" class="kafofond.entity.Statut"/>
```

### Pour OrdreDePaiement

1. **Template** : `ordre_paiement.jrxml`

2. **Champs** :
```xml
<field name="id" class="java.lang.Long"/>
<field name="montant" class="java.lang.Double"/>
<field name="compteOrigine" class="java.lang.String"/>
<field name="compteDestinataire" class="java.lang.String"/>
<field name="dateExecution" class="java.time.LocalDate"/>
<field name="statut" class="kafofond.entity.Statut"/>
```

---

## 🔧 FONCTIONNALITÉS AVANCÉES

### 1. Ajouter un logo d'entreprise

```xml
<!-- Dans le template JRXML -->
<image>
    <reportElement x="10" y="10" width="80" height="60"/>
    <imageExpression>
        <![CDATA["reports/logo_" + $P{ENTREPRISE_ID} + ".png"]]>
    </imageExpression>
</image>
```

Placer le logo dans : `src/main/resources/reports/logo_entreprise.png`

### 2. Utiliser des sous-rapports (subreports)

Pour afficher les **Designations** dans une FicheDeBesoin :

```java
// Dans le service
Map<String, Object> params = new HashMap<>();
params.put("SUBREPORT_DIR", "reports/");

// Dans le template principal
<subreport>
    <reportElement x="0" y="100" width="555" height="200"/>
    <dataSourceExpression>
        <![CDATA[new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource($F{designations})]]>
    </dataSourceExpression>
    <subreportExpression>
        <![CDATA[$P{SUBREPORT_DIR} + "designation_subreport.jasper"]]>
    </subreportExpression>
</subreport>
```

### 3. Graphiques et diagrammes

JasperReports supporte des graphiques (barres, camemberts, etc.) via JFreeChart.

Exemple :
```xml
<chart>
    <chartTitle/>
    <pieChart>
        <pieDataset>
            <dataset/>
            <pieSeries>
                <keyExpression><![CDATA[$F{statut}]]></keyExpression>
                <valueExpression><![CDATA[$F{montantEstime}]]></valueExpression>
            </pieSeries>
        </pieDataset>
    </pieChart>
</chart>
```

### 4. Exporter vers d'autres formats

Le `JasperReportService` peut être étendu pour d'autres formats :

```java
// Export Excel
JasperExportManager.exportReportToXlsFile(jasperPrint, "rapport.xls");

// Export HTML
JasperExportManager.exportReportToHtmlFile(jasperPrint, "rapport.html");

// Export CSV
JRCsvExporter exporter = new JRCsvExporter();
exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
exporter.setExporterOutput(new SimpleWriterExporterOutput("rapport.csv"));
exporter.exportReport();
```

---

## ⚠️ PROBLÈMES COURANTS ET SOLUTIONS

### Problème 1 : "Template not found"

**Cause** : Le fichier JRXML n'est pas dans le classpath

**Solution** :
```
✅ Bon emplacement : src/main/resources/reports/mon_template.jrxml
❌ Mauvais : src/main/java/...
```

### Problème 2 : "Field not found in datasource"

**Cause** : Le nom du champ dans le JRXML ne correspond pas au getter de l'entité

**Solution** :
```java
// Entité
public class Fiche {
    private String objet;  // getter : getObjet()
}

// Template JRXML
<field name="objet" class="java.lang.String"/>  ✅
<field name="Objet" class="java.lang.String"/>  ❌ (mauvaise casse)
```

### Problème 3 : "Class not found"

**Cause** : Le type Java dans le JRXML est incorrect

**Solution** :
```xml
✅ Pour enum : <field name="statut" class="kafofond.entity.Statut"/>
✅ Pour LocalDate : <field name="date" class="java.time.LocalDate"/>
✅ Pour Double : <field name="montant" class="java.lang.Double"/>
❌ <field name="statut" class="String"/>  (pour enum)
```

### Problème 4 : "Compilation errors"

**Cause** : Erreurs de syntaxe XML dans le JRXML

**Solution** :
- Utiliser Jaspersoft Studio pour valider
- Vérifier les balises fermées
- Vérifier les CDATA : `<![CDATA[...]]>`

### Problème 5 : PDF vide ou avec erreurs

**Cause** : La liste de données est vide ou null

**Solution** :
```java
// Vérifier avant de générer
if (fiches == null || fiches.isEmpty()) {
    throw new RuntimeException("Aucune donnée à afficher");
}
```

---

## 📊 STRUCTURE RECOMMANDÉE DES FICHIERS

```
src/main/resources/reports/
├── fiche_besoin.jrxml              ✅ Créé
├── demande_achat.jrxml             📝 À créer
├── bon_commande.jrxml              📝 À créer
├── ordre_paiement.jrxml            📝 À créer
├── attestation_service_fait.jrxml  📝 À créer
├── decision_prelevement.jrxml      📝 À créer
├── subreports/
│   ├── designation_detail.jrxml
│   └── footer_common.jrxml
└── images/
    ├── logo.png
    └── watermark.png
```

---

## 🎓 RESSOURCES ET DOCUMENTATION

### Documentation officielle
- **JasperReports** : https://community.jaspersoft.com/documentation
- **Jaspersoft Studio** : https://community.jaspersoft.com/project/jaspersoft-studio

### Tutoriels
- **iReport Designer Tutorial** : https://jasperreports.sourceforge.net/
- **JasperReports Library** : https://github.com/TIBCOSoftware/jasperreports

### Exemples
- Repository officiel : https://github.com/TIBCOSoftware/jasperreports-samples

---

## ✅ CHECKLIST DE DÉPLOIEMENT

Avant de déployer en production :

- [ ] Tous les templates JRXML sont testés et compilent sans erreur
- [ ] Les endpoints PDF sont protégés par authentification JWT
- [ ] Les vérifications de droits d'accès sont en place
- [ ] Les logs sont configurés pour tracer les générations de PDF
- [ ] Les erreurs sont gérées proprement (messages utilisateur)
- [ ] Les performances sont testées avec de gros volumes de données
- [ ] Les PDFs générés sont conformes aux attentes métier
- [ ] La documentation est à jour

---

## 🚀 PROCHAINES ÉTAPES

1. **Créer les templates pour les autres entités** (DemandeDAchat, BonCommande, etc.)
2. **Ajouter des contrôleurs PDF** pour chaque type de document
3. **Personnaliser les templates** avec logos, en-têtes personnalisés
4. **Optimiser les performances** (compilation des templates, cache)
5. **Ajouter des tests unitaires** pour les services PDF

---

**✅ JasperReports est maintenant intégré et fonctionnel !**  
**🎯 Testez l'endpoint : GET /api/fiche-besoin/{id}/pdf**  
**📚 Consultez ce guide pour créer vos propres templates**
