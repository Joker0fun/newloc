package cn.chigoo.loc.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static cn.chigoo.loc.lib.BleTraingleLoc.getDisRssi;
import static cn.chigoo.loc.lib.BleTraingleLoc.getdistance;
import static cn.chigoo.loc.lib.LocLoad.SearchId;


/**
 * Created by Administrator on 2017/9/19.
 */

public class Algrithms extends LocType{

    static final String LogCode = "WifiFinger:";

    public static int GetNearstNavId(TPoint locp,List <TNavHotMsg> nlist){
        int getId = -1;
        if ((null == nlist)||(0 == nlist.size())||(null == locp)){
            return getId;
        }
        double Mindis = -1 , dis;
        for (TNavHotMsg Naviter:nlist){
            if (Naviter.getFloor() == locp.getZ()) {
                dis = getdistance(locp,Naviter.getPoint());
                if (Mindis == -1){
                    getId = Naviter.getId();
                    Mindis = dis;
                    continue;
                }
                if (Mindis > dis){
                    getId = Naviter.getId();
                    Mindis = dis;
                }
            }
        }
        return getId;
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

    //find MAC index
    public static int SearchMIndex(char[] MAC, List<TLocM> nlist){
        if (nlist != null){
            for (int i = 0 ;i < nlist.size(); i++){
                int MinLen = MAC.length<nlist.get( i ).getM().length?MAC.length:nlist.get( i ).getM().length;
                boolean IsEqual = true;
                for (int j = 0;j < MinLen;j ++){
                    if (MAC[j] != nlist.get( i ).getM()[j]){
                        IsEqual = false;
                        break;
                    }
                }
                if (IsEqual)
                    return i;
                /*if (String.valueOf( MAC ).equals( String.valueOf( nlist.get( i ).getM() ))){
                    return i;
                }*/
            }
        }
        return -1;
    }

    //search rmmid index
    public static int SearchBFIndex(char[] bId, List<TRealBleFinger> nlist){
        if (nlist != null){
            for (int i = 0 ;i < nlist.size(); i++){
                if (String.valueOf( bId ).equals( String.valueOf( nlist.get( i ).getmmid() ))){
                    return i;
                }
            }
        }
        return -1;
    }

    //search mac index
    public static int SearchBTIndex(char[] bId, List<TBlueTooth> nlist){
        if (nlist != null){
            for (int i = 0 ;i < nlist.size(); i++){
                if (String.valueOf( bId ).equals( String.valueOf( nlist.get( i ).getBleId() ))){
                    return i;
                }
            }
        }
        return -1;
    }

    //create folder
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdir();
            }
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void ReCompareMarkMacList(List<TMAC> mlist){
        if (null == mlist) return;
        if (LocLog)System.out.print( "\n ReCompare mindexlist \n"  );
        Collections.sort( mlist, new Comparator<TMAC>() {
            @Override
            public int compare(TMAC tmac, TMAC t1) {
                if (tmac.getMark()) {
                    return -1;
                }
                if ( (!tmac.getMark()) & (t1.getMark()) ){
                    return 1;
                }
                return 0;
            }
        } );
        for (int i = 0;i < mlist.size();i ++){
            if (LocLog)System.out.print( "Mindex:"+ mlist.get( i ).getMACIndex()+"|is mark:"
             + mlist.get( i ).getMark()+"\n");
        }
    }

    public static void ResetMarkMacList(List<TMAC> mlist){
        if (null == mlist) return;
        for (int i=0;i < mlist.size();i++){
            mlist.get( i ).setMark( false );
        }
    }

    public static int SearchMarkMac(short Mindex, List<TMAC> mlist){
        if (null != mlist){
            for (int i = 0;i < mlist.size();i++){
                if (Mindex == mlist.get( i ).getMACIndex())
                    return i;
            }
        }
        return -1;
    }

    public static void FreeMarkList(List<TMAC> mlist,List<TLocIdMR> slist){
        if ((null == mlist)|(null == slist)) return;
        System.out.print( "freeing\n" );
        if (LocLog)System.out.print( "Mlen:"+mlist.size()+"\n");
        for (int i = mlist.size() -1 ;i >= 0 ;i--){
            {//if(false = mlist.get( i ).getMark())
                if (LocLog)System.out.print( "MlenLen:"+mlist.get( i ).Idlist.size()+"\n");
                for (int j = 0;j < mlist.get( i ).Idlist.size();j++){
                    for (int k = slist.size() -1 ;k >= 0;k--  ){
                        if ( slist.get( k ).getId() == mlist.get( i ).Idlist.get( j ).getId() ){
                            System.out.print( "Delete ID: "+ slist.get( k ).getId()+"\n" );
                            slist.remove( k );
                        }
                    }
                }
                mlist.remove( i );
                if (getListsize(slist) < LimiteMem) break;
            }
        }
    }

    public static void ReCompareSimpleList(List<TLocIdMR> slist){
        if (null != slist){
            if (LocLog)System.out.print( "\n ReCompare simplelist \n"  );
            Collections.sort( slist, new Comparator<TLocIdMR>() {
                @Override
                public int compare(TLocIdMR tLocIdMR, TLocIdMR t1) {
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

    public static void ResetSimpleList(List<TLocIdMR> slist){
        if (slist != null){
            System.out.print( "\n Reset mindexlist simplelist \n"  );
            for (int i=0;i<slist.size();i++){
                slist.get( i ).Clean();
            }
        }
    }

    public static void MarkMacList(List<TMAC> milist,List<TLocMR> rlist,
                                   List<TLocM> mlist,List<TLocIdMR> slist){
        if (null == mlist) {
            System.out.print( "milist is null" );
            return;
        }
        if (null == rlist){
            System.out.print( "rlist is null" );
            return;
        }
        if (null == mlist){
            System.out.print( "mlist is null" );
            return;
        }
        if (LocLog)System.out.print( "\n ReMark mindexlist \n"  );
        short indexbuf = 0;
        int SearL = 0;
        int Markindex = 0;
        String fpath;
        for (int i = 0;i < rlist.size() - 1;i++){
            indexbuf = (short) Algrithms.SearchMIndex( rlist.get( i ).getM(),mlist );
            if ((indexbuf != -1)&(SearL < SearchLen)){
                Markindex = SearchMarkMac(indexbuf,milist);
                if (-1 != Markindex){
                    milist.get( Markindex ).setMark(true);
                }else {
                    TMAC markMac = new TMAC();
                    markMac.setMark( true );
                    markMac.setMACIndex( indexbuf );
                    markMac.Idlist = new ArrayList<TId>(  );
                    fpath = String.format( "C:\\Users\\Administrator\\Desktop\\WIFIFINGER\\%05d.BIN", indexbuf+1 );
                    LocLoad.ReadWifiFinger( fpath, slist,markMac.Idlist);
                    milist.add( markMac );
                }
                SearL++;
            }
            rlist.get( i ).setIndex( indexbuf );
        }

    }

    public static void MarkSimpleList(List<TLocIdMR> slist,List<TMAC> mlist){
        if ((slist == null)|(mlist == null)) return;
        if (LocLog)System.out.print( "\n ReMark simplelist \n"  );
        boolean Jump;
        for (int i =0;i < slist.size();i++){
            for (int j = 0;j < mlist.size();j++){
                Jump = false;
                if ((false == mlist.get( j ).getMark())
                        |(null ==  mlist.get( j ).Idlist)) continue;
                    for (int k = 0;k < mlist.get( j ).Idlist.size();k++) {
                    if (slist.get( i ).getId() == mlist.get( j ).Idlist.get( k ).getId()){
                        slist.get( i ).setMark( true );
                        Jump = true;
                        break;
                    }
                }
                if (true == Jump) break;
            }
        }
    }

    public static short FindindexList(short Mindex,List<TLocWMR> slist){
        if ((null == slist)|(-1 == Mindex)) return -1;
        for (short i = 0;i < slist.size();i++){
            //System.out.print( "Mindex:"+ +Mindex + "|getMac:" +slist.get( i ).getMacIndex()+"\n");
            if (Mindex == slist.get( i ).getMacIndex()){
                return i;
            }
        }
        return -1;
    }

    public static void SubMatchFinger(List<TLocMR> rlist,TLocCMR sublist){
        short Mindex;
        int   Rbuf;
        int Count = 0;
        double VarR = 0.0;
        for (int n = 0;n < rlist.size();n++){
            Mindex = FindindexList(rlist.get( n ).getI(),sublist.CList);
            if (LocLog)System.out.print( "GetrealIndex:"+ rlist.get( n ).getI()+"Mindex:"+Mindex+"\n" );
            if (-1 != Mindex){
                Rbuf = Math.abs(rlist.get( n ).getR() - sublist.CList.get( Mindex ).GetRss());
                Count = Count+1;
                if (Rbuf <= Rmax){
                    VarR = VarR + (Rbuf*Rbuf);//*(1+1-sublist.CList.get( Mindex ).getWt());
                }else{
                    VarR =VarR + (Rmax * Rmax);
                }
            }
            else {
                VarR = VarR + (Rmax * Rmax);
            }
        }
        sublist.Values.setMC( Count );
        sublist.Values.setMR( VarR );
    }

    public static void WifiWKNN(TLocIdMR Idfinger,Integer k){
        if ((Idfinger.IList != null) && (Idfinger.IList.size() != 0)){
            Collections.sort( Idfinger.IList );
            Idfinger.Values.Clean();
            for (int i = 0; i < Idfinger.IList.size();i++){
                if ( i == k) break;
                Idfinger.Values.setMR( Idfinger.IList.get( i ).Values.getMR() );
                Idfinger.Values.setMC( Idfinger.IList.get( i ).Values.getMC() );
            }
            if (k != 0) {
                Idfinger.Values.setMR( Idfinger.Values.getMR()*1.0 / k );
                Idfinger.Values.setMC( Integer.valueOf(Idfinger.Values.getMC() / k ) );
            }
        }
    }

    public static void MatchFinger(List<TLocMR> rlist,List<TLocIdMR> slist){
        if ((rlist == null)|(slist == null)) return;
        if (LocLog)System.out.print( "start to Match:\n" );
        for (int i = 0;i < slist.size();i ++){
            if (false == slist.get( i ).getMark()){
                if (LocLog)System.out.print( "Match break from Id:"+slist.get( i ).getId() +"\n" );
                break;
            }
            for (int j = 0; j < slist.get( i ).IList.size();j++){
                SubMatchFinger(rlist,slist.get( i ).IList.get( j ));
            }

            /*for (int j = 0; j < slist.get( i ).IList.size();j++){
                SubMatchFinger(rlist,slist.get( i ).IList.get( j ));
            }*/

            WifiWKNN(slist.get( i ),1);
            System.out.print( "Id:"+ slist.get( i ).getId()
                    +"|MC:"+slist.get( i ).Values.getMC()
                    + "|MR:"+slist.get( i ).Values.getMR()+
                    "\n" );
        }
    }

    public static Integer LocQurary(List<TLocIdMR> slist,List<TLocMR> rlist){

        int MinRId = -1,MaxCId = -1;
        MemCalValue calValueR = new MemCalValue();
        calValueR.setMC( 0 );
        calValueR.setMR( rlist.size()*Rmax*Rmax*2 );

        MemCalValue calValueC = new MemCalValue();
        calValueC.setMC( 0 );
        calValueC.setMR( rlist.size()*Rmax*Rmax*2 );

        if (null == slist) return -1;

        for (int i = 0;i < slist.size();i++){
            if (false == slist.get( i ).getMark()) {
                System.out.print( "break from Id:"+slist.get( i ).getId() +"\n" );
                break;
            }
            if (slist.get( i ).Values.getMC() >= calValueC.getMC()){
                if (slist.get( i ).Values.getMC() == calValueC.getMC()){
                    if (slist.get( i ).Values.getMR() < calValueC.getMR()) {
                        calValueC.setMR(slist.get( i ).Values.getMR() );
                        MaxCId = slist.get( i ).getId();
                    }
                }else{
                    calValueC.setMC(slist.get( i ).Values.getMC());
                    calValueC.setMR(slist.get( i ).Values.getMR());
                    MaxCId = slist.get( i ).getId();
                }
            }

            if (slist.get( i ).Values.getMR() <= calValueR.getMR()){
                if ((slist.get( i ).Values.getMR() == calValueR.getMR())
                        &&( slist.get( i ).Values.getMC() > calValueR.getMC())){
                    calValueR.setMC(slist.get( i ).Values.getMC());
                    MinRId = slist.get( i ).getId();
                }else {
                    calValueR.setMR(slist.get( i ).Values.getMR());
                    calValueR.setMC(slist.get( i ).Values.getMC());
                    MinRId = slist.get( i ).getId();
                }
            }
        }
        System.out.print("\n Id:"+MaxCId+ "|MaxC:"+ calValueC.getMC()+"|MaxC_R:"+calValueC.getMR()+"\n" );
        System.out.print("\n Id:"+MinRId+ "|MinR:"+ calValueR.getMR()+"|MinR_C:"+calValueR.getMC()+"\n" );

        calValueC = null;
        calValueR = null;
        return MaxCId;
    }

    public static long getListsize(List<TLocIdMR> nlist){
        long membuf = 0;
        if (nlist != null){
            for (int i = 0 ;i < nlist.size();i++){
                membuf += 20;
                if(nlist.get( i ).IList != null) {
                    for (int j = 0; j < nlist.get( i ).IList.size();j ++){
                        if (nlist.get( i ).IList.get( j ).CList != null){
                            membuf += nlist.get( i ).IList.get( j ).CList.size() * (8);
                        }
                    }
                }
            }
        }
        return membuf;
    }

    public static void startLoc(List<TMAC> milist,List<TLocMR> rlist,
                                List<TLocM> mlist,List<TLocIdMR> slist){
        ResetMarkMacList(milist);
        ResetSimpleList(slist);                //init

        MarkMacList(milist,rlist,mlist,slist); //mark and read

        ReCompareMarkMacList(milist);           //range

        MarkSimpleList(slist,milist);           //mark

        ReCompareSimpleList(slist);             //range

        MatchFinger(rlist,slist);
        LocQurary(slist,rlist);

        /**判读是否超过限制内存*/
        if(getListsize(slist) > LimiteMem){
            System.out.print( "start to free\n" );
            FreeMarkList(milist,slist);
        }

        System.out.print( "CurSize:"+getListsize( slist )+"\n" );
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


    public static List<TRealBleFinger> calcAverage(List<List<TRealBleFinger>> nlist){
        if ((null == nlist)|(0 == nlist.size())) return null;

        int I,J,K,MinV=0;
        float AveR,Var,MinVar,SumVar;
        List<TRealBleFinger> AveList = new ArrayList<TRealBleFinger>(  );

        //mmid
        for (I = 0;I < nlist.size(); I++) {
            for (J = 0;J < nlist.get( I ).size();J++){
                if (SearchBFIndex(nlist.get( I ).get( J ).getmmid(),AveList) == -1){
                    TRealBleFinger bitor = new TRealBleFinger();
                    bitor.Rssilist = new ArrayList<Integer>();
                    bitor.setmmid(  nlist.get( I ).get( J ).getmmid());
                    AveList.add( bitor );
                }
            }
        }

        //rssi
        for (List<TRealBleFinger> bilist: nlist)
        {
            Integer ind = -1;
            for (TRealBleFinger bitor: bilist)
            {
                ind = SearchBFIndex(bitor.getmmid(),AveList);
                if (ind != -1 ){
                    if ((AveList.get( ind ).Rssilist != null) && (bitor.Rssilist != null)){
                        for (Integer rss :bitor.Rssilist){
                            AveList.get( ind ).Rssilist.add(rss);
                        }
                    }
                }
            }
        }

        //Average Rssi
        for (TRealBleFinger bitor: AveList){
            AveR = 0;
            for (I = 0;I < bitor.Rssilist.size();I++){
                AveR = bitor.Rssilist.get( I ) + AveR;
            }
            if (bitor.Rssilist.size() != 0)
                AveR = AveR/bitor.Rssilist.size();

            Var =  MinVar = SumVar = 0;MinV = 0;
            for (I = 0;I < bitor.Rssilist.size();I++){
                Var = (float)(Math.pow( bitor.Rssilist.get( I )-AveR,2 ));
                if (I == 0 ){
                    MinVar = Var;
                    MinV = I;
                }
                if (MinVar > Var){
                    MinVar = Var;
                    MinV = I;
                }
                SumVar = SumVar + Var;
                bitor.setRssi( bitor.Rssilist.get( MinV ).shortValue());
            }

            if(bitor.Rssilist.size() != 0) {
                bitor.setAverage( (float)(Math.sqrt( SumVar)/ bitor.Rssilist.size()) );
            }

            if (nlist.size() != 0){
                bitor.setWeight( (float)(bitor.Rssilist.size() * 1.0 / nlist.size()) );
            }
        }
        return AveList;
    }

    public static TPoint getCentre(List<TBlueTooth> soulist,int undob){
        if ((soulist == null)||(soulist.size() == 0)) return null;

        float bx = 0,by = 0,bz = 0,count = 0;
        for (int i = 0 ; i < soulist.size();i++){
            if (i == undob) continue;
            bx += soulist.get( i ).getPoint().getX();
            by += soulist.get( i ).getPoint().getY();
            bz += soulist.get( i ).getPoint().getZ();
            count ++;
        }

        if (count == 0)return null;
        TPoint result = new TPoint();
        result.setX( bx/count );
        result.setY( by/count );
        result.setZ( bz/count );

        return result;
    }

    public static int getNearstSec(List<TBlueTooth> deslist){
        int indexb = -1;
        if ((deslist == null)||(deslist.size() == 0)) return indexb;
        double mindis = -1;

        for (int i  = 1;i < deslist.size(); i ++){
            double temdis = getdistance( deslist.get( 0 ).getPoint(),deslist.get( i ).getPoint() )
                    * (1+ Math.abs( deslist.get( 0 ).getRssi() - deslist.get( i ).getRssi() )/3);
            if (mindis == -1){
                indexb = i;
                mindis = temdis;
                continue;
            }

            if (temdis < mindis){
                indexb = i;
                mindis = temdis;
            }
        }
        return indexb;
    }

    public static void DelInvaliDate(List<TBlueTooth> deslist){
        if ((deslist == null)||(deslist.size() == 0)) return;

        for (int i = deslist.size() -1; i >= 0;i-- ){
            deslist.get( i ).setDis( getDisRssi( deslist.get( i ).getRssi()
                    ,deslist.get( i ).getA()
                    ,deslist.get( i ).getRn() ) );
            if ((deslist.get( i ).getRssi() < -90) ||(deslist.get( i ).getRssi() > -20)) {
                deslist.get( i ).setIsExist( false );
                continue;
            }
            /*double dis = getdistance(deslist.get( i ).getPoint(),getCentre(deslist,i));
            if (dis > 20){
                deslist.get( i ).setIsExist( false );
            }*/
        }

        int indexb = getNearstSec( deslist );
        if ((indexb != -1)&&(indexb != 1)){
            Collections.swap( deslist ,1,indexb );
            if (indexb != 2)
                Collections.swap( deslist ,2,indexb );
        }
    }

    public static TPoint GetMinDis(TPoint des,List<TNavHotMsg> nlist){
        if ((des == null)||(nlist == null)) return null;
        TPoint res = null;
        double mindis = -1;
        for (TNavHotMsg nav:nlist){
            if (mindis == -1){
                res = new TPoint();
                mindis = getdistance( des,nav.getPoint() );
                res.setX( nav.getPoint().getX() );
                res.setY( nav.getPoint().getY() );
                continue;
            }
            if (mindis > getdistance( des,nav.getPoint() )){
                mindis = getdistance( des,nav.getPoint() );
                res.setX( nav.getPoint().getX() );
                res.setY( nav.getPoint().getY() );
            }
        }
        return res;
    }

    //bundle navhot
    public static void BundlNav(TPoint des,List<TNavHotMsg> nList,float limdis){
        if ((des == null) ||(nList == null)||(limdis <=0)) return;

        TPoint minp = GetMinDis(des,nList);
        double dis = getdistance( des,minp );
        if (dis > limdis){
            des.setX((float) (minp.getX() - (dis - limdis)/dis * (minp.getX() - des.getX())) );
            des.setY((float) (minp.getY() - (dis - limdis)/dis * (minp.getY() - des.getY())) );
        }
    }

    //bundle blehot
    public static void BundlBle(TPoint des,List<TBlueTooth> bList,float limdis){
        if ((des == null) ||(bList == null)||(limdis <=0)) return;

        TPoint minp = GetMinDisFromBle(des,bList);
        if (minp != null) {
            double dis = getdistance( des, minp );
            if (dis > limdis) {
                des.setX( (float)(minp.getX() - (dis - limdis) / dis * (minp.getX() - des.getX())) );
                des.setY( (float)(minp.getY() - (dis - limdis) / dis * (minp.getY() - des.getY())) );
            }
        }
    }

    //bundle Pathhot
    public static TPoint BundlPath(TPoint des,List<TPathRgn> pList,List<TNavHotMsg> nlist,float limdis){
        if ((des == null) ||(pList == null)||(limdis <=0)||(null == nlist))
            return null;

        int nindex = getNearstPFromPath(des,pList);
        if (-1 == nindex) return null;
        TPathRgn minP = pList.get( nindex );
        TPoint res = null;
        res = getfootPoint(des,minP);
        res.setZ( des.getZ() );
        return res;
        /*double xlimi = des.getX() - res.getX()
                ,ylimi = des.getY() - res.getY()
                ,dis = getdistance( des,res );
        if (dis > limdis) {
            des.setX( (float)(des.getX() - limdis/dis*xlimi) );
            des.setY( (float)(des.getY() - limdis/dis*ylimi) );
        }*/
    }

    public static int getNearstPFromPath(TPoint np,List<TPathRgn> plist){
        if ((null == np) || (null == plist)) return -1;

        double mindis = -1;
        int nindex = -1;
        for (int i = 0 ;i <plist.size();i++){
            if ((np.getZ() == plist.get( i ).getStartID().getFloor())
                &&(np.getZ() == plist.get( i ).getStartID().getFloor())){
                double temdis =  getDistancToPath(np,plist.get( i ));
                if (mindis == -1){
                    nindex = i;
                    mindis = temdis;
                    continue;
                }

                if (mindis > temdis){
                    nindex = i;
                    mindis = temdis;
                }
            }
        }

        return  nindex;
    }

    public static TPoint getfootPoint(TPoint lp,TPathRgn sP){
        if ((null == lp)||(null == sP)) return  null;

        TPoint spoi = sP.getStartID().getPoint(),epoi = sP.getEndID().getPoint();
        double K1 = 0,b1 = 0,K2 = 0,b2 = 0,temx = 0,temy = 0;
        if ((spoi.getX() > epoi.getX() - 0.1)
                &&(spoi.getX() < epoi.getX() + 0.1)){
            temx = spoi.getX();
            temy = lp.getY();
        }else {
            K1 = (spoi.getY() - epoi.getY())/(spoi.getX() - epoi.getX());
            if ((spoi.getX() < 0.1)&&(spoi.getX() > -0.1)) {
                b1 = spoi.getY();
            }else {
                b1 = (epoi.getY() - spoi.getY()*epoi.getX()/spoi.getX())/(1 - epoi.getX()/spoi.getX());
            }
            if ((-0.1 < K1)&&(K1 < 0.1)){
                temx = lp.getX();
                temy = spoi.getY();
            }else {
                K2 = -1/K1;
                b2 = lp.getY() - K2*lp.getX();
                temx = (b1 - b2)/(K1 - K2);
                temy = temx * K2 + b2;
            }
        }
        TPoint gefp = new TPoint();
        gefp.setX( (float)temx );
        gefp.setY( (float)temy );

        return gefp;
    }

    public static double getDistancToPath(TPoint dp,TPathRgn sP){
        if ((null == dp)||(null == sP)) return  -1;

        TPoint spoi = sP.getStartID().getPoint(),epoi = sP.getEndID().getPoint();
        double K1 = 0,b1 = 0,K2 = 0,b2 = 0,temx = 0,temy = 0;
        if ((spoi.getX() > epoi.getX() - 0.005)
                &&(spoi.getX() < epoi.getX() + 0.005)){
            temx = spoi.getX();
            temy = dp.getY();
        }else {
            K1 = (spoi.getY() - epoi.getY())/(spoi.getX() - epoi.getX());
            if (spoi.getX() != 0) {
                b1 = (epoi.getY() - spoi.getY()*epoi.getX()/spoi.getX())/(1 - epoi.getX()/spoi.getX());
            }else {
                b1 = spoi.getY();
            }
            if ((K1 > -0.1)&&(K1 < 0.1)){
                temx = dp.getX();
                temy = dp.getY() - spoi.getY();
            }else {
                K2 = -1/K1;
                b2 = dp.getY() - K2*dp.getX();
                temx = (b1 - b2)/(K1 - K2);
                temy = temx * K1 + b1;
            }
        }
        TPoint getp = new TPoint();
        getp.setX( (float)temx );
        getp.setY( (float)temy );
        double diss = getdistance( getp,spoi )
                ,dise = getdistance( getp,epoi )
                ,dissum = getdistance( spoi,epoi );
        if (dise + diss <= dissum + 1){
            return getdistance( getp,dp );
        }else {
            double dps = getdistance( dp,spoi )
                    ,dpe = getdistance( dp,epoi );
            return dps < dpe ? dps:dpe;
        }
    }

    public static TPoint GetMinDisFromBle(TPoint des,List<TBlueTooth> blist){
        if ((des == null)||(blist == null)) return null;
        TPoint res = null;
        double mindis = -1;
        for (TBlueTooth ble:blist){
            if (ble.getNfloor() == des.getZ()) {
                if (mindis == -1) {
                    res = new TPoint();
                    mindis = getdistance( des, ble.getPoint() );
                    res.setX( ble.getPoint().getX() );
                    res.setY( ble.getPoint().getY() );
                    continue;
                }
                if (mindis > getdistance( des, ble.getPoint() )) {
                    mindis = getdistance( des, ble.getPoint() );
                    res.setX( ble.getPoint().getX() );
                    res.setY( ble.getPoint().getY() );
                }
            }
        }
        return res;
    }

    public static Map<String,Float> KalmanFilterL(TPoint des, TPoint kal, float xP, float yP){
        if ((des == null)&&(kal == null)) return null;
        double Rn = 0.5, Qn = 0.01,limd = 5;
        /*if (des == null){
            des = new TPoint();
            double xPpre = kal.getX(),xKg = xPpre / (xPpre + Rn), xke = des.getX() - xPpre
                    ,yKg = yPpre / (yPpre + Rn),yke = des.getY() - yPpre;
            xPpre = (float)((1 - xKg) / xPpre);
            des.setX( (float)(xPpre + xke * xKg) );
            kal.setX( des.getX() );

            yPpre = (float)((1 - yKg) / yPpre);
            des.setY( (float)(yPpre + yke * yKg) );
            kal.setY( des.getY() );
        }else */
        {   if (des == null)
                return null;
            double xPpre = xP + Qn
                    ,xKg = xPpre / (xPpre + Rn)
                    ,xke = des.getX() - kal.getX()
                    ,yPpre = yP + Qn
                    ,yKg = yPpre / (yPpre + Rn)
                    ,yke = des.getY() - kal.getY();
            if (limd < Math.abs( xke )){
                double bA = xke/Math.abs( xke );
                kal.setX( (float)(kal.getX() + limd * bA * xKg) ); //kal
            }else {
                kal.setX( (float)(kal.getX() + xke * xKg) ); //kal
            }
            xP = (float)((1 - xKg) / xPpre);            //var
            des.setX( kal.getX() );

            if (limd < Math.abs( yke )){
                double bA = yke/Math.abs( yke );
                kal.setY( (float)(kal.getY() + limd * bA * yKg) ); //kal
            }else {
                kal.setY( (float)(kal.getY() + yke * yKg) );
            }
            yP = (float)((1 - yKg) / yPpre);
            des.setY( kal.getY() );
            Map<String,Float> res = new HashMap<String,Float>(  );
            res.put( "xP",xP );
            res.put( "yP",yP );
            return res;

        }

    }

    public static void KalmanFilterB(List<TBlueTooth> deslist,List<TBlueTooth> kalmanblist,int limiR){
        if ((kalmanblist == null)||(deslist == null)||(deslist.size() == 0)) return;
        int i = 0;
        double Rn = 0.5,Qn = 0.1;
        for (i = 0;i < deslist.size();i++) {
            TBlueTooth itord = deslist.get( i );
            int indexs = SearchBTIndex( itord.getBleId(), kalmanblist );
            TBlueTooth itors = null;
            if (indexs != -1) {
                itors = kalmanblist.get( indexs );
                double Ppre = itors.getWeight() + Qn
                        , Kg = Ppre/ (Ppre + Rn)
                        , ke = itord.getRssi() - itors.getVar();

                if (Math.abs( ke ) > limiR){
                    if (ke != 0)
                        ke = ke / Math.abs( ke )*limiR;
                }
                itors.setVar( itors.getVar() + Kg * ke );
                itors.setWeight( (float)((1 - Kg) / Ppre) );
                //itord.setVar( itord.getRssi() );
                itord.setRssi( (short)itors.getVar() );
                itors.setIsExist( true );
            } else {
                itors = new TBlueTooth();
                itors.setBleId( itord.getBleId() );
                itors.setWeight( (float)0.1 );   //p(1)
                itors.setVar( itord.getRssi() );  //kf(1)
                itors.setIsExist( true );
                itors.setPoint( itord.getPoint() );
                kalmanblist.add( itors );
            }
        }

        for (i = kalmanblist.size() - 1;i >=0;i--){
            TBlueTooth del = kalmanblist.get( i );
            if (SearchBTIndex( del.getBleId(), deslist ) == -1){
                if (!kalmanblist.get( i ).getIsExist()){
                    kalmanblist.remove( i );
                }/*else {
                    double Xpre = del.getVar()
                            , Ppre = del.getWeight() + Qn
                            , Kg = Ppre / (Ppre + Rn)
                            , ke = del.getRssi() - Xpre;

                    del.setVar( Xpre + Kg * ke );
                    del.setWeight( (float)((1 - Kg) / Ppre) );
                    del.setIsExist( false );

                    TBlueTooth add = new TBlueTooth();
                    add.setPoint( del.getPoint() );
                    add.setRssi( (short)del.getVar() );
                    //add.setVar( add.getRssi() );
                    add.setBleId( del.getBleId() );
                    add.setIsExist( true );
                    deslist.add( add );
                }*/
            }
        }
    }

    public static List<TBlueTooth> Var_Fileter_Ble(List<List<TBlueTooth>> nlist,List<TBlueTooth> flist){
        if ((null == nlist)|(0 == nlist.size())) return null;

        List<TBlueTooth> AveList = new ArrayList<TBlueTooth>(  );

        getBleAverList(nlist,AveList);
        //BleFileterByWeight(AveList,nlist.size()/2,-90,5);
        BleFilterByGuassDistribute(nlist,AveList);
        getBleAverList(nlist,AveList);

        for (int i= 0; i < AveList.size();i++){
            AveList.get( i ).setRssi( (short)AveList.get( i ).getRn() );
        }
        //getMAXList(nlist,AveList);

        Collections.sort( AveList);
        /*, new Comparator<TBlueTooth>() {
            @Override
            public int compare(TBlueTooth t0, TBlueTooth t1) {
                return (int)(Math.abs( t0.getRssi() )/t0.getWeight() - Math.abs( t1.getRssi() )/t1.getWeight());
            }
        } */
        return AveList;
    }

    public static void  getMAXList(List<List<TBlueTooth>> nlist,List<TBlueTooth> Avelist){
        if ((nlist == null)||(Avelist == null)) return;
        Avelist.clear();
        //mmid
        int I,J;
        for (I = 0;I < nlist.size(); I++) {
            for (J = 0;J < nlist.get( I ).size();J++){
                int indexb = SearchBTIndex(nlist.get( I ).get( J ).getBleId(),Avelist);
                if (indexb == -1){
                    TBlueTooth bitor = new TBlueTooth();
                    bitor.setBleId(  nlist.get( I ).get( J ).getBleId());
                    bitor.setWeight( (short)0 );
                    bitor.setVar( 0 );
                    bitor.setRssi( nlist.get( I ).get( J ).getRssi() );
                    Avelist.add( bitor );
                }else {
                    if (Avelist.get( indexb ).getRssi() < nlist.get( I ).get( J ).getRssi())
                        Avelist.get( indexb ).setRssi( nlist.get( I ).get( J ).getRssi() );
                }
            }
        }
    }

    public static void getBleAverList(List<List<TBlueTooth>> nlist,List<TBlueTooth> Avelist){
        if ((nlist == null)||(Avelist == null)) return;
        Avelist.clear();
        //mmid
        int I,J;
        for (I = 0;I < nlist.size(); I++) {
            for (J = 0;J < nlist.get( I ).size();J++){
                if (SearchBTIndex(nlist.get( I ).get( J ).getBleId(),Avelist) == -1){
                    TBlueTooth bitor = new TBlueTooth();
                    bitor.setBleId(  nlist.get( I ).get( J ).getBleId());
                    bitor.setWeight( (short)0 );
                    bitor.setVar( 0 );
                    Avelist.add( bitor );
                }
            }
        }

        //rssi
        for (List<TBlueTooth> bilist: nlist)
        {
            for (TBlueTooth bitor: bilist)
            {
                Integer ind;
                ind = SearchBTIndex(bitor.getBleId(),Avelist);
                if (ind != -1 ){
                    Avelist.get(ind).setRssi((short)(bitor.getRssi() + Avelist.get(ind).getRssi()) );
                    Avelist.get(ind).setWeight(1 + Avelist.get(ind).getWeight() );
                }
            }
        }

        //Average Rssi
        float AveR = 0;
        for (TBlueTooth bitor: Avelist){
            AveR = bitor.getRssi()/bitor.getWeight();
            bitor.setRn( AveR );
        }
    }

    public static void BleFilterByGuassDistribute(List<List<TBlueTooth>> nlist,List<TBlueTooth> Avelist) {
        for (int I = nlist.size() - 1; I >= 0; I--) {
            for (int J = nlist.get( I ).size() - 1; J >= 0; J--) {
                int ind = SearchBTIndex( nlist.get( I ).get( J ).getBleId(), Avelist );
                if (ind != -1) {
                    Avelist.get( ind ).setVar( Avelist.get( ind ).getVar() +
                            Math.pow( Avelist.get( ind ).getRn() - nlist.get( I ).get( J ).getRssi(), 2 ) );
                }
            }
        }

        for (int i = Avelist.size() - 1; i > 0; i--) {
            Avelist.get( i ).setVar( Math.sqrt( Avelist.get( i ).getVar() / Avelist.get( i ).getWeight() ) );
        }

        for (int I = nlist.size() - 1; I >= 0; I--) {
            for (int J = nlist.get( I ).size() - 1; J >= 0; J--) {
                int ind = SearchBTIndex( nlist.get( I ).get( J ).getBleId(), Avelist );
                if (ind != -1) {
                    if((nlist.get( I ).get( J ).getRssi() > Avelist.get( ind ).getRn() + Avelist.get( ind ).getVar()*2)
                            || (nlist.get( I ).get( J ).getRssi() < Avelist.get( ind ).getRn() - Avelist.get( ind ).getVar()*2)){
                        nlist.get( I ).remove( J );
                    }
                }
            }
        }
    }

    public static void BleFileterByWeight(List<TBlueTooth> Avelist,int filwight,int filRss,int filvar){
        if (Avelist == null)return;
        for (int i = Avelist.size()-1;i>=0;i--){
            if ((Avelist.get( i ).getVar() > filvar)
                    ||(Avelist.get( i ).getWeight() < filwight)
                    ||(Avelist.get( i ).getRn() < filRss)){
                Avelist.remove( i );
            }
        }
    }

    public static class CompareByRssi implements Comparator {
        @Override
        public int compare(Object ob1,Object ob2){
            LocType.TLocMR B1 = (LocType.TLocMR)ob1;
            LocType.TLocMR B2 = (LocType.TLocMR)ob2;
            if (B1.getR() > B2.getR())
                return -1;
            else if (B1.getR() == B2.getR())
                return 0;
            else
                return 1;
        }
    }

    public static class CompareByFingerRssi implements Comparator {
        @Override
        public int compare(Object ob1,Object ob2){
            LocType.TRealBleFinger B1 = (LocType.TRealBleFinger)ob1;
            LocType.TRealBleFinger B2 = (LocType.TRealBleFinger)ob2;
            if (B1.getR() > B2.getR())
                return 1;
            else if (B1.getR() == B2.getR())
                return 0;
            else
                return -1;
        }
    }


    //20171226
    public static int NEWstartLoc( List<TLocMR> rlist,
                                    List<TLocM> mlist,
                                    List<TLocIdMR> slist,
                                    String fpath )
    {
        ResetSimpleList(slist);                         //init

        NEWMarkMacList(rlist,mlist,slist,fpath);        //mark and read slist

        ReCompareSimpleList(slist);                     //range

        MatchFinger(rlist,slist);

        Integer getloc = LocQurary(slist,rlist);
        System.out.print( LogCode + "get Loc:" + getloc +"\n");

        /**判读是否超过限制内存*/
        NEWFreeMarkList(slist);

        System.out.print( "CurSize:"+ getListsize( slist )+"\n" );

        return getloc;
    }

    public static void NEWMarkMacList(  List<TLocMR> rlist,
                                        List<TLocM> mlist,
                                        List<TLocIdMR> slist
                                        ,String fpath)
    {
        if (null == mlist) {
            System.out.print( "milist is null" );
            return;
        }
        if (null == rlist){
            System.out.print( "rlist is null" );
            return;
        }
        if (null == mlist){
            System.out.print( "mlist is null" );
            return;
        }
        if (LocLog)System.out.print( "\n ReMark mindexlist \n"  );
        short indexbuf = 0;
        int SearL = 0;
        for (int i = 0;i < rlist.size() - 1;i++){
            indexbuf = (short) Algrithms.SearchMIndex( rlist.get( i ).getM(),mlist );
            if ((indexbuf != -1)&&(SearL < SearchLen)){
                if (-1 != indexbuf){
                    for (int k = 0 ;k <mlist.get( indexbuf ).Idlist.size();k++)
                    {
                        short idbuf = mlist.get( indexbuf ).Idlist.get(k).Id;
                        int idindex = SearchId( idbuf,slist );
                        if (idindex == -1) {
                            LocLoad.ReadNewWifiFinger( fpath, slist, idbuf );
                            System.out.print( LogCode + "readId:" + idbuf );

                        }else {
                            slist.get( idindex ).setMark( true );
                            System.out.print( LogCode + "readId:exist" );
                        }
                    }
                }
                SearL++;
            }
            rlist.get( i ).setIndex( indexbuf );
        }

    }

    public static void NEWFreeMarkList(List<TLocIdMR> slist){
        if (null == slist) return;
        System.out.print( "start to free\n" );
        for (int i = slist.size() -1 ;i >= 0;i--  )
        {//
            if (getListsize(slist) < LimiteMem) break;
            if(false == slist.get( i ).getMark())
            {
                System.out.print( "Delete ID: "+ slist.get( i ).getId()+"\n" );
                slist.remove( i );
            }
        }
    }


    public static List<TLocMR> calcWifiAverage(List<List<TLocMR>> nlist,Integer counts){
        if ((null == nlist)|(0 == nlist.size())) return null;

        int I,J,K,MinV=0;
        float AveR,Var,MinVar,SumVar;
        List<TLocMR> AveList = new ArrayList<TLocMR>(  );

        //mmid
        for (I = 0;I < nlist.size(); I++) {
            for (J = 0;J < nlist.get( I ).size();J++){
                if (SearchWifiIndex(nlist.get( I ).get( J ).getM(),AveList) == -1){
                    TLocMR witor = new TLocMR();
                    witor.setAver( 0 );
                    witor.Rssilist = new ArrayList<Integer>();
                    witor.setMAC(  nlist.get( I ).get( J ).getM());
                    witor.setFeq( nlist.get( I ).get( J ).getFeq() );
                    AveList.add( witor );
                }
            }
        }

        //rssi
        for (List<TLocMR> wilist: nlist)
        {
            Integer ind = -1;
            for (TLocMR witor: wilist)
            {
                ind = SearchWifiIndex(witor.getM(),AveList);
                if (ind != -1 ){
                    if (AveList.get( ind ).Rssilist != null){
                        Integer rss = Integer.valueOf( witor.getR());
                        AveList.get( ind ).Rssilist.add(rss);
                    }
                }
            }
        }

        //Average Rssi
        for (TLocMR witor: AveList){
            AveR = 0;
            Var = SumVar = 0;
            for (I = 0;I < witor.Rssilist.size();I++){
                AveR = witor.Rssilist.get( I ) + AveR;
            }

            AveR = AveR/witor.Rssilist.size();
            for (I = 0;I < witor.Rssilist.size();I++){
                Var = (float)(Math.pow( witor.Rssilist.get( I )-AveR,2 ));
                SumVar = SumVar + Var;
            }

            if(witor.Rssilist.size() != 0) {
                witor.setAver( AveR );
                witor.setRss( (short)Math.round( witor.getAver() ));
                witor.setVar( (float)(Math.sqrt( SumVar/ witor.Rssilist.size() ) ) );
                witor.setWeight( (float)(witor.Rssilist.size()*1.0/counts) );
            }
        }
        return AveList;
    }

    public static float getKalmanRss(List<Integer> RList){
        int len = RList.size();
        float res_pre = 0,resuls = 0;
        return 0;
    }


    public static int SearchWifiIndex(char[] mac, List<TLocMR> nlist){
        if (nlist != null){
            for (int i = 0 ;i < nlist.size(); i++){
                if (String.valueOf( mac ).equals( String.valueOf( nlist.get( i ).getM() ))){
                    return i;
                }
            }
        }
        return -1;
    }

}
