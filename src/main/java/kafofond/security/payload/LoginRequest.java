package kafofond.security.payload;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String motDePasse;
}
