package com.THLight.USBeacon.Sample.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.Sample.R;
import com.THLight.USBeacon.Sample.ui.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ScannerService extends Service implements DeviceScanManager.iBeaconScanListener {
    private static final String PRIMARY_CHANNEL_ID = "PRIMARY_CHANNEL_ID";
    private ScannerServiceBinder scannerServiceBinder;
    private ScanDeviceThread scanDeviceThread;
    private DeviceScanManager deviceScanManager;
    private ScanResponseListener scanResponseListener;
    private boolean isScanBoolean;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceScanManager = new DeviceScanManager(this, this);
        scannerServiceBinder = new ScannerServiceBinder();
        scanDeviceThread = new ScanDeviceThread();
        isScanBoolean = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        scanDeviceThread.start();
        deviceScanManager.startScanDevice();
        return scannerServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isScanBoolean = false;
        scanDeviceThread = null;
        deviceScanManager.stopScanDevice();
        stopSelf();
        stopForeground(true);
        return super.onUnbind(intent);
    }

    public void startForegroundService() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("背景掃描");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Need to set channel after version 8.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager != null) {
                NotificationChannel channel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    "123",
                    NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
                mBuilder.setChannelId(PRIMARY_CHANNEL_ID);
            }
            else Log.d("debug", "mNotificationManager is NULL");
        }
        startForeground(110, mBuilder.build());
    }

    public void setScanResponseListener(ScanResponseListener scanResponseListener) {
        this.scanResponseListener = scanResponseListener;
    }

    @Override
    public void onScanIBeaconDataResponse(iBeaconData iBeaconData) {
        if (scanResponseListener != null && iBeaconData != null)
            scanResponseListener.onScanDeviceResponse(iBeaconData);
    }

    @Override
    public void onScanBatteryPowerResponse(BatteryPowerData batteryPowerData) {
        if (scanResponseListener != null && batteryPowerData != null)
            scanResponseListener.onBatteryPowerUpdate(batteryPowerData);
    }

    public class ScannerServiceBinder extends Binder {
        public ScannerService getService() {
            return ScannerService.this;
        }
    }

    private class ScanDeviceThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isScanBoolean) {
                deviceScanManager.startScanDevice();
                try {
                    Thread.sleep(1000 * 35); //Stop scan for every 35 seconds.
                    deviceScanManager.stopScanDevice();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface ScanResponseListener {
        void onScanDeviceResponse(iBeaconData iBeaconData);

        void onBatteryPowerUpdate(BatteryPowerData batteryPowerData);
    }
}
