package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class UnitPriceDTO  implements Serializable {
    private String unitId;
    private String unitName;
    private String description;

    public UnitPriceDTO() {
    }

    public UnitPriceDTO(String unitId, String unitName, String description) {
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