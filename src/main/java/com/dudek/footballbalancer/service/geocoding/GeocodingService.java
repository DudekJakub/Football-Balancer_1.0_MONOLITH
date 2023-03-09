package com.dudek.footballbalancer.service.geocoding;

import com.google.maps.model.LatLng;

public interface GeocodingService {
    LatLng getLatLng(String address) throws Exception;
}
