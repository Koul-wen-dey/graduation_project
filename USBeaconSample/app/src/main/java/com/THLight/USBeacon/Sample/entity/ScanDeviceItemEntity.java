package com.THLight.USBeacon.Sample.entity;

public class ScanDeviceItemEntity {
    private String deviceName;
    private String major;
    private String minor;
    private String rssi;
    private String batteryPower;
    private String macAddress;
    private long lastUpdateTime;

    public ScanDeviceItemEntity(String deviceName, String major, String minor, String rssi, String batteryPower, String macAddress, long lastUpdateTime) {
        this.deviceName = deviceName;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.batteryPower = batteryPower;
        this.macAddress = macAddress;
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getBatteryPower() {
        return batteryPower;
    }

    public void setBatteryPower(String batteryPower) {
        this.batteryPower = batteryPower;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
