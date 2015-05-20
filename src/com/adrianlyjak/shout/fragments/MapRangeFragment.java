package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.fragments.GoogleMapFragment.OnGoogleMapFragmentListener;
import com.adrianlyjak.shout.models.ArcDistance;
import com.adrianlyjak.shout.service.MessageService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapRangeFragment extends ShoutFragment implements
		OnGoogleMapFragmentListener {
	final String TAG = "RadiusPreferenceFragment";
	public ArcDistance shoutRadius;
	private GoogleMap map;
	private Circle circle;
	private ValueAnimator circleAnimator;
	private Marker locationMarker;
	private View rootView;

	private CameraUpdate newCameraBounds() {
		float zoomLevel = 15.0f;
		if (circle != null) {
			double radius = shoutRadius.meters();
			double scale = radius / 225;
			zoomLevel = (float) (16 - Math.log(scale) / Math.log(2));
		}
		return CameraUpdateFactory.newLatLngZoom(
				messageService.location.coordinates, (zoomLevel));

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		shoutRadius = new ArcDistance(0d);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onServiceConnected(MessageService ms) {
		super.onServiceConnected(ms);
		if (map == null) {
			Log.w("PlacholderFragment", "WTF map is null");
			return;
		}
		shoutRadius.setRadians(messageService.shoutRadius.radians);

		circle = map.addCircle(new CircleOptions()
				.center(messageService.location.coordinates)
				.radius(shoutRadius.meters()).strokeWidth(15)
				.strokeColor(Color.argb(127, 247, 180, 35)));
		circle.setVisible(true);
		locationMarker = map.addMarker(new MarkerOptions()
				.position(messageService.location.coordinates)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_map_marker))
				.anchor(0.5f, 0.5f));
		updateViews();
		setMapLocation();
	}

	@Override
	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = super
				.setUpFilteredActions();
		filteredActions.put(Constants.SHOUT_RADIUS_CHANGED,
				new BroadcastCallback() {
					@Override
					public void doActionFor(Intent intent) {
						shoutRadius
								.setRadians(messageService.shoutRadius.radians);
						updateViews();

					}
				});
		filteredActions.put(Constants.SHOUT_LOCATION_CHANGED,
				new BroadcastCallback() {

					@Override
					public void doActionFor(Intent intent) {
						MapRangeFragment.this.updateViews();
					}
				});
		return filteredActions;
	}

	private void setMapLocation() {
		map.moveCamera(newCameraBounds());
		circle.setCenter(messageService.location.coordinates);
		locationMarker.setPosition(messageService.location.coordinates);
	}

	private void updateViews() {
		map.animateCamera(newCameraBounds());
		animateCircle();
	}

	private double radiusStart;
	private double radiusDif;
	private double latStart;
	private double latDif;
	private double longStart;
	private double longDif;

	private void animateCircle() {
		int duration = 0;
		if (circleAnimator != null) {
			duration = 1000 - (int) circleAnimator.getCurrentPlayTime();
			circleAnimator.cancel();
		}
		if (duration <= 0) {
			duration = 1000;
		}
		circleAnimator = new ValueAnimator();
		radiusStart = circle.getRadius();
		radiusDif = (shoutRadius.meters() - radiusStart);
		latStart = (circle.getCenter().latitude);
		latDif = messageService.location.coordinates.latitude - latStart;
		longStart = (circle.getCenter().longitude);
		longDif = messageService.location.coordinates.longitude - longStart;
		circleAnimator.setFloatValues(0.00f, 1.00f);
		circleAnimator.setDuration(duration);
		circleAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						Float floatPercent = (Float) (valueAnimator
								.getAnimatedFraction());
						double percent = floatPercent;
						if (latDif != 0d | longDif != 0d) {
							LatLng center = new LatLng(
									(latStart + (latDif * percent)), longStart
											+ (longDif * percent));
							circle.setCenter(center);
							locationMarker.setPosition(center);
						}
						if (radiusDif != 0d) {
							float meters = (float) (radiusStart + (radiusDif * percent));
							circle.setRadius(meters);
						}
					}
				});
		circleAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				MapRangeFragment.this.circleAnimator = null;

			}

			@Override
			public void onAnimationCancel(Animator animation) {
				MapRangeFragment.this.circleAnimator = null;

			}
		});
		circleAnimator.start();
	}

	GoogleMapFragment fragment;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getActivity().getSupportFragmentManager();
		// fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_frame);
		// if (fragment == null) {
		fragment = GoogleMapFragment.newInstance(this);
		fm.beginTransaction().replace(rootView.getId(), fragment).commit();
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_map_range, container,
				false);
		rootView.setId(rootView.getId() + instId);
		return rootView;
	}

	public void onMapReady(GoogleMap newMap) {
		map = newMap;
		LatLng location;
		if (messageService != null) {
			location = messageService.location.coordinates;
		} else {
			location = new LatLng(0, 0);
		}
		map.addMarker(new MarkerOptions().position(location));
		map.getUiSettings().setScrollGesturesEnabled(false);
		map.getUiSettings().setZoomControlsEnabled(false);
	}
}
