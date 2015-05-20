package com.adrianlyjak.shout.fragments;

import java.util.HashMap;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.MessageServiceConnection;
import com.adrianlyjak.shout.MessageServiceConnectionListener;
import com.adrianlyjak.shout.service.MessageService;

public class ShoutFragment extends Fragment implements
		MessageServiceConnectionListener {

	// private static final String TAG = "AbstractShoutFragment";
	public MessageService messageService;
	public HashMap<String, BroadcastCallback> filteredActions = new HashMap<String, BroadcastCallback>();
	protected HashMap<Integer, ShoutFragment> fragments;
	protected static int id = 0;
	protected int instId;
	
	protected static int nextInstId() {
		id++;
		return id;
	}
	
	public void onServiceConnected(MessageService ms) {
		this.messageService = ms;
		setUpBroadcastCallbacks();
	}

	
	private void setUpBroadcastCallbacks() {
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				doActionFor(intent);
			}
		};
		IntentFilter filter = new IntentFilter();
		filteredActions = setUpFilteredActions();
		for (String actionName : filteredActions.keySet()) {
			filter.addAction(actionName);
		}
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br,
				filter);
	}

	protected void doActionFor(Intent intent) {
		BroadcastCallback action = filteredActions.get(intent.getAction());
		Log.v("ShoutFragment", " looking for action for " + intent.getAction()
				+ ", found one? " + (action != null));
		if (action != null) {
			action.doActionFor(intent);
		}
	};

	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = new HashMap<String, BroadcastCallback>();
		return filteredActions;
	}

	protected HashMap<Integer, ShoutFragment> createFragments() {
		fragments = new HashMap<Integer, ShoutFragment>();
		return fragments;
	}

	protected void attachFragments() {
		for (Entry<Integer, ShoutFragment> entry : fragments.entrySet()) {
			getFragmentManager().beginTransaction()
					.add(entry.getKey(), entry.getValue()).commit();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instId = nextInstId();
		new MessageServiceConnection(this).connectFrom(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View defaultView = super.onCreateView(inflater, container,
				savedInstanceState);
		createFragments();
		if (savedInstanceState == null) {
			attachFragments();
		}
		return defaultView;
	}

	@Override
	public void onServiceDisconnected() {

	}

	@Override
	public void setService(MessageService service) {
		messageService = service;
	}
}
