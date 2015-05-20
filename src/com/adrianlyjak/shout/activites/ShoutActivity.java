package com.adrianlyjak.shout.activites;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.MessageServiceConnection;
import com.adrianlyjak.shout.MessageServiceConnectionListener;
import com.adrianlyjak.shout.fragments.ShoutFragment;
import com.adrianlyjak.shout.service.MessageService;
import com.adrianlyjak.shout.service.MessageService.LocalBinder;

public class ShoutActivity extends FragmentActivity implements MessageServiceConnectionListener {

	private static final String TAG = "AbstractShoutActivity.";
	boolean mBound = false;
	HashMap<Integer, ShoutFragment> fragments;
	public MessageService messageService;
	protected HashMap<String, BroadcastCallback> filteredActions = new HashMap<String, BroadcastCallback>();

	public MessageServiceConnection mServiceConnection;

	public void onServiceConnected(MessageService ms) {
		messageService = ms;
		mBound = true;
		messageService.shoutActivityCreated(this);
//		for (ShoutFragment fragment : fragments.values()) {
//			fragment.onServiceConnected(ms);
//		}
	}
	public void onServiceDisconnected() {
		mBound = false;
//		for (ShoutFragment fragment : fragments.values()) {
//			fragment.onServiceDisconnected();
//		}
	}

	protected boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	protected boolean isActive() {
		return messageService.shoutActivities.contains(this);
	}


	protected void showDialogFor(Intent warning) {
		Log.w(TAG,
				"Got warning of class"
						+ warning.getExtras().getString(
								Constants.SHOUT_EXCEPTION_CLASS)
						+ " with message "
						+ warning.getExtras().getString(
								Constants.SHOUT_EXCEPTION_MESSAGE));
		if (isActive()) {
			showOkDialog(
					warning.getExtras().getString(
							Constants.SHOUT_EXCEPTION_CLASS),
					warning.getExtras().getString(
							Constants.SHOUT_EXCEPTION_MESSAGE));
		}

	}

	protected HashMap<Integer, ShoutFragment> createFragments() {
		fragments = new HashMap<Integer, ShoutFragment>();
		return fragments;
	}

	protected void attachFragments() {
		for (Entry<Integer, ShoutFragment> entry : fragments.entrySet()) {
			getSupportFragmentManager().beginTransaction()
					.add(entry.getKey(), entry.getValue()).commit();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpBroadcastCallbacks();
		Log.v(TAG, "on create binding to message service");
		mServiceConnection = new MessageServiceConnection(this);
		mServiceConnection.connectFrom(this);
		createFragments();
		if (savedInstanceState == null) {
			attachFragments();
		}
	}

	private void setUpBroadcastCallbacks() {
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				Log.v(TAG, "searching for action for " + intent.getAction());
				doActionFor(intent);
			}
		};
		IntentFilter filter = new IntentFilter();
		filteredActions = setUpFilteredActions();
		for (String actionName : filteredActions.keySet()) {
			filter.addAction(actionName);
		}
		LocalBroadcastManager.getInstance(this).registerReceiver(br, filter);
	}

	protected void doActionFor(Intent intent) {
		BroadcastCallback action = filteredActions.get(intent.getAction());
		if (action != null) {
			action.doActionFor(intent);
		}
	};

	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = new HashMap<String, BroadcastCallback>();
		filteredActions.put(Constants.SHOUT_EXCEPTION, new BroadcastCallback() {
			@Override
			public void doActionFor(Intent intent) {
				showDialogFor(intent);
			}
		});
		return filteredActions;
	}

	public void showOkDialog(String title, String description) {
		showOkDialog(title, description, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
	}

	protected void showErrorDialogAndQuit(String title, String description) {
		showOkDialog(title, description, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				moveTaskToBack(true);
			}
		});

	}

	protected void showOkDialog(String title, String description,
			OnClickListener callback) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setTitle(title);

		alertDialogBuilder.setMessage(description).setPositiveButton("OK",
				callback);

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (messageService != null) {
			final MessageService ms = messageService;
			final ShoutActivity activity = this;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					ms.shoutActivityDestroyed(activity);
				}
			}, 1000);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (messageService != null) {
			messageService.shoutActivityCreated(this);
		}
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "unbinding message service: " + mServiceConnection);
		unbindService(mServiceConnection);
		super.onDestroy();
	}
	@Override
	public void setService(MessageService service) {
		messageService = service;
	}
}
