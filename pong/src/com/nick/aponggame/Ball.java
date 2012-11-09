package com.nick.aponggame;

public class Ball
{
	private int xPos;
	private int yPos;
	private int xPosDef;
	private int yPosDef;
	private int xVel;
	private int yVel;
	private int size;
	
	
	public Ball(int x, int y, int xv, int yv, int s)
	{
		xPos=xPosDef=x;
		yPos=yPosDef=y;
		xVel=xv;
		yVel=yv;
		size=s;
	}
	
	public int getX()
	{
		return xPos;
	}
	public int getY()
	{
		return yPos;
	}
	public void setX(int x)
	{
		xPos=x;
		return;
	}
	public void setY(int y)
	{
		yPos=y;
		return;
	}
	public void reverseX()
	{
		xVel=-xVel;
		return;
	}
	public void reverseY()
	{
		yVel=-yVel;
		return;
	}
	public int getSize()
	{
		return size;
	}
	
	public void move()
	{
		xPos+=xVel;
		yPos+=yVel;
		return;
	}
	
	public void moveToDefault()
	{
		xPos=xPosDef;
		yPos=yPosDef;
		return;
	}
}
