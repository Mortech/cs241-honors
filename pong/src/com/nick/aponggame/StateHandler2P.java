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
	private SurfaceHolder holder;
	private int scoreP1;
	private int scoreP2;
	private int winningScore;
	private ArrayList<Ball> balls; //TODO: balls need to be protected somehow...
	private Paddle paddle;
	private boolean isPlayer1;
	private Thread gameThread;
	private Activity owner;
	
	WifiP2pManager manager;
	Channel channel;
	BroadcastReceiver receiver;
	IntentFilter intent;
	
//NOTE:THIS HORIZONTAL ORIENTATION RESULTS IN UNTESTABLE BEHAVIOR ATM
	public StateHandler2P(Activity context)
	{//NOTE: getHeight() and getWidth() will return 0 at this point
		super(context);
		
		owner=context;
		balls=new ArrayList<Ball>();
		paddle=new Paddle(122, 680, 75, 10); //professional version needs a way to know height and width to set it at proper location, not a big deal
		holder=getHolder();
		holder.addCallback(this);
		scoreP1=0;
		scoreP2=0;
		winningScore=10; //MAKE CHOOSABLE LATER
		gameThread=new Thread(this);
		
		
		
		
		isPlayer1=true;//WILL BE DYNAMIC ONCE WE HAVE NETWORK CODE
	   	if(isPlayer1)
	   		balls.add(new Ball(155, 10, 3, 3, 10));//always starts on p1 screen [for now?]
	   		//professional version needs a way to know height and width to set it at proper starting location, not a big deal
	   	
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
	
	//used by run
	public void update()// later change to return int reflecting who scored, if anyone
	{
		for(Ball b : balls)
		{
			b.move();
		
			//DEATH!
			if(b.getY() > getHeight())
			{//Collisions with the top/bot walls
				b.setX(getWidth()/2-b.getSize()/2); 
				b.setY(10);
				b.setXV(3);//in professional version, would make random angle and set velocity appropriately
				b.setYV(3);
				
				scoreP2++;
			}
			if(b.getY() < 0)
			{
				//CHANGE LATER TO NETWORK BALL TO OTHER PHONE
				b.setX(100); 
				b.setY(100);
			}
			if(b.getX()+b.getSize() > getWidth() || b.getX() < 0)
			{//Collisions with left/right walls
				b.reverseX();  
			}
			if(	b.getX() > paddle.getX() &&
				b.getX() < paddle.getX()+paddle.getLength() &&
				b.getY()+b.getSize() >= paddle.getY()) 
			{//collision with paddles
				b.reverseY();
			}
		}
	}
	
	//used by run()
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

	/* thread part of view. continuouslt updates ball position, and draws 
	 * ball and paddle
	 * 
	 * @param - none
	 * @return - none
	 */
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
	
	/* This function is called from the networking thread, when a ball is returning
	 * 
	 * @param - x position, x velocity, y velocity
	 * @return - none
	 */
	public void returningBall(int x, int xvel, int yvel){
		for(Ball b : balls){
			if(b.getY()<0){
				b.setX(x);
				b.setY(0);
				b.setXV(xvel);
				b.setYV(yvel);
				break;
			}
		}
	}
	
	//TODO: send ball data over network
	
    /* handles touch event. moves paddle horizontally to where screen was touched
     * 
     * @param - MotionEvent sent by phone
     * @return - true, indicating always successful. Probably bad to do that
     */
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
		//Mandatory, just swallowing it for this

	}

    //Implemented as part of the SurfaceHolder.Callback interface. starts thread
	//@Override
	public void surfaceCreated(SurfaceHolder holder) {
		gameThread.start();
	}

    //Implemented as part of the SurfaceHolder.Callback interface. ends thread 
	//abrubtly if needed
	//@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        gameThread.stop();
	}
}
