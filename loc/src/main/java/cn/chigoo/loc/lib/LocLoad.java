package cn.chigoo.loc.lib;


import org.omg.CORBA.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static cn.chigoo.loc.lib.Algrithms.SearchNav;
import static cn.chigoo.loc.lib.Algrithms.byte2float;
import static cn.chigoo.loc.lib.BleTraingleLoc.getdistance;
import static cn.chigoo.loc.lib.MapType.*;

/**
 * Created by Administrator on 2017/9/13.
 */

public class LocLoad extends LocType {

    public static void ClearArray(byte[] s){
        for (int i = 0;i < s.length; i++){
            s[i] = 0;
        }
    }

    public static void getvalidArray(byte[] s){
        boolean start = false;
        for (int i = 0;i < s.length; i++){
            if (s[i] == 0){
                start = true;
                continue;
            }
            if (start){
                s[i] = 0;
            }
        }
    }

    public static int BtI(byte[] Dbyte,int bindex, int bcount){
        if (bindex+bcount <= Dbyte.length){
            switch (bcount){
                case 1:{return Dbyte[bindex]&0xFF;}

                case 2:{return (Dbyte[bindex]&0xFF)|
                                (Dbyte[bindex+1]<<8)&0xFF00;}

                case 3:{return (Dbyte[bindex]&0xFF)|
                                (Dbyte[bindex+1]<<8)&0xFF00|
                                (Dbyte[bindex+2]<<16)&0xFF0000;}

                case 4:{return (Dbyte[bindex]&0xFF)|
                        (Dbyte[bindex+1]<<8)&0xFF00|
                        (Dbyte[bindex+2]<<16)&0xFF0000|
                        (Dbyte[bindex+3]<<24)&0xFF000000;}
                default:
                    return 0;
            }
        }
        return 0;
    }

    public static int BtC(byte[] Dbyte,char[] Dchar){
        int minlen = Dbyte.length < Dchar.length ? Dbyte.length:Dchar.length;
        byte bbuf =  Byte.MAX_VALUE;
        char cbuf = Character.MAX_VALUE;
        for (int i = 0; i < minlen ; i++){
            Dchar[i] = (char)Dbyte[i];
        }
        return 0;
    }

    public static String BtS(byte[] dbyte){
        byte[] hex = "0123456789ABCDEF".getBytes();
        byte[] buff = new byte[2 * dbyte.length];
        for (int i = 0; i < dbyte.length; i++) {
            buff[2 * i] = hex[(dbyte[i] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[dbyte[i] & 0x0f];
        }
        return new String(buff);
    }

    public static int SearchId(int Id,List<TLocIdMR> list){
        if (list != null){
            for (int i=0;i < list.size();i++){
                if (Id == list.get( i ).getId()){
                    return i;
                }
            }
        }
        return -1;
    }

    public static int ReadWifiFinger(String fileName, List<TLocIdMR> list,List<TId> idList) {
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print("can not found this file: \n");
            System.out.print(fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] idlen= new byte[2];
            byte[] mac= new byte[2];
            byte R;
            int Id,len,Clen,Wlen;
            reader = new FileInputStream(fileName);
            reader.read(idlen,0,2);
            len =  BtI( idlen,0,2 );
            for (int i = 0; i < len ; i++) {
                reader.read( idlen, 0, 2 );
                Id = BtI( idlen, 0, 2 );
                TLocIdMR WifiId = new TLocIdMR();
                WifiId.setId( Id );
                TId Iditor = new TId();
                Iditor.setId( (short) Id );
                idList.add( Iditor );
                reader.read( idlen, 0, 2 );
                if (LocLog)System.out.print( "Id:" +Id+"\n");
                Clen = BtI( idlen, 0, 2 );
                WifiId.IList = new ArrayList<TLocCMR>();
                for (int j = 0;j < Clen;j++) {
                    if (LocLog) System.out.print( "clen:"+j+"\n" );
                    TLocCMR Cwifi = new TLocCMR();
                    Cwifi.CList = new ArrayList<TLocWMR>(  );
                    reader.read( idlen, 0, 2 );
                    Wlen = BtI( idlen, 0, 2 );
                    for (int k = 0;k < Wlen;k ++){
                        //System.out.print( "Wlen:\n" );
                        //System.out.print( Wlen );
                        TLocWMR mr = new TLocWMR();
                        reader.read(mac,0,2);
                        mr.setMacIndex( (short) BtI( mac,0,2 ));
                        mr.R = (char)(reader.read()&0xFF);
                        reader.read();
                        if (LocLog)System.out.print( "MAC:"+mr.getMacIndex() +"|R:"+(Integer.valueOf(  mr.R )-256)+"\n"  );
                        Cwifi.CList.add( mr );
                    }
                    WifiId.IList.add( Cwifi );
                }
                if (0 == SearchId( WifiId.Id,list )){
                    WifiId = null;
                }else {
                    list.add( WifiId );
                }
            }
            System.out.print( "\n" );

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int ReadWifiMACList(String fileName, List<TLocM> list) {
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] vbuf = new byte[16];
            byte[] lenbuf = new byte[2];
            byte[] MAC =new byte[20];
            byte[] Unuse = new byte[256];
            char[] vc = new char[10];
            char[] cbuf = new char[20];
            int len = 0;
            reader = new FileInputStream( fileName ) ;
            reader.read( vbuf,0,16 );
            BtC( vbuf,vc );
            String s = String.valueOf(  vc);
            if ((!LVersion.equals( s ))){
                System.out.print( "read file error\n" );
                System.out.print(LVersion + "\n");
                System.out.print( (LVersion.equals( s ) ));
                System.out.print(LVersion.length());
                System.out.print(s.length());
                reader.close();
                return -1;
            }
            reader.read(lenbuf,0,2);
            len = BtI( lenbuf,0,2);
            for (int i=0;i < len ;i++){
                reader.read( MAC,0,20);
                TLocM m = new TLocM();
                BtC( MAC, cbuf);
                m.setMAC(cbuf);
                reader.read( Unuse,0,256);
                list.add( m );
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int ReadRealWifi(String fileName,List<TLocMR> list){
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            char[] MAC =new char[20];
            char[] Rss =new char[4];
            char R ;
            int Mlen = 0,MoR = 0; //
            reader = new FileInputStream( fileName ) ;
            long len = file.length();
            //Format("|MAC|R| " 1 ,2)
            TLocMR m = null;
            for (int i=0;i < len ;i++){
                R = (char)reader.read();
                if (R == '|'){
                    Mlen = 0;
                    MoR = MoR +1;
                    if ((MoR == 2)&(Mlen == 0)){
                        m = new TLocMR();
                        m.setMAC( MAC );
                        if (LocLog)System.out.print("MAC:" + String.valueOf(  m.getM() ) + ";" );
                        continue;
                    }
                    if ((MoR == 3)&(Mlen == 0)){
                        String rs = String.valueOf(  Rss );
                        int Rs =  Integer.parseInt( rs.trim() );
                        m.setRss( (short) Rs );
                        if (LocLog)System.out.print("Rss:"+ m.getR() +"\n");
                        list.add( m );
                        MoR = 0;
                        continue;
                    }
                    continue;
                }
                if (MoR == 1) {
                    MAC[Mlen] = R;
                }
                if (MoR == 2) {
                    Rss[Mlen] = R;
                }
                if (MoR!= 0)
                Mlen ++;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static int ReadBTDatafromBin(String pathdir,int narea,int nfloor,List<TBlueTooth> list){
        String fileName = String.format( "%sCH/A%02d/F%02d/BLELOCATION.BIN",pathdir,narea,nfloor );
        File file = new File( fileName );
        if ((! file.exists()) ||  (list == null)){
            System.out.print("can not found this file: \n");
            System.out.print(fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] idlen= new byte[2];
            byte[] mac= new byte[20];
            byte[] xyz= new byte[4];
            byte[] BleID= new  byte[12];
            byte Area = 0;
            int len;
            reader = new FileInputStream(fileName);
            reader.read(idlen,0,2);
            len =  BtI( idlen,0,2 );
            for (int i = 0; i < len ; i++) {
                TBlueTooth btitor = new TBlueTooth();
                TPoint btpoint = new TPoint();
                reader.read(BleID,0,12);
                BtC( BleID, btitor.getBleId());
                reader.read(mac,0,2);//nArea 字段对齐
                reader.read(mac,0,20);
                reader.read(xyz,0,4);
                btpoint.setX( byte2float(xyz,0) );
                reader.read(xyz,0,4);
                btpoint.setZ( byte2float(xyz,0) );
                reader.read(xyz,0,4);
                btpoint.setY( byte2float(xyz,0) );

                btitor.setRn( (float)3.0 );
                btitor.setA( (short)-60 );
                btitor.setNfloor( (short)nfloor );
                btitor.setPoint( btpoint );
                list.add(btitor);
            }
            System.out.print( "\n" );

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int ReadPathDatafromBin(String pathdir,int narea,int nfloor,List<TPathRgn> plist,List<TNavHotMsg> nlist){
        String fileName = String.format( "%sCH/A%02d/F%02d/VPATHSEARCH.BIN",pathdir,narea,nfloor );
        File file = new File( fileName );
        if ((!file.exists()) || (plist == null) ||(nlist == null)){//
            System.out.print("can not found this file: \n");
            System.out.print(fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] tem= new byte[256];
            int len = 0,SId,EId;
            reader = new FileInputStream(fileName);
            reader.read(tem,0,2);
            len =  BtI( tem,0,2 );
            reader.read(tem,0,2);
            for (int i = 0; i < len ; i++) {
                reader.read(tem,0,12);
                SId = BtI( tem,4,4 );
                EId = BtI( tem,8,4 );
                TPathRgn addp = new TPathRgn();
                TNavHotMsg startID = SearchNav(SId,nlist);
                addp.setStartID( startID );
                TNavHotMsg EndId = SearchNav(EId,nlist);
                addp.setEndID( EndId );
                plist.add( addp );
            }
            System.out.print( "\n" );

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static int ReadNavDatafromBin(String pathdir,int narea,int nfloor,List<TNavHotMsg> list){
        String fileName = String.format( "%sCH/A%02d/F%02d/IDSET.BIN",pathdir,narea,nfloor );
        File file = new File( fileName );
        if ((!file.exists()) || (list == null)){//
            System.out.print("can not found this file: \n");
            System.out.print(fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] tem= new byte[256];
            int len = 0;

            reader = new FileInputStream(fileName);
            reader.read(tem,0,2);
            len =  BtI( tem,0,2 );
            for (int i = 0; i < len ; i++) {
                TNavHotMsg navitor = new TNavHotMsg();
                navitor.setnFloor( nfloor );
                TPoint point = new TPoint();
                reader.read(tem,0,4);
                navitor.Id = BtI( tem,0,4 );
                reader.read(tem,0,4);
                point.x = byte2float(tem,0);
                reader.read(tem,0,4);
                point.z = byte2float(tem,0);
                reader.read(tem,0,4);
                point.y = byte2float(tem,0);
                reader.read(tem,0,13);
                reader.read(tem,0,27);
                reader.read(tem,0,64);
                reader.read(tem,0,64);
                reader.read(tem,0,3);
                reader.read(tem,0,125);
                navitor.setPoint( point );
                list.add(navitor);
            }
            System.out.print( "\n" );

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return 0;
    }

    /**
     * @describe
     * @author Administrator
     * @time 2016-5-31
     * @param Path 搜索目录
     * @param Extension 搜索目录
     */
    public static void searchFile(String Path, String Extension,HashMap<String, String> ssidMap) // 搜索目录，扩展名
    {
        if (!new File(Path).exists())
            return;
        File[] files = new File(Path).listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile()) {

                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) {
                    String piont = f.getPath().split("_")[0].replace(Path, "");
                    if (ssidMap.get( piont ) == null) {
                        ssidMap.put( piont, f.getPath() );
                    }
                }
            }
        }
    }

    // write ble file
    public static String write2File(String scanid,String fileDir, Map<String,String> ssidmap, List<TRealBleFinger> result) {
        String filepath     = ssidmap.get( scanid );
        File file;
        if (filepath != null) {
            file = new File( filepath );
            if (!file.exists()) {
                String dataString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                String fileName = scanid + "_" + dataString + ".txt";
                //case error create file after make folder
                Algrithms.makeFilePath(fileDir, fileName);
                filepath = fileDir + fileName;
            }
        }else{
            String dataString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            String fileName = scanid + "_" + dataString + ".txt";
            //case error create file after make folder
            Algrithms.makeFilePath(fileDir, fileName);
            filepath = fileDir + fileName;
        }

        // change line with write
        try {
            file = new File(filepath );
            if (!file.exists()) {
                /*Log.d("TestFile", "Create the file:" + strFilePath);*/
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Collections.sort(result,new Algrithms.CompareByFingerRssi());

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            for (LocType.TRealBleFinger BleGatherInfo : result)
            {
                String content = BleGatherInfo.toString()+"\r\n";
                raf.write(content.getBytes());
            }
            String content = "\r\n";
            raf.write(content.getBytes());
            raf.close();
        } catch (Exception e) {
            /*Log.e("WriteFile", "Error on write File:" );*/
            e.printStackTrace();
            return null;
        }
        return filepath;
    }

    public static int ReadBleFinger(String fileName, List<TIdBleFinger> list,short idbuf) {
        fileName = String.format( "%s/%d.BIN",fileName,idbuf );
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print("can not found this file: \n");
            System.out.print(fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] tem = new byte[255];
            int Clen,Wlen;
            reader = new FileInputStream(fileName);

            TIdBleFinger IdBle = new TIdBleFinger();
            IdBle.setId( idbuf );
            reader.read( tem, 0, 2 );
            if (LocLog)System.out.print( "Id:" +idbuf+"\n");
            Clen = BtI( tem, 0, 2 );
            IdBle.Cblelist = new ArrayList<TCBleFinger>();
            for (int j = 0;j < Clen;j++) {
                if (LocLog) System.out.print( "clen:"+j+"\n" );
                TCBleFinger CBle = new TCBleFinger();
                CBle.blelist = new ArrayList<TBleFinger>(  );
                reader.read( tem, 0, 2 );
                Wlen = BtI( tem, 0, 2 );
                for (int k = 0;k < Wlen;k ++){
                    TBleFinger mr = new TBleFinger();
                    reader.read(tem,0,2);
                    mr.setIndex( (short) BtI( tem,0,2 ));
                    mr.Rssi = (short)(reader.read()&0xFF);
                    reader.read();//Align
                    reader.read(tem,0,4);
                    mr.setVarrss( byte2float(tem,0) );
                    reader.read(tem,0,4);
                    mr.setWeight( byte2float(tem,0) );
                    if (LocLog)System.out.print( "MAC:"+ mr.getIndex() +"|R:"+Integer.valueOf(  mr.Rssi )+
                            "|Varrss:" +mr.getVarrss() +"|Weight:" + mr.getWeight()+"\n"  );
                    CBle.blelist.add( mr );
                }
                IdBle.Cblelist.add(CBle);
            }
            list.add( IdBle );
            System.out.print( "\n" );
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static String uplocprefile(String fileDir,List<TPoint> loclist ,TPoint rightP){
        String filepath  = fileDir + "locpre.txt";
        File file;
        Algrithms.makeFilePath(fileDir, "locpre.txt");

        // change line with write
        try {
            file = new File(filepath );
            if (file.exists()){
                file.delete();
            }
            file.getParentFile().mkdirs();
            file.createNewFile();

            if (!file.exists() || !file.canWrite()){
                return null;
            }

            OutputStreamWriter raf = new OutputStreamWriter(new FileOutputStream(file) ,"gbk");


            double ave = 0,mis = 0,dir = 0;
            String content = "";
            content = String.format("TestPoint:%s\n", rightP.toString());
            ave += mis;
            raf.write(content);
            if (loclist != null)
            {
                for (LocType.TPoint loc : loclist)
                {
                    mis = getdistance(loc,rightP);
                    content = String.format( "Location Point:%s,Error:[%.2f]m\n",loc.toString(),mis );
                    ave += mis;
                    raf.write(content);
                }
                content = String.format( "\n" );
                raf.write(content);
                if (loclist.size() != 0)
                    ave = ave*1.0/loclist.size();
                content = String.format( "Mean Error:[%.2f]m\n" ,ave);
                raf.write(content);
                for (LocType.TPoint loc : loclist)
                {
                    mis = getdistance(loc,rightP);
                    dir += Math.pow( mis-ave,2 ) ;
                }
                dir = Math.sqrt( dir );
                if (loclist.size() != 0)
                    dir = dir*1.0/loclist.size();
                content = String.format( "Variance fluctuation:[%.2f]m\n" ,dir);
                raf.write(content);
            }
            //raf.flush();
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return filepath;
    }

    //upwifilocfile
    public static String upwifilocfile(String fileDir, List<TLocMR> result) {
        String filepath  = fileDir + "test.txt";
        File file;
        Algrithms.makeFilePath(fileDir, "test.txt");

        // change line with write
        try {
            file = new File(filepath );
            file.createNewFile();

            if (!file.exists()){
                return null;
            }

            if (!file.canWrite()){
                /*file.setWritable( true );*/
                if (!file.canWrite())
                    return null;
            }
            /*FileOutputStream fos = new FileOutputStream( file );
            OutputStreamWriter osw = new OutputStreamWriter( fos );*/

            Collections.sort(result,new Algrithms.CompareByRssi());

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            if (result != null)
            {
                for (LocType.TLocMR wif : result)
                {
                    String content = wif.toString()+"\r\n";
                    //osw.write( content );
                    raf.write(content.getBytes());
                }
            }else {
                String content = "=";
                //osw.write( content );
                raf.write(content.getBytes());
            }
            String content = "\r\n";
            //osw.write( content );
            raf.write(content.getBytes());
            raf.close();
            //osw.flush();
            //osw.close();
            //fos.close();
        } catch (Exception e) {
            /*Log.e("WriteFile", "Error on write File:" );*/
            e.printStackTrace();
            return null;
        }
        return filepath;
    }

    // write wifi file
    public static String writeWifi2File(String scanid,String fileDir, Map<String,String> ssidmap, List<TWifiInfo> result) {
        String filepath     = ssidmap.get( scanid );
        File file;
        if (filepath != null) {
            file = new File( filepath );
            if (!file.exists()) {
                String dataString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String fileName = scanid + "_" + dataString + ".txt";
                //case error create file after make folder
                Algrithms.makeFilePath(fileDir, fileName);
                filepath = fileDir + fileName;
            }
        }else{
            String dataString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = scanid + "_" + dataString + ".txt";
            //case error create file after make folder
            Algrithms.makeFilePath(fileDir, fileName);
            filepath = fileDir + fileName;
        }

        // change line with write
        try {
            file = new File(filepath );
            if (!file.exists()) {
                /*Log.d("TestFile", "Create the file:" + strFilePath);*/
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            //Collections.sort(result,new Algrithms.CompareByRssi());

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            if (result != null)
            {
                for (LocType.TWifiInfo wif : result)
                {
                    String content = wif.toString()+"\r\n";
                    raf.write(content.getBytes());
                }
            }else {
                String content = "=";
                raf.write(content.getBytes());
            }
            String content = "\r\n";
            raf.write(content.getBytes());
            raf.close();
        } catch (Exception e) {
            /*Log.e("WriteFile", "Error on write File:" );*/
            e.printStackTrace();
            return null;
        }
        return filepath;
    }

    // write wifiRlist file
    public static String writeWifiR2File(String scanid,String fileDir, List<TLocMR> result) {
        String filepath = fileDir + "SubData/";
        String fileName = scanid + "R.txt";
        File file;
        file = new File( filepath + fileName );
        if (!file.exists()) {
            //case error create file after make folder
            Algrithms.makeFilePath(filepath, fileName);
        }
        filepath = filepath + fileName;

        // change line with write
        try {
            file = new File(filepath );
            if (!file.exists()) {
                /*Log.d("TestFile", "Create the file:" + strFilePath);*/
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Collections.sort(result,new Algrithms.CompareByRssi());

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());

            for (LocType.TLocMR wif : result)
            {
                String content = String.valueOf( wif.getM() ) + "\r\n";
                raf.write( content.getBytes() );
                for (Integer rs:wif.Rssilist) {
                    content = rs + "\r\n";
                    raf.write( content.getBytes() );
                }
                content = "\r\n";
                raf.write( content.getBytes() );
            }
            String content = "\r\n";
            raf.write(content.getBytes());
            raf.close();
        } catch (Exception e) {
            /*Log.e("WriteFile", "Error on write File:" );*/
            e.printStackTrace();
            return null;
        }
        return filepath;
    }

    // write subwifi file
    public static String writeSubWifi2File(String scanid,String fileDir, List<TLocMR> result) {
        String filepath = fileDir + "SubData/";
        String fileName = scanid + ".txt";
        File file;
        file = new File( filepath + fileName );
        if (!file.exists()) {
            //case error create file after make folder
            Algrithms.makeFilePath(filepath, fileName);
        }
        filepath = filepath + fileName;

        // change line with write
        try {
            file = new File(filepath );
            if (!file.exists()) {
                /*Log.d("TestFile", "Create the file:" + strFilePath);*/
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Collections.sort(result,new Algrithms.CompareByRssi());

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());

            for (LocType.TLocMR wif : result)
            {
                String content = wif.toString()+"\r\n";
                raf.write(content.getBytes());
            }
            String content = "\r\n";
            raf.write(content.getBytes());
            raf.close();
        } catch (Exception e) {
            /*Log.e("WriteFile", "Error on write File:" );*/
            e.printStackTrace();
            return null;
        }
        return filepath;
    }

    public static int ReadBleTableList(String fileName, List<TBleTable> list) {
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] tem = new byte[256];
            char[] vc = new char[10];
            int len = 0,sublen = 0;
            reader = new FileInputStream( fileName ) ;
            reader.read( tem,0,16 );
            BtC( tem,vc );
            String s = String.valueOf(vc);
            if ((!LVersion.equals( s ))){
                System.out.print( "read file error\n" );
                System.out.print(LVersion + "\n");
                System.out.print( (LVersion.equals( s ) ));
                System.out.print(LVersion.length());
                System.out.print(s.length());
                reader.close();
                return -1;
            }
            reader.read(tem,0,2);
            len = BtI( tem,0,2);
            for (int i=0;i < len ;i++){
                reader.read( tem,0,10);
                TBleTable bt = new TBleTable();
                BtC( tem, bt.Blemmid);
                bt.Idlist = new ArrayList<TId>(  );
                reader.read(tem,0,2);
                sublen = BtI( tem,0,2 );
                for (int j = 0;j < sublen;j++){
                    reader.read(tem,0,2);
                    TId id = new TId();
                    id.setId( (short)BtI( tem,0,2 ) );
                    bt.Idlist.add( id );
                }
                list.add( bt );
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int BleSearchId(int Id,List<TIdBleFinger> list){
        if (list != null){
            for (int i=0;i < list.size();i++){
                if (Id == list.get( i ).getId()){
                    list.get( i ).setMark( true );
                    return i;
                }
            }
        }
        return -1;
    }

    //20171226 wififinger
    public static int ReadNewWifiFinger(String filedir, List<TLocIdMR> list,short idbuf) {
        String fileName = String.format( "%s/WIFIFINGER/%d.BIN",filedir,idbuf );
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print("can not found this file: \n");
            System.out.print(fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] idlen= new byte[2];
            byte[] mac= new byte[2];
            byte[] Wt = new byte[4];
            int Clen,Wlen;
            reader = new FileInputStream(fileName);

            TLocIdMR WifiId = new TLocIdMR();
            WifiId.setMark( true );
            WifiId.setId( idbuf );

            reader.read( idlen, 0, 2 );
            Clen = BtI( idlen, 0, 2 );
            WifiId.IList = new ArrayList<TLocCMR>();
            for (int j = 0;j < Clen;j++) {
                if (LocLog) System.out.print( "clen:"+j+"\n" );
                TLocCMR Cwifi = new TLocCMR();
                Cwifi.CList = new ArrayList<TLocWMR>(  );
                reader.read( idlen, 0, 2 );
                Wlen = BtI( idlen, 0, 2 );
                for (int k = 0;k < Wlen;k ++){
                    TLocWMR mr = new TLocWMR();
                    reader.read(mac,0,2);
                    mr.setMacIndex( (short) BtI( mac,0,2 ));
                    reader.read(Wt,0,4);
                    mr.setWt( byte2float(Wt,0) );
                    mr.R = (char)(reader.read()&0xFF);
                    reader.read();
                    if (LocLog)System.out.print( "MAC:"+mr.getMacIndex() +"|R:"+(Integer.valueOf(  mr.R )-256)+"\n"  );
                    Cwifi.CList.add( mr );
                }
                WifiId.IList.add( Cwifi );
            }
            WifiId.setMark( true );
            list.add( WifiId );


            System.out.print( "\n" );

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int ReadNEWWifiMACList(String filedir, List<TLocM> list) {
        String fileName = String.format( "%sWIFIFINGER/NWIFIFINGER.BIN" ,filedir);
        File file = new File( fileName );
        if ((! file.exists())||(list == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( fileName );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] buf = new byte[256];
            char[] vc = new char[10];
            int len = 0,sublen = 0;
            reader = new FileInputStream( fileName ) ;
            reader.read( buf,0,16 );
            BtC( buf,vc );
            String s = String.valueOf(  vc);
            if ((!LVersion.equals( s ))){
                System.out.print( "read file error\n" );
                System.out.print(LVersion + "\n");
                System.out.print( (LVersion.equals( s ) ));
                System.out.print(LVersion.length());
                System.out.print(s.length());
                reader.close();
                return -1;
            }
            reader.read(buf,0,2);
            len = BtI( buf,0,2);
            for (int i=0;i < len ;i++){
                TLocM m = new TLocM();
                m.Idlist = new ArrayList<TId>(  );
                reader.read( buf,0,20);
                BtC( buf, m.MAC);
                reader.read( buf,0,1);
                m.setKind( (char)buf[0] );
                reader.read(buf,0,2);
                sublen = BtI( buf,0,2 );
                for (int j = 0 ; j < sublen;j++)
                {
                    reader.read( buf, 0, 2 );
                    TId iditor = new TId();
                    iditor.setId((short) BtI( buf,0,2 ) );
                    m.Idlist.add( iditor );
                }
                list.add( m );
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int LoadMap(String filedir,int nfloor,String getstream){
        getstream = String.format("%sCH/A01/F%02d/MAP/MAP.jpg",filedir,nfloor);
        return 0;
    }

    public static int LoadArea(String filedir,List<ARegion> aRegionList){
        String filename = String.format( "%s/CH/AREA.BIN",filedir );
        File file = new File( filename );
        if ((! file.exists())||(aRegionList == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( filename );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] buf = new byte[256];
            //char[] arename = new char[66];
            int len = 0;
            reader = new FileInputStream( filename ) ;
            reader.read(buf,0,2);
            len = BtI( buf,0,2);
            for (int i=0;i < len ;i++){
                ARegion area = new ARegion();
                ClearArray(buf);
                reader.read( buf,0,65);
                //BtC( buf,arename );
                getvalidArray(buf);
                area.setAreaName( new String( buf,"gbk" ) );
                area.FloorList = null;
                area.wifiidlist = null;
                area.wifiMlist = null;
                area.wifiRlist = null;
                aRegionList.add( area );
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    public static int LoadFloor(String pathdir,int nArea,List<MapMsg> mapMsgList){
        String filename = String.format( "%sCH/A%02d/FLOOR.bin",pathdir,nArea );
        File file = new File( filename );
        if ((! file.exists())||(mapMsgList == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( filename );
            return -1;
        }
        InputStream reader = null;
        try {
            byte[] buf = new byte[256];
            //char[] mapname = new char[64];
            int len = 0;
            reader = new FileInputStream( filename ) ;
            reader.read(buf,0,2);
            len = BtI( buf,0,2);
            for (int i=0;i < len ;i++){
                MapMsg mapMsg = new MapMsg();
                mapMsg.mapInfo = new MapInfo();
                ClearArray(buf);
                reader.read( buf,0,64);
                //BtC( buf, mapname);
                getvalidArray(buf);
                mapMsg.mapInfo.setMapName(  new String( buf ,"gbk") );
                reader.read( buf,0,28);
                mapMsg.mapInfo.setMapHeight(BtI( buf,0,4 ));
                mapMsg.mapInfo.setMapWidth(BtI( buf,4,4 ));
                mapMsg.m_dPoint  = null;
                mapMsg.RB        = null;

                mapMsgList.add( mapMsg );
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;
    }

    //[F1]; =;
    public static int SaveFloorConfig(String pathdir,int nArea,ARegion aRegion){
        String fileDir = String.format( "%sCH/A%02d/",pathdir,nArea );
        String filename = String.format( "%sCFG.INI",fileDir );

        if ((aRegion == null)||(aRegion.FloorList == null)) return -1;
        File file = new File( filename );
        Algrithms.makeFilePath(fileDir, "CFG.INI");
        try {
            if (file.exists()){
                file.delete();
            }
            file.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            int i = 0;
            String content = "";
            for (MapMsg mi : aRegion.FloorList)
            {
                content = String.format( "[F%d]\r\n",i+1 );
                raf.write(content.getBytes());
                content = String.format( "m_dPoint.cx=%.2f\r\n",mi.m_dPoint.getX() );
                raf.write(content.getBytes());
                content = String.format( "m_dPoint.cy=%.2f\r\n",mi.m_dPoint.getY() );
                raf.write(content.getBytes());
                content = String.format( "m_HotZoom=%.2f\r\n",mi.MapZoom );
                raf.write(content.getBytes());
                content = "\r\n";
                raf.write(content.getBytes());
                i++;
            }
            content = "\r\n";
            raf.write(content.getBytes());
            raf.close();
        }catch (Exception e){

        }



        return 0;

    }

    public static int LoadFloorConfig(String pathdir,int nArea,HashMap<String,HashMap<String,String>> FloorCFG){
        String filename = String.format( "%sCH/A%02d/CFG.INI",pathdir,nArea );

        File file = new File( filename );
        if ((! file.exists())||(FloorCFG == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( filename );
            return -1;
        }
        InputStream inputStream;
        InputStreamReader reader = null;
        BufferedReader getcontext;
        try {
            inputStream = new FileInputStream( filename );
            reader = new InputStreamReader( inputStream );
            getcontext = new BufferedReader( reader );
            String line;

            HashMap<String,String> keyvalue = null;
            String key,value;
            while ((line = getcontext.readLine()) != null){
                if (line.matches( "^\\[\\S+\\]$" )){
                    line.replace( "^\\[(\\S+)\\]$","$1" );
                    keyvalue = new HashMap<String, String>(  );
                    String head = line;
                    FloorCFG.put( head,keyvalue );
                }else if (line.matches( "^\\S+=.*$" )){
                    int i = line.indexOf( "=" );
                    key = line.substring( 0,i ).trim();
                    value = line.substring( i + 1 ).trim();
                    if (keyvalue != null)
                        keyvalue.put( key,value );
                }
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return 0;

    }

    public static void getOrignAndZoom(HashMap<String,HashMap<String,String>> CFGHash,int nfloor,MapMsg mapInfo){
        HashMap<String,String> getCfg = CFGHash.get( String.format( "[F%d]",nfloor ) );
        mapInfo.m_dPoint = new TPoint();
        mapInfo.m_dPoint.setX(Float.parseFloat(  getCfg.get( "m_dPoint.cx" ) )); ;
        mapInfo.m_dPoint.setY(Float.parseFloat(  getCfg.get( "m_dPoint.cy" ) ) );
        mapInfo.MapZoom     = Double.parseDouble( getCfg.get( "m_HotZoom" ) );

    }

    public static int LoadMainMsg(String pathdir,MainMsg mainMsg){
        File file = new File( pathdir );
        if ((! file.exists())||(mainMsg == null)){
            System.out.print( "can not found this file:\n");
            System.out.print( pathdir+"\n" );
            return -1;
        }
        
        mainMsg.AreaList = new ArrayList<ARegion>(  );
        mainMsg.nArea = 0;
        mainMsg.nFloor = 0;

        LoadArea(pathdir,mainMsg.AreaList);

        for (int i = 0;i < mainMsg.AreaList.size();i++){
            ARegion aregion = mainMsg.AreaList.get( i );
            aregion.FloorList = new ArrayList<MapMsg>(  );
            aregion.navlist   = new ArrayList<TNavHotMsg>(  );
            aregion.wifiMlist = new ArrayList<TLocM>(  );
            aregion.wifiidlist= new ArrayList<TLocIdMR>(  );
            aregion.wifiRlist = new ArrayList<TLocMR>(  );
            aregion.blist     = new ArrayList<TBlueTooth>(  );
            aregion.pathlist = new ArrayList<TPathRgn>(  );
            aregion.FloorListConfig = new HashMap<String,HashMap<String, String>>(  );

            ReadNEWWifiMACList(pathdir,aregion.wifiMlist);
            LoadFloorConfig(pathdir,i+1,aregion.FloorListConfig);
            LoadFloor(pathdir,i+1,aregion.FloorList);
            for (int j = 0;j < aregion.FloorList.size();j ++){
                String pic = String.format("%sCH/A%02d/F%02d/MAP/MAP.jpg",pathdir,i+1,j+1);;
                aregion.FloorList.get( j ).setpicfile( pic );
                ReadNavDatafromBin(pathdir,i+1,j+1,aregion.navlist);
                ReadPathDatafromBin(pathdir,i+1,j+1,aregion.pathlist,aregion.navlist);
                ReadBTDatafromBin(pathdir,i+1,j+1,aregion.blist);
                getOrignAndZoom(aregion.FloorListConfig,j+1,aregion.FloorList.get( j ));
            }
            Collections.sort( aregion.blist );
            Collections.sort( aregion.navlist );
        }
        return 0;
    }

    public static void InitMainMsg(MainMsg mainMsg){
        if ((null == mainMsg)||(mainMsg.AreaList == null)) return;
        for (int i = 0;i < mainMsg.AreaList.size();i ++){
            mainMsg.AreaList.get( i ).FloorList.clear();
            mainMsg.AreaList.get( i ).blist.clear();
            mainMsg.AreaList.get( i ).wifiRlist.clear();
            mainMsg.AreaList.get( i ).wifiidlist.clear();
            mainMsg.AreaList.get( i ).FloorListConfig.clear();
            mainMsg.AreaList.get( i ).bleTableList.clear();
            mainMsg.AreaList.get( i ).bletemlist.clear();
        }
        mainMsg.AreaList.clear();
    }

    public static short getFloorFromPoint(TPoint point,List<TNavHotMsg> nlist){
        short nfloor = -1;
        if (nlist == null) return nfloor;
        for (int i = 0;i < nlist.size();i++){
            if ((point.getZ() - 1 > nlist.get( i ).getPoint().getZ())
                    &&(point.getZ() +1 > nlist.get( i ).getPoint().getZ())){
                nfloor = (short)nlist.get( i ).getFloor();
                break;
            }
        }
        return nfloor;
    }

}
