package common.entity;

public class RoomType {
    private String roomTypeId;
    private String typeName;
    private double pricePerHour;
    private double pricePerNight;
    private double pricePerDay;
    private double lateFeePerHour;
    private int maxAdults;
    private int maxChildren;
    private String description;

    public RoomType() {
    }

    public RoomType(String roomTypeId, String typeName, double pricePerHour, double pricePerNight, double pricePerDay, double lateFeePerHour,
                    int maxAdults, int maxChildren, String description) {
        this.roomTypeId = roomTypeId;
        this.typeName = typeName;
        this.pricePerHour = pricePerHour;
        this.pricePerNight = pricePerNight;
        this.pricePerDay = pricePerDay;
        this.lateFeePerHour = lateFeePerHour;
        this.maxAdults = maxAdults;
        this.maxChildren = maxChildren;
        this.description = description;
    }

    public String getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(String roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại phòng không được rỗng");
        }
        this.typeName = typeName.trim();
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public double getLateFeePerHour() {
        return lateFeePerHour;
    }

    public void setLateFeePerHour(double lateFeePerHour) {
        this.lateFeePerHour = lateFeePerHour;
    }

    public int getMaxAdults() {
        return maxAdults;
    }

    public void setMaxAdults(int maxAdults) {
        this.maxAdults = maxAdults;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public void setMaxChildren(int maxChildren) {
        this.maxChildren = maxChildren;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}