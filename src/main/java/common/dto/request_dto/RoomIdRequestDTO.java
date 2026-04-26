package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class RoomIdRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomID;

    public RoomIdRequestDTO() {
    }

    public RoomIdRequestDTO(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}