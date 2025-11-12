# KafoFond Backend

Système de gestion de fonds d'équipements

## Configuration requise

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Configuration initiale

### 1. Variables d'environnement

Créez un fichier `.env` à la racine du projet avec les informations suivantes :

```bash
# Base de données
DB_USERNAME=votre_nom_utilisateur
DB_PASSWORD=votre_mot_de_passe

# Sécurité JWT
JWT_SECRET=votre_cle_secrete_jwt

# Configuration email
MAIL_USERNAME=votre_email@gmail.com
MAIL_PASSWORD=votre_mot_de_passe_application
```

### 2. Configuration de la base de données

Assurez-vous que MySQL est en cours d'exécution et créez une base de données :

```sql
CREATE DATABASE kafobackDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Lancement de l'application

```bash
# Compiler et lancer l'application
./mvnw spring-boot:run

# Ou compiler et exécuter le JAR
./mvnw clean package
java -jar target/kafofond-0.0.1-SNAPSHOT.jar
```

## Utilisateurs de test

Après le premier lancement, les utilisateurs suivants sont créés automatiquement :

- **Super Admin** : admin@kafofond.com (mot de passe: password123)

## Configuration des fonctionnalités

### Sécurité JWT

La configuration JWT est gérée via les propriétés :

- `jwt.secret` : Clé secrète pour signer les tokens
- `jwt.expiration` : Durée de validité des tokens (en millisecondes)

### Emails

L'application utilise Gmail SMTP pour l'envoi d'emails :

- Hôte : smtp.gmail.com
- Port : 587
- TLS : Activé

### JasperReports

Pour la génération de PDFs :

- Templates stockés dans `src/main/resources/reports/`
- Polices personnalisées configurées dans `src/main/resources/fonts/`

## Dépendances principales

- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- MySQL Connector
- Lombok
- JWT
- JasperReports
- SpringDoc OpenAPI

## Profils Spring

- `prod` : Profil de production
- `secrets` : Profil pour charger les configurations sensibles

## Structure du projet

```
src/main/java/kafofond/
├── config/           # Configurations de l'application
├── controller/       # Contrôleurs REST
├── dto/              # Objets de transfert de données
│   ├── BudgetCreateDTO.java  # DTO simplifié pour création budget
│   └── ...           # Autres DTOs
├── entity/           # Entités JPA
├── exception/        # Gestion des exceptions
├── mapper/           # Mappers DTO/Entity
├── repository/       # Repositories JPA
├── security/         # Configuration de sécurité
├── service/          # Services métier
└── util/             # Classes utilitaires
```
