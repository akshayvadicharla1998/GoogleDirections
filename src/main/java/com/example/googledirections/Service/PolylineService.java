package com.example.googledirections.Service;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.EncodedPolyline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PolylineService {

    public ArrayList<LatLng> getList(Double originLat, Double originLng, Double destLat, Double destLng, Integer distance) throws ApiException, IOException {
    String origin = originLat.toString()+","+originLng.toString();
    String dest = destLat.toString() + "," + destLng.toString();
    List<LatLng> path = new ArrayList();
    GeoApiContext context = new GeoApiContext.Builder()
            .apiKey("AIzaSyAEQvKUVouPDENLkQlCF6AAap1Ze-6zMos")
            .build();
    DirectionsApiRequest req = DirectionsApi.getDirections(context, origin, dest);
        DirectionsResult res = null;
        try {
            res = req.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    path.add(new LatLng(originLat, originLng));
        try{
        //Loop through legs and steps to get encoded polylines of each step
        if (res.routes != null && res.routes.length > 0) {
            DirectionsRoute route = res.routes[0];

            if (route.legs != null) {
                for (int i = 0; i < route.legs.length; i++) {
                    DirectionsLeg leg = route.legs[i];
                    if (leg.steps != null) {
                        for (int j = 0; j < leg.steps.length; j++) {
                            DirectionsStep step = leg.steps[j];
                            if (step.steps != null && step.steps.length > 0) {
                                for (int k = 0; k < step.steps.length; k++) {
                                    DirectionsStep step1 = step.steps[k];
                                    EncodedPolyline points1 = step1.polyline;
                                    if (points1 != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                        for (com.google.maps.model.LatLng coord1 : coords1) {
                                            path.add(new LatLng(coord1.lat, coord1.lng));
                                        }
                                    }
                                }
                            } else {
                                EncodedPolyline points = step.polyline;
                                if (points != null) {
                                    //Decode polyline and add points to list of route coordinates
                                    List<com.google.maps.model.LatLng> coords = points.decodePath();
                                    for (com.google.maps.model.LatLng coord : coords) {
                                        path.add(new LatLng(coord.lat, coord.lng));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (Exception E){
            log.info("error");
        }
        path.add(new LatLng(destLat,destLng));
        return getDesiredPoints((ArrayList<LatLng>) path,distance);
    }
    private ArrayList<LatLng> getDesiredPoints(ArrayList<LatLng> path,Integer meters) {
        ArrayList<LatLng> latLngPairs = new ArrayList<>();
        double distance = 0;
        int next = meters;
        double oldDistance = 0.0;
        for(int i = 1;i< path.size();i++){
            oldDistance = distance;
            distance = distance + distance(path.get(i).lat,path.get(i).lng,path.get(i-1).lat,path.get(i-1).lng);
            while(distance>next){
                Double lat1 = path.get(i-1).lat;
                Double lng1 = path.get(i-1).lng;
                Double lat2 = path.get(i).lat;
                Double lng2 = path.get(i).lng;
                float multiplier = (float) ((next-oldDistance)/(distance-oldDistance));
                Double stalat = lat1 + (lat2-lat1)*multiplier;
                stalat = Double.valueOf(String.format("%.5f", stalat));
                Double stalng = Double.valueOf(String.valueOf(lng1 + (lng2-lng1)*multiplier));
                stalng = Double.valueOf(String.format("%.5f", stalng));
                LatLng latLng = new LatLng(stalat, stalng);
                latLngPairs.add(latLng);
                next = next + meters;
            }
        }
        return latLngPairs;
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist*1000);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
