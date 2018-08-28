package com.chigoo.wifilocation;

import android.chigoo.ChigooService;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zoe on 18-6-21.
 */

public class ChigooWifiLocation extends Thread{

    static {
        System.loadLibrary( "ChigooWifiLocation" );
    }

    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList;
    private Context mContext;
    private ScanThread mScanThread;
    private LocationInfo locationInfo;

    private ArrayList<ChigooWifiInfo> mWifiInfoArray;

    public interface LocationInfo {
        public void GetLocationInfo(String outJson);
    }

    //jni interface
    public static native String Locate(ArrayList<ChigooWifiInfo> infoArrayList);//,CVector3 vector3

    public ChigooWifiLocation(Context context) {
        mContext = context;
        mScanThread = new ScanThread();
        mWifiInfoArray = new ArrayList<>();
    }

    public void setLocationInfo(LocationInfo listener)
    {
        locationInfo = listener;
    }

    public void init(String ConfigFilePath) {
    //Open Wifi
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

    }

    public void deinit() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

    }

    public void startLocation(int frequence,ChigooService chigooService) {
        mScanThread.setmSleepTime(frequence);
        mScanThread.setChigooService( chigooService );
        mScanThread.setThreadRunning(true);
        mScanThread.start();
    }

    public void stopLocation() {
        mScanThread.setThreadRunning(false);
        mScanThread.interrupt();
    }

    class ScanThread extends Thread {
        private boolean isThreadRunning;
        private int mSleepTime;
        ChigooService chigooService;

        public void setThreadRunning(boolean threadRunning) {
            isThreadRunning = threadRunning;
        }

        public void setmSleepTime(int mSleepTime) {
            this.mSleepTime = mSleepTime;
        }

        public void setChigooService(ChigooService cs){ this.chigooService = cs;}

        public boolean Stoped(){
            //判断线程是否要停止
            return (isThreadRunning==true) ? false : true;
        }

        @Override
        public void run() {
            while(!Stoped() && !interrupted()) {
                Log.e( "weight loc"," 1" );
                mWifiInfoArray.clear();
                mWifiManager.startScan();
                mWifiList = mWifiManager.getScanResults();
                Log.e( "weight loc","2" );
                for (ScanResult result : mWifiList) {
                    //if (result.frequency >= 5000 ) continue;//<=3000 2.4
                    String mac = result.BSSID;
                    int rssi = result.level;
                    ChigooWifiInfo info = new ChigooWifiInfo(mac, rssi);
                    mWifiInfoArray.add(info);
                }
                Log.e( "weight loc"," 3" );
                Lock lock = new ReentrantLock(  );
                lock.lock();
                //CVector3 vector3 = new CVector3( 0,0,0 );
                //String out = Locate(mWifiInfoArray,vector3);
                int compass = -1,compass1 = 0;
                compass1 = chigooService.getInt( "compass",-1 );
                chigooService.getInt( "compass" ,compass);
                String out = Locate(mWifiInfoArray);
                lock.unlock();
                Log.e( "weight loc"," 4" );
                locationInfo.GetLocationInfo(out);
                Log.e( "weight loc"," 5" );
                try {
                    sleep(1000 * mSleepTime);
                    Log.e( "weight loc","6" );
                } catch (InterruptedException e) {
                    Log.e( "weight loc","7 " );
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
