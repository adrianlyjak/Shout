package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adrianlyjak.shout.R;

public class MessagingTabFragment extends ShoutFragment {
	final String TAG = "MessagingTabFragment";

	@Override
	protected HashMap<Integer, ShoutFragment> createFragments() {
		HashMap<Integer, ShoutFragment> frags = super.createFragments();
		frags.put(R.id.map_background_container, new MapRangeFragment());
		frags.put(R.id.compose_message_container, new ComposeMessageFragment());
		frags.put(R.id.message_list_container, new MessageListFragment());
		return frags;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_tab_messaging, container,
				false);
		return rootView;
	}

}
