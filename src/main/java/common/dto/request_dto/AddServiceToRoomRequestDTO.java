package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class AddServiceToRoomRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomID;
    private String serviceName;
    private int quantity;

    public AddServiceToRoomRequestDTO() {
    }

    public AddServiceToRoomRequestDTO(String roomID, String serviceName, int quantity) {
        this.roomID = roomID;
        this.serviceName = serviceName;
        this.quantity = quantity;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}