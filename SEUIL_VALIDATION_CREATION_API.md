# API de Création de Seuil de Validation - Documentation

## Endpoint de Création

```
POST /api/seuils
```

## Nouveau Format de Requête

Le JSON d'entrée pour la création d'un seuil de validation a été simplifié pour ne contenir que le montant :

```json
{
  "montantSeuil": 5000.0
}
```

## Champs Requis

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| montantSeuil | number | Montant du seuil de validation | Oui |

## Champs Générés Automatiquement

Les champs suivants sont générés automatiquement par le système et ne doivent pas être inclus dans la requête :

- `id` - Identifiant unique généré par la base de données
- `dateCreation` - Date de création (date courante)
- `actif` - État initial (false - inactif jusqu'à activation)
- `entrepriseNom` - Nom de l'entreprise (récupéré à partir de l'utilisateur authentifié)

## Exemple de Réponse

```json
{
  "message": "Seuil de validation configuré avec succès",
  "seuil": {
    "id": 1,
    "montantSeuil": 5000.0,
    "dateCreation": "2025-10-23",
    "actif": false,
    "entrepriseNom": "Ma Entreprise"
  }
}
```

## Activation du Seuil

Après la création, le seuil doit être activé pour être utilisé :

```
POST /api/seuils/1/activer
```

Lors de l'activation, tous les autres seuils de l'entreprise sont automatiquement désactivés.

## Avantages de cette Approche

1. **Simplicité** : L'utilisateur n'a besoin de fournir que le montant
2. **Sécurité** : Les champs sensibles sont gérés automatiquement par le système
3. **Clarté** : Interface beaucoup plus simple et intuitive
4. **Maintenabilité** : Moins de champs à valider côté client
5. **Cohérence** : Tous les seuils suivent le même processus de création/activation

## Exemple d'Erreur

```json
{
  "message": "Seuls les Directeurs peuvent configurer les seuils de validation"
}
```