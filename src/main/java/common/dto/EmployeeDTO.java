package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class EmployeeDTO implements Serializable {
    private String employeeId;
    private String fullName;
    private String phone;
    private String email;
    private String employeeTypeId;
    private String employeeTypeName;
    private String imgSource;
    private boolean gender;

    public EmployeeDTO() {
    }

    public EmployeeDTO(String employeeId, String fullName, String phone, String email,
                       String employeeTypeId, String employeeTypeName,
                       String imgSource, boolean gender) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.employeeTypeId = employeeTypeId;
        this.employeeTypeName = employeeTypeName;
        this.imgSource = imgSource;
        this.gender = gender;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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