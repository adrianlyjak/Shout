package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.models.ChatMessage;
import com.adrianlyjak.shout.service.MessageService;

public class MessageListFragment extends ShoutFragment {

	String TAG = "MainApp.";

	// View mFakeHeader;

	BroadcastReceiver br;
	boolean initialized = false;
	private ChatMessageAdapter messageListAdapter;
	private boolean shouldVibrate = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_message_list,
				container, false);
		return rootView;
	}

	@Override
	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = super
				.setUpFilteredActions();
		filteredActions.put(Constants.SHOUT_RECEIVED_MESSAGES,
				new BroadcastCallback() {
					@Override
					public void doActionFor(Intent intent) {
						updateMessages();
					}
				});
		filteredActions.put(Constants.SHOUT_LOCATION_CHANGED,
				new BroadcastCallback() {
					@Override
					public void doActionFor(Intent intent) {
						refreshList();
					}
				});
		filteredActions.put(Constants.SHOUT_SENT_MESSAGE,
				new BroadcastCallback() {

					@Override
					public void doActionFor(Intent intent) {
						updateMessages();
					}
				});
		filteredActions.put(Constants.SHOUT_RADIUS_CHANGED,
				new BroadcastCallback() {

					@Override
					public void doActionFor(Intent intent) {
						refreshList();
					}
				});
		return filteredActions;
	}

	@Override
	public void onServiceConnected(MessageService ms) {
		super.onServiceConnected(ms);
		setUpMessageView();
	}

	private void setUpMessageView() {
		messageListAdapter = new ChatMessageAdapter(getActivity(),
				android.R.id.text1, messageService.messages);
		messageListAdapter.sort(ChatMessage.DateComparator);
		((ListView) getActivity().findViewById(R.id.messages))
				.setAdapter(messageListAdapter);
		shouldVibrate = false;
		updateMessages();
	}

	public void updateMessages() {

		Log.v(TAG, "updating messsages");
		if (!messageService.recentMessages.isEmpty() && shouldVibrate) {
			if (((AudioManager) getActivity().getSystemService(
					Context.AUDIO_SERVICE)).getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
				Vibrator v = (Vibrator) getActivity().getSystemService(
						Context.VIBRATOR_SERVICE);
				v.vibrate(100);
			}
		} else {
			shouldVibrate = true;
		}
		messageService.recentMessages.clear();
		Log.v(TAG, "cleared recentMessages, new size: "
				+ messageService.recentMessages.size());
		refreshList();
	}

	private void refreshList() {
		// maintain scroll position from the top when list changes
		ListView messageListView = (ListView) getActivity().findViewById(
				R.id.messages);

		// stay at the bottom if we're at the bottom
		int lastIndex = messageListView.getLastVisiblePosition();
		if (lastIndex != -1 && (messageListView.getCount() - lastIndex) > 2) {
			int index = messageListView.getFirstVisiblePosition();
			Log.v(TAG,
					"last index: " + messageListView.getLastVisiblePosition()
							+ " messageListView.getCount() "
							+ messageListView.getCount());
			View v = messageListView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			messageListAdapter.notifyDataSetInvalidated();
			messageListView.setSelectionFromTop(index, top);
		} else {
			Log.v(TAG, "just updating the list, no scrolling crap");
			messageListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (messageService != null) {
			setUpMessageView();
		}
	}

}
