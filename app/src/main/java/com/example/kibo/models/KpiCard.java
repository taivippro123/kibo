package com.example.kibo.models;

public class KpiCard {
    private String title;
    private String value;
    private String unit;
    private int colorRes;

    public KpiCard(String title, String value, String unit, int colorRes) {
        this.title = title;
        this.value = value;
        this.unit = unit;
        this.colorRes = colorRes;
    }

    // Getters
    public String getTitle() { return title; }
    public String getValue() { return value; }
    public String getUnit() { return unit; }
    public int getColorRes() { return colorRes; }
}
