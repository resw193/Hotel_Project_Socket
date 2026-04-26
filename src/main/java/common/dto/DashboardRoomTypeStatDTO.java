package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardRoomTypeStatDTO implements Serializable {
    private String typeName;
    private int roomCount;

    public DashboardRoomTypeStatDTO() {
    }

    public DashboardRoomTypeStatDTO(String typeName, int roomCount) {
        this.typeName = typeName;
        this.roomCount = roomCount;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }
}