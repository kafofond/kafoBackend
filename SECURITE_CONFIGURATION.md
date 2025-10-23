# Configuration de la Sécurité pour KafoFond

## 1. Fichiers de Configuration Sensibles

Les informations confidentielles de l'application sont stockées séparément pour des raisons de sécurité :

- `src/main/resources/application-secrets.properties` - Configuration des identifiants
- `config/application-secrets.properties` - Alternative pour configuration externe

## 2. Variables d'Environnement

Les informations sensibles peuvent être configurées via des variables d'environnement :

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

## 3. Fichier .env (Développement)

Pour le développement local, vous pouvez créer un fichier `.env` à la racine du projet :

```bash
cp .env.example .env
# Puis modifiez les valeurs dans .env
```

## 4. Exclusion Git

Les fichiers sensibles sont exclus via `.gitignore` :
- `config/` - Dossier de configuration externe
- `src/main/resources/application-secrets.properties` - Fichier de propriétés secrets
- `.env` - Variables d'environnement locales

## 5. Profils Spring

L'application utilise le profil `secrets` qui est activé par défaut :
```
spring.profiles.active=prod,secrets
```

## 6. Configuration de l'Application

Les fichiers de configuration sont chargés dans cet ordre :
1. `application.properties` - Configuration générale
2. `application-secrets.properties` - Configuration sécurisée

## 7. Bonnes Pratiques

- Ne jamais committer les fichiers de configuration sensibles
- Utiliser des mots de passe d'application pour Gmail
- Générer des clés JWT fortes et uniques
- Utiliser des variables d'environnement en production