package com.adrianlyjak.shout.activites;

import java.util.HashMap;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.ShoutPagerAdapter;
import com.adrianlyjak.shout.service.MessageService;

public class MainActivity extends ShoutActivity implements 
		ActionBar.TabListener {

	String TAG = "MainApp.";

	// View mFakeHeader;

	BroadcastReceiver br;
	boolean initialized = false;
	private MenuItem numberOfUsers;

	private ViewPager viewPager;
	private ShoutPagerAdapter mAdapter;
	private ActionBar actionBar;

	private String[] tabs = { "Messages", "Map" };

	@Override
	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {
		HashMap<String, BroadcastCallback> filteredActions = super
				.setUpFilteredActions();
		filteredActions.put(Constants.SHOUT_USERS_CHANGED,
				new BroadcastCallback() {
					@Override
					public void doActionFor(Intent intent) {
						tryUpdateNumberOfUsers();
					}
				});
		return filteredActions;
	}

	/*
	 * @Override protected HashMap<Integer, ShoutFragment> createFragments() {
	 * HashMap<Integer, ShoutFragment> frags = super.createFragments();
	 * frags.put(R.id.map_tab_container, new MapTabFragment());
	 * frags.put(R.id.messaging_tab_container, new MessagingTabFragment());
	 * return frags; }
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new ShoutPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

	}

	@Override
	public void onServiceConnected(MessageService ms) {
		super.onServiceConnected(ms);
		if (mAdapter.getCurrent() != null) {
			mAdapter.getCurrent().onServiceConnected(ms);
		}
		tryUpdateNumberOfUsers();

	}

	private void tryUpdateNumberOfUsers() {
		if (messageService != null && numberOfUsers != null) {
			numberOfUsers.setTitle(((Integer) messageService.shoutusers)
					.toString());
			invalidateOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.users_menu_item_icon).setEnabled(false);
		numberOfUsers = menu.findItem(R.id.users_menu_item_count);
		tryUpdateNumberOfUsers();

		return true;
	}

	/*
	 * public void openRadiusPreferenceActivity() { DrawerLayout drawer =
	 * (DrawerLayout) findViewById(R.id.main_activity_drawer_layout); if
	 * (drawer.isDrawerOpen(Gravity.RIGHT)) { drawer.closeDrawer(Gravity.RIGHT);
	 * } else { drawer.openDrawer(Gravity.RIGHT); }
	 * 
	 * }
	 */
	@Override
	protected void onResume() {
		super.onResume();
		tryUpdateNumberOfUsers();

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

}
