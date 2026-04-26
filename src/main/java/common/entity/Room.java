package common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Room {
    private String roomId;
    private String description;

    @JsonProperty("isAvailable")
    private boolean isAvailable;

    private RoomType roomType;
    private String imgRoomSource;
    private String view;

    public Room() {
    }

    public Room(String roomId, String description, boolean available, RoomType roomType, String imgRoomSource, String view) {
        this.roomId = roomId;
        this.description = description;
        this.isAvailable = available;
        this.roomType = roomType;
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
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả phòng không được rỗng");
        }
        this.description = description.trim();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("RoomType không được null");
        }
        this.roomType = roomType;
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