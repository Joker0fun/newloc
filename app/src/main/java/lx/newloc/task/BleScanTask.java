package lx.newloc.task;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.chigoo.ChigooBleListener;
import android.chigoo.ChigooCommunicationService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.chigoo.loc.lib.Algrithms;
import cn.chigoo.loc.lib.LocLoad;
import cn.chigoo.loc.lib.LocType.*;
import lx.newloc.SubActivity.BleGatherActivity;

/**
 * Created by Administrator on 2017/11/27.
 */

public class BleScanTask extends AsyncTask<Void,Object,List<TRealBleFinger>> {
    private static final String STlog = "BleAsy";
    private String scanId;
    private Context context;
    private String filePathDir;
    private int scanCounts;
    private int scanCount;
    private long scanInterval;
    private boolean startscan = false;
    private BleGatherActivity bleGatherActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BleScanListener bleTestListener;
    private Handler handler;
    Map<String,String> ssidmap;
    private List<TRealBleFinger> temblelist = new ArrayList<TRealBleFinger>(  );

    public BleScanTask(Context context, String scanid,String filePathDir
            , int scanCounts, long scanInterval,Map<String,String> ssidmap){
        this.context = context;
        this.bleGatherActivity = (BleGatherActivity)context;
        this.scanInterval = scanInterval;
        this.scanId = scanid;
        this.filePathDir = filePathDir;
        this.scanCounts = scanCounts;
        this.ssidmap = ssidmap;
    }

    @Override
    protected void onPreExecute(){

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
                if (temblelist != null){
                    TBlueTooth blueTooth = AnalyticBleInfo(device,rssi,scanRecord);
                    int Bleindex =Algrithms.SearchBFIndex( blueTooth.getBleId(),temblelist );
                    if (-1 != Bleindex){
                        if (null == temblelist.get( Bleindex ).Rssilist){
                            temblelist.get( Bleindex ).Rssilist = new ArrayList<Integer>(  );
                        }
                        temblelist.get( Bleindex ).setRssi(  (short)blueTooth.getRssi()  );
                        temblelist.get( Bleindex ).Rssilist.add( Integer.valueOf( blueTooth.getRssi() ) );
                        publishProgress(temblelist.get( Bleindex ),scanCount);
                    }else {
                        TRealBleFinger bluefinger = new TRealBleFinger();
                        bluefinger.setmmid( blueTooth.getBleId() );
                        bluefinger.setRssi( (short)blueTooth.getRssi() );
                        bluefinger.Rssilist = new ArrayList<Integer>(  );
                        bluefinger.Rssilist.add( Integer.valueOf( blueTooth.getRssi() ) );
                        temblelist.add( bluefinger );
                        publishProgress(bluefinger,scanCount);
                    }

                    scanCount++;
                }
            }
        }
    };

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

    @SuppressWarnings("unchecked")
    @Override
    protected List<TRealBleFinger> doInBackground(Void... vale){
        List<List<TRealBleFinger>> results = new ArrayList<List<TRealBleFinger>>(  );
        int TaskscanCount = 1;

        do {
            scanCount = 0;
            temblelist.clear();
            startscan = true;
            publishProgress( null,"------------------开始第"+TaskscanCount+"次扫描---------------" );
            try {
                Thread.sleep(scanInterval);
            } catch (Exception e) {
            }
            startscan = false;

            if (scanCount > 0){
                TaskscanCount++;
                List<TRealBleFinger> group = new ArrayList<TRealBleFinger>(  );
                for (TRealBleFinger re:temblelist){
                    TRealBleFinger getBlefinger = new TRealBleFinger();
                    getBlefinger.setmmid(re.getmmid());
                    getBlefinger.setRssi( re.getR() );
                    if (re.Rssilist != null) {
                        getBlefinger.Rssilist = new ArrayList<Integer>();
                        for (Integer rss : re.Rssilist) {
                            getBlefinger.Rssilist.add( rss );
                        }
                    }
                    group.add( getBlefinger );
                }
                results.add( group );
            }
        }while (TaskscanCount < scanCounts + 1);

        List<TRealBleFinger> avelist = Algrithms.calcAverage( results );
        if (avelist != null && avelist.size() != 0){
            //按R强度排序
            Collections.sort( avelist );
        }
        publishProgress( null,"------------------统计平均---------------" );
        for (TRealBleFinger avinfo:avelist){
            publishProgress( avinfo,"" );
        }

        String filename = LocLoad.write2File(scanId,filePathDir,ssidmap,avelist);
        if (!TextUtils.isEmpty(filename)) {
            publishProgress(null, "扫描成功！");
            ssidmap.put( scanId,filename );
        }else{
            publishProgress(null, "文件写入失败，请重新扫描收集！");
        }

        return avelist;
    }

    /**
     * 运行在ui线程中，在doInBackground()执行完毕后执行
     */
    @Override
    protected void onPostExecute(List<TRealBleFinger> rlist){
        super.onPostExecute(rlist);
        if (this.bleTestListener != null) {
            this.bleTestListener.OnScanfinished( rlist );
            this.scanLeDevice(false);
        }
    }

    /**
     * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
     */
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate( values );
        if (this.bleTestListener != null){
            if (values.length > 0 ){
                this.bleTestListener.OnScanning(values);
            }
        }
    }

    public void setBleTestListener(BleScanListener bleTestListener){
        this.bleTestListener = bleTestListener;
    }

    public interface BleScanListener{
        void OnScanning(Object... values);
        void OnScanfinished(List<TRealBleFinger> result);
        void onDeleteFinished();
    }
}
/*cm = (ChigooCommunicationService)bleGatherActivity.getSystemService( Context.CHIGOO_COMMUNICATIONSERVICE );
        cb = new ChigooBleListener() {
            @Override
            public void onBleUpdate(int Rss, int major, int minor) {
                if (startscan){
                    Log.d("BleGatherInfo |", "BleGatherInfo: rssi = " + Rss + " major = "
                            + major + " minor " + minor + "|ScanCount:"+scanCount);

                    String temstr = String.format( "%05d%05d",major,minor );
                    if (temblelist != null){
                        int Bleindex =Algrithms.SearchBFIndex( temstr.toCharArray(),temblelist );
                        if (-1 != Bleindex){
                            if (null == temblelist.get( Bleindex ).Rssilist){
                                temblelist.get( Bleindex ).Rssilist = new ArrayList<Integer>(  );
                            }
                            temblelist.get( Bleindex ).setRssi(  (short)Rss  );
                            temblelist.get( Bleindex ).Rssilist.add( Integer.valueOf( Rss ) );
                            publishProgress(temblelist.get( Bleindex ),scanCount);
                        }else {
                            TRealBleFinger bluefinger = new TRealBleFinger();
                            bluefinger.setmmid( temstr.toCharArray() );
                            bluefinger.setRssi( (short)Rss );
                            bluefinger.Rssilist = new ArrayList<Integer>(  );
                            bluefinger.Rssilist.add( Integer.valueOf( Rss ) );
                            temblelist.add( bluefinger );
                            publishProgress(bluefinger,scanCount);
                        }

                        scanCount++;
                    }
                }
                *//*Log.d("BleGatherInfoTest |", "BleGatherInfo: rssi = " + Rss + " major = "
                        + major + " minor " + minor);*//*
            }
        };

        cm.setOnListener( cb );*/