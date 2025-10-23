package kafofond.dto;

import lombok.Data;

@Data
public class ReinitialisationMotDePasseDTO {
    private String code;
    private String nouveauMotDePasse;
    private String confirmationMotDePasse;
}