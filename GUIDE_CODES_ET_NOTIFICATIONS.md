# 📋 Guide : Système de Codes Uniques et Notifications

## 📌 Vue d'ensemble

Ce guide détaille l'implémentation d'un système de codes uniques pour tous les documents de la chaîne de traitement, ainsi que la mise à jour du système de notifications pour distinguer les notifications internes des transmissions par email.

## 🔑 Système de Codes Uniques

### Format des codes

Tous les documents manipulés dans la chaîne possèdent maintenant un code unique au format :

```
PREFIX-NNNN-MM-YYYY
```

Où :
- **PREFIX** : Identifiant du type de document (2 à 4 caractères)
- **NNNN** : Numéro séquentiel sur 4 chiffres (basé sur l'ID)
- **MM** : Mois de création (01-12)
- **YYYY** : Année de création (4 chiffres)

### Préfixes par type de document

| Type de document | Préfixe | Exemple |
|-----------------|---------|---------|
| Budget | `BUD` | `BUD-0035-11-2025` |
| Ligne de crédit | `LC` | `LC-0042-01-2025` |
| Fiche de besoin | `FB` | `FB-0035-11-2025` |
| Demande d'achat | `DA` | `DA-0120-03-2025` |
| Bon de commande | `BC` | `BC-0089-12-2024` |
| Attestation de service fait | `ASF` | `ASF-0056-02-2025` |
| Décision de prélèvement | `DP` | `DP-0023-04-2025` |
| Ordre de paiement | `OP` | `OP-0078-05-2025` |

### Avantages du système de codes

✅ **Lisibilité** : Les codes sont plus parlants que des IDs numériques bruts  
✅ **Traçabilité** : Le code inclut la date de création du document  
✅ **Professionnalisme** : Améliore la présentation dans les PDFs et les rapports  
✅ **Unicité** : Chaque code est unique dans le système  
✅ **Identification rapide** : Le préfixe permet d'identifier immédiatement le type de document

## 🛠️ Implémentation technique

### 1. Service de génération de codes

Le service [`CodeGeneratorService`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\service\CodeGeneratorService.java) centralise la génération de tous les codes :

```java
@Service
@Slf4j
public class CodeGeneratorService {
    
    public String generateCode(String prefix, Long sequenceNumber, LocalDate date) {
        String month = date.format(DateTimeFormatter.ofPattern("MM"));
        String year = date.format(DateTimeFormatter.ofPattern("yyyy"));
        String formattedNumber = String.format("%04d", sequenceNumber);
        return String.format("%s-%s-%s-%s", prefix, formattedNumber, month, year);
    }
    
    // Méthodes spécifiques pour chaque type de document
    public String generateBudgetCode(Long id, LocalDate dateCreation) {
        return generateCode("BUD", id, dateCreation);
    }
    
    public String generateFicheBesoinCode(Long id, LocalDate dateCreation) {
        return generateCode("FB", id, dateCreation);
    }
    
    // ... autres méthodes
}
```

### 2. Génération automatique des codes

Les codes sont générés automatiquement lors de la création/modification des entités grâce à `@PrePersist` et `@PreUpdate` :

```java
@Entity
@Table(name = "fiches_de_besoin")
public class FicheDeBesoin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 20)
    private String code;
    
    @PrePersist
    @PreUpdate
    public void generateCode() {
        // Le code sera généré par le service lors de la persistance
    }
}
```

### 3. Exposition dans les APIs

Tous les DTOs incluent maintenant le champ `code` :

```java
@Data
@Builder
public class FicheBesoinDTO {
    private Long id;
    private String code;  // Nouveau champ
    private String serviceBeneficiaire;
    // ... autres champs
}
```

Les Mappers ont été mis à jour pour inclure le code :

```java
public FicheBesoinDTO toDTO(FicheDeBesoin fiche) {
    return FicheBesoinDTO.builder()
        .id(fiche.getId())
        .code(fiche.getCode())  // Ajouté
        .serviceBeneficiaire(fiche.getServiceBeneficiaire())
        // ... autres champs
        .build();
}
```

### 4. Utilisation dans les PDFs

Les templates JasperReports utilisent maintenant le champ `code` au lieu de `id` :

```xml
<!-- Dans fiche_besoin.jrxml -->
<field name="code" class="java.lang.String"/>

<!-- En-tête de colonne -->
<staticText>
    <text><![CDATA[Code]]></text>
</staticText>

<!-- Affichage du code -->
<textField>
    <textFieldExpression><![CDATA[$F{code}]]></textFieldExpression>
</textField>
```

## 📧 Système de Notifications amélioré

### Distinction notifications internes / emails

L'entité [`Notification`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\Notification.java) a été mise à jour pour distinguer :

1. **Notifications internes** (dans l'application)
2. **Transmissions par email** (envoi effectif d'emails)

### Nouveaux attributs

```java
@Entity
@Table(name = "notifications")
public class Notification {
    
    /**
     * État de la notification interne (pour l'application)
     * true = lu, false = non lu
     */
    @Column(name = "etat")
    private Boolean etat;
    
    /**
     * État de transmission par email
     * true = email envoyé avec succès
     * false = échec d'envoi
     * null = pas d'email envoyé
     */
    @Column(name = "transmission")
    private Boolean transmission;
}
```

### Cas d'usage

#### 1. Notification interne simple (pas d'email)

```java
Notification notif = Notification.builder()
    .titre("Nouvelle fiche de besoin")
    .message("Une fiche FB-0035-11-2025 a été créée")
    .etat(false)           // Non lue
    .transmission(null)     // Pas d'email
    .destinataire(utilisateur)
    .build();
```

#### 2. Notification avec email envoyé avec succès

```java
Notification notif = Notification.builder()
    .titre("Validation requise")
    .message("Veuillez valider le bon BC-0089-12-2024")
    .etat(false)           // Non lue dans l'app
    .transmission(true)    // Email envoyé avec succès
    .destinataire(utilisateur)
    .build();
```

#### 3. Notification avec échec d'envoi d'email

```java
Notification notif = Notification.builder()
    .titre("Rejet de demande")
    .message("Votre demande DA-0120-03-2025 a été rejetée")
    .etat(false)           // Non lue dans l'app
    .transmission(false)   // Échec d'envoi d'email
    .destinataire(utilisateur)
    .build();
```

### API de notification

Le [`NotificationDTO`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\NotificationDTO.java) expose ces deux états :

```java
@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String titre;
    private String message;
    
    private Boolean etat;         // État de lecture interne
    private Boolean transmission;  // État d'envoi d'email
    
    private LocalDateTime dateEnvoi;
    // ... autres champs
}
```

### Requêtes utiles

#### Récupérer les notifications non lues

```java
List<Notification> nonLues = notificationRepository
    .findByDestinataireAndEtat(utilisateur, false);
```

#### Récupérer les notifications avec échec d'email

```java
List<Notification> emailsEchoues = notificationRepository
    .findByTransmission(false);
```

#### Marquer comme lue

```java
notification.setEtat(true);
notificationRepository.save(notification);
```

## 🗄️ Migration de la base de données

Le script de migration [`V4__add_code_and_update_notification.sql`](c:\Users\Kalandew20\Desktop\kafofond\src\main\resources\db\migration\V4__add_code_and_update_notification.sql) effectue :

### 1. Ajout des colonnes 'code'

```sql
ALTER TABLE budgets 
ADD COLUMN code VARCHAR(20) UNIQUE;

CREATE INDEX idx_budgets_code ON budgets(code);

-- Idem pour les 7 autres tables
```

### 2. Modification de la table notifications

```sql
-- Renommer 'lu' en 'etat'
ALTER TABLE notifications 
CHANGE COLUMN lu etat BOOLEAN DEFAULT FALSE;

-- Ajouter 'transmission'
ALTER TABLE notifications 
ADD COLUMN transmission BOOLEAN DEFAULT NULL;
```

### 3. Génération des codes pour données existantes

Le script fournit des exemples de requêtes UPDATE pour générer les codes des enregistrements existants :

```sql
UPDATE fiches_de_besoin 
SET code = CONCAT('FB-', LPAD(id, 4, '0'), '-', 
                  LPAD(MONTH(date_creation), 2, '0'), '-', 
                  YEAR(date_creation))
WHERE code IS NULL AND date_creation IS NOT NULL;
```

## 📝 Bonnes pratiques

### 1. Ne jamais modifier manuellement un code

❌ **À éviter :**
```java
ficheBesoin.setCode("FB-9999-99-9999");
```

✅ **Correct :**
```java
// Laisser le @PrePersist générer le code automatiquement
// ou utiliser le CodeGeneratorService
String code = codeGenerator.generateFicheBesoinCode(
    ficheBesoin.getId(), 
    ficheBesoin.getDateCreation()
);
```

### 2. Utiliser les codes dans les messages et logs

✅ **Recommandé :**
```java
log.info("Création de la fiche de besoin {}", fiche.getCode());
notification.setMessage("Votre fiche " + fiche.getCode() + " a été validée");
```

❌ **À éviter :**
```java
log.info("Création de la fiche de besoin ID {}", fiche.getId());
```

### 3. Afficher les codes dans les interfaces utilisateur

Dans vos interfaces front-end, privilégiez l'affichage du `code` plutôt que de l'`id` :

```javascript
// React/Angular/Vue exemple
<h3>Fiche de besoin {fiche.code}</h3>
// Au lieu de : <h3>Fiche #{fiche.id}</h3>
```

### 4. Recherche par code

Implémentez des méthodes de recherche par code dans vos repositories :

```java
public interface FicheBesoinRepository extends JpaRepository<FicheDeBesoin, Long> {
    Optional<FicheDeBesoin> findByCode(String code);
}
```

## 🔍 Vérification et tests

### Tester la génération de codes

```java
@Test
public void testGenerateCode() {
    LocalDate date = LocalDate.of(2025, 11, 15);
    String code = codeGenerator.generateFicheBesoinCode(35L, date);
    assertEquals("FB-0035-11-2025", code);
}
```

### Tester l'unicité des codes

```java
@Test
public void testCodeUniqueness() {
    // Créer deux fiches avec la même date
    FicheDeBesoin fiche1 = createFiche();
    FicheDeBesoin fiche2 = createFiche();
    
    // Les codes doivent être différents (IDs différents)
    assertNotEquals(fiche1.getCode(), fiche2.getCode());
}
```

### Tester les notifications

```java
@Test
public void testNotificationInterne() {
    Notification notif = new Notification();
    notif.setEtat(false);
    notif.setTransmission(null);
    
    assertFalse(notif.getEtat());
    assertNull(notif.getTransmission());
}
```

## 📊 Exemples d'utilisation

### Créer une fiche de besoin avec code

```java
@Service
public class FicheBesoinService {
    
    @Autowired
    private CodeGeneratorService codeGenerator;
    
    public FicheDeBesoin creerFiche(FicheBesoinDTO dto) {
        FicheDeBesoin fiche = new FicheDeBesoin();
        fiche.setServiceBeneficiaire(dto.getServiceBeneficiaire());
        fiche.setDateCreation(LocalDate.now());
        
        // Sauvegarder pour obtenir un ID
        fiche = ficheBesoinRepo.save(fiche);
        
        // Générer le code
        String code = codeGenerator.generateFicheBesoinCode(
            fiche.getId(), 
            fiche.getDateCreation()
        );
        fiche.setCode(code);
        
        // Sauvegarder avec le code
        return ficheBesoinRepo.save(fiche);
    }
}
```

### Envoyer une notification avec email

```java
@Service
public class NotificationService {
    
    @Autowired
    private EmailService emailService;
    
    public void envoyerNotificationAvecEmail(Utilisateur destinataire, String titre, String message) {
        Notification notif = new Notification();
        notif.setTitre(titre);
        notif.setMessage(message);
        notif.setDestinataire(destinataire);
        notif.setEtat(false);
        
        // Tenter d'envoyer l'email
        boolean emailEnvoye = emailService.envoyerEmail(destinataire.getEmail(), titre, message);
        
        // Mettre à jour le statut de transmission
        notif.setTransmission(emailEnvoye);
        
        notificationRepo.save(notif);
    }
}
```

## 🎯 Résumé des modifications

### Entités modifiées

- ✅ [`Budget`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\Budget.java) : Ajout attribut `code`
- ✅ [`LigneCredit`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\LigneCredit.java) : Ajout attribut `code`
- ✅ [`FicheDeBesoin`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\FicheDeBesoin.java) : Ajout attribut `code`
- ✅ [`DemandeDAchat`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\DemandeDAchat.java) : Ajout attribut `code`
- ✅ [`BonDeCommande`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\BonDeCommande.java) : Ajout attribut `code`
- ✅ [`AttestationDeServiceFait`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\AttestationDeServiceFait.java) : Ajout attribut `code`
- ✅ [`DecisionDePrelevement`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\DecisionDePrelevement.java) : Ajout attribut `code`
- ✅ [`OrdreDePaiement`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java) : Ajout attribut `code`
- ✅ [`Notification`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\Notification.java) : Ajout `transmission`, renommage `lu` → `etat`

### Services créés

- ✅ [`CodeGeneratorService`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\service\CodeGeneratorService.java) : Service de génération de codes

### DTOs et Mappers mis à jour

- ✅ Tous les DTOs : Ajout du champ `code`
- ✅ Tous les Mappers : Mapping du champ `code`
- ✅ [`NotificationDTO`](c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\NotificationDTO.java) : Ajout `etat` et `transmission`

### Templates JRXML mis à jour

- ✅ [`fiche_besoin.jrxml`](c:\Users\Kalandew20\Desktop\kafofond\src\main\resources\reports\fiche_besoin.jrxml) : Utilisation du champ `code` au lieu de `id`

### Scripts SQL

- ✅ [`V4__add_code_and_update_notification.sql`](c:\Users\Kalandew20\Desktop\kafofond\src\main\resources\db\migration\V4__add_code_and_update_notification.sql) : Migration complète

## 📚 Ressources supplémentaires

- [Documentation Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JasperReports Documentation](https://community.jaspersoft.com/documentation)
- [Pattern Builder avec Lombok](https://projectlombok.org/features/Builder)

---

**Date de création** : 16 janvier 2025  
**Version** : 1.0  
**Auteur** : Équipe KafoFond
