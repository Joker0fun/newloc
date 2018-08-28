package cn.chigoo.loc.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.chigoo.loc.lib.LocType.*;

/**
 * Created by Administrator on 2018/4/8.
 */

public class BleTraingleLoc {

    public static double getDisRssi(int rssi,int a,double n){
        return Math.pow( 10,Math.abs(Math.abs( rssi - a)*1.0 / (10 * n))   );
    }

    public static double getdistance(TPoint p1,TPoint p2){
        if ((p1 == null)||(p2 == null)) return -1;
        return Math.sqrt(  Math.pow(p1.getY() - p2.getY(),2) + Math.pow(p1.getX() - p2.getX(),2) );
    }

    public static TPoint getGeometricbarycenter(TBlueTooth b1,TBlueTooth b2,TBlueTooth b3){
        TPoint barycenter = new TPoint();

        double A = getdistance(b1.getPoint(),b2.getPoint());
        double B = getdistance(b1.getPoint(),b3.getPoint());
        double C = getdistance(b3.getPoint(),b2.getPoint());

        barycenter.setX( (b1.getPoint().getX()
                    +b2.getPoint().getX()
                    +b3.getPoint().getX())/3 );
        barycenter.setY( (b1.getPoint().getX()
                    +b2.getPoint().getX()
                    +b3.getPoint().getX())/3 );


        if ((b1.getRssi() > b2.getRssi())&&(b1.getRssi() > b3.getRssi())){
            barycenter.setZ( b1.getPoint().getZ() );
        }else if ((b2.getRssi() > b3.getRssi())&&(b2.getRssi() > b1.getRssi())){
            barycenter.setZ( b2.getPoint().getZ() );
        }else {
            barycenter.setZ( b3.getPoint().getZ() );
        }
        return  barycenter;
    }

    public static double getWeight(TBlueTooth rb){
        double rdis = getDisRssi(rb.getRssi(),rb.getRssi(),rb.getRn());
        return rdis;
    }

    public static TPoint GetPosition(List<TBlueTooth> blist){
        TPoint gebc = null;
        double bwight = 100;

        for (int i = 0 ;i < blist.size();i++){
            if (!blist.get( i ).getIsExist())continue;
            double twight = Math.abs(blist.get( i ).getRssi())*1.0/blist.get( i ).getWeight();
            if (bwight > twight){
                bwight = twight;
                if (gebc == null)
                    gebc = new TPoint();
                gebc.setX( blist.get( i ).getPoint().getX() );
                gebc.setY( blist.get( i ).getPoint().getY() );
                gebc.setZ( blist.get( i ).getNfloor() );
            }
        }

        return gebc;
    }

    public static TPoint WieghtPosition(List<TBlueTooth> blist){
        double MAXABSRD = 0,MaxR = 0,sum = 0 ,x = 0,y = 0,gain = 0;

        int Maxindex = -1;

        for (int i = 0;i < blist.size();i++){
            TBlueTooth b = blist.get( i );
            if (! b.getIsExist()) continue;
            if (MAXABSRD <  getDisRssi( b.getRssi(),b.getA(),b.getRn() )){
                MAXABSRD = getDisRssi( b.getRssi(),b.getA(),b.getRn() );//+0.1*b.getWeight()
            }
            if (Maxindex == -1){
                Maxindex = i;
                MaxR = b.getRssi();
            }

            if (MaxR < b.getRssi()){
                Maxindex = i;
                MaxR = b.getRssi();
            }
        }
        if (Maxindex == -1 ) return null;

        TPoint gebc = new TPoint();
        for (int i = 0;i < blist.size();i++){
            TBlueTooth b = blist.get( i );
            if (! b.getIsExist()) continue;
            sum = sum + (MAXABSRD - getDisRssi( b.getRssi(),b.getA(),b.getRn() ))/MAXABSRD;//+0.1*b.getWeight()
        }

        for (int i = 0;i < blist.size();i++){
            TBlueTooth b = blist.get( i );
            if (! b.getIsExist()) continue;
            gain = (MAXABSRD - getDisRssi( b.getRssi(),b.getA(),b.getRn() ))/MAXABSRD/sum;//+0.1*b.getWeight()
            x = x + b.getPoint().getX()*gain;
            y = y + b.getPoint().getY()*gain;
        }

        gebc.setX( (float)x );
        gebc.setY( (float)y );
        gebc.setZ( blist.get( Maxindex ).getNfloor() );

        return gebc;
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
        if (p0 != null)return p0;

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

    public static TPoint BleTrilateration(List<TBlueTooth> rlist,int fg){
        TPoint getp = null;
        if (fg == 0) {
            getp = getBestPoint( rlist );
        }else if (fg == 1){
            getp = WieghtPosition(rlist);
        }else if (fg == 2){
            getp = GetPosition(rlist);
        }
        return getp;
    }

    public static int searchBleIndexFromPost(char[] BleId,List<TBlueTooth> blist){
        if ((blist == null)||(blist.size() == 0 )) return -1;
        int head = 0,end = blist.size(),bindex,value;
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
            if (bindex != -1){
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

    public static int GetFloorAccordingPosition(TPoint nowpoint,List<TNavHotMsg> navHotMsgList){
        if (navHotMsgList == null) return 0;

        double mindis = 0;
        int nindex = -1;
        for (int i = 0;i < navHotMsgList.size();i++){
            if (i == 0){
                mindis = getdistance( nowpoint,navHotMsgList.get( i ).getPoint() );
                nindex = i;
            }

            if (mindis > getdistance( nowpoint,navHotMsgList.get( i ).getPoint() )){
                mindis = getdistance( nowpoint,navHotMsgList.get( i ).getPoint() );
                nindex = i;
            }
        }

        if (nindex == -1){
            return 0;
        }else {
            return navHotMsgList.get( nindex ).nFloor;
        }
    }
}
