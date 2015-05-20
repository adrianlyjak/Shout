package com.adrianlyjak.shout.fragments;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.adrianlyjak.shout.BroadcastCallback;
import com.adrianlyjak.shout.Constants;
import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.activites.ShoutActivity;
import com.adrianlyjak.shout.service.MessageService;

public class ComposeMessageFragment extends ShoutFragment {
	EditText messageInput;
	ImageButton sendButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_compose_message,
				container, false);

		messageInput = (EditText) rootView
				.findViewById(R.id.message_description);
		messageInput.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int arg1, KeyEvent arg2) {
				sendMessage(v);
				return true;
			}
		});
		messageInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				updateViews();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}
		});
		sendButton = (ImageButton) rootView.findViewById(R.id.send_button);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage(v);
			}
		});
		return rootView;
	}

	@Override
	public void onServiceConnected(MessageService ms) {
		super.onServiceConnected(ms);
		updateViews();
	}

	@Override
	protected HashMap<String, BroadcastCallback> setUpFilteredActions() {

		filteredActions.put(Constants.SHOUT_CONNECTION_CHANGED,
				new BroadcastCallback() {

					@Override
					public void doActionFor(Intent intent) {
						updateViews();
					}
				});
		return filteredActions;
	}

	public void sendMessage(View view) {
		if (messageInput.getText().toString().equals(""))
			return;
		if (!messageService.isLoggedIn()) {
			((ShoutActivity) getActivity()).showOkDialog("Not Connected",
					"Cannot send message");
			return;
		}
		String message = messageInput.getText().toString();
		messageService.addmessage(message);
		messageInput.setText("");

		// Hmmm... might need to refactor this
		// ((ListView) findViewById(R.id.messages))
		// .setSelection(messageListAdapter.getCount() - 1);

	}

	private void updateViews() {
		int drawable;
		if (messageService != null && messageService.isLoggedIn()) {
			drawable = R.drawable.ic_send;
		} else {
			drawable = R.drawable.ic_disconnected;
		}
		sendButton.setImageResource(drawable);
		int alpha;
		if (messageInput.getText().toString().equals("")) {
			alpha = 50;
		} else {
			alpha = 100;
		}
		sendButton.setImageAlpha(alpha);
	};

}
