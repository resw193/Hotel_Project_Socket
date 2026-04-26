package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class DashboardSaveNoteRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String content;
    private String employeeId;

    public DashboardSaveNoteRequestDTO() {
    }

    public DashboardSaveNoteRequestDTO(String content, String employeeId) {
        this.content = content;
        this.employeeId = employeeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}