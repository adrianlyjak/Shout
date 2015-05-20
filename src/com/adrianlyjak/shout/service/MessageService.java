package com.adrianlyjak.shout.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.activites.MainActivity;
import com.adrianlyjak.shout.activites.ShoutActivity;
import com.adrianlyjak.shout.models.ArcDistance;
import com.adrianlyjak.shout.models.ChatMessage;
import com.adrianlyjak.shout.models.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class MessageService extends Service {
	LocationClient locationClient;
	WebSocketHandler webSocketHandler;
	String TAG = "MessageService";
	LocalBroadcastManager broadcastManager;
	private boolean isLoggedIn = false;
	public boolean isInitialized = false;
	public GeoLocation location;
	public ArcDistance shoutRadius = new ArcDistance(-1.0d);
	public ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	public ArrayList<ChatMessage> recentMessages = new ArrayList<ChatMessage>();
	private int reconnectDelay = 0;
	public Set<ShoutActivity> shoutActivities = new HashSet<ShoutActivity>();
	private boolean isBackground = true;
	private LocationListener locationListener;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notificationBuilder;
	int notifyID = 8493729;
	public int shoutusers = 0;

	public final static String deviceID = "35"
			+ // we make this look like a valid IMEI
			Build.BOARD.length() % 10 + Build.BRAND.length() % 10
			+ Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
			+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
			+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
			+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
			+ Build.TAGS.length() % 10 + Build.TYPE.length() % 10
			+ Build.USER.length() % 10;

	public class LocalBinder extends Binder {
		public MessageService getService() {
			return MessageService.this;
		}
	}

	private void sendNotification() {
		int size = recentMessages.size();
		notificationBuilder.setContentText("\""
				+ (recentMessages.get(size - 1).description) + "\"");
		if (size == 1) {
			long[] pattern = { 0, 300 };
			notificationBuilder.setContentTitle("Shout: New Message")
					.setVibrate(pattern);

		} else {
			notificationBuilder.setContentTitle("Shout: " + size + " Messages")
					.setVibrate(null);
		}
		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setNumber(recentMessages.size()).setContentIntent(
				resultPendingIntent);
		notificationManager.notify(notifyID, notificationBuilder.build());
	}

	private final IBinder mBinder = new LocalBinder();

	public void shoutActivityCreated(ShoutActivity activity) {
		Log.v(TAG, String.format("adding activity %s", activity));
		if (shoutActivities.size() == 0) {
			Log.v(TAG,
					"Shout Activities are starting back up... I should switch to foreground behaviour");
			onMovedToForeground();
		}
		shoutActivities.add(activity);

	}

	public boolean shoutActivityDestroyed(ShoutActivity activity) {
		Log.v(TAG, String.format("removing activity %s", activity));
		boolean removed = shoutActivities.remove(activity);
		if (removed && shoutActivities.size() == 0) {
			Log.v(TAG,
					"Shout Activities are gone... I should switch to background behaviour");
			onMovedToBackground();
		}
		return removed;
	}

	private void onMovedToBackground() {
		Log.v(TAG, "Moved to background");
		isBackground = true;
		setLocationUpdates();
	}

	private void onMovedToForeground() {
		Log.v(TAG, "Moved to foreground");
		isBackground = false;
		notificationManager.cancelAll();
		setLocationUpdates();
	}

	private void setLocationUpdates() {
		if (!locationClient.isConnected()) {
			return;
		}
		if (locationListener != null) {
			locationClient.removeLocationUpdates(locationListener);
		}
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				MessageService ms = MessageService.this;
				GeoLocation newLocation = new GeoLocation(location);
				if (ms.location == null
						|| ms.location.distanceBetween(newLocation).meters() >= 1.0d)
					ms.onLocationChanged(location);
			}
		};
		LocationRequest locationRequest = LocationRequest.create();
		if (isBackground) {
			locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
			locationRequest.setInterval(120000);
		} else {
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setInterval(1000);
		}

		locationClient
				.requestLocationUpdates(locationRequest, locationListener);
	}

	public void checkInitialization() {
		Log.v(TAG, "checking initialization isLoggedIn: " + isLoggedIn
				+ " has location " + (location != null)
				+ " webSocketHandler isConnected "
				+ webSocketHandler.isConnected + " isInitialized "
				+ isInitialized + " messages size " + messages.size());
		if (!isLoggedIn) {
			if (location != null && webSocketHandler.isConnected) {
				reconnectDelay = 0;
				sendHello(true);
			}
		} else {
			if (!isInitialized && shoutRadius.radians != -1.0d) {
				initialized();
			}
		}
	}

	private void initialized() {
		isInitialized = true;
		broadcastManager.sendBroadcast(new Intent(Constants.SHOUT_READY));
	}

	private JSONObject deviceInfo() {
		JSONObject deviceInfo = new JSONObject();
		try {
			deviceInfo.put("geometry", location.toJSONObject());
			deviceInfo.put("deviceID", deviceID);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return deviceInfo;
	}

	public void onLocationChanged(Location location) {
		Log.v(TAG, location.toString());
		this.location = new GeoLocation(location);
		if (!isLoggedIn) {
			checkInitialization();
		} else {
			sendHello(false);
		}
		Log.v(TAG, "location changed");
		broadcastManager.sendBroadcast(new Intent(
				Constants.SHOUT_LOCATION_CHANGED));
	}

	ConnectionCallbacks connectionCallbacks = new ConnectionCallbacks() {
		@Override
		public void onConnected(Bundle aBundle) {
			// TODO Auto-generated method stub
			Log.v(TAG, "Connected to mLocation client.");
			setLocationUpdates();

			// initialize with last location if there is one (at least for the
			// sake of emulator testing-- still requires a location mocker)
			Location lastLocation = locationClient.getLastLocation();
			if (lastLocation != null) {
				MessageService.this.onLocationChanged(lastLocation);
			} else {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (location == null) {
							Log.w(TAG, "No location found");
							broadcastManager.sendBroadcast(new Intent(
									Constants.SHOUT_LOCATION_NOT_FOUND));
						}
					}
				}, 10000);
			}
		}

		@Override
		public void onDisconnected() {
			// TODO Auto-generated method stub
			Log.v(TAG, "location client disconnected");
		}
	};
	/*
	 * public void frequentUpdates (boolean frequent) { if (frequent) period =
	 * 5000; else period = 30*1000; }
	 */
	OnConnectionFailedListener connectionFailedListener = new OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			Log.v(TAG, "Could not connect to location service.");
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "Bound.");
		locationClient.connect();
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationClient.connect();
		webSocketHandler.connect();
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "Created.");
		createNotification();
		locationClient = new LocationClient(this, connectionCallbacks,
				connectionFailedListener);
		webSocketHandler = new WebSocketHandler(this);
		broadcastManager = LocalBroadcastManager.getInstance(this);
	}

	private void createNotification() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle("New Message")
				.setContentText("You've received new messages.")
				.setSmallIcon(R.drawable.ic_shout_notification)
				.setAutoCancel(true);
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "Message Service Destroyed.");
		// Shutting down the handler.
		webSocketHandler.close();
		super.onDestroy();
	}

	public void addmessage(String description) {
		try {
			webSocketHandler.send(new JSONObject().put("addmessage",
					new JSONArray().put(0, description)));
			messages.add(new ChatMessage(this, description));
			broadcastManager.sendBroadcast(new Intent(
					Constants.SHOUT_SENT_MESSAGE));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void handleServerCommand(JSONObject command) {
		try {
			if (command.opt("shoutRadius") != null) {
				handleShoutRadius(command.getJSONArray("shoutRadius").getInt(0));
			} else if (command.opt("ping") != null) {
				// whatever
			} else if (command.opt("relogin") != null) {
				Log.v(TAG, "Server lost track of us... relogging in");
				isLoggedIn(false);
				checkInitialization();
			} else if (command.opt("shoutusers") != null) {
				shoutusers = command.getJSONArray("shoutusers").getInt(0);
				broadcastManager.sendBroadcast(new Intent(
						Constants.SHOUT_USERS_CHANGED));
			} else if (command.opt("messages") != null) {
				JSONArray messages = command.getJSONArray("messages")
						.getJSONArray(0);
				if (messages.length() > 0) {
					for (int i = 0; i < messages.length(); i++) {
						handleMessage(messages.getJSONObject(i));
					}

					if (isBackground) {
						Log.v(TAG, "received messages and sending notification");
						sendNotification();
					} else {
						Log.v(TAG, "received messages and sending broadcast");
						broadcastManager.sendBroadcast(new Intent(
								Constants.SHOUT_RECEIVED_MESSAGES));

					}
				}
			} else {
				Log.w(TAG, "unexpected server command: " + command.toString());
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleMessage(JSONObject message) {
		ChatMessage msg = new ChatMessage(this, message);
		if (!messages.contains(msg)) {
			boolean sentFromHere = false;
			for (ChatMessage current : messages) {
				if (current.shouldUpdateFrom(msg)) {
					current.updateFrom(msg);
					sentFromHere = true;
					Log.v(TAG,
							"found a match for msg from here... updating it from"
									+ msg.description);
					break;
				}
			}
			if (!sentFromHere) {
				Log.v(TAG, "adding msg: " + msg.description);
				messages.add(msg);
				recentMessages.add(msg);
			}
		}
	}

	private void handleShoutRadius(int meters) {
		if (shoutRadius.radians < 0) {
			ArcDistance range = new ArcDistance();
			range.setMeters(meters);
			setShoutRadius(range);
		} else {
			// must've changed radius when connection was lost. Update it.
			sendShoutRadius();
		}
		checkInitialization();
	}

	public void lostConnection() {
		isLoggedIn(false);
		shoutusers = 0;
		try {
			Thread.sleep(reconnectDelay);
		} catch (InterruptedException e) {

		}
		webSocketHandler.connect();
		if (reconnectDelay == 0) {
			reconnectDelay++;
		} else if (reconnectDelay < 10000) {
			reconnectDelay = Math.min(reconnectDelay * 2, 10000);
		}

	}

	public void setShoutRadius(ArcDistance range) {
		shoutRadius.setRadians(range.radians);
		broadcastManager.sendBroadcast(new Intent(
				Constants.SHOUT_RADIUS_CHANGED));
		Log.v(TAG, "shout set to: " + shoutRadius.meters() + " from percent: "
				+ range.percent());
	}

	public void sendShoutRadius() {
		try {
			webSocketHandler.send(new JSONObject().put("shoutRadius",
					new JSONArray().put(0, shoutRadius.meters())));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendHello(boolean isLogin) {
		JSONObject command = new JSONObject();
		JSONArray args = new JSONArray();
		try {
			args.put(0, deviceInfo());
			args.put(1, isLogin);
			command.put("hello", args);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		webSocketHandler.send(command);
		isLoggedIn(true);
	}

	private void isLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		broadcastManager.sendBroadcast(new Intent(
				Constants.SHOUT_CONNECTION_CHANGED));
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void broadcastException(Exception e) {
		String className = e.getClass().getName();
		String eMessage = e.getMessage();
		broadcastWarning(className, eMessage);
	}

	private void broadcastWarning(String title, String description) {
		broadcastManager.sendBroadcast(new Intent(Constants.SHOUT_EXCEPTION)
				.putExtra(Constants.SHOUT_EXCEPTION_MESSAGE, description)
				.putExtra(Constants.SHOUT_EXCEPTION_CLASS, title));
	}
}
