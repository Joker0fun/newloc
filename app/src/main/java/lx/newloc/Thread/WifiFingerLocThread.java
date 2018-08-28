package lx.newloc.Thread;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.chigoo.loc.lib.Algrithms;
import cn.chigoo.loc.lib.LocType;
import lx.newloc.SubActivity.LunchActivity;


/**
 * Created by Administrator on 2017/12/27.
 */

public class WifiFingerLocThread extends Thread{

    private static final String LTlog = "WifiLocThread";
    private Context context;
    private int scanCount;
    private long scanInterval;
    private String rootpath;
    private boolean startLoc = false;
    private WifiManager wifiManager;
    private LunchActivity lunchActivity;
    //table list
    private List<LocType.TLocM> WifiMACList;
    //buff sample list
    private List<LocType.TLocIdMR> wififingerTempList;
    //get real wifilist
    private List<LocType.TLocMR> rlist = new ArrayList<LocType.TLocMR>(  );


    WifiLocListner wifiLocListener;

    boolean flag = false;

    public void ThreadLoccatTask(Context context, long scanInterval,String rootpath
            , List<LocType.TLocM>      wifiMACList
            , List<LocType.TLocIdMR>   wfingerTempList
            , List<LocType.TLocMR>     wifireallist)
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

    public void setwifiLocListener(WifiLocListner wifiLocListener1){
        this.wifiLocListener = wifiLocListener1;
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public void doexecute(Object... values){
        if (this.wifiLocListener != null){
            if (values.length == 2 ){
                this.wifiLocListener.OnwifiLocating(values);
            }
        }
    }

    @Override
    public void run(){
        while (this.flag) {
            rlist.clear();
            int ShouldScans = 0;
            String temstr= "{\"wifi\":[";
            {//while (ShouldScans < 3)
                if (this.wifiLocListener != null)
                    this.wifiLocListener.WifiScanning( wifiManager );
                try {
                    Thread.sleep( scanInterval );
                } catch (Exception e) {
                }
                List<ScanResult> wrlist = wifiManager.getScanResults();
                scanCount = wrlist.size();
                rlist.clear();
                if (scanCount > 0) {
                    for (ScanResult re : wrlist) {
                        LocType.TLocMR wifiter = new LocType.TLocMR();
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
                    int count = 0;
                    for (LocType.TLocMR wifiter : rlist) {
                        count++;
                        String subtemstr = "";
                        if (count == wrlist.size()){
                            subtemstr = String.format( "{\"mac\":\"%s\",\"rssi\":%d}", String.valueOf( wifiter.getM() ), wifiter.getR() );
                        }else {
                            subtemstr = String.format( "{\"mac\":\"%s\",\"rssi\":%d},", String.valueOf( wifiter.getM() ), wifiter.getR() );
                        }
                        temstr = temstr + subtemstr;
                    }
                    temstr = temstr + "],\"user\":\"postman1\",\"move\":1}";

                    ShouldScans++;
                }
            }
            boolean uploadResult = false;
            //String fileName = LocLoad.upwifilocfile(rootpath,rlist);
            //File file = new File(fileName);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式
            StringBody sb = new StringBody( temstr , ContentType.APPLICATION_JSON);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart( "locdata",sb )
                    .build();// 生成 HTTP POST 实体

            String res = "";
            LocType.TPoint LocId  = null;
            int locid = -1;
            /*try {//for yuanzhen loc algorithms
                //设置请求参数
                HttpClient client = new DefaultHttpClient();// 开启一个客户端 HTTP 请求
                HttpPost post = new HttpPost("http://192.168.103.82:40000");//创建 HTTP POST 请求
                post.addHeader( "Content-type","application/json; charset=utf-8" );
                post.setHeader( "Accept", "application/json" );
                post.setEntity(new StringEntity(temstr));
                HttpResponse response = client.execute(post);// 发起请求 并返回请求的响应
                Thread.sleep( 500 );
                if (response.getStatusLine().getStatusCode()==200) {
                    res = EntityUtils.toString( response.getEntity() );
                    uploadResult = true;
                    JSONObject den = new JSONObject( res );
                    locid = den.optInt( "id",-1 );
                    if (den.optInt( "floor",-1 ) != -1){
                        LocId = new LocType.TPoint();
                        LocId.setX((float)den.optDouble( "x" ));
                        LocId.setY((float)den.optDouble( "z" ));
                        LocId.setZ(den.optInt( "floor" ));
                    }
                }
                Thread.sleep( scanInterval );
            }catch (Exception e){
                e.printStackTrace();
            }*/

            locid = Algrithms.NEWstartLoc( rlist, WifiMACList, wififingerTempList, rootpath );
            //LocId
            if(locid != -1){
                doexecute( locid,"");
            }else {
                doexecute( -1,"当前无法定位");
            }
        }
    }

    public interface WifiLocListner{
        void OnwifiLocating(Object... values);
        void onwifiLocfinished();
        void WifiScanning(WifiManager wifiManager);
    }
}
