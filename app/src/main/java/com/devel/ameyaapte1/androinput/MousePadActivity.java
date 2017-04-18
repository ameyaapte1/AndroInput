package com.devel.ameyaapte1.androinput;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;


public class MousePadActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, CustomGestureHandler.MultiTouchListener, View.OnClickListener {

    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    private DatagramSocketThread datagramSocketThread;
    private BluetoothSocketThread bluetoothSocketThread;
    private Handler connectionHandler;
    private double dx, dy, mPrevX = 0, mPrevY = 0, mCurrentX, mCurrentY, currentSensitivity;
    private Message message;
    private String connection;
    private CustomGestureHandler customGestureHandler;
    private Vibrator vibrator;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_showKeyboard:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int ascii = event.getUnicodeChar(event.getMetaState());
        Log.d(DEBUG_TAG, "Key event: " + event.toString() + String.valueOf(ascii));
        if(keyCode == KeyEvent.KEYCODE_DEL){
            message.obj = "key," + "8";
            connectionHandler.dispatchMessage(message);
        }
        if(ascii != 0) {
            message.obj = "key," + String.valueOf(ascii);
            connectionHandler.dispatchMessage(message);
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse_pad);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        customGestureHandler = new CustomGestureHandler(this);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        Button button_copy = (Button) findViewById(R.id.button_copy);
        Button button_paste = (Button) findViewById(R.id.button_paste);
        Button button_undo = (Button) findViewById(R.id.button_undo);

        button_copy.setOnClickListener(this);
        button_paste.setOnClickListener(this);
        button_undo.setOnClickListener(this);

        currentSensitivity = 0.75;

        Intent intent = getIntent();
        connection = intent.getStringExtra("connection");
        message = new Message();
        if (connection.equals("wifi")) {
            datagramSocketThread = new DatagramSocketThread("AndroInputWifi", intent.getStringExtra("ip"), 4950);
            datagramSocketThread.start();
            connectionHandler = datagramSocketThread.getHandler();
        } else if (connection.equals("bluetooth")) { final BluetoothManager mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothSocketThread = new BluetoothSocketThread("AndroInputWifi");
            bluetoothSocketThread.start();
            connectionHandler = bluetoothSocketThread.getHandler();
        }
        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.MultiTouchListener
        mDetector = new GestureDetectorCompat(this, this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "TapListener: " + event.toString());
        Log.d(DEBUG_TAG, "pointCount: " + event.getPointerCount());
        this.mDetector.onTouchEvent(event);
        int actionType = event.getAction();
        int pointerCount = event.getPointerCount();

        if (customGestureHandler.TapListener(event)) {
            return true;
        }

        switch (actionType) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                mPrevY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentX = event.getX();
                mCurrentY = event.getY();
                dx = (int) ((mCurrentX - mPrevX) * currentSensitivity);
                dy = (int) ((mCurrentY - mPrevY) * currentSensitivity);
                if (dx != 0 || dy != 0) {
                    if (pointerCount == 1) {
                        message.obj = "move," + String.valueOf(dx) + "," + String.valueOf(dy);
                        connectionHandler.dispatchMessage(message);
                    }
                    if (pointerCount == 2) {
                        message.obj = "scroll," + String.valueOf(dy);
                        connectionHandler.dispatchMessage(message);
                    }
                }
                mPrevX = mCurrentX;
                mPrevY = mCurrentY;
                break;
        }
        //Log.d(DEBUG_TAG,"TapListener: " + event.toString());
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        //Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        //Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        vibrator.vibrate(50);
        message.obj = "long_press";
        connectionHandler.dispatchMessage(message);
        //Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString() + e2.toString());
        return true;
    }


    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        message.obj = "double_click";
        connectionHandler.dispatchMessage(message);
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        message.obj = "left_click";
        connectionHandler.dispatchMessage(message);
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        datagramSocketThread.interrupt();
        bluetoothSocketThread.interrupt();
    }

    @Override
    public boolean onTripleFingerTap(MotionEvent ev) {
        message.obj = "middle_click";
        connectionHandler.dispatchMessage(message);
        return true;
    }

    @Override
    public boolean onDoubleFingerTap(MotionEvent ev) {
        message.obj = "right_click";
        connectionHandler.dispatchMessage(message);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_undo:
                message.obj = "key,undo";
                connectionHandler.dispatchMessage(message);
                break;
            case R.id.button_copy:
                message.obj = "key,copy";
                connectionHandler.dispatchMessage(message);
                break;
            case R.id.button_paste:
                message.obj = "key,paste";
                connectionHandler.dispatchMessage(message);
                break;
        }
    }
}
