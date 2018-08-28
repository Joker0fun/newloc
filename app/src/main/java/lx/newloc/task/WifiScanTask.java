package lx.newloc.task;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.chigoo.loc.lib.Algrithms;
import cn.chigoo.loc.lib.LocLoad;
import cn.chigoo.loc.lib.LocType;
import lx.newloc.SubActivity.WifiGatherActivity;
import lx.newloc.ftp.FtpUtils;

/**
 * Created by Administrator on 2018/1/8.
 */

public class WifiScanTask extends AsyncTask<Void,Object,List<LocType.TWifiInfo>>{
    private static final String STlog = "WifiAsy";
    private String scanId;
    private Context context;
    private String filePathDir;
    private int scanCounts;
    private long scanInterval;
    private WifiGatherActivity wifiGatherActivity;
    private WifiScanListener wifiTestListener;
    private Handler handler;
    WifiManager wifiManager;
    Map<String,String> ssidmap;

    public WifiScanTask(Context context, String scanid,String filePathDir
            , int scanCounts, long scanInterval,Map<String,String> ssidmap){
        this.context = context;
        this.wifiGatherActivity = (WifiGatherActivity)context;
        this.scanInterval       = scanInterval;
        this.scanId             = scanid;
        this.filePathDir        = filePathDir;
        this.scanCounts         = scanCounts;
        this.ssidmap            = ssidmap;
        this.wifiManager        = (WifiManager)context.getSystemService( Context.WIFI_SERVICE );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<LocType.TWifiInfo> doInBackground(Void... vale){
        //List<List<LocType.TWifiInfo>> SumList = new ArrayList<List<LocType.TWifiInfo>>(  );
        List<LocType.TWifiInfo> results = new ArrayList<LocType.TWifiInfo>(  );
        int TaskscanCount = 1;
        do {
            publishProgress( null,"------------------开始第"+TaskscanCount+"次扫描---------------" );
            if (this.wifiTestListener != null)
                this.wifiTestListener.WifiScanning(wifiManager);
            try {
                Thread.sleep(scanInterval);
            } catch (Exception e) {
            }
            List<ScanResult> wrlist = wifiManager.getScanResults();

            if (wrlist.size() > 0){
                TaskscanCount++;
                results.clear();
                for (ScanResult  re:wrlist){
                    LocType.TWifiInfo getwififinger = new LocType.TWifiInfo();
                    getwififinger.setMAC( re.BSSID.toCharArray() );
                    getwififinger.setRss( (short)re.level );
                    if (re.frequency <= 3000) {
                        getwififinger.setFeq( "2.4G" );
                    }else if (re.frequency >= 5000){
                        getwififinger.setFeq( "5.8G" );
                    }

                    publishProgress( getwififinger, "" );
                    results.add( getwififinger );
                }
                Collections.sort( results);

                if (results.size() != 0) {
                    String filename = LocLoad.writeWifi2File(scanId,filePathDir,ssidmap,results);
                    if (!TextUtils.isEmpty(filename)) {
                        publishProgress(null, "扫描成功！");
                        ssidmap.put( scanId,filename );
                    }else{
                        publishProgress(null, "文件写入失败，请重新扫描收集！");
                    }
                    //SumList.add( results );
                }
            }
        }while (TaskscanCount < scanCounts + 1);

        LocLoad.writeWifi2File(scanId,filePathDir,ssidmap,null);

        /*publishProgress( null,"------------------写入平均---------------" );
        List<LocType.TLocMR> result = Algrithms.calcWifiAverage(SumList,scanCounts);
        Collections.sort( result);
        for(LocType.TLocMR mr : result)
        {
            publishProgress(mr, "");
        }

        LocLoad.writeWifiR2File(scanId,filePathDir,result);

        String filename = LocLoad.writeWifi2File(scanId,filePathDir,ssidmap,result);
        if (!TextUtils.isEmpty(filename)) {
            publishProgress(null, "扫描成功！");
            ssidmap.put( scanId,filename );
        }else{
            publishProgress(null, "文件写入失败，请重新扫描收集！");
        }*/

        return results;
    }

    @Override
    protected void onPostExecute(List<LocType.TWifiInfo> relist){
        super.onPostExecute( relist );
        if (this.wifiTestListener != null) {
            this.wifiTestListener.OnScanfinished( relist );
        }
    }

    /**
     * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
     */
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate( values );
        if (this.wifiTestListener != null){
            if (values.length > 0 ){
                this.wifiTestListener.OnScanning(values);
            }
        }
    }

    public void setWifiScanListener(WifiScanListener wifiTestListener){
        this.wifiTestListener = wifiTestListener;
    }

    public interface WifiScanListener{
        void OnScanning(Object... values);
        void OnScanfinished(List<LocType.TWifiInfo> result);
        void onDeleteFinished();
        void WifiScanning(final WifiManager wifiManager);
    }
}
