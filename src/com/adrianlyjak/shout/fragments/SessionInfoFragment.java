package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.service.MessageService;

public class SessionInfoFragment extends ShoutFragment{
	TextView shoutRadiusView;
	TextView shoutusers;
	ImageView shoutConnectionStatus;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_session_info,
				container, false);
		shoutRadiusView = (TextView) rootView.findViewById(R.id.session_info_shout_radius);
		shoutConnectionStatus = (ImageView) rootView.findViewById(R.id.session_info_connection_status);
		shoutusers = (TextView) rootView.findViewById(R.id.session_info_shoutusers);
		return rootView;
	}
	
	@Override
	public void onServiceConnected(MessageService ms) {
		super.onServiceConnected(ms);
		updateViews();
	}
	
	@Override
	protected HashMap<String,BroadcastCallback> setUpFilteredActions() {
		HashMap<String,BroadcastCallback> filteredActions = super.setUpFilteredActions();
		filteredActions.put(Constants.SHOUT_RADIUS_CHANGED, new BroadcastCallback() {
			@Override
			public void doActionFor(Intent intent) {
				updateViews();
			}
		});
		filteredActions.put(Constants.SHOUT_CONNECTION_CHANGED, new BroadcastCallback() {
			
			@Override
			public void doActionFor(Intent intent) {
				updateViews();
			}
		});
		filteredActions.put(Constants.SHOUT_USERS_CHANGED, new BroadcastCallback() {
			
			@Override
			public void doActionFor(Intent intent) {
				updateViews();
			}
		});
		return filteredActions;
	}

	
	private void updateViews() {
		shoutRadiusView.setText(messageService.shoutRadius.metersToString());
		shoutusers.setText(((Integer) messageService.shoutusers).toString());
		if (messageService.isLoggedIn()) {
			shoutConnectionStatus.setImageResource(android.R.color.transparent);
		} else {
			shoutConnectionStatus.setImageResource(R.drawable.ic_disconnected);
		}
	};
	

	
}


