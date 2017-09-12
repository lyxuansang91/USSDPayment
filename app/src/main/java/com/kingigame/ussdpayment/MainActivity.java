package com.kingigame.ussdpayment;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST = 10;
    private static final String SERVER_IP = "kingviet.top";
    private static final int SERVER_PORT = 1240;
    private Socket socket = null;

    private void sendMessage() {
        byte[] message = {1, 2, 3, 4};
        DataOutputStream dOut = null;
        try {
            dOut = new DataOutputStream(socket.getOutputStream());
            Log.d(TAG, "send message:"+  message.length);
            dOut.writeInt(message.length);
            dOut.write(message);
            dOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connectSocket() {
        new Thread(new ClientThread()).start();
    }

    class CommunicateThread implements Runnable {

        private Socket clientSocket;
        private InputStream inputStream;

        public CommunicateThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                inputStream = this.clientSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] content = new byte[2048];
                int bytesRead = -1;
                try {
                    while((bytesRead = this.inputStream.read(content)) != -1) {
                        baos.write(content, 0, bytesRead);
                    }
                    for(byte _byte: baos.toByteArray()) {
                        Log.d(TAG, _byte + " ");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                BufferedReader input;
                if (socket == null) {
                    socket = new Socket(serverAddr, SERVER_PORT);
                }
                Log.d(TAG, "socket:" + socket);
//                sendMessage();
                CommunicateThread comThread = new CommunicateThread(socket);
                new Thread(comThread).start();
            }  catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkAndRequestPermissions() {
        List<String> listPermissionNeeded = new ArrayList<>();

        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(Manifest.permission.INTERNET);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(Manifest.permission.CALL_PHONE);
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(Manifest.permission.RECEIVE_SMS);
        }

        if(listPermissionNeeded.isEmpty()) {
            return true;
        }
        ActivityCompat.requestPermissions(MainActivity.this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), MY_PERMISSIONS_REQUEST);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "begin main activity");
        connectSocket();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Do not need to check permission
        } else {
            checkAndRequestPermissions();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "permission granted");
                }
                break;
        }

    }
}
