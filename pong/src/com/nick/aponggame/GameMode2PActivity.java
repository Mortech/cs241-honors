package com.nick.aponggame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class GameMode2PActivity extends Activity
{
	private SurfaceView view;
	private SurfaceHolder holder;
	private Paint painter;
	private int scoreP1;
	private int scoreP2;
	private int winningScore;
	private StateHandler handler;
	private ArrayList<Ball> balls;
	private Paddle paddle;
	boolean isPlayer1;
	

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        
        balls=new ArrayList<Ball>();
		paddle=new Paddle(view.getWidth()/2 - (75/*length*/ / 2), 400, 75, 10);//temporary values, I havn't mathed it out yet
		painter=new Paint();
	   	super.onCreate(savedInstanceState);
	   	view=new SurfaceView(this);
	   	holder=view.getHolder();
	   	scoreP1=0;
	   	scoreP2=0;
	   	winningScore=10; //MAKE CHOOSABLE LATER
	   	
	   		   	
	   	view.setZOrderOnTop(true);
	   	view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
	   												ViewGroup.LayoutParams.MATCH_PARENT));
	   	
	   	/*
	   	 * HANDLE SETTING UP NETWORK HERE
	   	 */
	   	isPlayer1=true;//WILL BE DYNAMIC ONCE WE HAVE NETWORK CODE
	   	
	   	if(isPlayer1)
	   		balls.add(new Ball(view.getWidth()/2, view.getHeight()/2, 3, 3, 10));
	   	
	   	handler=new StateHandler(view.getWidth(), view.getHeight(), balls, paddle);
	   	

	   	setContentView(view);
	   	start();
	   	return;
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
    
    private void start()
	{
		while(scoreP1<winningScore && scoreP2<winningScore)
		{
			Canvas canvas = holder.lockCanvas();
						
			//HANDLE STUFF SENT BY NETWORK
			
			handler.update();
			handler.draw(canvas, painter);
			holder.unlockCanvasAndPost(canvas);
		}
		//HANDLE WINNER HERE
		
		
		return;
	}
}
