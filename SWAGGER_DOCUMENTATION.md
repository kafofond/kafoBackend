# Documentation Swagger - API KafoFond

## 🚀 Accès à la Documentation

Une fois l'application démarrée, vous pouvez accéder à la documentation Swagger via :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Docs JSON** : http://localhost:8080/api-docs
- **API Docs YAML** : http://localhost:8080/api-docs.yaml

## 📋 Fonctionnalités Swagger

### Interface Utilisateur
- **Interface interactive** : Testez directement les endpoints depuis le navigateur
- **Authentification JWT** : Bouton "Authorize" pour saisir votre token
- **Exemples de requêtes** : Modèles de données avec exemples
- **Codes de réponse** : Documentation complète des réponses possibles

### Authentification
1. **Connectez-vous** via `/api/auth/login`
2. **Copiez le token** JWT de la réponse
3. **Cliquez sur "Authorize"** dans Swagger UI
4. **Saisissez** : `Bearer {votre_token}`
5. **Testez** tous les endpoints protégés

## 🔐 Endpoints Documentés

### Authentification
- `POST /api/auth/login` - Connexion utilisateur
- `POST /api/auth/signup` - Inscription (Admin/SuperAdmin)
- `GET /api/auth/verify` - Vérification token

### Gestion des Utilisateurs
- `GET /api/utilisateurs` - Liste utilisateurs
- `POST /api/utilisateurs` - Créer utilisateur
- `PUT /api/utilisateurs/{id}` - Modifier utilisateur
- `POST /api/utilisateurs/{id}/desactiver` - Désactiver
- `POST /api/utilisateurs/{id}/reactiver` - Réactiver

### Budgets
- `POST /api/budgets` - Créer budget
- `PUT /api/budgets/{id}` - Modifier budget
- `POST /api/budgets/{id}/valider` - Valider (Directeur)
- `POST /api/budgets/{id}/rejeter` - Rejeter (Directeur)
- `POST /api/budgets/{id}/activer` - Activer (Directeur)
- `POST /api/budgets/{id}/desactiver` - Désactiver

### Fiches de Besoin
- `POST /api/fiches-besoin` - Créer (Trésorerie)
- `PUT /api/fiches-besoin/{id}` - Modifier (Trésorerie)
- `POST /api/fiches-besoin/{id}/valider` - Valider (Gestionnaire)
- `POST /api/fiches-besoin/{id}/approuver` - Approuver (Comptable)
- `POST /api/fiches-besoin/{id}/rejeter` - Rejeter

### Demandes d'Achat
- `POST /api/demandes-achat` - Créer (Trésorerie)
- `PUT /api/demandes-achat/{id}` - Modifier (Trésorerie)
- `POST /api/demandes-achat/{id}/valider` - Valider (Gestionnaire)
- `POST /api/demandes-achat/{id}/approuver` - Approuver (Comptable)
- `POST /api/demandes-achat/{id}/rejeter` - Rejeter

### Bons de Commande
- `GET /api/bons-commande` - Lister
- `GET /api/bons-commande/{id}` - Détails
- `PUT /api/bons-commande/{id}` - Personnaliser
- `POST /api/bons-commande/{id}/valider` - Valider → génère PDF
- `POST /api/bons-commande/{id}/rejeter` - Rejeter
- `GET /api/bons-commande/{id}/pdf` - Télécharger PDF

### Ordres de Paiement
- `POST /api/ordres-paiement` - Créer (Comptable)
- `POST /api/ordres-paiement/{id}/valider` - Valider (Responsable si < seuil)
- `POST /api/ordres-paiement/{id}/approuver` - Approuver (Directeur si >= seuil)
- `POST /api/ordres-paiement/{id}/rejeter` - Rejeter

### Notifications
- `GET /api/notifications` - Lister notifications
- `GET /api/notifications/non-lues` - Compter non lues
- `POST /api/notifications/{id}/marquer-lu` - Marquer comme lu

### Historique
- `GET /api/historique/document/{type}/{id}` - Historique document
- `GET /api/historique/entreprise` - Historique entreprise

### Seuils de Validation
- `POST /api/seuils` - Configurer seuil (Directeur)
- `GET /api/seuils/actif` - Obtenir seuil actif

### Rapports
- `GET /api/rapports/budget/{id}/pdf` - Rapport PDF budget
- `GET /api/rapports/budget/{id}/excel` - Rapport Excel budget
- `GET /api/rapports/demande-achat/{id}/excel` - Rapport Excel demande
- `GET /api/rapports/bon-commande/{id}/pdf` - Rapport PDF bon de commande

## 🧪 Tests avec Swagger

### 1. Test de Connexion
1. Allez sur `POST /api/auth/login`
2. Cliquez sur "Try it out"
3. Saisissez :
```json
{
  "email": "directeur@tresor.ml",
  "motDePasse": "password123"
}
```
4. Cliquez sur "Execute"
5. Copiez le token de la réponse

### 2. Test d'Authentification
1. Cliquez sur "Authorize" (cadenas vert)
2. Saisissez : `Bearer {votre_token}`
3. Cliquez sur "Authorize"
4. Tous les endpoints protégés sont maintenant accessibles

### 3. Test de Création de Budget
1. Allez sur `POST /api/budgets`
2. Cliquez sur "Try it out"
3. Saisissez :
```json
{
  "intituleBudget": "Budget Test 2024",
  "description": "Budget de test via Swagger",
  "montantBudget": 1000000,
  "commentaire": "Test via Swagger UI",
  "periode": "Annuelle"
}
```
4. Cliquez sur "Execute"
5. Vérifiez la réponse

### 4. Test de Validation
1. Allez sur `POST /api/budgets/{id}/valider`
2. Saisissez l'ID du budget créé
3. Cliquez sur "Execute"
4. Vérifiez que le budget est validé

## 📊 Modèles de Données

### Utilisateur
```json
{
  "id": 1,
  "nom": "Traoré",
  "prenom": "Mamadou",
  "email": "mamadou@kafofond.com",
  "departement": "Administration",
  "role": "SUPER_ADMIN",
  "actif": true,
  "entrepriseNom": "Trésor"
}
```

### Budget
```json
{
  "id": 1,
  "intituleBudget": "Budget Informatique 2024",
  "description": "Budget pour équipement informatique",
  "montantBudget": 5000000,
  "commentaire": "Budget prévisionnel",
  "dateCreation": "2024-01-15",
  "periode": "Annuelle",
  "statut": "EN_COURS",
  "actif": false,
  "createurNom": "Amadou Sangaré",
  "entrepriseNom": "Trésor"
}
```

### Fiche de Besoin
```json
{
  "id": 1,
  "serviceBeneficiaire": "Informatique",
  "objet": "Ordinateurs portables",
  "description": "Achat de 5 ordinateurs portables",
  "quantite": 5,
  "montantEstime": 2500000,
  "dateAttendu": "2024-03-15",
  "statut": "EN_COURS",
  "createurNom": "Aïssata Konaté",
  "entrepriseNom": "Trésor"
}
```

## 🔧 Configuration Swagger

### Propriétés dans application.properties
```properties
# Configuration Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.display-request-duration=true
```

### Configuration Java
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API KafoFond")
                        .description("API REST pour le système de gestion financière KafoFond")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .bearerFormat("JWT")
                                        .scheme("bearer")));
    }
}
```

## 🎯 Avantages de Swagger

### Pour les Développeurs
- **Documentation automatique** : Plus besoin de maintenir la documentation manuellement
- **Tests interactifs** : Testez l'API directement depuis le navigateur
- **Validation des données** : Vérification automatique des formats
- **Exemples de code** : Génération automatique de code client

### Pour les Utilisateurs
- **Interface intuitive** : Interface graphique claire et moderne
- **Tests en temps réel** : Voir les réponses immédiatement
- **Authentification intégrée** : Gestion simple des tokens JWT
- **Documentation complète** : Tous les endpoints documentés

## 🚀 Démarrage Rapide

1. **Démarrez l'application** :
```bash
mvn spring-boot:run
```

2. **Ouvrez Swagger UI** :
```
http://localhost:8080/swagger-ui.html
```

3. **Connectez-vous** :
- Utilisez `POST /api/auth/login`
- Copiez le token JWT

4. **Autorisez-vous** :
- Cliquez sur "Authorize"
- Saisissez `Bearer {token}`

5. **Testez l'API** :
- Explorez tous les endpoints
- Testez le workflow complet

## 📝 Notes Importantes

- **Token JWT** : Valide pendant 24h par défaut
- **Rôles** : Chaque endpoint a des restrictions de rôles
- **Workflow** : Suivez l'ordre des validations
- **Commentaires** : Obligatoires lors des rejets
- **Notifications** : Automatiques lors des modifications

La documentation Swagger est maintenant intégrée et prête à être utilisée ! 🎉
