package com.adrianlyjak.shout.models;

import android.util.Log;

public class ArcDistance {
	public final static int EARTH_RADIUS_METERS = 6371000;
	public final static int MAX_METERS = 20000000;
	public final static int MIN_METERS = 16;
	public final static double CURVE = 8.000d;
	private final static String TAG = "ArcDistance";
	public double radians;

	public ArcDistance(double radians) {
		setRadians(radians);
	}

	public ArcDistance() {
	}

	public ArcDistance(ArcDistance other) {
		this.radians = other.radians;
	}

	public double dividedBy(ArcDistance other) {
		return this.radians / other.radians;
	}

	public void setRadians(double radians) {
		this.radians = radians;
	};

	public void setMeters(double meters) {
		this.radians = meters / EARTH_RADIUS_METERS;
	}

	public void setPercent(int percent) {
		/*
		 * (25 / 100 * 400 - 10) + 10 meters = (percent^4 / 100^4) * (max - min)
		 * + min
		 */
		double multiplier = Math.pow((double) percent, CURVE)
				/ Math.pow(100.00d, CURVE);
		Log.v(TAG, "setting percent to: " + percent + " via multiplier: "
				+ multiplier);
		setMeters(((MAX_METERS - MIN_METERS) * multiplier) + MIN_METERS);
	}

	public double meters() {
		return radians * EARTH_RADIUS_METERS;
	}

	public int percent() {
		/*
		 * ((meters - min) / (max - min) * 100^4)^0.25 = percent
		 */
		double meters = meters();
		if (meters < MIN_METERS) {
			return 0;
		}
		return (int) (Math.pow((meters - MIN_METERS)
				/ (MAX_METERS - MIN_METERS) * Math.pow(100, CURVE),
				1.000d / CURVE));
	}

	public String metersToString() {
		int meters = (int) Math.round(meters());
		if (meters < 1000) {
			return Integer.toString(meters) + "m";
		} else if (meters < 1000000) {
			return Double.toString(((meters / 100) / 10.0d)) + "km";
		} else {
			return Integer.toString((meters / 1000)) + "km";
		}
	}

	public int calculateZoomLevel(int screenWidth) {
		double equatorLength = 40075004; // in meters
		double widthInPixels = screenWidth;
		double metersPerPixel = equatorLength / 256;
		int zoomLevel = 1;
		while ((metersPerPixel * widthInPixels) > 2000) {
			metersPerPixel /= 2;
			++zoomLevel;
		}
		Log.i("ADNAN", "zoom level = " + zoomLevel);
		return zoomLevel;
	}
}
