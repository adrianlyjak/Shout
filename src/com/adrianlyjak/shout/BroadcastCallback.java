package com.adrianlyjak.shout;

import android.content.Intent;

public interface BroadcastCallback {
	abstract void doActionFor(Intent intent);
}
