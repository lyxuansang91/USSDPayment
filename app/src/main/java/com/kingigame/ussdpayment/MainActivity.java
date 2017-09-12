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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.protobuf.Message;
import com.kingigame.ussdpayment.models.Initialize;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST = 10;
    private static final String SERVER_IP = "192.168.0.200";
    private static final int SERVER_PORT = 1240;
    private Socket socket = null;

    private byte[] convertIntToByteArray(int val) {
        return ByteBuffer.allocate(4).putInt(val).array();
    }

    private int fromByteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    private byte[] convertShortToByteArray(short val) {
        return ByteBuffer.allocate(2).putShort(val).array();
    }

    public static byte[] decompress(byte[] contentBytes){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(contentBytes));
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }


    private byte[] initDataMessage(Message request, int os, int messid, String session) {
        byte[] result = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int lenSession = session.length();
            int size = request.toByteArray().length + 11 + lenSession;
            // write os
            baos.write((byte)os);
            int dataSize = request.toByteArray().length + 4;

            // write data size + size
            baos.write(convertIntToByteArray(dataSize));
            // write len session
            Log.d(TAG, "len session byte array:"+ convertShortToByteArray((short) lenSession));
            baos.write(convertShortToByteArray((short) lenSession));

            // write n byte session
            baos.write(session.getBytes());

            // write message id
            baos.write(convertShortToByteArray((short) messid));
            baos.write(request.toByteArray());
            // write \r\n
            baos.write("\r\n".getBytes());
            result = baos.toByteArray();
            Log.d(TAG, "result:" + result.toString());
            baos.close();
        } catch(Exception ex) {
            ex.printStackTrace();
            baos.close();
        } finally {
            return result;
        }
    }

    private void parseFrom(byte[] receivedMessage) {
        if(receivedMessage == null || receivedMessage.length <= 0) {
            Log.d(TAG, "message byte: 0");
            return;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(receivedMessage);
        int lenPacket = receivedMessage.length;

        while(lenPacket > 0) {
            int offset = 0;
            byte[] tmpInt = new byte[4];
            int count;
            try {
                count = bais.read(tmpInt);

                // read byte size
                if(count <= 0) return;
                int bytes_size = fromByteArrayToInt(tmpInt);
                // read is_compressed
                int isCompressed = bais.read();
                if(isCompressed < 0) return;
                int leftByteSize = bytes_size - 1;
                lenPacket -= (leftByteSize + 4);
                if(isCompressed == 1) {
                    byte[] dataCompressed = new byte[leftByteSize];
                    bais.read(dataCompressed);
                    byte[] dataDecompressed = decompress(dataCompressed);
                    int length = dataDecompressed.length;
                    int index = 0;
                    while(index < length) {
                        // read data size block ...
                        short data_size_block = ByteBuffer.wrap(dataDecompressed, index, 2).getShort(); // convert to short data_size_block
                        // read message id
                        short messageid = ByteBuffer.wrap(dataDecompressed, index+2, 2).getShort(); // convert to short message id
                        
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendMessage() {
        Initialize.BINInitializeRequest request = Initialize.BINInitializeRequest
                .newBuilder()
                .setCp("17")
                .setAppVersion("20")
                .setPakageName("com.game.boom.online")
                .setCountry("vi")
                .setLanguage("vi")
                .setDeviceId("Samsung")
                .setDeviceInfo("Samsung")
                .build();
        try {
            DataOutputStream dos = new DataOutputStream((socket.getOutputStream()));
            dos.write(initDataMessage(request, 1, 1111, ""));
            dos.close();
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
            while(!Thread.currentThread().isInterrupted() && inputStream != null) {
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
                sendMessage();

                while(!Thread.currentThread().isInterrupted()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] content = new byte[2048];
                    int bytesRead = -1;
                    try {
                        InputStream inputStream = socket.getInputStream();
                        while((bytesRead = inputStream.read(content)) != -1) {
                            baos.write(content, 0, bytesRead);
                        }
                        for(byte _byte: baos.toByteArray()) {
                            Log.d(TAG, _byte + " ");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
//                CommunicateThread comThread = new CommunicateThread(socket);
//                new Thread(comThread).start();
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
