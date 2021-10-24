package com.example.googledirections.Controller;

import com.example.googledirections.Service.GoogleDirectionService;
import com.example.googledirections.Service.PolylineService;
import com.example.googledirections.requests.LatLngRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/directions")
@Api("Google Directions Controller")
public class DirectionController {

    @Autowired
    private GoogleDirectionService googleDirectionService;

    @Autowired
    private PolylineService polylineService;

    @GetMapping
    @ApiOperation("Finding latLngs in the path between two given points")
    public ArrayList<LatLng> getList(@RequestParam(value = "origin_lat") Double originLat,
                                     @RequestParam(value = "destination_lat") Double destLat,
                                     @RequestParam(value = "origin_lng") Double originLng,
                                     @RequestParam(value = "destination_lng") Double destLng,
                                     @RequestParam(value = "distance") Integer distance) throws IOException, ApiException {
        return polylineService.getList(originLat,originLng,destLat,destLng,distance);
    }
    @GetMapping(value = "/lat-lng" )
    @ApiOperation("Finding latLngs in the path between two given points")
    public ArrayList<LatLng> getLatLngPairs(@RequestParam(value = "origin_lat") Double originLat,
                                                @RequestParam(value = "destination_lat") Double destLat,
                                                @RequestParam(value = "origin_lng") Double originLng,
                                                @RequestParam(value = "destination_lng") Double destLng,
                                                @RequestParam(value = "distance") Integer distance) throws IOException {
        LatLng originLatLng = new LatLng(originLat, originLng);
        LatLng destLatLng = new LatLng(destLat, destLng);
        LatLngRequest latLngRequest  = new LatLngRequest();
        latLngRequest.setOriginLatLng(originLatLng);
        latLngRequest.setDestinationLatLng(destLatLng);
        return googleDirectionService.getLatLngPairs(latLngRequest,distance);
    }

}
