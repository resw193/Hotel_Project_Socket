package other;

import java.sql.Timestamp;

public class OdrInfo {
    public String orderDetailRoomID, orderID, bookingType;
    public Timestamp checkIn, checkOut;
    public double roomFee;
    public String customerID;
    public String customerName;
    public String customerPhone;


    public OdrInfo() {

    }

    public OdrInfo(String odrID, String orderID, String bookingType, Timestamp checkIn, Timestamp checkOut, double roomFee){
        this.orderDetailRoomID = odrID;
        this.orderID = orderID;
        this.bookingType = bookingType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomFee = roomFee;
    }

    public OdrInfo(String odrID, String orderID, String bookingType, Timestamp checkIn, Timestamp checkOut, double roomFee, String customerID, String customerName) {
        this(odrID, orderID, bookingType, checkIn, checkOut, roomFee);
        this.customerID = customerID;
        this.customerName = customerName;
    }


    public OdrInfo(String odrID, String orderID, String bookingType, Timestamp checkIn, Timestamp checkOut,
                   double fee, String customerID, String customerName, String customerPhone) {
        this(odrID, orderID, bookingType, checkIn, checkOut, fee);
        this.customerID = customerID;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    public String getOrderDetailRoomID() {
        return orderDetailRoomID;
    }

    public void setOrderDetailRoomID(String orderDetailRoomID) {
        this.orderDetailRoomID = orderDetailRoomID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public Timestamp getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Timestamp checkIn) {
        this.checkIn = checkIn;
    }

    public Timestamp getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Timestamp checkOut) {
        this.checkOut = checkOut;
    }

    public double getRoomFee() {
        return roomFee;
    }

    public void setRoomFee(double roomFee) {
        this.roomFee = roomFee;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}
