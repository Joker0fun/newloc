package lx.newloc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import lx.newloc.SubActivity.BleGatherActivity;
import lx.newloc.SubActivity.LunchActivity;
import lx.newloc.SubActivity.MusicTest;
import lx.newloc.SubActivity.WifiGatherActivity;

public class MainActivity extends Activity {
    private Button BleStart;
    private Button BleGatherbtn;
    private Button WifiGatherbtn;
    private Button MusicTestbtn;

    private Handler MsgShow = new Handler(  ){
        @Override
        public void handleMessage(Message msg) {
            if (msg.getData().getInt( "loc" ) == -1) {
                Toast.makeText( MainActivity.this, "当前无法定位", Toast.LENGTH_SHORT ).show();
            }else {
                String temshow = String.format( "X:[%.2f],Y:[%.2f]"
                        ,msg.getData().getFloat( "locx" )
                        ,msg.getData().getFloat( "locy" ));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        BleStart = (Button)findViewById( R.id.Btn_start );
        BleGatherbtn  = (Button)findViewById( R.id.Btn_Gather );
        WifiGatherbtn = (Button)findViewById( R.id.Btn_Wifi_Gather );
        MusicTestbtn = (Button)findViewById( R.id.Btn_Mt );

        BleStart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent BTLoc = new Intent();
                BTLoc.setClass( MainActivity.this,LunchActivity.class );
                MainActivity.this.startActivity( BTLoc );
            }
        } );

        BleGatherbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent BtGather = new Intent();
                BtGather.setClass( MainActivity.this,BleGatherActivity.class );
                MainActivity.this.startActivity( BtGather );
            }
        } );

        WifiGatherbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent WifiGather = new Intent();
                WifiGather.setClass( MainActivity.this,WifiGatherActivity.class );
                MainActivity.this.startActivity( WifiGather );
            }
        } );

        MusicTestbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent MTi = new Intent();
                MTi.setClass( MainActivity.this,MusicTest.class );
                MainActivity.this.startActivity( MTi );
            }
        } );

        //StartWifiLocW();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //CancelWifiLocW();
        System.exit(0);
    }
}
