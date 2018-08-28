package lx.newloc.SubActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import cn.chigoo.loc.lib.LocType.*;
import lx.newloc.Adapter.UpdateAdapter;
import lx.newloc.R;
import lx.newloc.task.BleScanTask;
import lx.newloc.task.BleScanTask.*;

import static cn.chigoo.loc.lib.LocLoad.searchFile;


public class BleGatherActivity extends AppCompatActivity {
    public String                usbpath = "/storage/usb2/BleInfoGather/BleInfoGather.xml";
    public String                filePathDir = "/storage/sdcard1/BleInfoGather/Data/";
    public String                sdpath = "/storage/sdcard1/BleInfoGather/BleInfoGather.xml";
    public boolean               SaveToUsb = false;
    public String                scanId;
    public String                AirportCodeThree = "CTU";
    public boolean               isStartScan = false;
    private String               RootDir = "storage/";
    private HashMap<String, String>     ssidMap;
    TextView                AirPortName;
    EditText                etPointName;
    Button                  btnScan;
    TextView                tvResult, textView2;
    EditText                edtScanCounts, edtScanInterval;
    ListView                ssidListView;
    Handler                 handler = new Handler();
    ScrollView              scrollViewResult;
    UpdateAdapter           upateAdapter = null;
    BleScanTask             scanTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_ble_gather );

        ssidMap = new HashMap<String, String>();
        String sdName = getSdName(RootDir);
        if (!TextUtils.isEmpty(sdName) && !"sdcard1".equalsIgnoreCase(sdName)) {
            filePathDir = filePathDir.replace("sdcard1", sdName);
            sdpath = sdpath.replace("sdcard1", sdName);
        }
        File usb = new File("/storage/usb2/BleInfoGather/");
        if (usb.exists()) {
            filePathDir = "/storage/usb2/BleInfoGather/Data/";
            SaveToUsb = true;
        }
        searchFile(filePathDir, "txt",ssidMap);
        findViewById();
        bindDate();
        bindEvents();
        showSoftInput(edtScanCounts, false);
    }

    private  void bindDate(){
        AirPortName.setText(AirportCodeThree);
        upateAdapter = new UpdateAdapter(BleGatherActivity.this, ssidMap);
        upateAdapter.setUplistn( bleScanListener );
        ssidListView.setAdapter(upateAdapter);
    }

    private void bindEvents() {
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSoftInput(edtScanCounts, false);
                showSoftInput(edtScanInterval, false);
                tvResult.setText("");
                textView2.setText(Html.fromHtml("开始扫描"));
                if (!isStartScan) {
                    scanId = etPointName.getText().toString().trim();
                    if (TextUtils.isEmpty(scanId)) {
                        String tems = "请填写扫描点！";
                        Toast.makeText(BleGatherActivity.this,tems,tems.length() );
                        return;
                    }
                    int scanCounts = Integer.parseInt(edtScanCounts.getText().toString());
                    int scanInterval = Integer.parseInt(edtScanInterval.getText().toString()) * 1000;
                    if (scanTask != null)
                        return;
                    scanTask = new BleScanTask(BleGatherActivity.this,scanId,filePathDir, scanCounts, scanInterval,ssidMap);
                    scanTask.setBleTestListener(bleScanListener);
                    scanTask.execute();
                    isStartScan = true;
                    btnScan.setText(" ");
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
            btnScan.setText( "开始" );
        }
    }

    private void findViewById(){
        etPointName = (EditText) findViewById(R.id.etPointName);
        AirPortName = (TextView) findViewById(R.id.AirPortName);
        btnScan = (Button) findViewById(R.id.btnScan);
        textView2 = (TextView) findViewById(R.id.textView2);
        tvResult = (TextView) findViewById(R.id.tvResult);
        ssidListView = (ListView) findViewById(R.id.ssidListView);
        edtScanCounts = (EditText) findViewById(R.id.edtScanCounts);
        edtScanInterval = (EditText) findViewById(R.id.edtScanInterval);
        scrollViewResult = (ScrollView) findViewById(R.id.scrollViewResult);
        tvResult.setTypeface( Typeface.MONOSPACE);
    }

    /**
     * 获取SD卡名称
     * @return
     */
    public static String getSdName(String rf) {
        String sdcard_path = null;
        String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
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
            return sdcard_path;
        }
    }

    BleScanTask.BleScanListener bleScanListener = new BleScanTask.BleScanListener(){
        @Override
        public void OnScanning(Object... info){
            StringBuilder sbBuilder = new StringBuilder();
            if (info.length == 2) {
                if (info[0] != null) {
                    sbBuilder.append( info[0].toString()+"|"+String.valueOf(info[1]) );
                } else {
                    sbBuilder.append( info[1]);
                }
            }else {
                tvResult.setText( "Ble 信息收集异常" );
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
        public void OnScanfinished(List<TRealBleFinger> result){
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
            btnScan.setText("开始");
            scanTask.cancel( true );
            scanTask = null;
        }

        @Override
        public void onDeleteFinished(){
            upateAdapter = new UpdateAdapter( BleGatherActivity.this, ssidMap );
            upateAdapter.setUplistn( bleScanListener );
            ssidListView.setAdapter( upateAdapter );
        }
    };

    public void showSoftInput(View focusView, boolean show) {
        InputMethodManager imm = (InputMethodManager) BleGatherActivity.this.getSystemService( Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.showSoftInput(focusView, InputMethodManager.SHOW_FORCED);
        } else {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); // 强制隐藏键盘
        }
    }

}


