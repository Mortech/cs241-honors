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

public class GameMode2PActivity extends Activity implements GameMode
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
	

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.out.println("Made it past super.onCreate");
        
        balls=new ArrayList<Ball>();
        view=new SurfaceView(this);
		paddle=new Paddle(view.getWidth()/2 - (75/*length*/ / 2), 400, 75, 10);//temporary values, I havn't mathed it out yet
	   	holder=view.getHolder();
	   	scoreP1=0;
	   	scoreP2=0;
	   	winningScore=10; //MAKE CHOOSABLE LATER
	   	System.out.println("instantiated variables");
	   		   	
	   	view.setZOrderOnTop(true);
	   	view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
	   												ViewGroup.LayoutParams.MATCH_PARENT));
	   	
	   	/*
	   	 * HANDLE SETTING UP NETWORK HERE
	   	 */
	   	isPlayer1=true;//WILL BE DYNAMIC ONCE WE HAVE NETWORK CODE
	   	
	   	if(isPlayer1)
	   		balls.add(new Ball(view.getWidth()/2, view.getHeight()/2, 3, 3, 10));
	   	
	   	handler=new StateHandler(view.getWidth(), view.getHeight(), balls, paddle, this, holder);
	   	
	   	System.out.println("setting Content View");
	   	setContentView(view);
	   	System.out.println("starting");
	   	handler.run();
	   	System.out.println("returning");
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
    
    public int getP1Score()
	{
		return scoreP1;
	}

	public int getP2Score()
	{
		return scoreP2;
	}
}
