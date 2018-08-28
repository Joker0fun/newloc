package lx.newloc.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import lx.newloc.MainActivity;
import lx.newloc.SubActivity.WifiGatherActivity;
import lx.newloc.ftp.FtpUtils;
import lx.newloc.task.WifiScanTask.*;

/**
 * Created by Administrator on 2018/2/2.
 */

public class WifiUploadTask extends AsyncTask<Void, Void, Boolean> {

    private HashMap<String, String> ssidMap;
    private String key;
    private WifiScanListener scanListener;
    private Context context;
    private File file;

    public WifiUploadTask(Context context, HashMap<String, String> ssidMap,
                          String key, WifiScanListener scanListener) {
        this.context = context;
        this.key = key;
        this.ssidMap = ssidMap;
        this.scanListener = scanListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String fileName = this.ssidMap.get(key);
        file = new File(fileName);

        InputStream inputStream;
        boolean uploadResult=false;
        try {
            inputStream = new FileInputStream(file);
            uploadResult = FtpUtils.uploadFile("221.178.187.38", 21,"chigooWifi", "chigooWifi","/"+ WifiGatherActivity.AirportCodeThree+"/", file.getName(),inputStream);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return uploadResult;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (file.isFile()&&file.exists()) {
                file.delete();
                ssidMap.remove(key);
                if (this.scanListener != null) {
                    this.scanListener.onDeleteFinished();
                }
            }
            /*Toast.makeText(context, "扫描点:" + key + "上传成功", Toast.LENGTH_SHORT)
                    .show();*/
        } else {
            Toast.makeText(context, "扫描点:" + key + "上传失败", Toast.LENGTH_LONG)
                    .show();
        }
    }

}
