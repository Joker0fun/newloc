package lx.newloc.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Binder;

import lx.newloc.R;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private int pos = 0;//play progress

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = mediaPlayer.create( MusicService.this,
                    R.raw.yequ);
            mediaPlayer.setLooping( false );// 设置不需要单曲循环
        }

        mediaPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();// 释放资源
            }
        } );
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        playMusic();
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }

    // play
        public void playMusic() {
            if (mediaPlayer != null && !mediaPlayer.isLooping()) {
                try {
                    if (pos != 0) {
                        mediaPlayer.seekTo(pos);// play in pointed pos
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.stop();
                        mediaPlayer.prepare();// must prepare before reboot
                        mediaPlayer.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // pause
        public void pauseMusic() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pos = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
        }

        // stop
        public void stopMusic() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }

    }
