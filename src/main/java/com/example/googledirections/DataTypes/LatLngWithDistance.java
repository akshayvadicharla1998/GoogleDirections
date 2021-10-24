package com.example.googledirections.DataTypes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LatLngWithDistance {
    private Double startLat;
    private Double startLng;
    private Integer distance;
}