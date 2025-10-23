# Correction des autorisations d'accès pour les rôles

## Problème identifié
Les utilisateurs avec différents rôles (COMPTABLE, GESTIONNAIRE, etc.) recevaient une erreur 403 (Forbidden) lors de l'accès aux endpoints de l'API, même s'ils devraient avoir les autorisations nécessaires.

## Cause du problème
Dans la configuration de sécurité, les rôles étaient vérifiés avec la méthode `.hasRole()` qui attend des rôles avec le préfixe "ROLE_". 
Par exemple : `.hasRole("COMPTABLE")` cherche le rôle "ROLE_COMPTABLE".

Cependant, les tokens JWT contenaient les rôles sans le préfixe "ROLE_". 
Par exemple : `"role": "COMPTABLE"` dans le token.

Cette incohérence empêchait Spring Security de reconnaître correctement les rôles des utilisateurs.

## Solution appliquée
Remplacement de toutes les occurrences de `.hasRole()` par `.hasAuthority()` dans le fichier [SecurityConfig.java](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\config\SecurityConfig.java).

La méthode `.hasAuthority()` vérifie directement les autorités telles qu'elles sont définies, sans ajouter de préfixe.

Par exemple :
- Ancien code : `.hasRole("COMPTABLE")`
- Nouveau code : `.hasAuthority("ROLE_COMPTABLE")`

## Modifications spécifiques
1. **Gestion des utilisateurs** :
   - Avant : `.hasAnyRole("SUPER_ADMIN", "ADMIN", "DIRECTEUR")`
   - Après : `.hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_DIRECTEUR")`

2. **Demandes d'achat** :
   - Avant : `.hasAnyRole("TRESORERIE", "GESTIONNAIRE", "COMPTABLE", "DIRECTEUR")`
   - Après : `.hasAnyAuthority("ROLE_TRESORERIE", "ROLE_GESTIONNAIRE", "ROLE_COMPTABLE", "ROLE_DIRECTEUR")`

3. **Fiches de besoin** :
   - Avant : `.hasAnyRole("TRESORERIE", "GESTIONNAIRE", "COMPTABLE", "DIRECTEUR")`
   - Après : `.hasAnyAuthority("ROLE_TRESORERIE", "ROLE_GESTIONNAIRE", "ROLE_COMPTABLE", "ROLE_DIRECTEUR")`

4. **Tous les autres endpoints** ont été mis à jour de la même manière.

## Résultat
- Les utilisateurs peuvent maintenant accéder aux endpoints selon leurs rôles
- Le compte COMPTABLE peut accéder aux demandes d'achat, fiches de besoin, etc.
- Tous les rôles (TRESORERIE, GESTIONNAIRE, COMPTABLE, DIRECTEUR, etc.) fonctionnent correctement
- L'erreur 403 est résolue pour les accès autorisés

## Exemple de fonctionnement
Avec cette correction, un utilisateur avec le rôle "COMPTABLE" dans son token JWT peut maintenant accéder à :
- `GET /api/demandes-achat` (liste des demandes d'achat)
- `GET /api/fiches-besoin` (liste des fiches de besoin)
- Et autres endpoints autorisés pour le rôle COMPTABLE