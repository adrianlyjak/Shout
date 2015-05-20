package com.adrianlyjak.shout.activites;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.service.MessageService;
import com.adrianlyjak.shout.service.MessageService.LocalBinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class SplashScreen extends ShoutActivity {

	String TAG = "Splash";
	MessageService messageService;
	boolean bound;
	LocalBroadcastManager broadcastManager;
	BroadcastReceiver broadcastReceiver;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection serviceConnectionCallbacks = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			messageService = binder.getService();
			bound = true;
			if (messageService.isInitialized) {
				startMainActivity();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};

	private boolean checkGooglePlayServices() {
		int hasGooglePlayServicesUtil = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (hasGooglePlayServicesUtil == ConnectionResult.SUCCESS) {
			Log.v(TAG, "Google play services is installed");
			return true;
		}
		Log.v(TAG, "Google play services is not installed");
		showErrorDialogAndQuit("Google Play Services not installed",
				"You need to install google play services in order to proceed");
		return false;

	}

	@Override
	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = super
				.setUpFilteredActions();
		filteredActions.put(Constants.SHOUT_READY, new BroadcastCallback() {
			@Override
			public void doActionFor(Intent intent) {
				startMainActivity();
			}
		});
		filteredActions.put(Constants.SHOUT_LOCATION_NOT_FOUND,
				new BroadcastCallback() {
					@Override
					public void doActionFor(Intent intent) {
						showErrorDialogAndQuit(
								Constants.SHOUT_LOCATION_NOT_FOUND,
								"You're location could not be found. To continue, enable location services and restart the app");

					}
				});
		return filteredActions;
	}

	private void startMainActivity() {
		Log.v(TAG, "starting MainActivity");
		startActivity(new Intent(SplashScreen.this, MainActivity.class));
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "On splash screen.");

		// Start the MessageService

		Intent intent = new Intent(this, MessageService.class);
		if (!isServiceRunning(MessageService.class)) {
			startService(intent);
		}
		bindService(intent, serviceConnectionCallbacks, 0);
		super.onCreate(savedInstanceState);

		// Make the activity view pretty
		setContentView(R.layout.activity_splash_screen);
		if (!checkGooglePlayServices()) {
			return;
		}
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy() in SplashScreen.");
		unbindService(serviceConnectionCallbacks);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

}
