package com.nick.aponggame;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

public class StateHandler extends Thread
{
	private int maxWidth;
	private int maxHeight;
	private int winningScore=10;
	ArrayList<Ball> ballList;
	private Paddle paddle;
	GameMode owner;
	SurfaceHolder holder;
	
	
	public StateHandler(int width, int height, ArrayList<Ball> balls, Paddle pad, GameMode caller, SurfaceHolder hold)
	{
		maxWidth=width;
		maxHeight=height;
		ballList=balls;
		paddle=pad;
		owner=caller;
		holder=hold;
	}
	
	public void update()// later change to return int reflecting who scored, if anyone
	{
		for(Ball b : ballList)
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
		for(Ball b : ballList)
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
		while(owner.getP1Score()<winningScore && owner.getP2Score()<winningScore)
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
}
