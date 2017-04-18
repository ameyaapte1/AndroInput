package com.devel.ameyaapte1.androinput;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


/**
 * Created by ameyaapte1 on 19/2/17.
 */

public class BluetoothSocketThread extends HandlerThread {
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket = null;
    BluetoothServerSocket bluetoothServerSocket = null;
    private OutputStream outputStream;

    public BluetoothSocketThread(String name) {
        super(name);
    }

    public static BluetoothSocket createRfcommSocket(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        try {
            Class class1 = device.getClass();
            Class aclass[] = new Class[1];
            aclass[0] = Integer.TYPE;
            Method method = class1.getMethod("createRfcommSocket", aclass);
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(1);

            tmp = (BluetoothSocket) method.invoke(device, aobj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public Handler getHandler() {
        handler = new Handler(getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(outputStream != null) {
                    String message = (String) msg.obj;
                    try {
                        outputStream.write(message.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(!bluetoothAdapter.isEnabled())
                    bluetoothAdapter.enable();
                try {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    for(BluetoothDevice bluetoothDevices : pairedDevices) {
                        bluetoothSocket = createRfcommSocket(bluetoothDevices);
                    }
                    outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write("Hello".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return handler;
    }
}
