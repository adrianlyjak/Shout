package com.adrianlyjak.shout.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class GeoLocation {
	public LatLng coordinates;
	private double degreesToRadians = Math.PI / 180.0d;

	public GeoLocation(double longitude, double latitude) {
		coordinates = new LatLng(latitude, longitude);
	}

	public GeoLocation(Location location) {
		coordinates = new LatLng(location.getLatitude(),
				location.getLongitude());
	}

	public double phi() {
		return (90.0d - coordinates.latitude) * degreesToRadians;
	}

	public double theta() {
		return coordinates.longitude * degreesToRadians;
	}

	public ArcDistance distanceBetween(GeoLocation other) {
		double cos = Math.sin(this.phi()) * Math.sin(other.phi())
				* Math.cos(this.theta() - other.theta()) + Math.cos(this.phi())
				* Math.cos(other.phi());
		double arc = Math.acos(cos);
		return new ArcDistance(arc);
	}

	public boolean shouldUpdateFrom(Location location) {
		return coordinates.equals(new LatLng(location.getLatitude(), location
				.getLongitude()));
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		try {
			json.put("type", "Point");
			JSONArray coordinates = new JSONArray(new ArrayList<Double>());
			coordinates.put(0, this.coordinates.longitude);
			coordinates.put(1, this.coordinates.latitude);
			json.put("coordinates", coordinates);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

}
