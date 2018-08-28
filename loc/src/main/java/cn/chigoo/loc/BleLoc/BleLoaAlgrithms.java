package cn.chigoo.loc.BleLoc;

/**
 * Created by Administrator on 2018/6/12.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import  cn.chigoo.loc.lib.LocType.*;
//import cn.chigoo.loc.BleLoc.BleLocType.*;

public class BleLoaAlgrithms {

    public static double getDisRssi(int rssi,int a,double n){
        return Math.pow( 10,Math.abs(Math.abs( rssi - a)*1.0 / (10 * n))   );
    }

    public static double getdistance(TPoint p1,TPoint p2){
        if ((p1 == null)||(p2 == null)) return -1;
        return Math.sqrt(  Math.pow(p1.getY() - p2.getY(),2) + Math.pow(p1.getX() - p2.getX(),2) );
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

    public static List<TBlueTooth> Var_Fileter_Ble(List<List<TBlueTooth>> nlist, List<TBlueTooth> flist){
        if ((null == nlist)|(0 == nlist.size())) return null;

        List<TBlueTooth> AveList = new ArrayList<TBlueTooth>(  );

        getBleAverList(nlist,AveList);
        BleFilterByGuassDistribute(nlist,AveList);
        getBleAverList(nlist,AveList);

        for (int i= 0; i < AveList.size();i++){
            AveList.get( i ).setRssi( (short)AveList.get( i ).getRn() );
        }

        Collections.sort( AveList);

        return AveList;
    }

    public static TPoint BundlPath(TPoint des,List<TPathRgn> pList,List<TNavHotMsg> nlist){
        if ((des == null) ||(pList == null)||(null == nlist))
            return null;

        int nindex = getNearstPFromPath(des,pList);
        if (-1 == nindex)
            return null;
        TPathRgn minP = pList.get( nindex );
        TPoint res = null;
        res = getfootPoint(des,minP);
        if (res == null){
            return null;
        }
        res.setZ( des.getZ() );
        return res;
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
        if ((null == lp)||(null == sP))
            return  null;

        TPoint spoi = sP.getStartID().getPoint(),epoi = sP.getEndID().getPoint();
        double K1 = 0,b1 = 0,K2 = 0,b2 = 0,temx = 0,temy = 0;
        if ((spoi.getX() > epoi.getX() - 0.1)
                &&(spoi.getX() < epoi.getX() + 0.1)){

            temx = spoi.getX();

            if (((lp.getY() < spoi.getY())&&(lp.getY() > epoi.getY()))
                    ||((lp.getY() > spoi.getY())&&(lp.getY() < epoi.getY())))
                temy = lp.getY();
            else
                temy = Math.abs( lp.getY() - spoi.getY() ) < Math.abs( lp.getY() - epoi.getY() )? spoi.getY():epoi.getY();

        }else {
            K1 = (spoi.getY() - epoi.getY())/(spoi.getX() - epoi.getX());
            if ((spoi.getX() < 0.1)&&(spoi.getX() > -0.1)) {
                b1 = spoi.getY();
            }else {
                b1 = (epoi.getY() - spoi.getY()*epoi.getX()/spoi.getX())/(1 - epoi.getX()/spoi.getX());
            }

            if ((-0.1 < K1)&&(K1 < 0.1)){
                temx = lp.getX();
                if (((lp.getY() < spoi.getY())&&(lp.getY() > epoi.getY()))
                        ||((lp.getY() > spoi.getY())&&(lp.getY() < epoi.getY())))
                    temy = lp.getY();
                else
                    temy = Math.abs( lp.getY() - spoi.getY() ) < Math.abs( lp.getY() - epoi.getY() )? spoi.getY():epoi.getY();
            }else {
                K2 = -1/K1;
                if ((-0.1 < K2)&&(K2 < 0.1)){

                    temx = (spoi.getX() + epoi.getX())/2;

                    if (((lp.getY() < spoi.getY())&&(lp.getY() > epoi.getY()))
                        ||((lp.getY() > spoi.getY())&&(lp.getY() < epoi.getY())))
                        temy = lp.getY();
                    else
                        temy = Math.abs( lp.getY() - spoi.getY() ) < Math.abs( lp.getY() - epoi.getY() )? spoi.getY():epoi.getY();

                }else {
                    b2 = lp.getY() - K2*lp.getX();
                    temx = (b1 - b2)/(K2 - K1);
                    temy = temx * K2 + b2;
                    //return null;
                }
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
        if ((spoi.getX() > epoi.getX() - 0.1)
                &&(spoi.getX() < epoi.getX() + 0.1)){
            temx = spoi.getX();
            temy = dp.getY();
        }else {
            K1 = (spoi.getY() - epoi.getY())/(spoi.getX() - epoi.getX());
            if ( (spoi.getX() > -0.1)&&(spoi.getX() < 0.1)) {
                b1 = spoi.getY();
            }else {
                if ( (1 - epoi.getX()/spoi.getX() > -0.1)&&(1 - epoi.getX()/spoi.getX() < 0.1) )
                    b1 = 0;
                else
                    b1 = (epoi.getY() - spoi.getY() * epoi.getX() / spoi.getX()) / (1 - epoi.getX() / spoi.getX());
            }

            if ((K1 > -0.1)&&(K1 < 0.1)){
                temx = dp.getX();
                temy = dp.getY() - spoi.getY();
            }else {
                K2 = -1/K1;
                b2 = dp.getY() - K2*dp.getX();
                if(((K1 - K2) > -0.1)&&((K1 - K2) < 0.1))
                    temx = 0;
                else
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

    public static TPoint Triagleration(TBlueTooth b1,TBlueTooth b2,TBlueTooth b3){
        TPoint getp = new TPoint();
        double r1 = b1.getDis()
                ,r2 = b2.getDis()
                ,r3 = b3.getDis();

        double A = getdistance(b1.getPoint(),b2.getPoint());

        if ((r1 + r2 < A)||(A + r1 < r2)) {
            double r12 = r1 + r2,r23 = r2 + r3;
            r1 = r1 + r1/r12*(A - r12);
            r2 = r2 + r2/r12*(A - r12);
            r3 = r3 + r3/r23*r2;
        }else if (r1 + r2 > 1.5*A){
            double r12 = r1 + r2,r23 = r2 + r3;
            r1 = r1 + r1/r12*(1.5*A - r12);
            r2 = r2 + r2/r12*(1.5*A - r12);
            r3 = r3 + r3/r23*r2;
        }


        double d = Math.sqrt( Math.pow( (b2.getPoint().getX() - b1.getPoint().getX()) ,2 )
                + Math.pow( (b2.getPoint().getY() - b1.getPoint().getY()) ,2) );
        double a = (Math.pow( r1,2 ) - Math.pow( r2,2 ) + Math.pow( d,2 ))/(2*d);

        double h = Math.sqrt(Math.abs( Math.pow( r1,2 ) - Math.pow( a,2 )) );

        double x0 = b1.getPoint().getX() + (a/d)*(b2.getPoint().getX() - b1.getPoint().getX());
        double y0 = b1.getPoint().getY() + (a/d)*(b2.getPoint().getY() - b1.getPoint().getY());

        double xa = x0 + (h/d)*(b2.getPoint().getY() - b1.getPoint().getY());
        double ya = y0 + (h/d)*(b2.getPoint().getX() - b1.getPoint().getX());
        double xd = x0 - (h/d)*(b2.getPoint().getY() - b1.getPoint().getY());
        double yd = y0 - (h/d)*(b2.getPoint().getX() - b1.getPoint().getX());

        double ma =Math.sqrt( Math.pow( xa - b3.getPoint().getX(),2 )
                + Math.pow( ya - b3.getPoint().getY(),2) );

        double md = Math.sqrt( Math.pow( xd - b3.getPoint().getX(),2 )
                + Math.pow( yd - b3.getPoint().getY(),2) );


        getp.setZ( b1.getNfloor() );
        if ( ma < md){
            if (Double.isNaN( xa )||Double.isNaN( ya ))//||(ma/r3 > 3)
                return null;
            getp.setX( (float)xa );
            getp.setY( (float)ya );
        }else {
            if (Double.isNaN( xd )||Double.isNaN( yd ))//||(md/r3 > 3)
                return null;
            getp.setX( (float)xd );
            getp.setY( (float)yd );
        }

        return getp;
    }

    public static TPoint combine(List<TBlueTooth> rlist){
        TPoint p0;
        //Collections.sort( rlist);
        int index0 = -1,index1 = -1,index2 = -1;
        for (int i = 0; i < rlist.size();i++){
            if ((index0 ==  -1)&&(rlist.get( i ).getIsExist()) ){
                index0 = i;
                continue;
            }
            if ((index1 == -1)&&(rlist.get( i ).getIsExist()) ){
                index1 = i;
                continue;
            }
            if ((index2 == -1)&&(rlist.get( i ).getIsExist()) ){
                index2 = i;
                break;
            }
        }

        if (index2 == -1) return null;

        p0 = Triagleration( rlist.get( index0 ),
                rlist.get( index1 ),
                rlist.get( index2 ));
        if (p0 != null)
            return p0;

        return null;


        /*        TPoint p1,p2;
       p1 = Triagleration( rlist.get( index0 ),
                rlist.get( index2 ),
                rlist.get( index1 ));

        p2 = Triagleration( rlist.get( index1 ),
                rlist.get( index2 ),
                rlist.get( index0 ));

        double dis01 = getdistance( p0,p1 )
                ,dis02 = getdistance( p0,p2 )
                ,dis12 = getdistance( p1,p2 );

        if ((dis01 <= dis02)&&(dis01 <= dis12)&&(dis01 != -1)){
            p0.setX( (p0.getX()+p1.getX())/2 );
            p0.setY( (p0.getY()+p1.getY())/2 );
            return p0;
        }else if ((dis02 <= dis01)&&(dis02 <= dis12)&&(dis02 != -1)){
            p0.setX( (p0.getX() + p2.getX())/2 );
            p0.setY( (p0.getY() + p2.getY())/2 );
            return p0;
        }else if ((dis12 != -1)){
            p0.setX( (float)((p2.getX()+p1.getX())/2) );
            p0.setY( (float)((p2.getY()+p1.getY())/2) );
            return p0;
        }else {
            return  null;
        }*/
    }

    public static int getFactorial(int n){
        int fac = 0;
        for (int i = 1;i <= n;i++){
            fac = fac*i;
        }
        return fac;
    }

    public static TPoint getBestPoint(List<TBlueTooth> rlist){
        if ((rlist == null)||(rlist.size() == 0)) return null;
        TPoint meanp = new TPoint();
        int Count = 0,firstR = 0;
        for (int i = 0;i < rlist.size();i++){
            if (rlist.get( i ).getIsExist()){
                if (Count == 1){
                    /*float mfw = rlist.get( i ).getRssi()/(rlist.get( i ).getRssi()+ firstR)
                            ,mbw = firstR /( rlist.get( i ).getRssi()+ firstR);*/
                    meanp.setX( rlist.get( i ).getPoint().getX()*1/3 + meanp.getX()*2/3 );
                    meanp.setY( rlist.get( i ).getPoint().getY()*1/3 + meanp.getY()*2/3 );
                }
                if (Count  == 0){
                    firstR = rlist.get( i ).getRssi();
                    meanp.setX( rlist.get( i ).getPoint().getX() );
                    meanp.setY( rlist.get( i ).getPoint().getY() );
                    meanp.setZ( rlist.get( i ).getNfloor() );
                }
                Count++;
            }
        }
        if (Count == 0) return null;
        if ((Count == 1)||(Count == 2)) return meanp;

        return combine(rlist);
    }

    public static int searchBleIndexFromPost(char[] BleId,List<TBlueTooth> blist){
        if ((blist == null)||(blist.size() == 0 )) return -1;
        int head = 0,end = blist.size()-1,bindex,value;
        while (head <= end){
            bindex = (Integer)(head+end)/2;
            value = String.valueOf( blist.get( bindex ).getBleId() ).compareTo( String.valueOf(BleId) );
            if (value == 0){
                return bindex;
            }else if (value < 0){
                head = bindex + 1;
            }else {
                end = bindex - 1;
            }
        }

        return -1;
    }

    public static void getRlistInfo(List<TBlueTooth> rlist,List<TBlueTooth> blist){
        if ((rlist == null)||(blist == null)||(rlist.size() ==0)) return;

        for (TBlueTooth rb:rlist){
            int bindex = searchBleIndexFromPost(rb.getBleId(),blist);
            if ((bindex != -1)&&(bindex < blist.size())){
                TBlueTooth blueTooth = blist.get( bindex );
                rb.setA( blueTooth.getA() );
                rb.setRn( blueTooth.getRn() );
                rb.setIsExist( true );
                rb.setNfloor( blueTooth.getNfloor() );
                rb.setPoint( blueTooth.getPoint() );
            }else {
                rb.setIsExist( false );
            }
        }

        for (int i = rlist.size()-1 ;i >= 0;i--){
            if (!rlist.get( i ).getIsExist()){
                rlist.remove( i );
            }
        }
    }

    public static Map<String,Float> KalmanFilterL(TPoint des, TPoint kal, float xP, float yP){
        if ((des == null)&&(kal == null)) return null;
        double Rn = 0.5, Qn = 0.01,limd = 5;
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
}
