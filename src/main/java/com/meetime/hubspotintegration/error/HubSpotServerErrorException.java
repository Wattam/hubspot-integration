package com.meetime.hubspotintegration.error;

public class HubSpotServerErrorException extends RuntimeException {

    public HubSpotServerErrorException(String message) {
        super("Server error on HubSpot's side: " + message);
    }
}
