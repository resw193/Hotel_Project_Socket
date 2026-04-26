package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class LoginResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String employeeId;
    private String employeeName;
    private String employeeTypeId;
    private String employeeTypeName;
    private String imgSource;
    private boolean gender;

    public LoginResultDTO() {

    }

    public LoginResultDTO(String username, String employeeId, String employeeName, String employeeTypeId, String employeeTypeName, String imgSource, boolean gender) {
        this.username = username;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeTypeId = employeeTypeId;
        this.employeeTypeName = employeeTypeName;
        this.imgSource = imgSource;
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeTypeId() {
        return employeeTypeId;
    }

    public void setEmployeeTypeId(String employeeTypeId) {
        this.employeeTypeId = employeeTypeId;
    }

    public String getEmployeeTypeName() {
        return employeeTypeName;
    }

    public void setEmployeeTypeName(String employeeTypeName) {
        this.employeeTypeName = employeeTypeName;
    }

    public String getImgSource() {
        return imgSource;
    }

    public void setImgSource(String imgSource) {
        this.imgSource = imgSource;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }
}