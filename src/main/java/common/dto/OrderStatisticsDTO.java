package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class OrderStatisticsDTO implements Serializable {
    private int soLuongHoaDon;
    private double tongThuNhap;

    public OrderStatisticsDTO() {
    }

    public OrderStatisticsDTO(int soLuongHoaDon, double tongThuNhap) {
        this.soLuongHoaDon = soLuongHoaDon;
        this.tongThuNhap = tongThuNhap;
    }

    public int getSoLuongHoaDon() {
        return soLuongHoaDon;
    }

    public void setSoLuongHoaDon(int soLuongHoaDon) {
        this.soLuongHoaDon = soLuongHoaDon;
    }

    public double getTongThuNhap() {
        return tongThuNhap;
    }

    public void setTongThuNhap(double tongThuNhap) {
        this.tongThuNhap = tongThuNhap;
    }
}