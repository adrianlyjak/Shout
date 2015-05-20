package com.adrianlyjak.shout.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class GoogleMapFragment extends SupportMapFragment {

	public static interface OnGoogleMapFragmentListener {
		void onMapReady(GoogleMap map);
	}

	public static GoogleMapFragment newInstance(
			OnGoogleMapFragmentListener listener) {

		GoogleMapFragment fragment = new GoogleMapFragment();
		fragment.setCallback(listener);
		return fragment;
	}

	public void setCallback(OnGoogleMapFragmentListener listener) {
		mCallback = listener;
	}

	
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			if (mCallback == null) {
				mCallback = (OnGoogleMapFragmentListener) getActivity();
			}
		} catch (ClassCastException e) {
			throw new ClassCastException(
					getActivity().getClass().getName()
							+ " must implement OnGoogleMapFragmentListener, or an explicit callback must be set");
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		Log.v("GoogleMapFragment", "onHiddenChanged(" + hidden + ")");
		super.onHiddenChanged(hidden);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		if (mCallback != null) {
			mCallback.onMapReady(getMap());
		}
		return view;
	}

	private OnGoogleMapFragmentListener mCallback;

}