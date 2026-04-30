package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class ForgotPasswordRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    public ForgotPasswordRequestDTO() {
    }

    public ForgotPasswordRequestDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}