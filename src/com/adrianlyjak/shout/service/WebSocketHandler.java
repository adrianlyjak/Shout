package com.adrianlyjak.shout.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adrianlyjak.shout.Constants;


public class WebSocketHandler {

	MessageService messageService;
	String TAG = "WebSocketHandler.";
	Location location;
	double FIXED_RANGE = 5;
	TelephonyManager tm;
	String postMessage = "";
	WebSocketClient ws;
	JSONArray messages = null;
	public boolean isClosed = true;
	public boolean isConnected = false;
	
	
	
	public WebSocketHandler(MessageService messageService) {
		this.messageService = messageService;
	}
	
	public void connect() {
		isClosed = false;
		URI server = null;
		try {
			server = new URI(Constants.SERVER_URL);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		Log.v(TAG, "Trying initialize WebSocket to " + server);
		ws = new WebSocketClient(server) {
			
			@Override
			public void onOpen(ServerHandshake arg0) {
				// TODO Auto-generated method stub
				Log.v(TAG, "websocket connected");
				isConnected = true;
				WebSocketHandler.this.messageService.checkInitialization();
			}
			
			@Override
			public void onMessage(String str) {
				Log.v(TAG, "string available: " + str);
				try {
					JSONObject command = new JSONObject(str);
					WebSocketHandler.this.messageService.handleServerCommand(command);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public void onError(Exception ex) {
				Log.v(TAG,"WebSocketClient had an error");
				ex.printStackTrace();
				isConnected = false;
				if (!isClosed)	WebSocketHandler.this.messageService.lostConnection();
			}
			
			@Override
			public void onClose(int intgr, String str, boolean bool) {
				Log.w(TAG, "WebSocket disconnected str:" + str + " bool: " + bool);		
				isConnected = false;
				if (!isClosed)	WebSocketHandler.this.messageService.lostConnection();
			}
		};
		ws.connect();
	}
	
	public void send(JSONObject json) {
		try {
		String str = json.toString();
		Log.v(TAG, "Sending " + str);
		ws.send(str);
		} catch (NotYetConnectedException e) {
			Log.v(TAG, "cannot send... not currently connected");
			
			messageService.broadcastException(e);
		}
	}
	
	public void close() {
		isClosed = true;
		ws.close();
	}
}
