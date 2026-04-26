package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class EmployeeTypeFilterRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String typeName;

    public EmployeeTypeFilterRequestDTO() {
    }

    public EmployeeTypeFilterRequestDTO(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}