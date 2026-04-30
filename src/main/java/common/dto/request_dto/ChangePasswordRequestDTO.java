package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class ChangePasswordRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String newPassword;
    private String confirmPassword;

    public ChangePasswordRequestDTO() {
    }

    public ChangePasswordRequestDTO(String employeeId, String newPassword, String confirmPassword) {
        this.employeeId = employeeId;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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