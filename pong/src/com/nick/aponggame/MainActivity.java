package com.nick.aponggame;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity
{
	//constants
	public static final String EXTRA_MESSAGE = 
			"com.CrankMachine.mytestapp.MESSAGE";	//to be used for 
													//inter-activity data exchange
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void button0Clicked(View current)
    {
    	Intent intent=new Intent(this, GameMode2PActivity.class);
    	startActivity(intent);
    }
    
    
    /*
	 * IDEAS:
	 * 		Make two different draw functions: draw paddle and draw ball, doesn't redraw
	 * 		paddle unless touch event happens
	 * 		
	 * 		netcode is a class extends thread, use mutex for handling a queue of sent data
	 * 		
	 */
}
