package com.nick.aponggame;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public interface NetworkStateHandler
{
	public BroadcastReceiver getReceiver();
    public IntentFilter getIntent();
}
