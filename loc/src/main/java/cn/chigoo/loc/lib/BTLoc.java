package cn.chigoo.loc.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.chigoo.loc.lib.LocLoad.*;

/**
 * Created by Administrator on 2017/10/24.
 */

public class BTLoc extends LocType{

    //find MAC index
    public static int SearchMMIDIndex(char[] mmid, List<TBleTable> nlist){
        if (nlist != null){
            for (int i = 0 ;i < nlist.size(); i++){
                if (String.valueOf( mmid ).equals( String.valueOf( nlist.get( i ).getmmid() ))){
                    return i;
                }
            }
        }
        return -1;
    }

    public static int PreBleLoc(List<TRealBleFinger> rlist//get real blelist
                                ,List<TBleTable> tableList//mmid table
                                ,List<TIdBleFinger> slist//buff sample list
                                ,String rootpath)
    {
        //Compare with Rss
        Collections.sort( rlist, new Comparator<TRealBleFinger>() {
            @Override
            public int compare(TRealBleFinger tLocMR, TRealBleFinger t1) {
                return  (t1.getR() - tLocMR.getR())*(-1) ;
            }
        } );
        return StartBlefingerLoc(rlist,tableList,slist,rootpath);
    }

    public static int  StartBlefingerLoc(List<TRealBleFinger> rlist//get real blelist
                                          ,List<TBleTable> tableList//mmid table
                                          ,List<TIdBleFinger> slist//buff sample list
                                          ,String rootpath)

    {
        ResetBleSimpleList(slist);                //init blesample list

        MarkmmidList(rlist,tableList,slist,rootpath); //mark and read Blesample

        ReCompareBleSampleList(slist);                //range

        MatchBleFinger(rlist,slist);
        Map<String,Integer> map = LocBleQurary(rlist,slist);

        /**judge the Memory is Over Limit*/
        if(getTBFListsize(slist) > LimiteMem){
            System.out.print( "start to free\n" );
            FreeBleMarkList(tableList,slist);
        }

        System.out.print( "CurSize:"+getTBFListsize( slist )+"\n" );
        if ((map == null)||(map.get( "MinRId" ) == 0)){
            return -1;
        }else {
            return map.get( "MinRId" );//"MaxC"
        }
    }

    public static void ResetBleSimpleList (List<TIdBleFinger> slist){
        if (slist != null){
            System.out.print( "\n Reset mindexlist simplelist \n"  );
            for (int i=0;i<slist.size();i++){
                slist.get( i ).Clean();
            }
        }
    }

    public static void MarkmmidList(List<TRealBleFinger> rlist
                                    ,List<TBleTable> tableList
                                    ,List<TIdBleFinger> slist
                                    ,String rootpath)
    {
        if (null == rlist){  System.out.print( "rlist is null" );   return;    }
        if (null == tableList){  System.out.print( "mlist is null" );  return; }
        if (LocLog)System.out.print( "\n ReMark mindexlist \n"  );
        short indexbuf = 0;
        int SearL = 0;
        for (int i = 0;i < rlist.size() - 1;i++){
            indexbuf = (short) SearchMMIDIndex( rlist.get( i ).getmmid(),tableList );
            if ((SearL <= SearchLen)&&(indexbuf != -1)){
                SearL++;
                for (int j = 0;j < tableList.get( indexbuf ).Idlist.size();j++) {
                    short idbuf = tableList.get( indexbuf ).Idlist.get( j ).Id;
                    if (-1 == BleSearchId( idbuf,slist )) {
                        LocLoad.ReadBleFinger( rootpath, slist, idbuf );
                    }
                }
            }
            rlist.get( i ).setIndex( indexbuf );
        }
    }

    public static void ReCompareBleSampleList(List<TIdBleFinger> slist)
    {
        if (null != slist){
            if (LocLog)System.out.print( "\n ReCompare simplelist \n"  );
            Collections.sort( slist, new Comparator<TIdBleFinger>() {
                @Override
                public int compare(TIdBleFinger tLocIdMR, TIdBleFinger t1) {
                    if (tLocIdMR.getMark())
                        return -1;
                    else if ((!tLocIdMR.getMark())&(t1.getMark()))
                        return 1;
                    return 0;
                }
            } );

            for (int i = 0; i < slist.size();i++){
                System.out.print( "Id:"+ slist.get( i ).getId()+"|is mark:"
                        + slist.get( i ).getMark()+"\n");
            }
        }
    }

    public static short FindBF_indexList(short ind,List<TBleFinger> SearchList)
    {
        if ((null == SearchList)|(-1 == ind)) return -1;
        for (short i = 0;i < SearchList.size();i++){
            //System.out.print( "Mindex:"+ +Mindex + "|getMac:" +slist.get( i ).getMacIndex()+"\n");
            if (ind == SearchList.get( i ).getIndex()){
                return i;
            }
        }
        return -1;
    }

    public static Map<String,Integer> SubMatchFinger(List<TRealBleFinger> rlist,List<TBleFinger> bflist)
    {
        short Mindex;
        int   Rbuf;
        int Count = 0;
        int Rssi = 0;
        Map<String,Integer> map = new HashMap<String,Integer>();
        for (int n = 0;n < rlist.size();n++){
            Rbuf = 0;
            Mindex = 0;
            Mindex = FindBF_indexList(rlist.get( n ).getIndex(),bflist);
            if (LocLog)System.out.print( "GetrealIndex:"+rlist.get( n ).getIndex()+"Mindex:"+Mindex+"\n" );
            if (-1 != Mindex){
                Rbuf = (int)(Math.sqrt(Math.pow(rlist.get( n ).getR() - bflist.get( Mindex ).getR(),2)
                        *(Math.abs(rlist.get( n ).getWeight() - bflist.get( Mindex ).getWeight()) + 1)
                        *(Math.abs(rlist.get( n ).getAverage() - bflist.get( Mindex ).getVarrss()) + 1) ));
                if (Rbuf <= Rmax){
                    Count = Count+1;
                    Rssi = Rssi + (Rbuf*Rbuf);
                }else{
                    Rssi =Rssi + (Rmax*Rmax);
                }
            }
            else {
                Rssi =Rssi+ (Rmax*Rmax);
            }
        }
        map.put( "Count",Count );
        map.put( "Rssi",Rssi );
        return map;
    }

    public static void MatchBleFinger(List<TRealBleFinger> rlist//get real blelist
            ,List<TIdBleFinger> slist)//buff list
    {
        if ((rlist == null)|(slist == null)) return;
        if (LocLog)System.out.print( "start to Match:\n" );
        for (int i = 0;i < slist.size();i ++){
            if (false == slist.get( i ).getMark()){
                if (LocLog)System.out.print( "Match break from Id:"+slist.get( i ).getId() +"\n" );
                break;
            }
            for (int j = 0; j < slist.get( i ).Cblelist.size();j++){
                //rlist as base
                Map<String,Integer> map =SubMatchFinger(rlist,slist.get( i ).Cblelist.get( j ).blelist);
                if ( j == 0 ){
                    slist.get( i ).setMinR(map.get( "Rssi" ));
                    slist.get( i ).setMaxC(map.get( "Count" ));
                    slist.get( i ).setMinR_C( map.get( "Count" ) );
                    slist.get( i ).setMaxC_R( map.get( "Rssi" ) );
                }
                //R
                if (slist.get( i ).getMaxC() < map.get( "Count" )) {
                    slist.get( i ).setMaxC( map.get( "Count" ) );
                    slist.get( i ).setMaxC_R( map.get( "Rssi" ) );
                }

                //D(R)
                if (slist.get( i ).getMinR() > map.get( "Rssi" )){
                    slist.get( i ).setMinR( map.get( "Rssi" ) );
                    slist.get( i ).setMinR_C( map.get( "Count" ) );
                }
                if (LocLog)System.out.print("Id:"+ slist.get( i ).getId()+ "Match C:"+map.get( "Count" )+"Match R:"+map.get( "Rssi" )+"\n" );
                map.clear();
                map = null;
            }
            System.out.print( "Id:"+ slist.get( i ).getId()
                    + "|MaxC:"+slist.get( i ).getMaxC()
                    + "|MaxC_R:"+slist.get( i ).getMaxC_R()
                    + "|MinR:"+slist.get( i ).getMinR()
                    + "|MinR_C:"+slist.get( i ).getMinR_C()+"\n" );
        }
    }

    public static Map<String,Integer> LocBleQurary(List<TRealBleFinger> rlist//get real blelist
                                                    ,List<TIdBleFinger> slist)//buff list
    {
        Map<String,Integer> map = new HashMap<>(  );
        int MaxCId = 0,MinRId = 0;
        map.put( "MaxC",0 );
        map.put( "MaxC_R",rlist.size()*Rmax*Rmax );
        map.put( "MinR_C",0 );
        map.put( "MinR",rlist.size()*Rmax*Rmax );

        if (null == slist) return map;

        for (int i = 0;i < slist.size();i++){
            if (false == slist.get( i ).getMark()) {
                System.out.print( "break from Id:"+slist.get( i ).getId() +"\n" );
                break;
            }
            if (slist.get( i ).getMaxC() >= map.get( "MaxC" )){
                if (slist.get( i ).getMaxC() == map.get( "MaxC" )){
                    if (slist.get( i ).getMaxC_R() < map.get( "MaxC_R" )) {
                        map.put( "MaxC_R", slist.get( i ).getMaxC_R() );
                        MaxCId = slist.get( i ).getId();
                    }
                }else{
                    map.put( "MaxC",slist.get( i ).getMaxC());
                    map.put( "MaxC_R",slist.get( i ).getMaxC_R());
                    MaxCId = slist.get( i ).getId();
                }
            }

            if (slist.get( i ).getMinR() <= map.get( "MinR" )){
                if ((slist.get( i ).getMinR() == map.get( "MinR" ))&( slist.get( i ).getMinR_C() > map.get( "MinR_C" ))){
                    map.put( "MinR_C" ,slist.get( i ).getMinR_C());
                    MinRId = slist.get( i ).getId();
                }else {
                    map.put( "MinR",slist.get( i ).getMinR());
                    map.put( "MinR_C" ,slist.get( i ).getMinR_C());
                    MinRId = slist.get( i ).getId();
                }
            }
        }
        System.out.print("\n Id:"+MaxCId+ "|MaxC:"+map.get( "MaxC" )+"|MaxC_R:"+map.get( "MaxC_R" )+"\n" );
        System.out.print("\n Id:"+MinRId+ "|MinR:"+map.get( "MinR" )+"|MinR_C:"+map.get( "MinR_C" )+"\n" );

        map.put("MaxCId", MaxCId );
        map.put("MinRId", MinRId );
        return map;
    }

    public static Long getTBFListsize(List<TIdBleFinger> slist)//缓存list
    {
        long membuf = 0;
        if (slist != null){
            for (int i = 0 ;i < slist.size();i++){
                membuf += 20;
                if(slist.get( i ).Cblelist != null) {
                    for (int j = 0; j < slist.get( i ).Cblelist.size();j ++){
                        if (slist.get( i ).Cblelist.get( j ).blelist != null){
                            membuf += slist.get( i ).Cblelist.get( j ).blelist.size() * (12);
                        }
                    }
                }
            }
        }
        return membuf;
    }

    public static void FreeBleMarkList(List<TBleTable> tableList//record Idlist of mmid
                                       ,List<TIdBleFinger> slist)//buff list
    {
        if ((null == tableList)|(null == slist)) return;
        System.out.print( "freeing\n" );
        if (LocLog)System.out.print( "Mlen:"+tableList.size()+"\n");
        for (int i = tableList.size() -1 ;i >= 0 ;i--)
        {
            if(false == slist.get( i ).getMark())
            {
                if (LocLog)System.out.print( "MlenLen:"+tableList.get( i ).Idlist.size()+"\n");
                for (int j = 0;j < tableList.get( i ).Idlist.size();j++){
                    for (int k = slist.size() -1 ;k >= 0;k--  ){
                        if ( slist.get( k ).getId() == tableList.get( i ).Idlist.get( j ).getId() ){
                            System.out.print( "Delete ID: "+ slist.get( k ).getId()+"\n" );
                            slist.remove( k );
                        }
                    }
                }
                if (getTBFListsize(slist) < LimiteMem) break;
            }
        }
    }
}
