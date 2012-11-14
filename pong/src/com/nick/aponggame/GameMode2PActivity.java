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

public class GameMode2PActivity extends Activity implements GameMode, SurfaceHolder.Callback
{
	private SurfaceView view;
	private SurfaceHolder holder;
	private int scoreP1;
	private int scoreP2;
	private int winningScore;
	private StateHandler handler;
	private ArrayList<Ball> balls;
	private Paddle paddle;
	boolean isPlayer1;
	
	WifiP2pManager manager;
	Channel channel;
	BroadcastReceiver receiver;
	IntentFilter intent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        balls=new ArrayList<Ball>();
        view=new SurfaceView(this);
		paddle=new Paddle(view.getWidth()/2 - (75/*length*/ / 2), 400, 75, 10);//temporary values, I havn't mathed it out yet
	   	holder=view.getHolder();
	   	holder.addCallback(this);
        view.setFocusable(true);
	   	scoreP1=0;
	   	scoreP2=0;
	   	winningScore=10; //MAKE CHOOSABLE LATER
	   		   	
	   	view.setZOrderOnTop(true);
	   	view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
	   												ViewGroup.LayoutParams.MATCH_PARENT));
	   	
	   	/*
	   	 * HANDLE SETTING UP NETWORK HERE
	   	 */
	   	manager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	   	channel=manager.initialize(this,  getMainLooper(),  null);
	   	receiver=new WifiReceiver(manager, channel, this);
	   	
	   	intent=new IntentFilter();
	   	intent.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	   	intent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	   	intent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	   	intent.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	   	
	   	manager.discoverPeers(channel, new WifiP2pManager.ActionListener(){
	   		//@Override
	   		public void onSuccess(){
	   			System.out.println("SUCCESSful discovery of peers");
	   		}
	   		//@Override
	   		public void onFailure(int reason){
	   			System.out.println("Discovery failed. ); reason="+reason);
	   		}
	   	});
	   	
	   	isPlayer1=true;//WILL BE DYNAMIC ONCE WE HAVE NETWORK CODE
	   	
	   	if(isPlayer1)
	   		balls.add(new Ball(view.getWidth()/2, view.getHeight()/2, 3, 3, 10));
	   	
	   	handler=new StateHandler(view.getWidth(), view.getHeight(), balls, paddle, this, holder);
	   	
	   	setContentView(view);
	   	return;
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	registerReceiver(receiver, intent);
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	unregisterReceiver(receiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_game_mode2_p, menu);
        return true;
    }
	
    @Override
	public boolean onTouchEvent(MotionEvent e)
	{
		paddle.setX((int)(e.getX()));
		return true;
	}
    
    public int getP1Score()
	{
		return scoreP1;
	}

	public int getP2Score()
	{
		return scoreP2;
	}
	
	//Implemented as part of the SurfaceHolder.Callback interface
		//@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			//Mandatory, just swallowing it for this example

		}

	    //Implemented as part of the SurfaceHolder.Callback interface
		//@Override
		public void surfaceCreated(SurfaceHolder holder) {
			handler.start();
		}

	    //Implemented as part of the SurfaceHolder.Callback interface
		//@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
	        handler.stop();
		}
}
