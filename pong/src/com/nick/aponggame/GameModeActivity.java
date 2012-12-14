package com.nick.aponggame;

import java.util.ArrayList;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GameModeActivity extends Activity
{
	private StateHandler2P view;
	private String mode;
	boolean usesNetwork;
	// Debugging
    private static final String TAG = "BluetoothPong";
    private static final boolean D = true;
    private boolean started=false;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private Server btServer = null;
	

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //set mode here
        mode=getIntent().getStringExtra(MainActivity.GAME_MODE);
        if(!mode.equals("1p"))//if not one player, begin setup of network
        {
            usesNetwork=true;
            // Get local Bluetooth adapter
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // If the adapter is null, then Bluetooth is not supported
            if (mBluetoothAdapter == null)
            {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }//theorhetically, at this point, there would be an else if 1p mode was chosen
        else
        {
        	view=null;
        	usesNetwork=false;
        }
        
        
	   	return;
    }
    
    @Override
    public void onStart() 
    {
        super.onStart();
        if(D) 
    	{
        	Log.e(TAG, "++ ON START ++");
    	}
        
        //Handle what the game mode is
        if(usesNetwork)
        {
        	// If BT is not on, request that it be enabled.
	        // setupChat() will then be called during onActivityResult
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        // Otherwise, setup the chat session
	        } 
	        else if (btServer == null)
	        {
	        	setup2pGame();//handles all the games that use 2p
	        }
        }//else handle any 1p game mode, if we make any
        
        
        return;
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	if(usesNetwork)
    	{
    		// Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (btServer != null)
            {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (btServer.getState() == Server.STATE_NONE)
                {
                  // Start the Bluetooth chat services
                	btServer.start();
                }
            }
    	}
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(usesNetwork)
        {
	        // Stop the Bluetooth chat services
	        if (btServer != null) btServer.stop();
	        if(D) Log.e(TAG, "--- ON DESTROY ---");
        }
    }
    
   private void setup2pGame()
   {
       Log.d(TAG, "setupGame()");
       btServer = new Server(this, mHandler);
       
       
       if(mode.equals("2p0"))
       {
       	ensureDiscoverable();
       	//Wait for another device to connect!
       }
       else if(mode.equals("2p1"))
       {
       	Intent serverIntent = new Intent(this, DeviceList.class);
           startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
           //Wait for successful connect!
       }
       //else if/else, any other game 2p game mode, if we make any
       
       
       return;
   }
    
    private void startGame()
    {
    	view=new StateHandler2P(this, mode.equals("2p0"), btServer);


        view.setFocusable(true);
	   	view.setZOrderOnTop(true);
	   
	   	
	   	setContentView(view);
    }
    
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
	            case MESSAGE_STATE_CHANGE:
	                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
	                switch (msg.arg1) 
	                {
		                case Server.STATE_CONNECTED:
		                	if(!started){
		                		started=true;
		                		startGame();
		                	}
		                    break;
		                case Server.STATE_CONNECTING:
		                    break;
		                case Server.STATE_LISTEN:
		                case Server.STATE_NONE:
		                    break;
	                }
	                break;
	            case MESSAGE_WRITE:
	                //byte[] writeBuf = (byte[]) msg.obj;
	            	System.out.println("Message written!");
	                // construct a string from the buffer
	                //String writeMessage = new String(writeBuf);
	                //mConversationArrayAdapter.add("Me:  " + writeMessage);
	                break;
	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                String[] split=readMessage.split(" ");
	                int x=Integer.parseInt(split[0]);
	                int xv=Integer.parseInt(split[1]);
	                int yv=Integer.parseInt(split[2]);
	                view.returningBall(x, xv, yv);
	                break;
	            case MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                btServer.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a match
                setupGame();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void ensureDiscoverable()
    {
        if(D)
        {
        	Log.d(TAG, "ensure discoverable");
        }
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_game_mode, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.quit:
        	finish();
        	return true;
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceList.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
}
