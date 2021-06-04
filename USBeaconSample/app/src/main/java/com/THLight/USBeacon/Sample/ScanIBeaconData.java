package com.THLight.USBeacon.Sample;
import com.THLight.USBeacon.App.Lib.iBeaconData;

/** ============================================================== */
public class ScanIBeaconData extends iBeaconData {
	public long lastUpdate = 0;

	/**
	 * ================================================
	 */
	public static ScanIBeaconData copyOf(iBeaconData iBeacon) {
		ScanIBeaconData newBeacon = new ScanIBeaconData();

		newBeacon.beaconUuid = iBeacon.beaconUuid;
		newBeacon.major = iBeacon.major;
		newBeacon.minor = iBeacon.minor;
		newBeacon.oneMeterRssi = iBeacon.oneMeterRssi;
		newBeacon.rssi = iBeacon.rssi;
		newBeacon.lastUpdate = 0;
		newBeacon.macAddress = iBeacon.macAddress;

		return newBeacon;
	}
}

/** ============================================================== */

