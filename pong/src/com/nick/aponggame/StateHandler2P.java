package com.nick.aponggame;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class StateHandler2P extends SurfaceView implements 	SurfaceHolder.Callback, 
															Runnable
{
	private SurfaceHolder holder;
	private int scoreP1;
	private int scoreP2;
	private int winningScore;
	public class SynchArray{
		private ArrayList<Ball> list=new ArrayList<Ball>();
		public synchronized void add(Ball b){
			list.add(b);
		}
		public synchronized void remove(Ball b){
			list.remove(b);
		}
		public synchronized Ball get(int i){
			return list.get(i);
		}
		public synchronized int size(){
			return list.size();
		}
	}
	private SynchArray balls;
	private Paddle paddle;
	private boolean isPlayer1;
	private volatile boolean running=true;
	private Thread gameThread;
	private Activity owner;
    private Server server;
    private boolean isInitialDraw=true;

	public StateHandler2P(Activity context, boolean play, Server serve)
	{
		super(context);
		
		owner=context;
		balls=new SynchArray();
		paddle=new Paddle(122, 680, 75, 10); 
		holder=getHolder();
		holder.addCallback(this);
		scoreP1=0;
		scoreP2=0;
		winningScore=10; //TODO: make choosable later
		gameThread=new Thread(this);
		server=serve;
		isPlayer1=play;//determined by network coding
		
		if(isPlayer1)
   		{
	   		balls.add(new Ball(155, 10, 3, 3, 10));//always starts on p1 screen [for now?]
   		}
		
	   	
	   	return;
	}
	
	//used by run
	public void update()// later change to return int reflecting who scored, if anyone
	{
		for(int i=0; i<balls.size(); i++)
		{
			Ball b=balls.get(i);
			b.move();
		
			//DEATH!
			if(b.getY() > getHeight())
			{//Collisions with the bottom wall
				String message;
				
				b.setX(getWidth()/2-b.getSize()/2); 
				b.setY(getHeight()/5);
				b.setXV(3);//TODO: make random angle and set velocity appropriately
				b.setYV(3);
				if(isPlayer1)
				{
					scoreP2++;
					message=scoreP2+" ";
				}
				else
				{
					scoreP1++;
					message=scoreP1+" ";
				}
				
				
				server.write(message.getBytes());
			}
			else if(b.getY() < 0)
			{
				String message=b.getX()+" "+b.getXV()+" "+b.getYV()+" ";
				// Get the ball data bytes and tell the Server to write
	            byte[] send = message.getBytes();
	            
	            server.write(send);
	            balls.remove(b);
	            i--;
			}
			else if((b.getX()+b.getSize() > getWidth() && b.getXV()>0) || (b.getX() < 0 && b.getXV()<0))
			{//Collisions with left/right walls
				b.reverseX();  
			}
			else if(	b.getX() > paddle.getX() && 
						b.getX() < paddle.getX()+paddle.getLength() &&
				
						b.getY()+b.getSize() >= paddle.getY() &&
						b.getY()+b.getSize()-b.getYV() <= paddle.getY() /*&& b.getYV()>0 is implied*/)
			{//if within paddle's x positions, and was above paddle before move, and is now below paddle, collission with paddle
			 //NOTE: this method is somewhat complicated to fix for the case that b.getYV()>paddle.getHeight()
				b.reverseY();
			}
		}
		
		
		return;
	}
	
	//used by run()
	public void draw(Canvas canvas, Paint paint)
	{
		if(isInitialDraw)
		{
			isInitialDraw=false;
			
			for(int i=0; i<balls.size(); i++)
			{
				Ball b=balls.get(i);
				b.setX(getWidth()/2 - b.getSize()/2);
				b.setY(getHeight()/5);
			}
			
			paddle.setX(getWidth()/2 - paddle.getLength()/2);
			paddle.setY(getHeight()*9/10);
		}
		
		
		//Clear the screen and set color
		canvas.drawRGB(20, 20, 20);
		paint.setARGB(200, 0, 200, 0);
	
		//draw the ball
		for(int i=0; i<balls.size(); i++)
		{
			Ball b=balls.get(i);
			canvas.drawRect(new Rect(	b.getX(), 				b.getY(),
										b.getX() + b.getSize(), b.getY() + b.getSize()),
										paint);
		}
		
		//draw the bats
		canvas.drawRect(new Rect(	paddle.getX(), paddle.getY(), 
									paddle.getX() + paddle.getLength(), 
									paddle.getY() + paddle.getHeight()), 
									paint); //bottom bat
			
		
		return;
	}

	/* thread part of view. continuously updates ball position, and draws 
	 * ball and paddle
	 * 
	 * @param - none
	 * @return - winner declared and activity ends
	 */
	public void run()
	{
		while(scoreP1<winningScore && scoreP2<winningScore && running)
		{
			Canvas canvas = holder.lockCanvas();
			
			
			update();
 	 		draw(canvas, new Paint());
 	 		holder.unlockCanvasAndPost(canvas);
 	 	}	
		//TODO: make a popup that says says you're the winner/loser
		
		//TODO: I think there should be a owner.finish() call here? but I'm not sure
		return;
	}
	
	/* This function is called from the Server handler, when a ball is returning
	 * 
	 * @param - x position, x velocity, y velocity
	 * @return - none
	 */
	public void returningBall(int x, int xvel, int yvel)
	{
		balls.add(new Ball(getWidth() - x, 1, xvel*(-1), yvel*(-1), 10));
	}
	
	/* This function is called from the Server handler, when the score is changed
	 * 
	 * @param - score that was changed
	 * @return -  
	 */
	public void scoreSync(int newScore)
	{
		if(isPlayer1)
		{
			scoreP1=newScore;
		}
		else
		{
			scoreP2=newScore;
		}
	}

	
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
	
	
	//Implemented as part of the SurfaceHolder.Callback interface
	//@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		//Mandatory, just swallowing it for this

	}

    //Implemented as part of the SurfaceHolder.Callback interface. starts thread
	//@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		gameThread.start();
	}

    //Implemented as part of the SurfaceHolder.Callback interface. ends thread 
	//abruptly if needed
	//@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
        running=false;
	}
}
