package com.dudek.footballbalancer.service.geocoding;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service(value = "GoogleMaps")
public class GoogleMapsGeocodingService implements GeocodingService {

    private final GeoApiContext context;

    @Value("${googleApiKey:default}")
    String googleMapsApiKey;

    public GoogleMapsGeocodingService() {
        System.out.println(googleMapsApiKey);
        this.context = new GeoApiContext.Builder()
                .apiKey("AIzaSyBxj3q-F4tZMmiZhsiagyvTKXfFoazQwvE")
                .build();
    }

    @Override
    public LatLng getLatLng(String address) throws Exception {
        GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

        if (results.length > 0) {
            return results[0].geometry.location;
        } else {
            throw new Exception("No results found for address: " + address);
        }
    }
}
