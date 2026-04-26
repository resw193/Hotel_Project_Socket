package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomId;
    private String description;

    @JsonProperty("isAvailable")
    private boolean available;

    private String roomTypeId;
    private String roomTypeName;
    private String imgRoomSource;
    private String view;

    public RoomDTO() {
    }

    public RoomDTO(String roomId, String description, boolean available, String roomTypeId, String roomTypeName, String imgRoomSource, String view) {
        this.roomId = roomId;
        this.description = description;
        this.available = available;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.imgRoomSource = imgRoomSource;
        this.view = view;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(String roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getImgRoomSource() {
        return imgRoomSource;
    }

    public void setImgRoomSource(String imgRoomSource) {
        this.imgRoomSource = imgRoomSource;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}