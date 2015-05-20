package com.adrianlyjak.shout;

import com.adrianlyjak.shout.service.MessageService;
import com.adrianlyjak.shout.service.MessageService.LocalBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MessageServiceConnection implements ServiceConnection {

	private MessageServiceConnectionListener listener;

	public MessageServiceConnection(MessageServiceConnectionListener listener) {
		this.listener = listener;
	}

	public void connectFrom(Context context) {
		Intent intent = new Intent(context, MessageService.class);
		context.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		LocalBinder localBinder = (LocalBinder) service;
		MessageService messageService = localBinder.getService();
		listener.setService(messageService);
		listener.onServiceConnected(messageService);

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		listener.onServiceDisconnected();
		listener.setService(null);

	}

}
