package com.adrianlyjak.shout.fragments;

import java.util.ArrayList;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adrianlyjak.shout.R;
import com.adrianlyjak.shout.models.ChatMessage;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
	public static String TAG = "ChatMessageAdapter";
	private Context context;

	public ChatMessageAdapter(Context context, int textViewResourceId,
			ArrayList<ChatMessage> items) {
		super(context, textViewResourceId, items);
		this.context = context;
	}

	public void itemsChanged(ArrayList<ChatMessage> items) {
		clear();
		addAll(items);
		sort(ChatMessage.DateComparator);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessage item = getItem(position);
		if (item != null && item.service != null) {

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int layout;
			if (item.fromHere()) {
				layout = R.layout.chat_message_from_here;
			} else {
				layout = R.layout.chat_message_from_there;
			}
			View view = inflater.inflate(layout, null);

			// My layout has only one TextView

			TextView descriptionView = (TextView) view
					.findViewById(R.id.chat_message_description);
			TextView datetimeView = (TextView) view
					.findViewById(R.id.chat_message_datetime);
			descriptionView.setText(item.description);
			String datetimeString = item.datetimeString();
			if (!item.isPending() && (position == 0
					|| !getItem(position - 1).datetimeString().equals(
							datetimeString))) {
				datetimeView.setText(item.datetimeString());
				datetimeView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			} else {
				datetimeView.setText("");
				datetimeView.getLayoutParams().height = 0;
			}
			if (!item.fromHere()) {

				TextView distanceView = (TextView) view
						.findViewById(R.id.chat_message_distance_away);
				TextView shoutRadiusView = (TextView) view
						.findViewById(R.id.chat_message_shout_radius);
				distanceView.setText(item.distanceAway().metersToString());
				distanceView.setTextColor(item.relativeColor());
				shoutRadiusView.setText(item.shoutRadius.metersToString());
			}
			return view;
		} else {

			return convertView;
		}
	}
}
