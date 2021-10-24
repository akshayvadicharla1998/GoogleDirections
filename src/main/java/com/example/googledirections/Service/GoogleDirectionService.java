package com.example.googledirections.Service;

import com.example.googledirections.DataTypes.LatLngWithDistance;
import com.example.googledirections.requests.LatLngRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class GoogleDirectionService {

    @Value("${GOOGLE_MAPS_BASE_URL}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    public ArrayList<LatLng> getLatLngPairs(LatLngRequest latLngRequest, Integer dist) throws IOException {
        ResponseEntity<JSONObject> response = null;
        ArrayList<LatLngWithDistance> latLngList = new ArrayList<LatLngWithDistance>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("origin", latLngRequest.getOriginLatLng().toString());
        params.put("destination", latLngRequest.getDestinationLatLng().toString());
        params.put("key","AIzaSyAEQvKUVouPDENLkQlCF6AAap1Ze-6zMos");
        try{
            response = restTemplate.exchange(baseUrl + "/api/directions/json?origin={origin}&destination={destination}&key={key}",
                HttpMethod.GET, null, JSONObject.class, params);
        if(Objects.nonNull(response) && Objects.nonNull(response.getBody())){
            JSONObject jsonResponse = mapper.readValue(Objects.toString(response.getBody(), StringUtils.EMPTY),
                    JSONObject.class);
            if(jsonResponse.containsKey("routes")){
                ArrayList<LinkedHashMap> routes = (ArrayList<LinkedHashMap>) jsonResponse.get("routes");
                for(LinkedHashMap list : routes) {
                    if(list.containsKey("legs")){
                        ArrayList<LinkedHashMap> legs = (ArrayList<LinkedHashMap>) list.get("legs");
                        for(LinkedHashMap leg : legs){
                            if(leg.containsKey("steps")){
                                ArrayList<LinkedHashMap> steps = (ArrayList<LinkedHashMap>) leg.get("steps");

                                for(LinkedHashMap step : steps){
                                    LatLngWithDistance latLngPair = new LatLngWithDistance();
                                    if(step.containsKey("distance")) {
                                        LinkedHashMap distance = (LinkedHashMap) step.get("distance");
                                        latLngPair.setDistance((Integer) distance.get("value"));
                                    }
                                    if(step.containsKey("start_location")) {
                                        LinkedHashMap startLocation = (LinkedHashMap) step.get("start_location");
                                        latLngPair.setStartLat((Double) startLocation.get("lat"));
                                        latLngPair.setStartLng((Double) startLocation.get("lng"));
                                    }
                                    latLngList.add(latLngPair);
                                }
                            }
                        }
                    }
                }
            }

        }
        } catch (RestClientException e){
            log.info("failed in fetching directions");
        }
        return findingIntermediatePointsAtXDistance(latLngList,dist);
    }

    private ArrayList<LatLng> findingIntermediatePointsAtXDistance(ArrayList<LatLngWithDistance> latLngList, Integer meters) {
        ArrayList<LatLng> latLngPairs = new ArrayList<LatLng>();
        if(meters < 0){
            return latLngPairs;
        }
        float next = meters.floatValue();
        double distance = 0.0;
        double oldDistance = 0.0;
        for(int i = 1;i < latLngList.size();i++){
            oldDistance = distance;
            distance = distance + latLngList.get(i-1).getDistance();
            while(distance > next){
                Double lat1 = latLngList.get(i-1).getStartLat();
                Double lng1 = latLngList.get(i-1).getStartLng();
                Double lat2 = latLngList.get(i).getStartLat();
                Double lng2 = latLngList.get(i).getStartLng();
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
}

