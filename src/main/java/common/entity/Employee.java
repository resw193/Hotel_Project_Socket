package common.entity;

public class Employee {
    private String employeeId;
    private String fullName;
    private String phone;
    private String email;
    private EmployeeType employeeType;
    private String imgSource;
    private boolean gender;

    public Employee() {
    }

    public Employee(String employeeId, String fullName, String phone, String email,
                    EmployeeType employeeType, String imgSource, boolean gender) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.employeeType = employeeType;
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
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được rỗng");
        }
        this.fullName = fullName.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || !phone.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        this.email = email.trim();
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        if (employeeType == null) {
            throw new IllegalArgumentException("EmployeeType không được null");
        }
        this.employeeType = employeeType;
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