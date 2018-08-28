package lx.newloc.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import lx.newloc.ftp.FtpUtils;

/**
 * Created by Administrator on 2018/5/15.
 */

public class fileUploadtask extends AsyncTask<Void, Void, Boolean> {

    private String filename;
    private Context context;
    private File file;

    public fileUploadtask(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String fileName = this.filename;
        file = new File(fileName);

        InputStream inputStream;
        boolean uploadResult=false;
        try {
            inputStream = new FileInputStream(file);
            uploadResult = FtpUtils.uploadFile("221.178.187.38", 21,"chigooWifi", "chigooWifi","/LocMis/", file.getName(),inputStream);
            Thread.sleep( 2000 );
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace(  );
        }catch (Exception e){
            e.printStackTrace();
        }
        return uploadResult;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        String fname = filename.substring( filename.lastIndexOf( "/")+1,filename.lastIndexOf( ".") );
        if (result) {
            if (file.isFile()&&file.exists()) {
                //file.delete();

                Toast.makeText(context, "文件"+fname+"上传成功", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(context, "文件"+fname+"上传失败", Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
