package com.adrianlyjak.shout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.adrianlyjak.shout.fragments.MapTabFragment;
import com.adrianlyjak.shout.fragments.MessagingTabFragment;
import com.adrianlyjak.shout.fragments.ShoutFragment;

public class ShoutPagerAdapter extends FragmentPagerAdapter {

	public final String TAG = "ShoutPagerAdapter";
	private ShoutFragment current;
	public ShoutPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		Log.v(TAG, "getItem("+index+")");
		switch (index) {
		case 0:
			return new MessagingTabFragment();
		case 1:
			return new MapTabFragment();
		}
		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}

	public ShoutFragment getCurrent() {
		return current;
	}
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		current = (ShoutFragment) object;

	}

}
