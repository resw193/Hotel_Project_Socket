package common.entity;

public class Account {
    private String accountId;
    private String username;
    private String password;
    private Employee employee;

    public Account() {
    }

    public Account(String accountId, String username, String password, Employee employee) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.employee = employee;
    }

    public Account(String username, String password, Employee employee) {
        this.username = username;
        this.password = password;
        this.employee = employee;
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
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được rỗng");
        }
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password không được rỗng");
        }
        this.password = password;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee không được null");
        }
        this.employee = employee;
    }
}