package lx.newloc.Thread;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.chigoo.loc.lib.Algrithms;
import cn.chigoo.loc.lib.LocType.*;
import lx.newloc.SubActivity.LunchActivity;

/**
 * Created by Administrator on 2018/5/18.
 */

public class FingerLocThread extends Thread{
    private static final String LTlog = "LocThread";
    private Context context;
    private int scanCount;
    private long scanInterval;
    private String rootpath;
    private boolean startLoc = false;
    private WifiManager wifiManager;
    private LunchActivity lunchActivity;
    //table list
    private List<TLocM> WifiMACList;
    //buff sample list
    private List<TLocIdMR> wififingerTempList;
    //get real wifilist
    private List<TLocMR> rlist = new ArrayList<TLocMR>(  );


    LocListner LocListener;

    boolean flag = false;

    public void ThreadLocTask(Context context, long scanInterval,String rootpath
            , List<TLocM>      wifiMACList
            , List<TLocIdMR>   wfingerTempList
            , List<TLocMR>     wifireallist)
    {
        this.context                = context;
        this.lunchActivity          = (LunchActivity)context;
        this.scanInterval           = scanInterval;
        this.rootpath               = rootpath;
        this.WifiMACList            = wifiMACList;
        this.wififingerTempList     = wfingerTempList;
        //this.rlist                  = wifireallist;
        this.wifiManager            = (WifiManager)context.getSystemService( Context.WIFI_SERVICE );
    }

    public void setLocListener(LocListner LocListener1){
        this.LocListener = LocListener1;
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public void doexecute(Object... values){
        if (this.LocListener != null){
            if (values.length == 2 ){
                this.LocListener.OnLocating(values);
            }
        }
    }

    @Override
    public void run(){
        while (this.flag) {
            rlist.clear();
            int ShouldScans = 0;
            {//while (ShouldScans < 3)
                if (this.LocListener != null)
                    this.LocListener.Scanning( wifiManager );
                try {
                    Thread.sleep( scanInterval );
                } catch (Exception e) {
                }
                List<ScanResult> wrlist = wifiManager.getScanResults();
                scanCount = wrlist.size();
                rlist.clear();
                if (scanCount > 0) {
                    for (ScanResult re : wrlist) {
                        TLocMR wifiter = new TLocMR();
                        if (re.frequency <= 3000) {
                            wifiter.setFeq( "2.4G" );
                        }else if (re.frequency >= 5000){
                            wifiter.setFeq( "5.8G" );
                        }
                        wifiter.setAver( 0 );
                        wifiter.setMAC(re.BSSID.toCharArray());
                        wifiter.setRss( (short)re.level );
                        rlist.add( wifiter );
                    }
                    Collections.sort( rlist );

                    ShouldScans++;
                }
            }
            boolean uploadResult = false;
            String res = "";
            TPoint LocId = null;
            //LocType.TPoint LocId  = Algrithms.NEWstartLoc( rlist, WifiMACList, wififingerTempList, rootpath );
            try {
                //设置请求参数
                Thread.sleep( scanInterval );
            }catch (Exception e){
                e.printStackTrace();
            }
            if(LocId != null){
                doexecute( LocId,"" );
            }else {
                doexecute( null,"当前无法定位" );
            }
        }
    }

    public interface LocListner{
        void OnLocating(Object... values);
        void onLocfinished();
        void Scanning(WifiManager wifiManager);
    }

}
