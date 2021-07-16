/**
 * ==============================================================
 */
package com.THLight.USBeacon.Sample.ui;
/**
 * ==============================================================
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.util.UUID;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;
import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.USBeaconData;
import com.THLight.USBeacon.App.Lib.USBeaconList;
import com.THLight.USBeacon.App.Lib.USBeaconServerInfo;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.Sample.R;
import com.THLight.USBeacon.Sample.ScanIBeaconData;
import com.THLight.USBeacon.Sample.entity.ScanDeviceItemEntity;
import com.THLight.USBeacon.Sample.service.DeviceScanManager;
import com.THLight.USBeacon.Sample.thLightApplication;
import com.THLight.USBeacon.Sample.THLConfig;
import com.THLight.USBeacon.Sample.service.ScannerService;
import com.THLight.USBeacon.Sample.ui.recyclerview.CustomLinearLayoutManager;
import com.THLight.USBeacon.Sample.ui.recyclerview.CustomRecyclerView;
import com.THLight.USBeacon.Sample.ui.recyclerview.RecyclerViewAdapter;
import com.THLight.USBeacon.Sample.ui.readCsvThread;
import com.THLight.USBeacon.Sample.ui.recyclerview.ScanDeviceViewType;
import com.THLight.Util.THLLog;

public class MainActivity extends Activity implements USBeaconConnection.OnResponse/*, CustomRecyclerView.CustomRecyclerViewScrollListener*/ {
    /**
     * this UUID is generate by Server while register a new account.
     */
    //private final UUID QUERY_UUID = UUID.fromString("BB746F72-282F-4378-9416-89178C1019FC");
    /**
     * server http api url.
     */
    //private static final String HTTP_API = "http://www.usbeacon.com.tw/api/func";
    private static String STORE_PATH = Environment.getExternalStorageDirectory().toString() + "/USBeaconSample/";
    private static final int REQ_ENABLE_BT = 2000;
    private static final int MSG_SERVER_RESPONSE = 3000;
    private static final int TIME_BEACON_TIMEOUT = 30000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private THLConfig Config = null;
    private List<ScanIBeaconData> scanIBeaconDataList = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private USBeaconConnection usBeaconConnection = new USBeaconConnection();
    private RecyclerViewAdapter scanDeviceAdapter;
    private List<ScanDeviceItemEntity> scanDeviceItemEntityList = new ArrayList<>();
    private LocalServiceConnection localServiceConnection = new LocalServiceConnection();
    private ReceiveMessageHandler receiveMessageHandler = new ReceiveMessageHandler(this);
    private int index;
    private boolean isFirstPage = true;
    private boolean isNeedNotify = true;
    private boolean isFront = true;
    //private CustomRecyclerView recyclerView;
    private ScannerService scannerService;
    private PowerManager.WakeLock serviceWakeLock;
    //private String Query;
    private readCsvThread csvTable;
    private ArrayList<String[]> csvData = new ArrayList<String[]>();
    ImageView imageView = null;
    TextView classroom = null;
    TextView course = null;
    TextView remark = null;
    Calendar currentTime;
    String star = "A4:34:F1:89:ED:B4";//5012
    String peko = "A4:34:F1:89:EF:4B";//5007
    String third = "A4:34:F1:89:E7:92";//系辦

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Config = thLightApplication.Config;
        imageView = (ImageView)findViewById(R.id.Pic);
        imageView.setImageResource(R.drawable.ic_launcher);
        classroom = (TextView)findViewById(R.id.classroom);
        course = (TextView)findViewById(R.id.course);
        remark = (TextView)findViewById(R.id.remark);
        openCsv();
        //readCsvThread MyThread = new readCsvThread(this);

        //Thread thread = new Thread(multiThread);
        //thread.run();
        permissionCheck();
        //CreateStoreFolder();
        //networkCheck();
        //bindRecyclerView();
        startScanDevice();

        //螢幕關閉, service 保持運作
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            serviceWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ScannerService.class.getName());
            serviceWakeLock.acquire();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFront = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFront = false;
        scannerService.startForegroundService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(localServiceConnection);
        if (serviceWakeLock != null) {
            serviceWakeLock.release();
            serviceWakeLock = null;
        }
    }
    /*
    private void bindRecyclerView() {
        recyclerView = findViewById(R.id.beacon_recyclerView);
        recyclerView.setLayoutManager(new CustomLinearLayoutManager(this));
        ArrayList<RecyclerViewAdapter.ViewTypeInterface> itemTypeList = new ArrayList<>();
        ScanDeviceViewType scanDeviceViewType = new ScanDeviceViewType(scanDeviceItemEntityList);
        itemTypeList.add(scanDeviceViewType);
        scanDeviceAdapter = new RecyclerViewAdapter(this, itemTypeList);
        recyclerView.setCustomRecyclerViewScrollListener(this);
        recyclerView.setAdapter(scanDeviceAdapter);
    }
*/
    //Do scanning in service.
    private void startScanDevice() {
        //Check the BT is on or off on the phone.
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT);   // A request for open Bluetooth
        } else {
            Intent service = new Intent(this, ScannerService.class);
            bindService(service, localServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }
/*
    private void CreateStoreFolder() {
        //create store folder.
        File file = new File(STORE_PATH);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this, "Create folder(" + STORE_PATH + ") failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void networkCheck() {
        // check network is available or not.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        if (null != connectivityManager) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (null == networkInfo || (!networkInfo.isConnected())) {
                dlgNetworkNotAvailable();     //Show a dialog to inform users to enable the network.
            } else {
                THLLog.d("debug", "NI not null");

                NetworkInfo niMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (null != niMobile) {
                    boolean isMobileInt = niMobile.isConnectedOrConnecting();

                    if (isMobileInt) {
                        dlgNetworkMobile();  //Show a dialog to make sure to use the Mobile Internet
                    } else {
                        USBeaconServerInfo usBeaconServerInfo = new USBeaconServerInfo();

                        usBeaconServerInfo.serverUrl = HTTP_API;
                        usBeaconServerInfo.queryUuid = QUERY_UUID;
                        usBeaconServerInfo.downloadPath = STORE_PATH;

                        usBeaconConnection.setServerInfo(usBeaconServerInfo, this);
                        usBeaconConnection.checkForUpdates();
                    }
                }
            }
        } else {
            THLLog.d("debug", "CM null");
        }
    }
*/
    //Check the locate permission for BT scan in Android 6.0
    void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }

            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs writing access");
                builder.setMessage("Please grant writing access.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("debug", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                break;
            }

            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("debug", "writing permission granted");
                    usBeaconConnection.checkForUpdates();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since writing access has not been granted, this app will not be able to update data.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                break;
        }
    }

    public void onResponse(int msg) {
        THLLog.d("debug", "Response(" + msg + ")");
        receiveMessageHandler.obtainMessage(MSG_SERVER_RESPONSE, msg, 0).sendToTarget();
    }
/*
    public void dlgNetworkNotAvailable() {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        alertDialog.setTitle("Network");
        alertDialog.setMessage("Please enable your network for updating beacon list.");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    //＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝

    public void dlgNetworkMobile() {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        alertDialog.setTitle("3G");
        alertDialog.setMessage("App will send/recv data via Mobile Internet, this may result in significant data charges.");

        // To check yes or no of using mobile Internet.
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Allow", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Config.allow3G = true;
                alertDialog.dismiss();
                USBeaconServerInfo info = new USBeaconServerInfo();

                info.serverUrl = HTTP_API;
                info.queryUuid = QUERY_UUID;
                info.downloadPath = STORE_PATH;

                usBeaconConnection.setServerInfo(info, MainActivity.this);
                usBeaconConnection.checkForUpdates();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Config.allow3G = false;
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
*/
    private static class ReceiveMessageHandler extends Handler {
        WeakReference<MainActivity> mainActivityWeakReference;

        ReceiveMessageHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mainActivityWeakReference == null) {
                return;
            }
            MainActivity activity = mainActivityWeakReference.get();
            switch (msg.what) {
                case MSG_SERVER_RESPONSE:
                    switch (msg.arg1) {
                        case USBeaconConnection.MSG_NETWORK_NOT_AVAILABLE:
                            break;

                        // Get the data from Server by the "QUERY_UUID"
                        case USBeaconConnection.MSG_HAS_UPDATE:
                            //Download beacon data to a zip file, and send MSG_DATA_UPDATE_FINISHED
                            activity.usBeaconConnection.downloadBeaconListFile();
                            Toast.makeText(activity, "HAS_UPDATE.", Toast.LENGTH_SHORT).show();
                            break;

                        case USBeaconConnection.MSG_HAS_NO_UPDATE:
                            Toast.makeText(activity, "No new BeaconList.", Toast.LENGTH_SHORT).show();
                            break;

                        case USBeaconConnection.MSG_DOWNLOAD_FINISHED:
                            break;

                        case USBeaconConnection.MSG_DOWNLOAD_FAILED:
                            Toast.makeText(activity, "Download file failed!", Toast.LENGTH_SHORT).show();
                            break;

                        case USBeaconConnection.MSG_DATA_UPDATE_FINISHED: {
                            USBeaconList usBeaconList = activity.usBeaconConnection.getUSBeaconList();  //Get the beacon list that was from Server

                            if (null == usBeaconList) {
                                Toast.makeText(activity, "Data Updated failed.", Toast.LENGTH_SHORT).show();
                                THLLog.d("debug", "update failed.");
                            } else if (usBeaconList.getList().isEmpty()) {
                                Toast.makeText(activity, "Data Updated but empty.", Toast.LENGTH_SHORT).show();
                                THLLog.d("debug", "this account doesn't contain any devices.");
                            } else {
                                String BeaconData = "";
                                Toast.makeText(activity, "Data Updated(" + usBeaconList.getList().size() + ")", Toast.LENGTH_SHORT).show();

                                for (USBeaconData data : usBeaconList.getList()) {
                                    BeaconData = BeaconData + "Name(" + data.name + "), Ver(" + data.major + "." + data.minor + ")\n";
                                    THLLog.d("debug", "Name(" + data.name + "), Ver(" + data.major + "." + data.minor + ")");
                                }
                            }
                        }
                        break;

                        case USBeaconConnection.MSG_DATA_UPDATE_FAILED:
                            Toast.makeText(activity, "UPDATE_FAILED!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    }
/*
    @Override
    public void onRecyclerViewScrolled(CustomRecyclerView recyclerView, boolean isScrollBottom) {
        isNeedNotify = false;
        if (isScrollBottom) {
            if (!isFirstPage && scanIBeaconDataList != null) {
                addScanDeviceItemList();
            }
        }
    }
*/
     /*
    @Override
    public void onRecyclerViewIdle() {
        isNeedNotify = true;
        removeIdleBeaconData();
    }
*/
    private class LocalServiceConnection implements ServiceConnection, ScannerService.ScanResponseListener {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            scannerService = ((ScannerService.ScannerServiceBinder) iBinder).getService();
            scannerService.setScanResponseListener(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scannerService = null;
        }

        @Override
        public void onScanDeviceResponse(iBeaconData iBeaconData) {
            System.out.println("onScanDeviceResponse");
            isFirstPage = scanDeviceItemEntityList.size() < 20;
            addOrUpdateIBeaconDataList(iBeaconData);
            if (isFirstPage) {
                addScanDeviceItemList();
            }
            removeIdleBeaconData();
            showSomePic(scanDeviceItemEntityList);
        }

        @Override
        public void onBatteryPowerUpdate(BatteryPowerData batteryPowerData) {
            for (ScanDeviceItemEntity entity : scanDeviceItemEntityList) {
                if (entity.getMacAddress().equals(batteryPowerData.macAddress)) {
                    entity.setBatteryPower(String.valueOf(batteryPowerData.batteryPower));
                }
            }
        }
    }

    private void addOrUpdateIBeaconDataList(iBeaconData iBeaconData) {
        ScanIBeaconData data = ScanIBeaconData.copyOf(iBeaconData);
        data.lastUpdate = System.currentTimeMillis();

        index = 0;
        for (ScanDeviceItemEntity scanDeviceItemEntity : scanDeviceItemEntityList) {
            if (data.macAddress.equalsIgnoreCase(scanDeviceItemEntity.getMacAddress())) {
                scanDeviceItemEntity.setRssi(String.valueOf(data.rssi));
                scanDeviceItemEntity.setLastUpdateTime(data.lastUpdate);
                return;
            }
            index++;
        }
        for (ScanIBeaconData scanIBeaconData : scanIBeaconDataList) {
            if (data.macAddress.equalsIgnoreCase(scanIBeaconData.macAddress)) {
                return;
            }
        }
        scanIBeaconDataList.add(data);
    }

    public void removeIdleBeaconData() {
        ScanDeviceItemEntity entity;
        int length = scanDeviceItemEntityList.size();
        for (int i = length - 1; 0 <= i; i--) {
            entity = scanDeviceItemEntityList.get(i);
            if (entity != null && (System.currentTimeMillis() - entity.getLastUpdateTime()) > TIME_BEACON_TIMEOUT) {
                scanDeviceItemEntityList.remove(entity);
            }
        }
    }

    private void addScanDeviceItemList() {
        for (int i = 0; i < scanIBeaconDataList.size(); i++) {
            //Each time add 20 data at most to the UI list.
            if (i > 20) {
                return;
            }
            ScanIBeaconData data = scanIBeaconDataList.remove(i);
            ScanDeviceItemEntity entity = new ScanDeviceItemEntity(
                    data.beaconUuid.toUpperCase(),
                    String.valueOf(data.major),
                    String.valueOf(data.minor),
                    String.valueOf(data.rssi),
                    String.valueOf(data.batteryPower),
                    data.macAddress,
                    data.lastUpdate);
            scanDeviceItemEntityList.add(entity);
        }
        printinfo(scanDeviceItemEntityList);
    }
    void printinfo(List<ScanDeviceItemEntity> info)
    {
        for(int i=0;i<info.size();i++){
            System.out.println("uuid:" +info.get(i).getDeviceName()+ "  major:"+info.get(i).getMajor());
            System.out.println("minor:"+info.get(i).getMinor()+"  mac:"+info.get(i).getMacAddress());
            System.out.println(info.get(i).getRssi());
        }
    }

    void showSomePic(List<ScanDeviceItemEntity> info){
        String which = null;
        if(info.size() == 0)
            return;
        else if(info.size() == 1)
        {
            if(star.equals(info.get(0).getMacAddress())){
                Drawable d = getResources().getDrawable(R.drawable.star);
                imageView.setImageDrawable(d);
            }
            else if(peko.equals(info.get(0).getMacAddress())){
                Drawable d = getResources().getDrawable(R.drawable.peko);
                imageView.setImageDrawable(d);
            }
            else{
                Drawable d = getResources().getDrawable(R.drawable.ic_launcher);
                imageView.setImageDrawable(d);
            }
            which = info.get(0).getMacAddress();
        }
        else{
            int pos=0;
            for (int i=0;i<info.size()-1;i++){
                if(Integer.valueOf(info.get(pos).getRssi()) < Integer.valueOf(info.get(i+1).getRssi())) {
                    pos = i;
                }
            }
            if(star.equals(info.get(pos).getMacAddress())){
                Drawable d = getResources().getDrawable(R.drawable.star);
                imageView.setImageDrawable(d);
            }
            else if(peko.equals(info.get(pos).getMacAddress())){
                Drawable d = getResources().getDrawable(R.drawable.peko);
                imageView.setImageDrawable(d);
            }
            else{
                Drawable d = getResources().getDrawable(R.drawable.ic_launcher);
                imageView.setImageDrawable(d);
            }
            which = info.get(pos).getMacAddress();
        }
        info_class(which);
    }
    void openCsv(){
        csvTable = new readCsvThread(this);
        csvTable.run();
        csvData.clear();
        csvData.addAll(csvTable.table);
    }
    void info_class(String which){
        currentTime = Calendar.getInstance();
        int month = currentTime.get(Calendar.MONTH);
        int day_of_week = currentTime.get(Calendar.DAY_OF_WEEK);
        int hour = currentTime.get(Calendar.HOUR) - 7;

        for(int i=0;i<csvData.size();i++){
            if(which.equals(csvData.get(i)[0])){
                if(csvData.get(i)[0].equals(third)){
                    classroom.setText("所在位置 : \n"+csvData.get(i)[1]);
                    course.setText("課程 : \n"+csvData.get(i)[6]);
                    remark.setText("備註 : \n"+csvData.get(i)[7]);
                    return;
                }
                else if((month > 8 || month == 1) && Integer.valueOf(csvData.get(i)[2]) == 1 ||
                        (month > 1 && month < 7) && Integer.valueOf(csvData.get(i)[2]) == 2)
                {
                    if(Integer.valueOf(csvData.get(i)[3]) == day_of_week && timerange(hour,Integer.valueOf(csvData.get(i)[4]),Integer.valueOf(csvData.get(i)[5]))) {
                        classroom.setText("所在位置 : \n"+csvData.get(i)[1]);
                        course.setText("課程 : \n"+csvData.get(i)[6]);
                        remark.setText("備註 : \n"+csvData.get(i)[7]);
                        return;
                    }
                }
                else if((month == 7 || month == 8) && timerange(hour,Integer.valueOf(csvData.get(i)[4]),Integer.valueOf(csvData.get(i)[5]))){
                    classroom.setText("所在位置 : \n"+csvData.get(i)[1]);
                    course.setText("課程 : \n"+csvData.get(i)[6]);
                    remark.setText("備註 : \n"+csvData.get(i)[7]);
                    return;
                }
            }
        }
    }
    boolean timerange(int now,int start,int end){
        return now >= start && now <= end;
    }
    /*
    private Runnable multiThread = new Runnable() {
        @Override
        public void run() {
            String result="";
            try {
                String db_php = "";
                String value3="",value2="",value1="";
                URL url = new URL(db_php);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("first",value1)
                        .appendQueryParameter("second",value2)
                        .appendQueryParameter("third",value3);
                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String str = null;
                    StringBuffer buffer = new StringBuffer();
                    while((str = bufferedReader.readLine())!=null){
                        buffer.append(str);
                    }
                    in.close();
                    bufferedReader.close();
                    result = buffer.toString();
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };*/



}
