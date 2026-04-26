package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ChangeRoomWhileCheckInRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String oldRoomID;
    private String newRoomID;
    private LocalDateTime changeTime;

    public ChangeRoomWhileCheckInRequestDTO() {
    }

    public ChangeRoomWhileCheckInRequestDTO(String oldRoomID, String newRoomID, LocalDateTime changeTime) {
        this.oldRoomID = oldRoomID;
        this.newRoomID = newRoomID;
        this.changeTime = changeTime;
    }

    public String getOldRoomID() {
        return oldRoomID;
    }

    public void setOldRoomID(String oldRoomID) {
        this.oldRoomID = oldRoomID;
    }

    public String getNewRoomID() {
        return newRoomID;
    }

    public void setNewRoomID(String newRoomID) {
        this.newRoomID = newRoomID;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }
}