package com.example.googledirections.requests;

import com.google.maps.model.LatLng;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LatLngRequest {

    private LatLng originLatLng;
    private LatLng destinationLatLng;

}
