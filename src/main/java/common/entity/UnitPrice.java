package common.entity;

public class UnitPrice {
    private String unitId;
    private String unitName;
    private String description;

    public UnitPrice() {

    }

    public UnitPrice(String unitId, String unitName, String description) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.description = description;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}