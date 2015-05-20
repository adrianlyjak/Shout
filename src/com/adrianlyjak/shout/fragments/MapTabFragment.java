package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adrianlyjak.shout.R;

public class MapTabFragment extends ShoutFragment {
	final String TAG = "MapTabFragment";

	@Override
	protected HashMap<Integer, ShoutFragment> createFragments() {
		HashMap<Integer, ShoutFragment> frags = super.createFragments();
		frags.put(R.id.map_range_container,
				(ShoutFragment) new MapRangeFragment());
		frags.put(R.id.radius_slider_container, new RadiusSliderFragment());
		return frags;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_tab_map, container,
				false);
		return rootView;
	}

}
