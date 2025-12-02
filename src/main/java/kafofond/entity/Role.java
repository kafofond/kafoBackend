package kafofond.entity;

/**
 * Enumération des rôles dans le système KafoFond
 * Hiérarchie : SUPER_ADMIN > ADMIN > DIRECTEUR > RESPONSABLE > COMPTABLE > GESTIONNAIRE > TRESORERIE
 */
public enum Role {
    TRESORERIE,
    GESTIONNAIRE,
    COMPTABLE,
    RESPONSABLE,
    DIRECTEUR,
    ADMIN,
    SUPER_ADMIN
}
