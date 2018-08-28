package lx.newloc.Thread;

/**
 * Created by Administrator on 2018/6/12.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import  cn.chigoo.loc.lib.LocType.*;
//import cn.chigoo.loc.BleLoc.BleLocType.*;
import lx.newloc.SubActivity.LunchActivity;

import static cn.chigoo.loc.BleLoc.BleLoaAlgrithms.*;
import static cn.chigoo.loc.lib.Algrithms.GetNearstNavId;
//import static cn.chigoo.loc.BleLoc.BleLocLoadFile.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.chigoo.ChigooBleListener;
import android.chigoo.ChigooCommunicationService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;


public class BleLocMainThread extends Thread{
    private long scanInterval;
    private List<TBlueTooth> btriList;
    private List<TNavHotMsg> nlist;
    private List<TPathRgn>   plist;
    private List<TBlueTooth> rlist = new ArrayList<TBlueTooth>(  );
    private List<List<TBlueTooth>> temlists = new ArrayList<List<TBlueTooth>>(  );
    private List<TBlueTooth> bFilterList = new ArrayList<>(  );
    //kalman loc
    private static TPoint kal = null;
    private static float xP = (float)0.01;
    private static float yP = (float)0.01;
    BleLocMainListener bleLocListener;
    boolean flag = false;
    private LunchActivity lunchActivity;
    private boolean startscan = false;
    private BluetoothAdapter mBluetoothAdapter;

    public void ThreadLoccatTask(LunchActivity context, long scanInterval
            , List<TBlueTooth>      triList
            , List<TNavHotMsg>      nlist
            , List<TPathRgn>        plist
            ,BluetoothAdapter mbluetoothadapter)
    {
        this.lunchActivity          = (LunchActivity)context;
        this.scanInterval           = scanInterval;
        this.btriList               = triList;
        this.nlist                  = nlist;
        this.plist                  = plist;
        this.mBluetoothAdapter      = mbluetoothadapter;
        //setListner();
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            /*Scanning = true;*/
            mBluetoothAdapter.startLeScan(this.mLeScanCallback);
        } else {
            /*Scanning = false;*/
            mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (startscan) {
                String address = device.getAddress();
                String deviceName = device.getName();
                if (rlist != null){
                    rlist.add( AnalyticBleInfo(device,rssi,scanRecord) );
                }
                doexecute("正在定位中");
            }
        }
    };

    /*public void setListner(){
        this.rlist.clear();
        this.cm = (ChigooCommunicationService)lunchActivity.getSystemService( Context.CHIGOO_COMMUNICATIONSERVICE );
        this.cb = new ChigooBleListener() {
            @Override
            public void onBleUpdate(int Rss, int major, int minor) {
                if ((startscan)&&(major == 65535)){
                    String temstr = String.format( "%06d%06d",major,minor );
                    Log.d("BleLocInfo |", "BleLocInfo: rssi = " + Rss + " major = "
                            + major + " minor " + minor);
                    if (rlist != null){
                        TBlueTooth bluetri = new TBlueTooth();
                        bluetri.setBleId( temstr.toCharArray() );
                        bluetri.setRssi( (short)(Rss*-1) );
                        rlist.add( bluetri );
                    }
                    doexecute("正在定位中");
                    scanCount++;
                }
            }
        };
        this.cm.setOnListener( cb );
    }*/

    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public void BleLocMainThread(BleLocMainListener bleLocListener1){
        this.bleLocListener = bleLocListener1;
    }

    public void doexecute(Object... values){
        if (this.bleLocListener != null){
            if (values.length == 3 ){
                this.bleLocListener.OnbleLocating(values);
            }
        }
    }


    @Override
    public void run(){
        while (this.flag) {

            temlists.clear();
            int ShouldScans = 0;
            while (ShouldScans < 3) {
                ShouldScans++;
                rlist.clear();
                //scanLeDevice(true);
                startscan = true;
                try {
                    Thread.sleep( scanInterval );
                }catch (Exception e){
                    e.printStackTrace();
                }
                startscan = false;
                //scanLeDevice(false);
                //rlist = this.bleLocListener.GetBleScanInfo();
                if (rlist.size() > 0) {
                    Lock lock = new ReentrantLock(  );
                    lock.lock();
                    try {
                        List<TBlueTooth> group = new ArrayList<TBlueTooth>();
                        for (TBlueTooth re : rlist) {
                            TBlueTooth getBletri= new TBlueTooth();
                            getBletri.setBleId( re.getBleId() );
                            getBletri.setRssi( re.getRssi());
                            group.add( getBletri );
                        }
                        temlists.add( group );
                        ShouldScans++;
                    }catch (Exception e){

                    }finally {
                        lock.unlock();
                    }
                }
            }

            List<TBlueTooth> rblist = Var_Fileter_Ble( temlists,bFilterList );
            getRlistInfo( rblist,btriList );
            KalmanFilterB(rblist,bFilterList,5);
            DelInvaliDate(rblist);
            TPoint LocId = null, resp = null;
            LocId = getBestPoint( rblist );
            if (LocId != null) {
                if (kal == null) {
                    kal = new TPoint();
                    xP = (float)0.1;
                    kal.setX( LocId.getX() );
                    yP = (float)0.1;
                    kal.setY( LocId.getY() );
                } else {
                    Map<String, Float> res = null;
                    res = KalmanFilterL( LocId, kal, xP, yP );
                    if (res != null) {
                        xP = res.get( "xP" ).floatValue();
                        yP = res.get( "yP" ).floatValue();
                    }
                }
            }
            resp = BundlPath(LocId,plist,nlist);
            try {
                Thread.sleep( 500 );
            }catch (Exception e){

            }

            int id = GetNearstNavId(LocId,nlist);

            if(LocId != null){
                doexecute( LocId,"",rblist );
            }else {
                doexecute( null,"当前无法定位",rblist );
            }
        }
    }

    public interface BleLocMainListener{
        void OnbleLocating(Object... valuse);
        void onbleLocfinished();
        List<TBlueTooth> GetBleScanInfo();
    }


    private TBlueTooth AnalyticBleInfo(BluetoothDevice dev, int rssi, byte[] record){
        int startByte = 2;
        boolean patternFound = false;
        byte[] mScanRecord;
        BluetoothDevice mBleDev;
        String uuid;
        int major = 0;
        int minor = 0;

        mScanRecord = record;
        while (startByte <= 5) {
            if (((int) mScanRecord[startByte + 2] & 0xff) == 0x02 &&
                    ((int) mScanRecord[startByte + 3] & 0xff) == 0x15) {
                patternFound = true;
                break;
            }
            startByte++;
        }

        //if found
        if (patternFound) {
            //change to HEX
            byte[] uuidBytes = new byte[16];
            System.arraycopy(mScanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //iBeacon's UUID value
            uuid = hexString.substring(0, 8) + "-"
                    + hexString.substring(8, 12) + "-"
                    + hexString.substring(12, 16) + "-"
                    + hexString.substring(16, 20) + "-"
                    + hexString.substring(20, 32);

            major = (mScanRecord[startByte + 20] & 0xff) << 8 | (mScanRecord[startByte + 21] & 0xff);

            minor = (mScanRecord[startByte + 22] & 0xff) << 8 | (mScanRecord[startByte + 23] & 0xff);
        }
        String temstr = String.format( "%06d%06d",major,minor );
        TBlueTooth tem = new TBlueTooth();
        tem.setRssi( (short)rssi );
        tem.setBleId( temstr.toCharArray() );
        return tem;
    }


    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    /*BleLocListener bleLocListener = new BleLocListener() {
        @Override
        public void OnbleLocating(final Object... values){
            Message msg = new Message();
            Bundle data = new Bundle(  );
            if(values[0] != null) {
                LocId = (TPoint)values[0];
                lochandler.post( new Runnable() {
                    @Override
                    public void run() {
                        if (LocId != null) {
                            OnLocDraw();
                        }
                    }
                } );
                data.putInt( "loc",0 );
                data.putFloat( "locx",LocId.getX() );
                data.putFloat( "locy",LocId.getY() );
            }else {
                data.putInt( "loc",-1 );
            }
            List<TBlueTooth> showlist = (List<TBlueTooth>)values[2];
            String[] strings = new String[showlist.size()];
            data.putStringArray( "show",strings );
            for (int i = 0;i < showlist.size();i++){
                strings[i] = String.format( "%s|%d|%.2f|%.2f"
                        ,String.valueOf(showlist.get( i ).getBleId())
                        ,showlist.get( i ).getRssi()
                        ,showlist.get( i ).getDis()
                        ,showlist.get( i ).getWeight());
            }
            data.putStringArray( "showble",strings );
            msg.setData( data );
            MsgShow.sendMessage( msg );
        }

        @Override
        public void onbleLocfinished(){
            StartLoc = false;
            bleFingerLocThread.setFlag( false );
            bleFingerLocThread.interrupt();
        }

        @Override
        public List<TBlueTooth> GetBleScanInfo(){
            List<TBlueTooth>  rlist = null;
            if (major == 65535){
                String temstr = String.format( "%06d%06d",major,minor );
                Log.d("BleLocInfo |", "BleLocInfo: rssi = " + Rss + " major = "
                        + major + " minor " + minor);
                if (rlist != null){
                    TBlueTooth bluetri = new TBlueTooth();
                    bluetri.setBleId( temstr.toCharArray() );
                    bluetri.setRssi( (short)(Rss*-1) );
                    rlist.add( bluetri );
                }
                doexecute("正在定位中");
                scanCount++;
            }
            return rlist;
        }
    };*/
}
