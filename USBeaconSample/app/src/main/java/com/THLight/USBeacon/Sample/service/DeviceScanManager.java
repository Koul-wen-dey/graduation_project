//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.THLight.USBeacon.Sample.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.Util.THLLog;

import java.util.ArrayList;
import java.util.List;

public class DeviceScanManager extends ScanCallback {
    private BluetoothAdapter bluetoothAdapter = null;
    private iBeaconScanListener iBeaconScanListener;

    DeviceScanManager(Context context, iBeaconScanListener IScan) {
        this.iBeaconScanListener = IScan;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (null == bluetoothManager) {
            THLLog.e("error", "DeviceScanManager-get BluetoothManager failed.");
                    }
        else
        {
            this.bluetoothAdapter = bluetoothManager.getAdapter();
            if (null == this.bluetoothAdapter) {
                THLLog.e("error", "DeviceScanManager-get BT Adapter failed.");
            }
        }
    }

    void startScanDevice() {
        List<ScanFilter> filters = new ArrayList<>();
        // Need to add filter for service continually running after the screen turn off.
        //4C is the assigned number of APPLE (iBeacon)
        ScanFilter filter = new ScanFilter.Builder()
                .setManufacturerData(0x004c, new byte[]{})
                .build();
        filters.add(filter);
        bluetoothAdapter.getBluetoothLeScanner().startScan(filters,
                new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), this);
    }

    void stopScanDevice() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
    }

    public boolean isEnable() {
        return bluetoothAdapter.isEnabled();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (result.getScanRecord() == null) {
            return;
        }
        iBeaconData iBeacon = iBeaconData.generateiBeacon(result.getScanRecord().getBytes());
        BatteryPowerData BatteryData = BatteryPowerData.generateBatteryBeacon(result.getScanRecord().getBytes());
        if (null != iBeacon && !iBeacon.beaconUuid.equals("00112233-4455-6677-8899-AABBCCDDEEFF")) {
            iBeacon.macAddress = null == result.getDevice().getAddress() ? "" : result.getDevice().getAddress();
            iBeacon.rssi = (byte) result.getRssi();
            if (null != this.iBeaconScanListener) {
                this.iBeaconScanListener.onScanIBeaconDataResponse(iBeacon);
            }
        }

        if (BatteryData != null && BatteryData.BatteryUuid.equals("00112233-4455-6677-8899-AABBCCDDEEFF")) {
            BatteryData.macAddress = null == result.getDevice().getAddress() ? "" : result.getDevice().getAddress();
            if (null != this.iBeaconScanListener) {
                this.iBeaconScanListener.onScanBatteryPowerResponse(BatteryData);
            }
        }
    }

    public interface iBeaconScanListener {
        void onScanIBeaconDataResponse(iBeaconData var1);

        void onScanBatteryPowerResponse(BatteryPowerData var1);
    }
}
