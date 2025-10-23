# Test Rapide de Swagger - KafoFond

## 🚀 Démarrage et Accès

1. **Démarrez l'application** :
```bash
mvn spring-boot:run
```

2. **Accédez à Swagger UI** :
```
http://localhost:8080/swagger-ui.html
```

## 🧪 Tests Rapides

### 1. Test de Connexion
1. Dans Swagger UI, trouvez `POST /api/auth/login`
2. Cliquez sur "Try it out"
3. Saisissez :
```json
{
  "email": "directeur@tresor.ml",
  "motDePasse": "password123"
}
```
4. Cliquez sur "Execute"
5. **Copiez le token** de la réponse (ex: `eyJhbGciOiJIUzUxMiJ9...`)

### 2. Configuration de l'Authentification
1. Cliquez sur le **cadenas vert** "Authorize" en haut à droite
2. Dans le champ "Value", saisissez :
```
Bearer eyJhbGciOiJIUzUxMiJ9...
```
(Remplacez par votre token réel)
3. Cliquez sur "Authorize"
4. Cliquez sur "Close"

### 3. Test de Création de Budget
1. Trouvez `POST /api/budgets`
2. Cliquez sur "Try it out"
3. Saisissez :
```json
{
  "intituleBudget": "Budget Test Swagger",
  "description": "Budget créé via Swagger UI",
  "montantBudget": 2000000,
  "commentaire": "Test d'intégration Swagger",
  "periode": "Annuelle"
}
```
4. Cliquez sur "Execute"
5. Vérifiez la réponse (status 200)

### 4. Test de Validation de Budget
1. Notez l'ID du budget créé (ex: `"id": 1`)
2. Trouvez `POST /api/budgets/{id}/valider`
3. Cliquez sur "Try it out"
4. Saisissez l'ID : `1`
5. Cliquez sur "Execute"
6. Vérifiez que le budget est validé

### 5. Test de Liste des Budgets
1. Trouvez `GET /api/budgets`
2. Cliquez sur "Try it out"
3. Cliquez sur "Execute"
4. Vérifiez la liste des budgets

## 🔍 Vérifications

### Interface Swagger
- ✅ **Titre** : "API KafoFond"
- ✅ **Description** : Contient "système de gestion financière"
- ✅ **Version** : "1.0.0"
- ✅ **Serveur** : "http://localhost:8080"

### Authentification
- ✅ **Bouton Authorize** : Visible et fonctionnel
- ✅ **Token JWT** : Accepté et reconnu
- ✅ **Endpoints protégés** : Accessibles après authentification

### Endpoints Documentés
- ✅ **Authentification** : `/api/auth/*`
- ✅ **Budgets** : `/api/budgets/*`
- ✅ **Utilisateurs** : `/api/utilisateurs/*`
- ✅ **Notifications** : `/api/notifications/*`

### Modèles de Données
- ✅ **BudgetDTO** : Champs documentés avec exemples
- ✅ **LoginRequest** : Structure claire
- ✅ **JwtResponse** : Réponse de connexion

## 🎯 Tests Avancés

### Workflow Complet
1. **Connexion Trésorerie** : `tresorerie@tresor.ml`
2. **Créer Fiche de Besoin** : `POST /api/fiches-besoin`
3. **Connexion Gestionnaire** : `gestionnaire@tresor.ml`
4. **Valider Fiche** : `POST /api/fiches-besoin/{id}/valider`
5. **Connexion Comptable** : `comptable@tresor.ml`
6. **Approuver Fiche** : `POST /api/fiches-besoin/{id}/approuver`

### Test des Rôles
- **Directeur** : Peut valider les budgets
- **Responsable** : Peut créer et modifier les budgets
- **Trésorerie** : Peut créer les fiches de besoin
- **Gestionnaire** : Peut valider les fiches de besoin
- **Comptable** : Peut approuver les fiches de besoin

## 🐛 Dépannage

### Problème : "401 Unauthorized"
- **Solution** : Vérifiez que vous avez bien configuré le token JWT
- **Vérification** : Le token doit commencer par "Bearer "

### Problème : "403 Forbidden"
- **Solution** : Vérifiez que votre utilisateur a le bon rôle
- **Test** : Utilisez un utilisateur avec les droits appropriés

### Problème : "404 Not Found"
- **Solution** : Vérifiez l'URL de Swagger UI
- **Alternative** : Essayez `http://localhost:8080/swagger-ui/index.html`

### Problème : Interface ne se charge pas
- **Solution** : Vérifiez que l'application est démarrée
- **Vérification** : `http://localhost:8080/api-docs` doit retourner du JSON

## 📊 Résultats Attendus

### Connexion Réussie
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "role": "DIRECTEUR",
  "email": "directeur@tresor.ml"
}
```

### Budget Créé
```json
{
  "message": "Budget créé avec succès",
  "budget": {
    "id": 1,
    "intituleBudget": "Budget Test Swagger",
    "montantBudget": 2000000,
    "statut": "EN_COURS",
    "actif": false
  }
}
```

### Budget Validé
```json
{
  "message": "Budget validé avec succès",
  "budget": {
    "id": 1,
    "statut": "VALIDÉ",
    "actif": true
  }
}
```

## ✅ Checklist de Validation

- [ ] Swagger UI accessible sur `http://localhost:8080/swagger-ui.html`
- [ ] Interface Swagger s'affiche correctement
- [ ] Tous les endpoints sont documentés
- [ ] Authentification JWT fonctionne
- [ ] Test de connexion réussi
- [ ] Test de création de budget réussi
- [ ] Test de validation de budget réussi
- [ ] Modèles de données documentés
- [ ] Exemples de requêtes fonctionnels
- [ ] Codes de réponse documentés

## 🎉 Succès !

Si tous les tests passent, Swagger est correctement intégré et fonctionnel ! 

Vous pouvez maintenant :
- **Documenter** automatiquement votre API
- **Tester** tous les endpoints depuis l'interface
- **Partager** la documentation avec votre équipe
- **Générer** du code client automatiquement

La documentation Swagger est maintenant prête à être utilisée ! 🚀
