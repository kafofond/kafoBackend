package kafofond.repository;

import kafofond.entity.Entreprise;
import kafofond.entity.Role;
import kafofond.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface UtilisateurRepo extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.entreprise WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithEntreprise(@Param("email") String email);

    boolean existsByRole(Role role);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    // ✅ Méthode pour récupérer le directeur d'une entreprise
    Optional<Utilisateur> findByEntrepriseAndRole(Entreprise entreprise, Role role);

    // Si besoin, tous les utilisateurs d'une entreprise
    List<Utilisateur> findByEntreprise(Entreprise entreprise);
}