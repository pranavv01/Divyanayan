package com.example.divyanayan20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.*;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WiFiDirectReceiver {
    private static final String TAG = "WiFiDirectReceiver";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context context;

    public WiFiDirectReceiver(Context context) {
        this.context = context;
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), null);
    }

    public void startServer() {
        new Thread(() -> {
            ServerSocket serverSocket = null;
            Socket socket = null;
            FileOutputStream fos = null;
            InputStream inputStream = null;

            try {
                serverSocket = new ServerSocket(8080);
                Log.d(TAG, "Waiting for connection...");

                socket = serverSocket.accept();
                Log.d(TAG, "Client connected! Receiving image...");

                inputStream = socket.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;

                File receivedFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "received_image.jpg");
                fos = new FileOutputStream(receivedFile);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

                Log.d(TAG, "Image received and saved at: " + receivedFile.getAbsolutePath());

                // Show toast on UI thread
                ((MainActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Image Received!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                Log.e(TAG, "Error in Wi-Fi Direct server: ", e);
            } finally {
                try {
                    if (fos != null) fos.close();
                    if (inputStream != null) inputStream.close();
                    if (socket != null) socket.close();
                    if (serverSocket != null) serverSocket.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing resources: ", e);
                }
            }
        }).start();
    }

    public void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Peer discovery started");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Peer discovery failed: " + reason);
            }
        });
    }

    public BroadcastReceiver getReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                    manager.requestPeers(channel, peerList -> {
                        if (!peerList.getDeviceList().isEmpty()) {
                            Log.d(TAG, "Found peers: " + peerList.getDeviceList());
                        } else {
                            Log.d(TAG, "No peers found.");
                        }
                    });
                }
            }
        };
    }
}
