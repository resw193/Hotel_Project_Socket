package client.presentation.core;

import common.dto.EmployeeDTO;

public class SessionContext {
    private String username;
    private String password;
    private String role;
    private EmployeeDTO employee;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public EmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDTO employee) {
        this.employee = employee;
    }

    public boolean isReceptionist() {
        return role != null && role.equalsIgnoreCase("Lễ tân");
    }

    public boolean isManager() {
        return !isReceptionist();
    }

    public void clear() {
        username = null;
        password = null;
        role = null;
        employee = null;
    }
}
