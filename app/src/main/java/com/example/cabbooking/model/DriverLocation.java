package com.example.cabbooking.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Data model for driver location
 */
public class DriverLocation {

    @DocumentId
    private String docId;
    private Double currentPositionLatitude;
    private Double currentPositionLongitude;

    public DriverLocation(){

    }

    public DriverLocation(String docId, Double currentPositionLatitude, Double currentPositionLongitude) {
        this.docId = docId;
        this.currentPositionLatitude = currentPositionLatitude;
        this.currentPositionLongitude = currentPositionLongitude;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Double getCurrentPositionLatitude() {
        return currentPositionLatitude;
    }

    public void setCurrentPositionLatitude(Double currentPositionLatitude) {
        this.currentPositionLatitude = currentPositionLatitude;
    }

    public Double getCurrentPositionLongitude() {
        return currentPositionLongitude;
    }

    public void setCurrentPositionLongitude(Double currentPositionLongitude) {
        this.currentPositionLongitude = currentPositionLongitude;
    }
}
