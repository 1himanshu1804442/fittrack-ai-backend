package com.example.demo.dto;

public class OneRepMaxDataDTO {
    private String date;
    private Double oneRepMax;

    public OneRepMaxDataDTO(String date, Double oneRepMax) {
        this.date = date;
        this.oneRepMax = oneRepMax;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getOneRepMax() {
        return oneRepMax;
    }

    public void setOneRepMax(Double oneRepMax) {
        this.oneRepMax = oneRepMax;
    }
}
