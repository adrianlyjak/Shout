package com.adrianlyjak.shout.models;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;

import com.adrianlyjak.shout.service.MessageService;

public class ChatMessage {
	final static String TAG = "ChatMessage";
	public String description;
	public String deviceID;
	public Date datetime;
	public String _id;
	public GeoLocation coordinates;
	public ArcDistance shoutRadius;
	public MessageService service;
	public static SimpleDateFormat timeFormatter;

	public static Comparator<ChatMessage> DateComparator = new Comparator<ChatMessage>() {
		public int compare(ChatMessage msg1, ChatMessage msg2) {
			return msg1.datetime.compareTo(msg2.datetime);
		}
	};

	public ChatMessage(MessageService messageService, JSONObject json) {
		service = messageService;
		updateFrom(json);
	}

	public ChatMessage(MessageService messageService, String description) {
		service = messageService;
		datetime = new Date();
		shoutRadius = new ArcDistance(service.shoutRadius);
		deviceID = MessageService.deviceID;
		coordinates = service.location;
		this.description = description;
	}

	@SuppressLint("SimpleDateFormat")
	public void updateFrom(JSONObject json) {
		try {
			description = json.getString("description");
			deviceID = json.getString("deviceID");
			String isoformat = "-00:00-" + json.getString("datetime");
			datetime = new SimpleDateFormat("ZZZZZ-yyyy-MM-dd'T'HH:mm:ss")
					.parse(isoformat);
			Log.v(TAG, isoformat + " parsed to " + datetime.toString());
			_id = json.getString("_id");
			shoutRadius = new ArcDistance();
			shoutRadius.setMeters(json.getInt("shoutRadius"));
			JSONArray jsonCoords = json.getJSONObject("geometry").getJSONArray(
					"coordinates");
			coordinates = new GeoLocation(jsonCoords.getDouble(0),
					jsonCoords.getDouble(1));

		} catch (JSONException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public void updateFrom(ChatMessage other) {
		description = other.description;
		datetime = other.datetime;
		deviceID = other.deviceID;
		_id = other._id;
		shoutRadius = other.shoutRadius;
		coordinates = other.coordinates;
	}
	
	public ArcDistance distanceAway() {
		return coordinates.distanceBetween(service.location);
	}

	public String datetimeString() {
		if (isPending()) return "sending...";
		if (timeFormatter == null) {
			timeFormatter = new SimpleDateFormat("h:mm a", Locale.getDefault());
			DateFormatSymbols symbols = new DateFormatSymbols();
			symbols.setAmPmStrings(new String[] { "am", "pm" });
			timeFormatter.setDateFormatSymbols(symbols);
		}
		return timeFormatter.format(datetime);
	}

	public JSONObject toJSONObject() {
		JSONObject message = new JSONObject();
		try {
			message.put("description", description);
			message.put("deviceID", deviceID);
			message.put("shoutRadius", shoutRadius);
			message.put("geometry", coordinates.toJSONObject());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return message;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChatMessage))
			return false;
		if (obj == this)
			return true;

		ChatMessage other = (ChatMessage) obj;
		if (_id == null) return false;
		return _id.equals(other._id);

	}

	@Override
	public int hashCode() {
		return this._id.hashCode();
	}

	public boolean shouldUpdateFrom(ChatMessage other) {
		return this.isPending() && other.description.equals(this.description);
	}
	
	public HashMap<String, String> toHashMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("description", description);
		return map;
	}

	public int relativeColor() {
		// closest = 66ba0c (104,203,13)
		// furthest = f3b307 (243,179,7)
		// err = d94512 (217,69,18)
		ArcDistance distanceBetween = this.coordinates
				.distanceBetween(service.location);
		double ratio = this.coordinates.distanceBetween(service.location)
				.dividedBy(service.shoutRadius);
		if (ratio > 1.0d || distanceBetween.radians > this.shoutRadius.radians) {
			int errr = 217;
			int errg = 69;
			int errb = 18;
			return Color.argb(255, errr, errg, errb);
		}
		int minr = 104;
		int ming = 203;
		int minb = 13;

		int maxr = 243;
		int maxg = 179;
		int maxb = 7;
		int r = Math.round((float) ((maxr - minr) * ratio) + minr);
		int g = Math.round((float) ((maxg - ming) * ratio) + ming);
		int b = Math.round((float) ((maxb - minb) * ratio) + minb);
		return Color.argb(255, r, g, b);
	}

	public boolean fromHere() {
		return isPending() || this.deviceID.equals(MessageService.deviceID);
	}

	public boolean isPending() {
		return _id == null;
	}

	public String coordinatesToString() {
		return "message coords: " + coordinates.coordinates.toString()
				+ " service coords: " + service.location.coordinates.toString();
	}

	
}
