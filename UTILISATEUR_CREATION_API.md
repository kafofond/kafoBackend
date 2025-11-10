# API de Création d'Utilisateur - Documentation

## Endpoint de Création

```
POST /api/utilisateurs
```

## Nouveau Format de Requête

Le JSON d'entrée pour la création d'un utilisateur a été simplifié pour ne contenir que les champs essentiels :

```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@entreprise.com",
  "motDePasse": "motdepasse123",
  "confirmationMotDePasse": "motdepasse123",
  "departement": "Finance",
  "role": "COMPTABLE",
  "entrepriseId": 1
}
```

## Champs Requis

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| nom | string | Nom de l'utilisateur | Oui |
| prenom | string | Prénom de l'utilisateur | Oui |
| email | string | Email de l'utilisateur (doit être unique) | Oui |
| motDePasse | string | Mot de passe de l'utilisateur | Oui |
| confirmationMotDePasse | string | Confirmation du mot de passe | Oui |
| departement | string | Département de l'utilisateur | Oui |
| role | string | Rôle de l'utilisateur (TRESORERIE, GESTIONNAIRE, COMPTABLE, RESPONSABLE, DIRECTEUR, ADMIN, SUPER_ADMIN) | Oui |
| entrepriseId | integer | ID de l'entreprise (uniquement pour le SUPER_ADMIN) | Conditionnel |

## Logique de Gestion de l'Entreprise

La gestion de l'entreprise dépend du rôle de l'administrateur qui crée l'utilisateur :

1. **SUPER_ADMIN** :
   - Doit spécifier l'[entrepriseId](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\UtilisateurDTO.java#L19-L19) dans la requête
   - Peut créer des utilisateurs pour n'importe quelle entreprise

2. **ADMIN/DIRECTEUR** :
   - L'[entrepriseId](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\UtilisateurDTO.java#L19-L19) est automatiquement associé à leur propre entreprise
   - Ne peuvent créer des utilisateurs que pour leur entreprise

## Champs Générés Automatiquement

Les champs suivants sont générés automatiquement par le système et ne doivent pas être inclus dans la requête :

- `id` - Identifiant unique généré par la base de données
- `etat` - État initial (true - actif)
- `entrepriseNom` - Nom de l'entreprise (récupéré automatiquement)

## Validation

Le système effectue les validations suivantes :

1. **Conformité des mots de passe** : [motDePasse](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\Utilisateur.java#L22-L22) et [confirmationMotDePasse](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\UtilisateurCreationDTO.java#L22-L22) doivent être identiques
2. **Unicité de l'email** : L'email doit être unique dans l'application
3. **Autorisations** : Seuls les SUPER_ADMIN, ADMIN et DIRECTEUR peuvent créer des utilisateurs
4. **Entreprise** : Validation de l'existence de l'entreprise spécifiée

## Exemple de Réponse

```json
{
  "message": "Utilisateur créé avec succès",
  "utilisateur": {
    "id": 1,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@entreprise.com",
    "motDePasse": null,
    "departement": "Finance",
    "role": "COMPTABLE",
    "actif": true,
    "entrepriseId": 1,
    "entrepriseNom": "Ma Entreprise"
  }
}
```

## Exemples d'Erreurs

### Mots de passe non conformes
```json
{
  "message": "Les mots de passe ne correspondent pas"
}
```

### Email déjà utilisé
```json
{
  "message": "Email déjà utilisé"
}
```

### Autorisations insuffisantes
```json
{
  "message": "Vous n'avez pas les autorisations nécessaires pour créer un utilisateur"
}
```

### SUPER_ADMIN n'a pas spécifié l'entreprise
```json
{
  "message": "Le Super Admin doit spécifier l'entreprise de l'utilisateur"
}
```

## Avantages de cette Approche

1. **Simplicité** : L'utilisateur n'a besoin de fournir que les informations essentielles
2. **Sécurité** : Les champs sensibles sont gérés automatiquement par le système
3. **Validation** : Vérification automatique de la conformité des mots de passe
4. **Logique métier** : Gestion automatique de l'entreprise selon le rôle
5. **Maintenabilité** : Moins de champs à valider côté client