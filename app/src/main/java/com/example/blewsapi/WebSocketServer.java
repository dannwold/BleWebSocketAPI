package com.example.blewsapi;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import android.util.Log;
import org.json.JSONObject;
import java.net.InetSocketAddress;

// Use the fully-qualified package path here to avoid the naming conflict
public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
    // ... rest of your code

    private BleService bleService;

    public WebSocketServer(int port, BleService service) {
        super(new InetSocketAddress(port));
        this.bleService = service;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d("WS", "Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d("WS", "Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject cmd = new JSONObject(message);
            String action = cmd.getString("action");
            JSONObject params = cmd.optJSONObject("params");
            switch(action) {
                case "startScan": bleService.startScan(); break;
                case "stopScan": bleService.stopScan(); break;
                // Extend JSON routing for every BLE function here
            }
        } catch (Exception e) { Log.e("WS", "Invalid JSON", e); }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) { Log.e("WS", "Error", ex); }

    @Override
    public void onStart() { Log.d("WS", "WebSocket Server started"); }
}
