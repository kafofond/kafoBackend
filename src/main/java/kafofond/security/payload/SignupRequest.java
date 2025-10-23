package kafofond.security.payload;
import lombok.Data;

@Data
public class SignupRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String confirmationMotDePasse;
    private String departement;
    private String role;
    private Long entrepriseId;

}