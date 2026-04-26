package common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookingType {
    GIO("Giờ"),
    NGAY("Ngày"),
    DEM("Đêm");

    private final String displayName;

    BookingType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static BookingType fromDisplayName(String value) {
        if (value == null) return null;

        for (BookingType type : values()) {
            if (type.displayName.equalsIgnoreCase(value.trim())
                    || type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("BookingType không hợp lệ: " + value);
    }
}