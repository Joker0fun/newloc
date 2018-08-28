package lx.newloc.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lx.newloc.R;
import lx.newloc.task.WifiUploadTask;
import lx.newloc.task.WifiScanTask.*;

/**
 * Created by Administrator on 2018/1/8.
 */

public class WifiUpdateAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> pointlist;
    private HashMap<String,String> ssidMap;
    WifiScanListener wifiTestListener;

    public void setUplistn(WifiScanListener wifiTestListener){
        this.wifiTestListener = wifiTestListener;
    }


    public WifiUpdateAdapter(Context mContext, HashMap<String, String> ssidMap) {
        this.mContext = mContext;
        this.ssidMap = ssidMap;
        pointlist = new ArrayList<String>();
        Iterator<Map.Entry<String, String>> it = ssidMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            pointlist.add(entry.getKey());
        }
    }

    @Override
    public int getCount() {
        return this.pointlist.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(mContext)
                .inflate( R.layout.item__update, null);
        TextView text = (TextView) view.findViewById(R.id.tvUpate);
        Button btndelete = (Button) view.findViewById(R.id.btndelete);
        Button btnUpload = (Button) view.findViewById( R.id.btnupload );
        final String key = pointlist.get(position);
        text.setText("扫描点:" + key);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WifiUploadTask( mContext, ssidMap, key, wifiTestListener ).execute();
            }
        });

        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = ssidMap.get(key);
                File file=new File(fileName);
                if (file.isFile()){
                    file.delete();
                    ssidMap.remove(key);
                    if (wifiTestListener != null) {
                        wifiTestListener.onDeleteFinished();
                    }
                }
                Toast.makeText(mContext, "扫描点:" + key + "删除成功",
                        Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
}
