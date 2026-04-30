package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class ResetPasswordRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String otp;
    private String newPassword;
    private String confirmPassword;

    public ResetPasswordRequestDTO() {
    }

    public ResetPasswordRequestDTO(String username, String otp, String newPassword, String confirmPassword) {
        this.username = username;
        this.otp = otp;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}