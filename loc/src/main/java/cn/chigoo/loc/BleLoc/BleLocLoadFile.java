package cn.chigoo.loc.BleLoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Administrator on 2018/6/12.
 */

public class BleLocLoadFile extends BleLocType{
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
        for (int i = 0; i < minlen ; i++){
            Dchar[i] = (char)Dbyte[i];
        }
        return 0;
    }

    public static float byte2float(byte[] b, int index) {
        int L;
        L = b[index + 0];
        L &= 0xff;
        L |= ((long) b[index + 1] << 8);
        L &= 0xffff;
        L |= ((long) b[index + 2] << 16);
        L &= 0xffffff;
        L |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(L);
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

    //Search NavHotMsgList
    public static TNavHotMsg SearchNav(Integer LocId,List<TNavHotMsg> nlist){
        if ((LocId == -1)||(nlist == null)||(nlist.size() == 0)){
            return null;
        }

        for (TNavHotMsg nav:nlist){
            if (LocId == nav.getId()){
                return nav;
            }
        }
        return null;
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



}
