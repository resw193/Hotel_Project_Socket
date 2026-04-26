package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO implements Serializable {
    private String accountId;
    private String username;
    private String employeeId;
    private String employeeName;

    public AccountDTO() {
    }

    public AccountDTO(String accountId, String username, String employeeId, String employeeName) {
        this.accountId = accountId;
        this.username = username;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
}