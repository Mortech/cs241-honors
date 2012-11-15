package com.nick.aponggame;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class StateHandler2P extends SurfaceView implements 	SurfaceHolder.Callback, 
															Runnable, 
															NetworkStateHandler
{
	private int maxWidth;
	private int maxHeight;
	private SurfaceHolder holder;
	private int scoreP1;
	private int scoreP2;
	private int winningScore;
	private ArrayList<Ball> balls;
	private Paddle paddle;
	private boolean isPlayer1;
	private Thread gameThread;
	private Activity owner;
	
	WifiP2pManager manager;
	Channel channel;
	BroadcastReceiver receiver;
	IntentFilter intent;
	
	
	public StateHandler2P(Activity context)
	{
		super(context);
		
		owner=context;		
		maxWidth=getWidth();
		maxHeight=getHeight();
		balls=new ArrayList<Ball>();
		paddle=new Paddle(maxWidth/2 - (75/*length*/ / 2), 400, 75, 10);//temporary values, I havn't mathed it out yet;
		holder=getHolder();
		holder.addCallback(this);
		scoreP1=0;
		scoreP2=0;
		winningScore=10; //MAKE CHOOSABLE LATER
		gameThread=new Thread(this);
		
		
		isPlayer1=true;//WILL BE DYNAMIC ONCE WE HAVE NETWORK CODE
	   	if(isPlayer1)
	   		balls.add(new Ball(maxWidth/2, maxHeight/2, 3, 3, 10));
	   	
	   	
	   	//HANDLE SETTING UP NETWORK HERE
	   	manager=(WifiP2pManager) owner.getSystemService(Context.WIFI_P2P_SERVICE);
	   	channel=manager.initialize(owner,  owner.getMainLooper(),  null);
	   	receiver=new WifiReceiver(manager, channel, owner);
	   	
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
	}
	
	public void update()// later change to return int reflecting who scored, if anyone
	{
		for(Ball b : balls)
		{
			b.move();
		
			//DEATH!
			if(b.getY() > maxHeight || b.getY() < 0)
			{
				b.setX(100);//Collisions with the sides
			}
		
			if(b.getX() > maxWidth || b.getX() < 0)
			{
				b.reverseX(); 	//Collisions with the bats 
			}
		
			if(		b.getX() > paddle.getX() &&
					b.getX() < paddle.getX()+paddle.getLength() && 
					b.getY() > paddle.getY())
			{
				b.reverseY();
			}
		}
	}
	
	public void draw(Canvas canvas, Paint paint)
	{
		//Clear the screen
		canvas.drawRGB(20, 20, 20);
	
		//set the color
		paint.setARGB(200, 0, 200, 0);
	
		//draw the ball
		for(Ball b : balls)
		{
			canvas.drawRect(new Rect(	b.getX(), 				b.getY(),
										b.getX() + b.getSize(), b.getY() + b.getSize()),
										paint);
		}
		
		//draw the bats
		canvas.drawRect(new Rect(	paddle.getX(), paddle.getY(), 
									paddle.getX() + paddle.getLength(), 
									paddle.getY() + paddle.getHeight()), 
									paint); //bottom bat

	}

	public void run()
	{
		while(scoreP1<winningScore && scoreP2<winningScore)
		{
			Canvas canvas = holder.lockCanvas();
			
			//HANDLE STUFF SENT BY NETWORK	
			update();
 	 		draw(canvas, new Paint());
 	 		holder.unlockCanvasAndPost(canvas);
 	 	}	
		//HANDLE WINNER HERE	
		return;
	}
	
    @Override
	public boolean onTouchEvent(MotionEvent e)
	{
		paddle.setX((int)(e.getX()));
		return true;
	}
    
    /* getter/setters
     * 
     * @param - if there is a parameter, set it to the corresponding variable
     * @return - corresponding variable
     */
    public BroadcastReceiver getReceiver()
    {
    	return receiver;
    }
    public IntentFilter getIntent()
    {
    	return intent;
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
		gameThread.start();
	}

    //Implemented as part of the SurfaceHolder.Callback interface
	//@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        gameThread.stop();
	}
}
