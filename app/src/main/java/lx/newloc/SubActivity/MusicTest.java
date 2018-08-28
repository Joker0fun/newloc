package lx.newloc.SubActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import lx.newloc.R;
import lx.newloc.service.MusicService;

public class MusicTest extends Activity {
    private static int commands = 0;
    private Button btn_ss;
    private ServiceConnection con;
    private MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_music_test );
        btn_ss = (Button)findViewById( R.id.btn_mt_ss );
        btn_ss.setText( "Play" );
        con = new Mycon();//build service of connection

        btn_ss.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (commands){
                    case 0:
                        btn_ss.setText( "Pause" );
                        if (musicService == null){
                            Intent intent = new Intent( MusicTest.this
                                    ,MusicService.class );
                            bindService( intent,con, Context.BIND_AUTO_CREATE );
                        }else {
                            musicService.playMusic();
                        }
                        break;
                    case 1:
                        btn_ss.setText( "Stop" );
                        if (musicService != null){
                            musicService.pauseMusic();
                        }
                        break;
                    case 2:
                        btn_ss.setText( "Play" );
                        if (musicService != null){
                            musicService.stopMusic();
                        }
                        commands = 0;
                        break;
                }
                commands += 1;
            }
        } );

    }


    public class Mycon implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder myBinder = (MusicService.MyBinder)service;
            musicService = myBinder.getMusicService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            con = null;
        }


    }
}
