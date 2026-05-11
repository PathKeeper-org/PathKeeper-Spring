package com.pathkeeper.backend.domain.locationLog.dto;

import com.pathkeeper.backend.controller.LocationLog.dto.LocationSaveRequest;

public record LocationSaveCommand(Double lat, Double lng, Integer batteryLevel) {

    public static LocationSaveCommand from(LocationSaveRequest request) {
        return new LocationSaveCommand(request.lat(), request.lng(), request.batteryLevel());
    }
}