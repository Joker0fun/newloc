package lx.newloc.SubActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.chigoo.ChigooBleListener;
import android.chigoo.ChigooCommunicationService;
import android.chigoo.ChigooIRListener;
import android.chigoo.ChigooService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Geocoder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import cn.chigoo.loc.lib.Algrithms;
import cn.chigoo.loc.lib.LocLoad;
import cn.chigoo.loc.lib.MapType.*;
import lx.newloc.Thread.BleFingerLocThread;
import lx.newloc.Thread.BleFingerLocThread.BleLocListener;
import lx.newloc.R;
import lx.newloc.Thread.BleLocMainThread;
import lx.newloc.Thread.WifiFingerLocThread;
import lx.newloc.Thread.WifiFingerLocThread.WifiLocListner;
import cn.chigoo.loc.lib.LocType.*;
import lx.newloc.task.fileUploadtask;

import static cn.chigoo.loc.lib.BleTraingleLoc.getdistance;
import static cn.chigoo.loc.lib.LocLoad.*;
import static cn.chigoo.loc.lib.MapType.CoordChangType.*;

import com.chigoo.wifilocation.*;

/**
 * Created by Administrator on 2017/10/27.
 */

public class LunchActivity extends AppCompatActivity {
    private MainMsg MainMsgState = new MainMsg();
    private ARegion areaMsg;
    private MapMsg mapMsg;
    private ImageView m_PoolImage;
    private ListView showrlist;
    private Canvas MapCanvas;
    private Paint mappaint;
    private Bitmap LoadPic;
    private Bitmap basebitmap;
    private Button btn_setA;
    private Button btn_setn;
    private Button btn_Zoom_up;
    private Button btn_Zoom_down;
    private Button btn_Recovery;
    private Button btn_test;
    private Button btn_Start;
    private Button btn_mov_left;
    private Button btn_mov_right;
    private Button btn_mov_up;
    private Button btn_mov_down;
    private Button btn_set_count;
    private Spinner SLocType;
    private Spinner SLocArea;
    private Spinner SLocFloor;
    private Spinner SpnMov;
    private EditText edt_A;
    private EditText edt_n;
    private EditText edt_count;
    private TextView tv_ShowPrecision;
    private Integer btA = 56;
    private float btn = (float)1.6;
    private int MapHeight;
    private int MapWidth;
    private int LocTypeIndex = 0;   //0:Triangle,1:weight centroid
    private int MovTypeIndex = 0;
    private String root = "storage/";
    private String path = "maptest/";
    private String sdcard = "/storage/emulated/0/";
    private TPoint LocId;
    private TPoint LocPoint;
    private TPoint LocId2;//1
    private TPoint LocId3;//2
    private TPoint LocId4;//3
    private TPoint LocId5;//4
    private TPoint m_oCoord = new TPoint();     //转换后的节点中心坐标;
    private MapPoint m_zWhZoom = new MapPoint();    //节点坐标转换倍数;
    private MapPoint m_zWH = new MapPoint();        //放缩后的图片与窗口的宽高比
    //节点放大缩小微调变量;
    private TPoint m_oPoint = new TPoint();     //平移坐标;
    private TPoint m_pPoint = new TPoint();     //用于记录地图放大时的点击坐标;
    private TPoint m_mPoint = new TPoint();     //记录按下时光标所在位置;
    private TPoint m_dclkPoint = new TPoint();  //测试精度
    private double m_MinRatio = 100;            //对话框尺寸与节点坐标的最小比例值;
    private double m_zDelta = 1.0;                // 放倍缩数;
    private boolean StartLoc = false;           //开始/关闭定位
    private boolean StartGetInfo = false;       //开始接收蓝牙信息
    private boolean MoveType = false;           //判断地图平移
    private boolean DateChange = false;
    private short OriAngle = 0;                 //原始方向
    private short compenAngle = 0;              //补充角度

    //20180625
    private ChigooWifiLocation chigooWifiLocation;

    BluetoothAdapter mBluetoothAdapter = null;


    ChigooWifiLocation.LocationInfo locationInfo = new ChigooWifiLocation.LocationInfo() {
        @Override
        public void GetLocationInfo(String outJson) {
            Bundle data = new Bundle();
            if (outJson != null) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject( outJson );
                    if (jsonObject != null) {
                        LocPoint = getJsonInfo( jsonObject );
                        int navid = Integer.valueOf( jsonObject.getString( "id" ) );
                        int navid2 = Integer.valueOf( jsonObject.getString( "id1" ) );
                        int navid3 = Integer.valueOf( jsonObject.getString( "id2" ) );
                        int navid4 = Integer.valueOf( jsonObject.getString( "id3" ) );
                        int navid5 = Integer.valueOf( jsonObject.getString( "id4" ) );
                        TNavHotMsg getnav = Algrithms.SearchNav( navid, areaMsg.navlist );
                        LocId = getLocIdFromNavHotByJson( jsonObject, "id" );
                        LocId2 = getLocIdFromNavHotByJson( jsonObject, "id1" );
                        LocId3 = getLocIdFromNavHotByJson( jsonObject, "id2" );
                        LocId4 = getLocIdFromNavHotByJson( jsonObject, "id3" );
                        LocId5 = getLocIdFromNavHotByJson( jsonObject, "id4" );

                        data.putInt( "loc", navid );
                        data.putFloat( "locx", LocId.getX() );
                        data.putFloat( "locy", LocId.getY() );
                        data.putFloat( "locz", LocId.getZ() );
                        Log.e( "weight loc:", LocPoint.toString() + "|NavId:"
                                + jsonObject.getString( "id" ) );
                    }

                    lochandler.post( new Runnable() {
                        @Override
                        public void run() {
                            if (LocId != null) {
                                OnLocDraw();
                            }
                        }
                    } );
                } catch (Exception e) {

                }
            } else {
                data.putInt( "loc", -1 );
            }
            Message message = new Message();
            message.setData( data );
            MsgShow.sendMessage( message );
        }
    };

    private TPoint getLocIdFromNavHotByJson(JSONObject jsonObject, String str) throws JSONException {

        int navid = Integer.valueOf( jsonObject.getString( str ) );
        TNavHotMsg getnav = Algrithms.SearchNav( navid, areaMsg.navlist );
        if (getnav == null) return null;
        TPoint getpoint = new TPoint();
        getpoint.setX( getnav.getPoint().getX() );
        getpoint.setY( getnav.getPoint().getY() );
        getpoint.setZ( getnav.getFloor() );

        return getpoint;
    }

    private TPoint getLocIdFromNavHot(TNavHotMsg getnav) {
        if (getnav == null) return null;
        TPoint getpoint = new TPoint();
        getpoint.setX( getnav.getPoint().getX() );
        getpoint.setY( getnav.getPoint().getY() );
        getpoint.setZ( getnav.getFloor() );

        return getpoint;
    }

    private TPoint getJsonInfo(JSONObject js) {
        if (js == null) return null;
        TPoint tem = new TPoint();
        try {
            tem.setX( (Float.valueOf( js.getString( "x" ) )) );
            tem.setY( (Float.valueOf( js.getString( "z" ) )) );
            tem.setZ( (Float.valueOf( js.getString( "y" ) )) );
            //tem.setZ( (Float.valueOf( js.getString( "floor" ) )) );
        } catch (Exception e) {

        }

        return tem;
    }


    //20180504 precision
    private int Precision_Count = 0;
    private int Now_pc = 0;
    List<TPoint> locPList = new ArrayList<TPoint>();
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


    /*LoccatTask loccatTast = null;*/
    BleLocMainThread bleLocMainThread = null;
    BleFingerLocThread bleFingerLocThread = null;
    WifiFingerLocThread wifiFingerLocThread = null;
    Handler lochandler = new Handler();
    Handler MsgShow = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.getData().getInt( "loc" ) == -1) {
                Toast.makeText( LunchActivity.this, "当前无法定位", Toast.LENGTH_SHORT ).show();
            } else {
                String temshow = String.format( "X:[%.2f],Y:[%.2f]"
                        , msg.getData().getFloat( "locx" )
                        , msg.getData().getFloat( "locy" ) );
                tv_ShowPrecision.setText( temshow );
            }

            String[] data = msg.getData().getStringArray( "showble" );
            if (data != null){//
                ArrayAdapter<String> adapter = new ArrayAdapter<String>( LunchActivity.this
                        , android.R.layout.simple_list_item_1, data );
                showrlist.setAdapter( adapter );
            }else {
                String[] temd = new String[1];
                temd[0] = "can not get data";
                ArrayAdapter<String> adapter = new ArrayAdapter<String>( LunchActivity.this
                        , android.R.layout.simple_list_item_1, temd);
                showrlist.setAdapter( adapter );
            }
        }
    };

    ChigooCommunicationService cms = null;
    ChigooBleListener bleListener = null;
    ChigooIRListener irListener = null;
    ChigooService chigooService = null;

    BleLocMainThread.BleLocMainListener bleLocMainListener = new BleLocMainThread.BleLocMainListener() {
        @Override
        public void OnbleLocating(final Object... values) {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (values[0] != null) {
                LocId = (TPoint)values[0];
                lochandler.post( new Runnable() {
                    @Override
                    public void run() {
                        if (LocId != null) {
                            OnLocDraw();
                        }
                    }
                } );
                data.putInt( "loc", 0 );
                data.putFloat( "locx", LocId.getX() );
                data.putFloat( "locy", LocId.getY() );
            } else {
                data.putInt( "loc", -1 );
            }
            if (((List<TBlueTooth>)values[2] != null)) {
                List<TBlueTooth> showlist = (List<TBlueTooth>)values[2];
                String[] strings = new String[showlist.size()];
                data.putStringArray( "show", strings );
                for (int i = 0; i < showlist.size(); i++) {
                    strings[i] = String.format( "%s|%d|%.2f|%.2f"
                            , String.valueOf( showlist.get( i ).getBleId() )
                            , showlist.get( i ).getRssi()
                            , showlist.get( i ).getDis()
                            , showlist.get( i ).getWeight() );
                }
                data.putStringArray( "showble", strings );
            }
            msg.setData( data );
            MsgShow.sendMessage( msg );
        }

        @Override
        public void onbleLocfinished() {
            StartLoc = false;
            bleLocMainThread.scanLeDevice(false);
            bleLocMainThread.setFlag( false );
            bleLocMainThread.interrupt();
        }

        @Override
        public List<TBlueTooth> GetBleScanInfo() {
            return null;
        }
    };

    BleLocListener bleLocListener = new BleLocListener() {
        @Override
        public void OnbleLocating(final Object... values) {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (values[0] != null) {
                LocId = (TPoint)values[0];
                lochandler.post( new Runnable() {
                    @Override
                    public void run() {
                        if (LocId != null) {
                            OnLocDraw();
                        }
                    }
                } );
                data.putInt( "loc", 0 );
                data.putFloat( "locx", LocId.getX() );
                data.putFloat( "locy", LocId.getY() );
            } else {
                data.putInt( "loc", -1 );
            }
            List<TBlueTooth> showlist = (List<TBlueTooth>)values[2];
            String[] strings = new String[showlist.size()];
            data.putStringArray( "show", strings );
            for (int i = 0; i < showlist.size(); i++) {
                strings[i] = String.format( "%s|%d|%.2f|%.2f"
                        , String.valueOf( showlist.get( i ).getBleId() )
                        , showlist.get( i ).getRssi()
                        , showlist.get( i ).getDis()
                        , showlist.get( i ).getWeight() );
            }
            data.putStringArray( "showble", strings );
            msg.setData( data );
            MsgShow.sendMessage( msg );
        }

        @Override
        public void onbleLocfinished() {
            StartLoc = false;
            /*loccatTast.cancel( true );
            loccatTast = null;*/
            bleFingerLocThread.setFlag( false );
            bleFingerLocThread.interrupt();
        }
    };

    WifiLocListner wifiLocListner = new WifiLocListner() {
        @Override
        public void OnwifiLocating(Object... values) {
            if ((int)values[0] != -1) {
                int navId = (int)values[0];
                if (navId != 0) {
                    TNavHotMsg getnav = Algrithms.SearchNav( navId, areaMsg.navlist );
                    LocId = getLocIdFromNavHot( getnav );
                }
                lochandler.post( new Runnable() {
                    @Override
                    public void run() {
                        if (LocId != null) {
                            OnLocDraw();
                        }
                    }
                } );
            } else {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt( "loc", -1 );
                msg.setData( data );
                MsgShow.sendMessage( msg );
            }
        }

        @Override
        public void onwifiLocfinished() {
            StartLoc = false;
            wifiFingerLocThread.setFlag( false );
            wifiFingerLocThread.interrupt();
        }

        @Override
        public void WifiScanning(final WifiManager wifiManager) {
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    wifiManager.startScan();
                }
            } );
        }
    };

    private void StartBleLocT(int Loctype) {
        if (bleFingerLocThread != null)
            return;
        long scanInterval = 1000;

        boolean LocChioce = true;
        if (Loctype == 1) LocChioce = false;
        /*bleFingerLocThread = new BleFingerLocThread();
        bleFingerLocThread.ThreadLoccatTask(LunchActivity.this,scanInterval,areaMsg.blist,areaMsg.navlist,areaMsg.pathlist,Loctype);
        bleFingerLocThread.setFlag( true );
        bleFingerLocThread.setBleLocListener( bleLocListener );
        bleFingerLocThread.start();*/
        bleLocMainThread = new BleLocMainThread();
        bleLocMainThread.setFlag( true );
        bleLocMainThread.ThreadLoccatTask( LunchActivity.this, scanInterval, areaMsg.blist, areaMsg.navlist, areaMsg.pathlist,mBluetoothAdapter );
        bleLocMainThread.BleLocMainThread( bleLocMainListener );
        bleLocMainThread.scanLeDevice(true);
        bleLocMainThread.start();

        StartLoc = true;
        btn_Start.setText( "停止" );
    }

    private void Blecancel() {
        if (bleFingerLocThread != null) {
            bleFingerLocThread.setFlag( false );
            bleFingerLocThread.interrupt();
            bleFingerLocThread = null;
            StartLoc = false;
            btn_Start.setText( "开始" );
        }

        if (bleLocMainThread != null) {
            bleLocMainThread.scanLeDevice( false );
            bleLocMainThread.setFlag( false );
            bleLocMainThread.interrupt();
            bleLocMainThread = null;
            StartLoc = false;
            btn_Start.setText( "开始" );
        }
    }

    private void StartWifiLocW() {
        if (chigooWifiLocation != null)
            return;
        chigooWifiLocation = new ChigooWifiLocation( this );
        chigooWifiLocation.setLocationInfo( locationInfo );
        chigooWifiLocation.init( null );
        chigooWifiLocation.startLocation( 1, chigooService );
        StartLoc = true;
        btn_Start.setText( "停止" );
    }

    private void CancelWifiLocW() {
        if (chigooWifiLocation != null) {
            chigooWifiLocation.stopLocation();
            chigooWifiLocation = null;
        }
        StartLoc = false;
        btn_Start.setText( "开始" );
    }

    private void StartWifiLocT() {
        if (wifiFingerLocThread != null)
            return;
        long scanInterval = 2000;

        wifiFingerLocThread = new WifiFingerLocThread();
        wifiFingerLocThread.ThreadLoccatTask( LunchActivity.this, scanInterval, sdcard, areaMsg.wifiMlist, areaMsg.wifiidlist, areaMsg.wifiRlist );
        wifiFingerLocThread.setFlag( true );
        wifiFingerLocThread.setwifiLocListener( wifiLocListner );
        wifiFingerLocThread.start();
        StartLoc = true;
        btn_Start.setText( "停止" );
    }

    private void Wificancel() {
        if (wifiFingerLocThread != null) {
            wifiFingerLocThread.setFlag( false );
            wifiFingerLocThread.interrupt();
            wifiFingerLocThread = null;
            StartLoc = false;
            btn_Start.setText( "开始" );
        }
    }

    private void BindData() {
        LocId = null;
        LocPoint = null;
        LocId2 = null;
        LocId3 = null;
        LocId4 = null;
        LocId5 = null;
        MainMsgState = new MainMsg();

        LocLoad.InitMainMsg( MainMsgState );
        LocLoad.LoadMainMsg( sdcard, MainMsgState );
        MainMsgState.setnArea( 0 );
        MainMsgState.setnFloor( 2 );
        String[] areanum = new String[MainMsgState.AreaList.size()],
                floornum = new String[MainMsgState.AreaList.get( MainMsgState.getnArea() ).FloorList.size()];
        for (int i = 0; i < MainMsgState.AreaList.size(); i++) {
            areanum[i] = MainMsgState.AreaList.get( i ).getAreaName();
        }
        ArrayAdapter<String> locAreadapter = new ArrayAdapter<String>( LunchActivity.this
                , android.R.layout.simple_list_item_1
                , areanum );
        SLocArea.setAdapter( locAreadapter );

        for (int i = 0; i < MainMsgState.AreaList.get( MainMsgState.getnArea() ).FloorList.size(); i++) {
            floornum[i] = MainMsgState.AreaList.get( MainMsgState.getnArea() ).FloorList.get( i ).mapInfo.getMapName();
        }
        ArrayAdapter<String> locFloordapter = new ArrayAdapter<String>( LunchActivity.this
                , android.R.layout.simple_list_item_1
                , floornum );
        SLocFloor.setAdapter( locFloordapter );

        m_PoolImage.post( new Runnable() {
            @Override
            public void run() {
                basebitmap = Bitmap.createBitmap( m_PoolImage.getWidth(), m_PoolImage.getHeight(), Bitmap.Config.ARGB_8888 );
                MapCanvas = new Canvas( basebitmap );
                mappaint = new Paint();
                InitFloor( 0, 2 );
            }
        } );
    }

    private void BindView() {
        m_PoolImage = (ImageView)findViewById( R.id.View_Map_Iamge );
        showrlist = (ListView)findViewById( R.id.Show_rlistview );
        SLocType = (Spinner)findViewById( R.id.spn_LocType );
        SLocArea = (Spinner)findViewById( R.id.spn_LocArea );
        SLocFloor = (Spinner)findViewById( R.id.spn_LocFloor );
        SpnMov = (Spinner)findViewById( R.id.spn_MoveType );
        btn_setA = (Button)findViewById( R.id.Btn_set_A );
        btn_setn = (Button)findViewById( R.id.Btn_set_n );
        btn_Zoom_up = (Button)findViewById( R.id.btn_Zoom_up );
        btn_Zoom_down = (Button)findViewById( R.id.btn_Zoom_down );
        btn_Recovery = (Button)findViewById( R.id.btn_Recovery );
        btn_test = (Button)findViewById( R.id.btn_test );
        btn_Start = (Button)findViewById( R.id.Btn_start );
        btn_mov_left = (Button)findViewById( R.id.btn_moveleft );
        btn_mov_right = (Button)findViewById( R.id.btn_moveright );
        btn_mov_up = (Button)findViewById( R.id.btn_moveup );
        btn_mov_down = (Button)findViewById( R.id.btn_movedown );
        btn_set_count = (Button)findViewById( R.id.Btn_set_C );
        edt_A = (EditText)findViewById( R.id.edt_input_A );
        edt_n = (EditText)findViewById( R.id.edt_input_n );
        edt_count = (EditText)findViewById( R.id.edt_input_C );
        tv_ShowPrecision = (TextView)findViewById( R.id.tv_ShowPrecision );
        edt_A.setText( String.valueOf( btA ) );
        edt_n.setText( String.valueOf( btn ) );
        Precision_Count = 20;
        edt_count.setText( String.valueOf( Precision_Count ) );

        ArrayAdapter<String> locTypedapter = new ArrayAdapter<String>( LunchActivity.this
                , android.R.layout.simple_list_item_1
                , getResources().getStringArray( R.array.loctype ) );
        SLocType.setAdapter( locTypedapter );

        ArrayAdapter<String> movTypedapter = new ArrayAdapter<String>( LunchActivity.this
                , android.R.layout.simple_list_item_1
                , getResources().getStringArray( R.array.movtype ) );
        SpnMov.setAdapter( movTypedapter );
    }

    private void SetListener() {
        cms = (ChigooCommunicationService)this.getSystemService( Context.CHIGOO_COMMUNICATIONSERVICE );

        bleListener = new ChigooBleListener() {
            @Override
            public void onBleUpdate(int rssi, int major, int minor) {
                String temstr;
                Integer index;
                Log.d( "BTTest |", "Ble update: rssi = " + rssi + " major = "
                        + major + " minor " + minor );

                if ((StartGetInfo) && (null != areaMsg.bletemlist)) {
                    Log.d( "BleFinger |", "BleFinger: rssi = " + rssi + " major = "
                            + major + " minor " + minor );
                    TBlueTooth btitor = new TBlueTooth();
                    temstr = String.format( "%05d%05d", major, minor );
                    btitor.setBleId( temstr.toCharArray() );
                    btitor.setRssi( (short)rssi );
                    btitor.setIsExist( false );
                    index = Algrithms.SearchBTIndex( btitor.getBleId(), areaMsg.bletemlist );
                    if (index != -1) {
                        areaMsg.bletemlist.get( index ).setRssi( btitor.getRssi() );
                        btitor = null;
                    } else {
                        areaMsg.bletemlist.add( btitor );
                    }
                }
            }
        };

        cms.setOnListener( bleListener );
    }

    private void SetClickListener() {

        btn_test.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = sdcard + "locpre.txt";
                new fileUploadtask( LunchActivity.this, filename ).execute();
            }
        } );

        m_PoolImage.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (StartLoc) return true;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        m_mPoint.setX( motionEvent.getX() );
                        m_mPoint.setY( motionEvent.getY() );
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float pmx = motionEvent.getX() - m_mPoint.getX(); //记录移动的变化量
                        float pmy = motionEvent.getY() - m_mPoint.getY();
                        switch (MovTypeIndex) {
                            case 0://Move Map
                                m_oPoint.setX( m_oPoint.getX() + pmx );
                                m_oPoint.setY( m_oPoint.getY() + pmy );
                                break;
                            case 1://Move Point
                                //MoveType = true;
                                DateChange = true;
                                mapMsg.m_dPoint.setX( mapMsg.m_dPoint.getX() + pmx );
                                mapMsg.m_dPoint.setY( mapMsg.m_dPoint.getY() + pmy );
                                break;
                            default:
                                break;
                        }
                        m_mPoint.setX( motionEvent.getX() );
                        m_mPoint.setY( motionEvent.getY() );
                        break;
                    case MotionEvent.ACTION_UP:
                        m_pPoint.setX( motionEvent.getX() );
                        m_pPoint.setY( motionEvent.getY() );
                        TPoint oCoord = CoordChang( mapMsg.m_dPoint, CoordR2M ); // 窗口坐标转地图坐标;
                        double xbuf = (motionEvent.getX() - oCoord.getX()) * 1.0 / (m_MinRatio + mapMsg.MapZoom) * m_zWH.getX();
                        double ybuf = (motionEvent.getY() - oCoord.getY()) * 1.0 / (m_MinRatio + mapMsg.MapZoom) * m_zWH.getY();
                        if (!StartLoc) {
                            m_dclkPoint.setX( (float)xbuf );
                            m_dclkPoint.setY( (float)ybuf );
                        }
                        m_mPoint.setX( 0 );
                        m_mPoint.setY( 0 );
                        if (MoveType) {
                            //mapMsg.m_dPoint = CoordChang(mapMsg.m_dPoint, CoordM2R);
                            MoveType = false;
                        }
                        break;
                    default:
                        break;
                }
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        OnDrawHot();
                    }
                };
                handler.post( runnable );
                return true;
            }
        } );

        btn_Start.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSoftInput( edt_A, false );
                showSoftInput( edt_n, false );
                Now_pc = 0;
                locPList.clear();
                if (!StartLoc) {
                    SLocType.setEnabled( false );
                    Handler handler = new Handler();
                    handler.post( new Runnable() {
                        @Override
                        public void run() {
                            if (LocTypeIndex <= 2) {
                                StartBleLocT( LocTypeIndex );
                            } else {

                                if (LocTypeIndex == 4) {
                                    StartWifiLocW();
                                } else {
                                    StartWifiLocT();
                                }
                            }
                        }
                    } );
                } else {
                    if (LocTypeIndex <= 2) {
                        Blecancel();
                    } else {
                        if (LocTypeIndex == 4) {
                            CancelWifiLocW();
                        } else {
                            Wificancel();
                        }
                    }
                    SLocType.setEnabled( true );
                }
            }
        } );

        btn_setA.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String na;
                Integer ia = btA;
                na = edt_A.getText().toString();
                try {
                    ia = Integer.valueOf( na );
                } catch (NumberFormatException e) {

                }

                if ((ia < -30) & (ia > -120)) {
                    btA = ia;
                    showSoftInput( edt_A, false );
                    showSoftInput( edt_n, false );
                    edt_A.clearFocus();
                    for (TBlueTooth bi : areaMsg.blist) {
                        bi.setA( btA.shortValue() );
                    }
                }
                edt_A.setText( String.valueOf( btA ) );
            }
        } );

        btn_setn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sn = edt_n.getText().toString();
                float in = btn;
                try {
                    btn = Float.parseFloat( sn );
                } catch (NumberFormatException e) {

                }

                if (in > 0) {
                    btn = in;
                    showSoftInput( edt_A, false );
                    showSoftInput( edt_n, false );
                    edt_n.clearFocus();
                    for (TBlueTooth bi : areaMsg.blist) {
                        bi.setRn( btn );
                    }
                }
                edt_n.setText( String.valueOf( btn ) );
            }
        } );

        btn_set_count.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sn = edt_count.getText().toString();
                int in = Precision_Count;
                try {
                    in = Integer.parseInt( sn );
                } catch (NumberFormatException e) {

                }
                if (in > 0) {
                    Precision_Count = in;
                    edt_count.clearFocus();
                    showSoftInput( edt_A, false );
                    showSoftInput( edt_count, false );
                }
                edt_count.setText( String.valueOf( Precision_Count ) );

            }
        } );

        btn_mov_left.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StartLoc) return;
                switch (MovTypeIndex) {
                    case 0:
                        m_oPoint.setX( m_oPoint.getX() - 2 );
                        break;
                    case 1:
                        DateChange = true;
                        mapMsg.m_dPoint.setX( mapMsg.m_dPoint.getX() - 2 );
                        break;
                    case 2:
                        if (!StartLoc)
                            m_dclkPoint.setX( (float)(m_dclkPoint.getX() - 0.5) );
                        break;
                    default:
                        break;
                }
                OnDrawHot();
            }
        } );

        btn_mov_right.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StartLoc) return;
                switch (MovTypeIndex) {
                    case 0:
                        m_oPoint.setX( m_oPoint.getX() + 2 );
                        break;
                    case 1:
                        DateChange = true;
                        mapMsg.m_dPoint.setX( mapMsg.m_dPoint.getX() + 2 );
                        break;
                    case 2:
                        if (!StartLoc)
                            m_dclkPoint.setX( (float)(m_dclkPoint.getX() + 0.5) );
                        break;
                    default:
                        break;
                }
                OnDrawHot();
            }
        } );

        btn_mov_up.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StartLoc) return;
                switch (MovTypeIndex) {
                    case 0:
                        m_oPoint.setY( m_oPoint.getY() - 2 );
                        break;
                    case 1:
                        DateChange = true;
                        mapMsg.m_dPoint.setY( mapMsg.m_dPoint.getY() - 2 );
                        break;
                    case 2:
                        if (!StartLoc)
                            m_dclkPoint.setY( (float)(m_dclkPoint.getY() - 0.5) );
                        break;
                    default:
                        break;
                }
                OnDrawHot();
            }
        } );

        btn_mov_down.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StartLoc) return;
                switch (MovTypeIndex) {
                    case 0:
                        m_oPoint.setY( m_oPoint.getY() + 2 );
                        break;
                    case 1:
                        DateChange = true;
                        mapMsg.m_dPoint.setY( mapMsg.m_dPoint.getY() + 2 );
                        break;
                    case 2:
                        if (!StartLoc)
                            m_dclkPoint.setY( (float)(m_dclkPoint.getY() + 0.5) );
                        break;
                    default:
                        break;
                }
                OnDrawHot();
            }
        } );
        SLocType.setOnItemSelectedListener( new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                LocTypeIndex = arg2;
            }

            public void onNothingSelected(AdapterView<?> agr0) {

            }
        } );

        SLocArea.setOnItemSelectedListener( new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                MainMsgState.setnArea( arg2 );
                InitFloor( MainMsgState.getnArea(), MainMsgState.getnFloor() );
            }

            public void onNothingSelected(AdapterView<?> agr0) {

            }
        } );

        SLocFloor.setOnItemSelectedListener( new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                MainMsgState.setnFloor( arg2 );
                InitFloor( MainMsgState.getnArea(), MainMsgState.getnFloor() );
            }

            public void onNothingSelected(AdapterView<?> agr0) {

            }

        } );

        SpnMov.setOnItemSelectedListener( new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                MovTypeIndex = arg2;
            }

            public void onNothingSelected(AdapterView<?> agr0) {

            }
        } );

        btn_Zoom_down.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StartLoc) return;
                switch (MovTypeIndex) {
                    case 0:
                        ZoomMapPro( false );
                        break;
                    case 1:
                        mapMsg.MapZoom -= 0.01 * m_MinRatio;
                        DateChange = true;
                        lochandler.post( new Runnable() {
                            @Override
                            public void run() {
                                OnDrawHot();
                            }
                        } );
                        break;
                    default:
                        break;
                }
            }
        } );

        btn_Zoom_up.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StartLoc) return;
                switch (MovTypeIndex) {
                    case 0:
                        ZoomMapPro( true );
                        break;
                    case 1:
                        mapMsg.MapZoom += 0.01 * m_MinRatio;
                        DateChange = true;
                        lochandler.post( new Runnable() {
                            @Override
                            public void run() {
                                OnDrawHot();
                            }
                        } );
                        break;
                    default:
                        break;
                }
            }
        } );

        btn_Recovery.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_zDelta = 1;
                m_oPoint.setX( 0 );
                m_oPoint.setY( 0 );
                OnDrawHot();
            }
        } );

    }

    private void ZoomMapPro(boolean Zoon_Up_Down) {

        int zDelta = 1;
        if (!Zoon_Up_Down) zDelta = -1;

        if (m_zDelta <= 0.1) {
            m_zDelta = m_zDelta - 0.002 * zDelta;
        } else {
            m_zDelta = m_zDelta - 0.01 * zDelta;
        }
        if (m_zDelta <= 0.050) {
            m_zDelta = 0.05;
        }
        if (m_zDelta >= 1.10) {
            m_zDelta = 1.10;
        }

        //记录放缩前的地图原点与节点原点相对位置
        TPoint dxy = CoordChang( mapMsg.m_dPoint, CoordR2M );
        MapPoint oPt = new MapPoint();
        MapPoint zoom = new MapPoint();
        zoom.setX( m_zWH.getX() / (m_MinRatio + mapMsg.MapZoom) );
        zoom.setY( m_zWH.getY() / (m_MinRatio + mapMsg.MapZoom) );
        oPt.setX( (m_pPoint.getX() - dxy.getX()) * zoom.getX() );
        oPt.setY( (m_pPoint.getY() - dxy.getY()) * zoom.getY() );

        OnDrawMap();
        dxy = CoordChang( mapMsg.m_dPoint, CoordR2M );
        MapPoint oPt1 = new MapPoint();
        zoom.setX( m_zWH.getX() / (m_MinRatio + mapMsg.MapZoom) );
        zoom.setY( m_zWH.getY() / (m_MinRatio + mapMsg.MapZoom) );
        oPt1.setX( oPt.getX() / zoom.getX() + dxy.getX() );//放缩后的变化距离
        oPt1.setY( oPt.getY() / zoom.getY() + dxy.getY() );//补齐

        m_oPoint.setX( (float)(m_oPoint.getX() + m_pPoint.getX() - oPt1.getX()) );
        m_oPoint.setY( (float)(m_oPoint.getY() + m_pPoint.getY() - oPt1.getY()) );

        OnDrawHot();
    }

    private void InitFloor(int nArea, int nfloor) {
        MainMsgState.setnArea( nArea );
        MainMsgState.setnFloor( nfloor );
        m_pPoint.setX( m_PoolImage.getWidth() / 2 );
        m_pPoint.setY( m_PoolImage.getHeight() / 2 );
        areaMsg = MainMsgState.AreaList.get( nArea );
        mapMsg = areaMsg.FloorList.get( nfloor );
        LoadPicture();
        mappaint.setColor( Color.BLACK );
        mappaint.setStrokeWidth( 1.0f );

        m_oPoint.setX( 0 );
        m_oPoint.setY( 0 );


        OnDrawHot();
    }

    private void OnLocDraw() {
        if (null != m_PoolImage) {
            MapHeight = m_PoolImage.getMeasuredHeight();
            MapWidth = m_PoolImage.getMeasuredWidth();
        }
        if (null == m_PoolImage || 0 == MapHeight || 0 == MapWidth) return;
        //locp
        if (LocId != null) {
            int nowfloor = (short)LocId.getZ();
            if (((nowfloor > 0)) && (nowfloor - 1 != MainMsgState.getnFloor())) {
                SLocFloor.setSelection( nowfloor - 1 );
                InitFloor( 0, nowfloor - 1 );
            }
        }
        OnDrawHot();
    }

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate( instance );
        setContentView( R.layout.activity_lunch );
        /*boolean Sdext = Environment.getExternalStorageState()
                .equals( Environment.MEDIA_MOUNTED );
        if (Sdext) {
            sdcard = getSdName(root);
        }*/

        if (getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE )) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager)getSystemService( Context.BLUETOOTH_SERVICE );
            mBluetoothAdapter = bluetoothManager.getAdapter();
            Intent enableBluetooth = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( enableBluetooth, 1 );
        }

        chigooService = (ChigooService)this.getSystemService( Context.CHIGOO_SERVICE );
        int perm = ActivityCompat.checkSelfPermission( this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE );
        if (perm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE );
        }
        sdcard = String.format( "%s/%s", sdcard, path );

        File file = new File( sdcard );
        if (!file.exists()) {
            sdcard = String.format( "/storage/emulated/0%s", path );
        }

        BindView();
        BindData();
        SetClickListener();
        /*SetListener();*/
        showSoftInput( edt_n, false );
        showSoftInput( edt_A, false );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DateChange)
            SaveFloorConfig( sdcard, MainMsgState.getnArea() + 1, areaMsg );
        cms = null;
        chigooService = null;
    }

    public static double GetMinRatio(TPoint mpoint, Rect mRect) {
        double x, y, ratio = 0.01;

        if ((Math.abs( mpoint.getX() ) < 1e-6)
                || (Math.abs( mpoint.getY() ) < 1e-6)) {
            return ratio;
        }

        x = Math.abs( mpoint.getX() ) * 1.0 / Math.abs( mRect.right - mRect.left ) * 0.5;
        y = Math.abs( mpoint.getY() ) * 1.0 / Math.abs( mRect.top - mRect.bottom ) * 0.5;

        if (x > ratio) ratio = x;
        if (y > ratio) ratio = y;

        return ratio;
    }

    private void LoadPicture() {
        if (mapMsg == null) return;
        String filepath = mapMsg.getpicfile();
        try {
            FileInputStream pic = new FileInputStream( filepath );
            if (pic != null) {
                LoadPic = BitmapFactory.decodeStream( pic );
                mapMsg.mapInfo.setMapHeight( LoadPic.getHeight() );
                mapMsg.mapInfo.setMapWidth( LoadPic.getWidth() );
            }
            pic.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void OnDrawMap() {
        MapPoint d = new MapPoint();        //裁剪图片的宽高
        MapPoint noPoint = new MapPoint();  //裁剪图片的起点
        double rHW = m_PoolImage.getHeight() * 1.0f / m_PoolImage.getWidth();
        double mHW = mapMsg.mapInfo.getMapHeight() * 1.0f / mapMsg.mapInfo.getMapWidth();
        double zoom;
        if (rHW > mHW) {//宽
            zoom = m_PoolImage.getWidth() * 1.0f / mapMsg.mapInfo.getMapWidth();
            noPoint.setX( (float)(m_PoolImage.getWidth() * (1 - 1.0f / m_zDelta) / 2) );
            noPoint.setY( (float)((m_PoolImage.getHeight() - mapMsg.mapInfo.getMapHeight() * zoom * 1.0f / m_zDelta) / 2) );
        } else {//高
            zoom = m_PoolImage.getHeight() * 1.0f / mapMsg.mapInfo.getMapHeight();
            noPoint.setX( (float)((m_PoolImage.getWidth() - mapMsg.mapInfo.getMapWidth() * zoom * 1.0f / m_zDelta) / 2) );
            noPoint.setY( (float)(m_PoolImage.getHeight() * (1 - 1.0f / m_zDelta) / 2) );
        }

        try {
            Matrix matrix = new Matrix();
            matrix.postScale( (float)(zoom / m_zDelta), (float)(zoom / m_zDelta) );
            m_zWH.setX( (float)(m_zDelta) );
            m_zWH.setY( (float)(m_zDelta) );
            matrix.postTranslate( (float)(noPoint.getX() + m_oPoint.getX()), (float)(noPoint.getY() + m_oPoint.getY()) );
            MapCanvas.drawColor( Color.rgb( 207, 207, 207 ) );
            MapCanvas.drawBitmap( LoadPic, matrix, null );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void OnDrawHot() {
        areaMsg = MainMsgState.AreaList.get( MainMsgState.getnArea() );
        mapMsg = areaMsg.FloorList.get( MainMsgState.getnFloor() );
        try {
            OnDrawMap(); // 画平面地图;
            m_oCoord = CoordChang( mapMsg.m_dPoint, CoordR2M );//窗口坐标转地图坐标;

            int x = (int)(m_oCoord.getX());
            int y = (int)(m_oCoord.getY());

            MapCanvas.translate( x, y );//设定节点坐标的相对原点;
            // 节点转换倍数;
            m_zWhZoom.setX( m_zWH.getX() / (m_MinRatio + mapMsg.MapZoom) );
            m_zWhZoom.setY( m_zWH.getY() / (m_MinRatio + mapMsg.MapZoom) );

            //画节点坐标
            for (int I = 0; I < areaMsg.navlist.size(); I++) {
                if (areaMsg.navlist.get( I ).getFloor() != MainMsgState.getnFloor() + 1) continue;
                _CallBackDrawHot( this.MapCanvas, areaMsg.navlist.get( I ), m_zWhZoom );
            }

            for (TBlueTooth bitor : areaMsg.blist) {
                if (bitor.getNfloor() != MainMsgState.getnFloor() + 1) continue;
                _CallBackDrawHot( this.MapCanvas, bitor, m_zWhZoom );
            }

            _CallBackDrawHot( this.MapCanvas, LocId, m_zWhZoom, Color.GREEN );

            if (LocTypeIndex == 4)
                _CallBackDrawHot( this.MapCanvas, LocPoint, m_zWhZoom, Color.BLUE );

            if (LocTypeIndex == 4)
                _CallBackDrawHot( this.MapCanvas, LocId2, m_zWhZoom, Color.BLACK );
            if (LocTypeIndex == 4)
                _CallBackDrawHot( this.MapCanvas, LocId3, m_zWhZoom, Color.BLACK );
            if (LocTypeIndex == 4)
                _CallBackDrawHot( this.MapCanvas, LocId4, m_zWhZoom, Color.BLACK );
            if (LocTypeIndex == 4)
                _CallBackDrawHot( this.MapCanvas, LocId5, m_zWhZoom, Color.BLACK );


            //画路径;
            /*for (int I = 0 ;I < areaMsg.pathlist.size();I ++) {
                TPathRgn dp = areaMsg.pathlist.get( I );
                if ((dp.getStartID().getFloor() != MainMsgState.getnFloor())
                    ||(dp.getEndID().getFloor() != MainMsgState.getnFloor()))
                    continue;
                _CallBackDrawPaths(this.MapCanvas,dp.getStartID().getPoint(),dp.getEndID().getPoint());
            }*/
            if (MovTypeIndex == 2) {
                MapCanvas.drawCircle( (float)(m_dclkPoint.getX() / m_zWhZoom.getX()), (float)(m_dclkPoint.getY() / m_zWhZoom.getY()), 5, mappaint );
                if (StartLoc && (LocId != null)) {
                    double dismis = getdistance( m_dclkPoint, LocId );
                    tv_ShowPrecision.setText( String.format( "当前定位误差：[%.2f]米", dismis ) );
                    if (Precision_Count > Now_pc) {
                        Now_pc++;
                        TPoint loc = new TPoint();
                        loc.setX( LocId.getX() );
                        loc.setY( LocId.getY() );
                        loc.setZ( LocId.getZ() );
                        locPList.add( loc );
                    }
                    if (Precision_Count == Now_pc) {
                        Now_pc++;
                        String fileName = LocLoad.uplocprefile( sdcard, locPList, m_dclkPoint );
                        new fileUploadtask( LunchActivity.this, fileName ).execute();
                        /*Message msg = new Message();
                        Bundle data = new Bundle(  );
                        data.putString( "filename",fileName );
                        msg.setData( data );
                        MsgShow.sendMessage( msg );*/
                    }
                }
            }
            mappaint.setColor( Color.RED );

            MapCanvas.drawCircle( 0, 0, 7, mappaint );
            MapCanvas.translate( -x, -y );
            m_PoolImage.setImageBitmap( basebitmap );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private TPoint CoordChang(TPoint point, CoordChangType fg) {
        Rect mrect = new Rect( 0, 0, m_PoolImage.getWidth(), m_PoolImage.getBottom() );
        TPoint rp = new TPoint();
        /*if ((MoveType)&&(MovTypeIndex == 0))
        {
            TPoint p = new TPoint();
            p = point;
            return p;
        }*/
        switch (fg) {
            case CoordR2M:
                rp.setX( (float)(0.5 * mrect.width()
                        + m_oPoint.getX() + point.getX() / m_zWH.getX()) );
                rp.setY( (float)(0.5 * mrect.height()
                        + m_oPoint.getY() + point.getY() / m_zWH.getY()) );
                break;
            case CoordM2R:
                rp.setX( (float)((-0.5 * mrect.width()
                        - m_oPoint.getX() + point.getX()) * m_zWH.getX()) );
                rp.setY( (float)((-0.5 * mrect.height()
                        - m_oPoint.getY() + point.getY()) * m_zWH.getY()) );
                break;
        }

        return rp;
    }

    private void _CallBackDrawHot(Canvas cv, TPoint bp, MapPoint zoom, int color) {
        if (bp == null) return;
        double xbuf = (bp.getX() * 1.0 / zoom.getX());
        double ybuf = (bp.getY() * 1.0 / zoom.getY());
        int x = (int)xbuf;
        int y = (int)ybuf;

        /*String txt = "定位点";*/
        mappaint.setStyle( Paint.Style.FILL );
        mappaint.setColor( color );
        cv.drawCircle( x, y, 8, mappaint );

        mappaint.setStyle( Paint.Style.STROKE );
        mappaint.setColor( color );
        float ra = (float)(1 / zoom.getX());
        cv.drawCircle( x, y, ra, mappaint );

        mappaint.setStyle( Paint.Style.FILL );
        /*mappaint.setColor( Color.rgb( 0,0,0 ) );
        cv.drawText( txt,x - 7,y,mappaint );*/
    }

    private void _CallBackDrawHot(Canvas cv, TBlueTooth bp, MapPoint zoom) {
        double xbuf = (bp.getPoint().getX() * 1.0 / zoom.getX());
        double ybuf = (bp.getPoint().getY() * 1.0 / zoom.getY());
        int x = (int)xbuf;
        int y = (int)ybuf;

        String txt = String.valueOf( bp.getBleId() );
        mappaint.setColor( Color.rgb( 128, 128, 128 ) );
        cv.drawCircle( x, y, 5, mappaint );

        mappaint.setColor( Color.BLUE );
        cv.drawText( txt.substring( txt.length() - 3 ), x, y, mappaint );
    }

    private void _CallBackDrawHot(Canvas cv, TNavHotMsg np, MapPoint zoom) {
        if (np == null) {
            Log.e( "drawID", "np is null" );
            return;
        }
        double xbuf = (np.getPoint().getX() * 1.0 / zoom.getX());
        double ybuf = (np.getPoint().getY() * 1.0 / zoom.getY());
        int x = (int)xbuf;
        int y = (int)ybuf;

        String txt = String.format( "%d", np.getId() );
        mappaint.setColor( Color.rgb( 255, 0, 0 ) );
        cv.drawCircle( x, y, 5, mappaint );

        mappaint.setColor( Color.rgb( 0, 0, 0 ) );
        cv.drawText( txt, x, y, mappaint );
    }

    private void _CallBackDrawPaths(Canvas cv, TPoint startp, TPoint endp) {
        Paint pen = new Paint();
        pen.setARGB( 150, 52, 158, 11 );
        pen.setStrokeWidth( 20 );
        cv.drawLine( (int)startp.getX()
                , (int)startp.getY()
                , (int)endp.getX()
                , (int)endp.getY()
                , pen );
    }


    private void showSoftInput(View focusView, boolean show) {
        InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE );
        if (show) {
            imm.showSoftInput( focusView, InputMethodManager.SHOW_FORCED );
        } else {
            imm.hideSoftInputFromWindow( focusView.getWindowToken(), 0 ); // 强制隐藏键盘.getWindowToken()
        }
    }

    private static String getSdName(String rf) {
        String sdcard_path = null;

        String sd_default = Environment.getExternalStorageSdDirectory().getAbsolutePath();
        //Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d( "text", sd_default );
        if (sd_default.endsWith( "/" )) {
            sd_default = sd_default.substring( 0, sd_default.length() - 1 );
        }
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec( "mount" );
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader( is );
            String line;
            BufferedReader br = new BufferedReader( isr );
            while ((line = br.readLine()) != null) {
                if (line.contains( "secure" ))
                    continue;
                if (line.contains( "asec" ))
                    continue;
                if (line.contains( "fat" ) && line.contains( "/mnt/" )) {
                    String columns[] = line.split( " " );
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals( columns[1].trim() )) {
                            continue;
                        }

                        sdcard_path = columns[1];
                    }
                } else if (line.contains( "fuse" ) && line.contains( "/mnt/" )) {
                    String columns[] = line.split( " " );
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals( columns[1].trim() )) {
                            continue;
                        }
                        String sdnamebuf[] = columns[1].split( "/" );
                        sdcard_path = sdnamebuf[sdnamebuf.length - 1];
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

}
