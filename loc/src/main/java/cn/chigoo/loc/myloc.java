package cn.chigoo.loc;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;


import javax.xml.crypto.Data;

import cn.chigoo.loc.lib.*;

public class myloc extends LocType{
    public static List<TLocM> Mlist = new ArrayList<TLocM>();           //MAC table
    public static List<TLocIdMR> idlist = new ArrayList<TLocIdMR>();    //load wififinger data
    public static List<TLocMR> Rlist = new ArrayList<TLocMR>();         //load Macindex
    public static List<TMAC> MACList = new ArrayList<TMAC>(  );         //buff MAC

    public static void main(String[] args){
        if (true){
            String fpath = String.valueOf("C:\\Users\\Administrator\\Desktop\\maptest\\WIFIFINGER\\NWIFIFINGER.BIN");
            LocLoad.ReadNEWWifiMACList(fpath,Mlist);
            newlocation();
        }else {
            String fpath = String.valueOf("C:\\Users\\Administrator\\Desktop\\maptest\\WIFIFINGER\\WIFIFINGER.BIN");
            LocLoad.ReadWifiMACList(fpath,Mlist);
            location();
        }
        Mlist.clear();
        Mlist = null;
        idlist.clear();
        idlist = null;
        Rlist.clear();
        Rlist = null;
    }

    public static void location (){
        try {
        while (true){
            char val = (char) System.in.read();
            if (val == 'e'){
                System.out.print( "end" );
                break;
            }
            if (val == 's'){
                System.out.print( "start: \n" );
                Rlist.clear();
                long Bet = System.currentTimeMillis();

                String fpath = "C:\\Users\\Administrator\\Desktop\\test.txt";
                LocLoad.ReadRealWifi(fpath,Rlist);
                //按R强度排序
                Collections.sort( Rlist, new Comparator<TLocMR>() {
                    @Override
                    public int compare(TLocMR tLocMR, TLocMR t1) {
                        return  t1.getR() - tLocMR.getR() ;
                    }
                } );
                System.out.print("RealList:\n");
                Algrithms.startLoc(MACList,Rlist,Mlist,idlist);
                long Aft = System.currentTimeMillis() - Bet;
                System.out.print( "\n runtime:"+  Aft + "ms" );
            }
        }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void newlocation(){
        try {
            while (true){
                char val = (char) System.in.read();
                if (val == 'e'){
                    System.out.print( "end" );
                    break;
                }
                if (val == 's'){
                    System.out.print( "start: \n" );
                    Rlist.clear();
                    long Bet = System.currentTimeMillis();

                    String fpath = "C:\\Users\\Administrator\\Desktop\\test.txt";
                    LocLoad.ReadRealWifi(fpath,Rlist);
                    //按R强度排序
                    Collections.sort( Rlist, new Comparator<TLocMR>() {
                        @Override
                        public int compare(TLocMR tLocMR, TLocMR t1) {
                            return  t1.getR() - tLocMR.getR() ;
                        }
                    } );
                    System.out.print("RealList:\n");
                    String readpath = "C:\\Users\\Administrator\\Desktop\\WIFIFINGER\\";
                    Algrithms.NEWstartLoc(Rlist,Mlist,idlist,readpath);
                    long Aft = System.currentTimeMillis() - Bet;
                    System.out.print( "\n runtime:"+  Aft + "ms" );
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
