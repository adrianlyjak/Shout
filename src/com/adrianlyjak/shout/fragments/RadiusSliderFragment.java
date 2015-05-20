package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.models.ArcDistance;
import com.adrianlyjak.shout.service.MessageService;

public class RadiusSliderFragment extends ShoutFragment {
	final String TAG = "RadiusPreferenceFragment";
	public ArcDistance shoutRadius;
	private SeekBar slideInput;
	private View rootView;
	private boolean isTracking = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shoutRadius = new ArcDistance(0d);
	}

	@Override
	public void onServiceConnected(MessageService ms) {
		super.onServiceConnected(ms);
		updateShoutRadius();
		updateViews();
	}

	@Override
	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = super
				.setUpFilteredActions();
		filteredActions.put(Constants.SHOUT_RADIUS_CHANGED,
				new BroadcastCallback() {
					@Override
					public void doActionFor(Intent intent) {
						if (!isTracking) {
							updateShoutRadius();
							updateViews();
						}
					}

				});
		return filteredActions;
	}

	private void updateViews() {
		slideInput.setProgress(shoutRadius.percent());
	}

	private void updateShoutRadius() {
		shoutRadius.setRadians(messageService.shoutRadius.radians);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_radius_slider, container,
				false);

		slideInput = (SeekBar) rootView.findViewById(R.id.shout_radius_slider);
		slideInput.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				if (shoutRadius == null)
					return;
				if (progress != shoutRadius.percent()) {
					shoutRadius.setPercent(progress);
					messageService.setShoutRadius(shoutRadius);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				isTracking = true;
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(TAG, "Tracking off");
				messageService.sendShoutRadius();
				isTracking = false;
			}
		});
		return rootView;
	}

}
