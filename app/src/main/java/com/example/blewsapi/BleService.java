package com.example.blewsapi;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import org.json.JSONObject;
import java.util.List;
import java.util.UUID;

public class BleService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private WebSocketServer wsServer;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        initBluetooth();
        startWebSocketServer();
    }

    private void startForegroundNotification() {
        String channelId = "ble_service_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "BLE Service", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("BLE WebSocket API")
                .setContentText("Service Running")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .build();
        startForeground(1, notification);
    }

    private void initBluetooth() {
        BluetoothManager manager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = manager.getAdapter();
    }

    private void startWebSocketServer() {
        wsServer = new WebSocketServer(8080, this);
        wsServer.start();
    }

    // -----------------------------
    // BLE API functions exposed via JSON
    // -----------------------------
    public void startScan() { bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback); }
    public void stopScan() { bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback); }
    public void connectDevice(BluetoothDevice device) { device.connectGatt(this, false, gattCallback); }
    public void disconnectGatt(BluetoothGatt gatt) { if (gatt != null) gatt.disconnect(); }
    public void readCharacteristic(BluetoothGattCharacteristic c) { if (c != null) c.getService().getCharacteristic(c.getUuid()); }
    public void writeCharacteristic(BluetoothGattCharacteristic c, byte[] data) { if (c != null) c.setValue(data); }
    public void setNotification(BluetoothGatt gatt, BluetoothGattCharacteristic c, boolean enable) { if(gatt != null && c != null) gatt.setCharacteristicNotification(c, enable); }
    public void readRssi(BluetoothGatt gatt) { if (gatt != null) gatt.readRemoteRssi(); }
    public void getServices(BluetoothGatt gatt) { if (gatt != null) gatt.getServices(); }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override public void onScanResult(int callbackType, ScanResult result) { Log.d("BLE", "Found: " + result.getDevice().getAddress()); }
        @Override public void onBatchScanResults(List<ScanResult> results) {}
        @Override public void onScanFailed(int errorCode) { Log.e("BLE", "Scan Failed: " + errorCode); }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { Log.d("BLE", "State: " + newState); }
        @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {}
        @Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {}
        @Override public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {}
        @Override public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {}
        @Override public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {}
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
