package com.example.demo.dto;

public class VolumeChartDataDTO {
    private String date;
    private Double volume;

    public VolumeChartDataDTO(String date, Double volume) {
        this.date = date;
        this.volume = volume;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
}