package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class RoomSearchRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;
    private String filter;

    public RoomSearchRequestDTO() {
    }

    public RoomSearchRequestDTO(String keyword, String filter) {
        this.keyword = keyword;
        this.filter = filter;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}