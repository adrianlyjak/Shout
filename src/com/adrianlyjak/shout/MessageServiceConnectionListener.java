package com.adrianlyjak.shout;

import com.adrianlyjak.shout.service.MessageService;

import android.os.IBinder;

public interface MessageServiceConnectionListener {

	public void onServiceConnected(MessageService ms);

	public void onServiceDisconnected();
	
	public void setService(MessageService service);

}
