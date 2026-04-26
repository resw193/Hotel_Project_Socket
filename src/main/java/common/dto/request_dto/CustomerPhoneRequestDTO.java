package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class CustomerPhoneRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String phone;

    public CustomerPhoneRequestDTO() {

    }

    public CustomerPhoneRequestDTO(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}