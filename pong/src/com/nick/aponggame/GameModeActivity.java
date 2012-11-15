package com.nick.aponggame;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class GameModeActivity extends Activity
{
	private SurfaceView view;
	private String mode;
	boolean usesNetwork;
	

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //set mode here
        mode=getIntent().getStringExtra(MainActivity.GAME_MODE);
        if(mode.equals("2p"))
        {
        	view=new StateHandler2P(this);
            usesNetwork=true;
        }
        else
        {
        	view=null;
        	usesNetwork=false;
        }
        
        view.setFocusable(true);
	   	view.setZOrderOnTop(true);
	   
	   	
	   	setContentView(view);
	   	return;
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	if(usesNetwork)
    	{
    		NetworkStateHandler temp=(NetworkStateHandler)view;
    		registerReceiver(temp.getReceiver(), temp.getIntent());
    	}
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	if(usesNetwork)
    	{
    		NetworkStateHandler temp=(NetworkStateHandler)view;
    		unregisterReceiver(temp.getReceiver());
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_game_mode, menu);
        return true;
    }
}
