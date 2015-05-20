package com.adrianlyjak.shout.service;

import android.location.Location;

import com.google.android.gms.location.LocationClient;

public class LocationHandler {
	
	MessageService messageService;
	LocationClient locationClient;
	Location location;
	
	public LocationHandler(MessageService service) {
		messageService = service;
	}
}
