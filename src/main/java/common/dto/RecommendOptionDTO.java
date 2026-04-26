package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendOptionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int singles;
    private int doubles;
    private List<String> singleRoomIDs;
    private List<String> doubleRoomIDs;

    public RecommendOptionDTO() {
    }

    public RecommendOptionDTO(int singles, int doubles, List<String> singleRoomIDs, List<String> doubleRoomIDs) {
        this.singles = singles;
        this.doubles = doubles;
        this.singleRoomIDs = singleRoomIDs;
        this.doubleRoomIDs = doubleRoomIDs;
    }

    public int getSingles() {
        return singles;
    }

    public void setSingles(int singles) {
        this.singles = singles;
    }

    public int getDoubles() {
        return doubles;
    }

    public void setDoubles(int doubles) {
        this.doubles = doubles;
    }

    public List<String> getSingleRoomIDs() {
        return singleRoomIDs;
    }

    public void setSingleRoomIDs(List<String> singleRoomIDs) {
        this.singleRoomIDs = singleRoomIDs;
    }

    public List<String> getDoubleRoomIDs() {
        return doubleRoomIDs;
    }

    public void setDoubleRoomIDs(List<String> doubleRoomIDs) {
        this.doubleRoomIDs = doubleRoomIDs;
    }

    public String getLabel() {
        if (singles == 0) return doubles + " phòng đôi";
        if (doubles == 0) return singles + " phòng đơn";
        return doubles + " phòng đôi + " + singles + " phòng đơn";
    }
}