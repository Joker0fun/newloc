package lx.newloc.Thread;

import android.chigoo.ChigooBleListener;
import android.chigoo.ChigooCommunicationService;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.chigoo.loc.lib.Algrithms;
import cn.chigoo.loc.lib.LocType.*;
import lx.newloc.SubActivity.LunchActivity;

import static cn.chigoo.loc.lib.Algrithms.*;
import static cn.chigoo.loc.lib.BleTraingleLoc.*;

/**
 * Created by Administrator on 2017/12/14.
 */

public class BleFingerLocThread extends Thread{
    private static final String LTlog = "LocTaskAsynlistner";
    private Context context;
    private int scanCount;
    private long scanInterval;
    private int LocMethod;
    private boolean startscan = false;
    private LunchActivity lunchActivity;
    private ChigooCommunicationService cm;
    private ChigooBleListener cb;
    //trilateration or base centra
    private List<TBlueTooth> bFilterList = new ArrayList<>(  );
    private List<TBlueTooth> btriList;
    private List<TNavHotMsg> nlist;
    private List<TPathRgn>   plist;
    private List<TBlueTooth> rlist = new ArrayList<TBlueTooth>(  );
    private List<List<TBlueTooth>> temlists = new ArrayList<List<TBlueTooth>>(  );
    //kalman loc
    private static TPoint kal = null;
    private static float xP = (float)0.01;
    private static float yP = (float)0.01;


    BleLocListener bleLocListener;

    boolean flag = false;

    public void ThreadLoccatTask(Context context, long scanInterval
            ,List<TBlueTooth>      triList
            ,List<TNavHotMsg>      nlist
            ,List<TPathRgn>        plist
            ,int LocMethod)
    {
        this.lunchActivity          = (LunchActivity)context;
        this.scanInterval           = scanInterval;
        this.btriList               = triList;
        this.LocMethod              = LocMethod;
        this.nlist                  = nlist;
        this.plist                  = plist;
        setListner();
    }

    public void setBleLocListener(BleLocListener bleLocListener1){
        this.bleLocListener = bleLocListener1;
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public void setListner(){
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
                scanCount = 0;
                rlist.clear();
                startscan = true;
                try {
                    Thread.sleep( scanInterval );
                }catch (Exception e){
                    e.printStackTrace();
                }
                startscan = false;
                if (scanCount > 0) {
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

            List<TBlueTooth> rblist = Algrithms.Var_Fileter_Ble( temlists,bFilterList );
            getRlistInfo( rblist,btriList );
            KalmanFilterB(rblist,bFilterList,5);
            DelInvaliDate(rblist);
            TPoint LocId = null, resp = null;
            LocId = BleTrilateration( rblist, LocMethod );
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
            //BundlNav(LocId,nlist,(float)1);
            //BundlBle(LocId,btriList,(float)2);
            resp = BundlPath(LocId,plist,nlist,1);

            int id = GetNearstNavId(LocId,nlist);
            try {
                Thread.sleep( 500 );
            }catch (Exception e){

            }

            if(resp != null){
                doexecute( resp,"",rblist );
            }else {
                doexecute( null,"当前无法定位",rblist );
            }
        }
    }

    public interface BleLocListener{
        void OnbleLocating(Object... valuse);
        void onbleLocfinished();
    }
}

