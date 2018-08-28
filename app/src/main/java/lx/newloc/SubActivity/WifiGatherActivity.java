package lx.newloc.SubActivity;
import java.net.HttpURLConnection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.chigoo.loc.lib.LocType;
import lx.newloc.Adapter.WifiUpdateAdapter;
import lx.newloc.R;
import lx.newloc.ftp.FtpUtils;
import lx.newloc.task.WifiScanTask;
import lx.newloc.task.WifiUploadTask;

import static cn.chigoo.loc.lib.LocLoad.searchFile;

public class WifiGatherActivity extends AppCompatActivity {
    public String                usbpath = "/WifiInfoGather/";
    public String                xmlpath = "";
    public String                filePathDir = "sdcard1/WifiInfoGather/";
    public String                sdpath = "/storage/sdcard1/WifiInfoGather/WifiInfoGather.xml";
    public String                scanId;
    public boolean               isStartScan = false;
    boolean                      Scan58g = false;
    boolean                      Scan24g = true;
    private String               RootDir = "storage/";
    private HashMap<String, String> ssidMap;
    TextView                    wifiAirPortName;
    EditText                    etPointName;
    Button                      btnwifiScan;
    Button                      btnwifiUpload;
    TextView                    tvResult, textView2,textViewP;
    EditText                    edtwifiScanCounts, edtwifiScanInterval;
    ListView                    ssidListView;
    ScrollView                  scrollViewResult;
    public static String AirportCodeThree = "WUX";
    WifiUpdateAdapter upateAdapter = null;
    WifiScanTask scanTask       = null;
    Handler handler             = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_wifi_gather );

        String sdName = null;
        try {
            sdName = Environment.getExternalStorageSdDirectory().getAbsolutePath();
        }catch (Exception e){
            Toast.makeText( WifiGatherActivity.this,"sdcard 不存在",Toast.LENGTH_SHORT );
            e.printStackTrace();
            AlertDialog showm = new AlertDialog.Builder( WifiGatherActivity.this )
                    .setTitle( "Error!" )
                    .setMessage( "Sdcard is not exist!" )
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit( 0 );
                        }
                    }).create();
            showm.show();
        }
        if (sdName != null) {
            ssidMap = new HashMap<String, String>();
            if (!TextUtils.isEmpty( sdName ) && !"sdcard1".equalsIgnoreCase( sdName )) {
                filePathDir = filePathDir.replace( "sdcard1", sdName );
                sdpath = sdpath.replace( "sdcard1", sdName );
            }

            File usb;
            if (Build.VERSION.SDK_INT >= 23) {
                usb = new File( getSdName( RootDir ) + usbpath );
            } else {
                usb = new File( "/storage/usb2/WifiInfoGather/" );
            }

            if (usb.exists()) {
                filePathDir = getSdName( RootDir ) + usbpath + "Data/";
                xmlpath = getSdName( RootDir ) + usbpath;
            }
            searchFile( filePathDir, "txt", ssidMap );
            loadScanConfig();
            findViewById();
            bindDate();
            bindEvents();
            showSoftInput( edtwifiScanCounts, false );
        }
    }

    private  void bindDate(){
        textViewP.setText( "保存路径："+ filePathDir );
        wifiAirPortName.setText(AirportCodeThree);
        upateAdapter = new WifiUpdateAdapter(WifiGatherActivity.this, ssidMap);
        upateAdapter.setUplistn( wifiScanListener );
        ssidListView.setAdapter(upateAdapter);
    }

    private void bindEvents() {
        btnwifiUpload.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanTask != null)
                    return;

                if (ssidMap != null && ssidMap.size() != 0) {

                    final ProgressDialog progressDialog = new ProgressDialog( WifiGatherActivity.this );
                    progressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
                    progressDialog.setTitle( "正在上传中。。。" );
                    progressDialog.setProgress( 0 );
                    progressDialog.setMax( ssidMap.size() );

                    progressDialog.show();
                    progressDialog.setCancelable( false );
                    progressDialog.setCanceledOnTouchOutside( false );

                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // TODO Auto-generated method stub
                            wifiScanListener.onDeleteFinished();
                        }
                    });
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            Iterator <Map.Entry<String,String>> it = ssidMap.entrySet().iterator();
                            float sum = ssidMap.size() + 1,count = 0;

                            while (it.hasNext()){
                                try {
                                    count++;
                                    progressDialog.setProgress( (int)(count/sum*100) );
                                    Map.Entry<String,String> entry = it.next();
                                    String fileName = entry.getValue();

                                    File file = new File(fileName);

                                    InputStream inputStream;
                                    boolean uploadResult=false;
                                    try {
                                        inputStream = new FileInputStream(file);
                                        uploadResult = FtpUtils.uploadFile("221.178.187.38", 21,"chigooWifi", "chigooWifi","/"+ WifiGatherActivity.AirportCodeThree+"/", file.getName(),inputStream);
                                    } catch (FileNotFoundException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    if (uploadResult){
                                        file.delete();
                                        it.remove();
                                        /*ssidMap.remove( entry.getKey() );*/
                                    }
                                    progressDialog.incrementProgressBy( 1 );
                                } catch (Exception e) {
                                }
                            }
                            progressDialog.cancel();
                        }
                    }).start();
                }
            }
        } );

        btnwifiScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSoftInput(edtwifiScanCounts, false);
                showSoftInput(edtwifiScanInterval, false);
                tvResult.setText("");
                textView2.setText(Html.fromHtml("开始扫描"));
                if (!isStartScan) {
                    scanId = etPointName.getText().toString().trim();
                    if (TextUtils.isEmpty(scanId)) {
                        String tems = "请填写扫描点！";
                        Toast.makeText(WifiGatherActivity.this,tems,tems.length() );
                        return;
                    }
                    int scanCounts = Integer.parseInt(edtwifiScanCounts.getText().toString());
                    int scanInterval = Integer.parseInt(edtwifiScanInterval.getText().toString()) * 1000;
                    if (scanTask != null)
                        return;

                    scanTask = new WifiScanTask( WifiGatherActivity.this
                            , scanId
                            , filePathDir
                            , scanCounts
                            , scanInterval
                            , ssidMap );
                    scanTask.setWifiScanListener( wifiScanListener );
                    scanTask.execute();
                    isStartScan = true;
                    btnwifiScan.setText( " " );
                } else {
                    cancel();
                }
            }
        });

    }

    private void cancel(){
        if (scanTask != null){
            scanTask.cancel( true );
            scanTask = null;
            tvResult.setText( "" );
            isStartScan = false;
            btnwifiScan.setText( "开始" );
        }
    }

    private void findViewById(){
        etPointName = (EditText) findViewById(R.id.etwifiPointName);
        wifiAirPortName = (TextView) findViewById(R.id.WifiAirPortName);
        btnwifiScan = (Button) findViewById(R.id.btnwifiScan);
        btnwifiUpload = (Button)findViewById( R.id.btnwifiupload );
        textView2 = (TextView) findViewById(R.id.textwifiView2);
        textViewP = (TextView) findViewById( R.id.tvPathShow );
        tvResult = (TextView) findViewById(R.id.tvwifiResult);
        ssidListView = (ListView) findViewById(R.id.macListView);
        edtwifiScanCounts = (EditText) findViewById(R.id.edtwifiScanCounts);
        edtwifiScanInterval = (EditText) findViewById(R.id.edtwifiScanInterval);
        scrollViewResult = (ScrollView) findViewById(R.id.scrollwifiViewResult);
        tvResult.setTypeface( Typeface.MONOSPACE);
    }

    /**
     * 获取SD卡名称
     * @return
     */
    private static String getSdName(String rf) {
        String sdcard_path = null;

        String sd_default = Environment.getExternalStorageSdDirectory().getAbsolutePath();
        //Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("text", sd_default);
        if (sd_default.endsWith("/")) {
            sd_default = sd_default.substring(0, sd_default.length() - 1);
        }
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }

                        sdcard_path = columns[1];
                    }
                } else if (line.contains("fuse") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        String sdnamebuf[] = columns[1].split( "/" );
                        sdcard_path = sdnamebuf[sdnamebuf.length-1];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File( rf + sdcard_path );
        if (!file.exists()) {
            return null;
        } else {
            //int index = sdcard_path.lastIndexOf("/");
            //String sdName = sdcard_path.substring(index + 1, sdcard_path.length());
            return rf + sdcard_path;
        }
    }

    private static String getUSBName(){
        String usb_default = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("usb", usb_default);
        return usb_default;
    }

    WifiScanTask.WifiScanListener wifiScanListener = new WifiScanTask.WifiScanListener() {
        @Override
        public void OnScanning(Object... values) {
            StringBuilder sbBuilder = new StringBuilder();
            if (values.length == 2) {
                if (values[0] != null) {
                    sbBuilder.append( values[0].toString()+"|"+String.valueOf(values[1]) );
                } else {
                    sbBuilder.append( values[1]);
                }
            }else {
                tvResult.setText( "Wifi 信息收集异常" );
            }
            sbBuilder.append( "\n" );
            sbBuilder.append( tvResult.getText().toString() + "\n" );
            tvResult.setText( sbBuilder.toString() );
            handler.post( new Runnable() {
                @Override
                public void run() {
                    scrollViewResult.fullScroll( ScrollView.FOCUS_UP );
                }
            } );
        }

        @Override
        public void OnScanfinished(List<LocType.TWifiInfo> result) {
            StringBuilder sbBuilder = new StringBuilder();
            sbBuilder.append(tvResult.getText());
            sbBuilder.append("\n");
            sbBuilder.append("扫描成功！");
            onDeleteFinished();
            if (result == null || result.size() == 0) {
                textView2.setText(Html.fromHtml("扫描结果：<font color='red'>扫描失败,请重新扫描 ！</font>"));
            } else {
                textView2.setText(Html.fromHtml("扫描结果：<font color='red'>扫描成功！</font>"));
            }
            isStartScan = false;
            btnwifiScan.setText("开始");
            scanTask.cancel( true );
            scanTask = null;
        }

        @Override
        public void onDeleteFinished() {
            upateAdapter = new WifiUpdateAdapter( WifiGatherActivity.this, ssidMap );
            upateAdapter.setUplistn( wifiScanListener );
            ssidListView.setAdapter( upateAdapter );
            ssidListView.refreshDrawableState();
        }

        @Override
        public void WifiScanning(final WifiManager wifiManager) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wifiManager.startScan();
                }
            });
        }
    };

    public void showSoftInput(View focusView, boolean show) {
        InputMethodManager imm = (InputMethodManager) WifiGatherActivity.this.getSystemService( Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.showSoftInput(focusView, InputMethodManager.SHOW_FORCED);
        } else {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); // 强制隐藏键盘
        }
    }

    private void loadScanConfig() {
        XmlPullParser pullParser = Xml.newPullParser();
        InputStream is;
        try {
            String xmlfilepath;

            xmlfilepath = xmlpath;
            File file = new File( xmlfilepath + "WifiInfoGather.xml" );
            if(file.exists()){
                is = new FileInputStream( file );
                pullParser.setInput( is, "utf-8" );
                //得到事件类型
                int eventType = pullParser.getEventType();
                //文档的末尾
                //遍历内部的内容

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String name = pullParser.getName();
                    if (!TextUtils.isEmpty( name )) {
                        String test = pullParser.getAttributeValue( null, "key" );
                        String s = pullParser.getAttributeValue( null, "value" );
                        if (test != null) {
                            switch (test) {
                                case "SCAN5G":
                                    Scan58g = Boolean.parseBoolean( s );
                                    break;
                                case "SCAN24G":
                                    Scan24g = Boolean.parseBoolean( s );
                                    break;
                                case "AirportCodeThree":
                                    AirportCodeThree = s;
                                    break;
                            }
                        }
                    }

                    eventType = pullParser.next();//读取下一个标签
                }
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
