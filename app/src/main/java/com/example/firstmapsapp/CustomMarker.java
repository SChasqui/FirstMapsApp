package com.example.firstmapsapp;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;

public class CustomMarker {

    private String name;
    private Marker marker;
    private CircleOptions area;

    public CustomMarker(String name,Marker marker, CircleOptions area) {
        this.name =name;
        this.marker = marker;
        this.area = area;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public CircleOptions getArea() {
        return area;
    }

    public void setArea(CircleOptions area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }
}
