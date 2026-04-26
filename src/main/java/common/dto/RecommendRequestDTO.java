package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class RecommendRequestDTO implements Serializable {
    private int adults;
    private int children;
    private String view;
    private int topK;

    public RecommendRequestDTO() {
    }

    public RecommendRequestDTO(int adults, int children, String view, int topK) {
        this.adults = adults;
        this.children = children;
        this.view = view;
        this.topK = topK <= 0 ? 3 : topK;
    }

    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK <= 0 ? 3 : topK;
    }

    @Override
    public String toString() {
        return "RecommendRequestDTO{" +
                "adults=" + adults +
                ", children=" + children +
                ", view='" + view + '\'' +
                ", topK=" + topK +
                '}';
    }
}