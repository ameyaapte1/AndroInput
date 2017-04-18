package com.devel.ameyaapte1.androinput;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by ameyaapte1 on 18/2/17.
 */

public class DatagramSocketThread extends HandlerThread {

    private Handler handler;
    private DatagramSocket datagramSocket;
    private String ip;
    private int port;

    public DatagramSocketThread(String name, String ip, int port) {
        super(name);
        this.ip = ip;
        this.port = port;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public Handler getHandler() {
        this.handler = new Handler(getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String message = (String) msg.obj;
                try {
                    datagramSocket.send(new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(ip), port));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        return this.handler;
    }
}
