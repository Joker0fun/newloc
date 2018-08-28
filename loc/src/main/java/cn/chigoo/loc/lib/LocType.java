package cn.chigoo.loc.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/9/13.
 */

public class LocType {
    public static final String LVersion = "Version1.2";

    public static final short Rmax = 5;

    public static final short SearchLen = 5;

    public static final long LimiteMem = 20000;

    public static final Integer BTA = 50;

    public static final float BTN = 0;

    public static final boolean LocLog = false;

    //20180202 new Wifi get
    public static class TWifiInfo implements Comparable{
        char[]  MAC = new char[17];
        short   Rss;
        String  Feq;

        public char[] getM(){   return this.MAC;    }
        public short getR(){     return this.Rss;    }
        public String getFeq() {            return Feq;        }
        public void setMAC(char[] M){
            int length = this.MAC.length > M.length ? M.length :this.MAC.length;
            System.arraycopy(M,0,this.MAC,0,length );
        }
        public void setRss(short R)  {   this.Rss = R;      }
        public void setFeq(String feq) {            Feq = feq;        }
        public String toString(){
            return String.format("%s|%s|%d", String.valueOf( MAC),Feq,Rss);
        }
        @Override
        public int compareTo(Object obj) {
            if(obj instanceof TWifiInfo){
                TWifiInfo objmr = (TWifiInfo)obj;
                return (this.getR() - objmr.getR());
            }
            return 0;
        }
    }

    public static class TLocMR implements Comparable{
        char[]  MAC = new char[17];
        short   Rss;
        String  Feq;
        short   Index;
        float   aver;
        float   weight;
        float   Var;
        public List<Integer> Rssilist;

        public char[] getM(){   return this.MAC;    }
        public short getR(){     return this.Rss;    }
        public short getI(){     return this.Index;    }
        public float getAver(){return this.aver;}
        public String getFeq() {            return Feq;        }
        public void setMAC(char[] M){
            int length = this.MAC.length > M.length ? M.length :this.MAC.length;
            System.arraycopy(M,0,this.MAC,0,length );
        }
        public void setRss(short R)  {   this.Rss = R;      }
        public void setIndex(short index)  {   this.Index = index;      }
        public void setAver(float aver){ this.aver = aver; }
        public void setWeight(float weight){this.weight = weight;}
        public void setVar(float var){this.Var = var;}
        public void setFeq(String feq) {            Feq = feq;        }
        public String toString(){
            return String.format("%s|%s|%d|%.2f|%.2f", String.valueOf( MAC),Feq,Rss,Var,weight );
        }

        public String toOldString(){
            return String.format("%s|%d|%.2f|%.2f", String.valueOf( MAC),Rss,Var,weight );
        }

        @Override
        public int compareTo(Object obj) {
            if(obj instanceof TLocMR){
                TLocMR objmr = (TLocMR)obj;
                return (objmr.getR() - this.getR());
            }
            return 0;
        }
    }

    public static class  TLocM implements Comparable{
        char[] MAC = new char[20];
        char Kind;
        List<TId> Idlist;

        public char[] getM(){   return this.MAC;    }
        public void setMAC(char[] M){  System.arraycopy(M,0,this.MAC,0,20  );}
        public char getKind(){return this.Kind;}
        public void setKind(char kind) {this.Kind = kind;}
        @Override
        public int compareTo(Object obj) {
            if(obj instanceof TLocM){
                TLocM objc = (TLocM)obj;
                String pstr = String.valueOf( objc.getM() );
                String sstr = String.valueOf( this.MAC );
                if (pstr.compareTo( sstr ) > 0){
                    return -1;
                }else if (pstr.compareTo( sstr ) < 0){
                    return 1;
                }else {
                    return 0;
                }
            }
            return 0;
        }
    }

    public static class TLocWMR{
        short mac;
        float Wt;
        char R;
        public void setWt(float wt) {
            Wt = wt;
        }
        public float getWt() {
            return Wt;
        }
        public short getMacIndex(){ return this.mac;}
        public void setMacIndex(short Mi){this.mac = Mi ;}
        public short GetRss(){ return (short)(Integer.valueOf(R) - 256);}
        public void SetRss(char r){this.R = r;}
    }

    public static class TLocCMR implements Comparable{
        MemCalValue Values = new MemCalValue();
        List<TLocWMR> CList;
        @Override
        public int compareTo(Object obj) {
            if (obj instanceof TLocCMR) {
                TLocCMR cobj = (TLocCMR) obj;
                if (cobj.Values.getMC() > this.Values.getMC()) {
                    return 1;
                } else if (cobj.Values.getMC() < this.Values.getMC()) {
                    return -1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }

    public static class MemCalValue {
        int MatchCount ;       //match number
        double MatchRss ;       //the MatchRssVar

        public int getMC(){return this.MatchCount;}
        public void setMR(double mR){ this.MatchRss = mR; }
        public double getMR(){return this.MatchRss;}
        public void setMC(int mC){ this.MatchCount = mC; }
        public void Clean(){
            this.MatchCount = 0;
            this.MatchRss = 0;
        }
    }

    public static class TLocIdMR implements Comparable{
        int Id;
        MemCalValue Values = new MemCalValue();
        boolean IsContain;  //judge the Id is needed
        List<TLocCMR> IList;

        public void setId(int id){ this.Id = id; }
        public int getId(){return this.Id;}
        public boolean getMark( ){ return this.IsContain;}
        public void setMark(boolean Isc){ this.IsContain = Isc;}
        public void Clean(){
            this.Values.Clean();
            this.IsContain = false;
        }
        @Override
        public int compareTo(Object obj) {
            if (obj instanceof TLocIdMR) {
                TLocIdMR cobj = (TLocIdMR) obj;
                if (cobj.Id > this.Id) {
                    return -1;
                } else if (cobj.Id < this.Id) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }
//==================================================
    public static class TId{
        short Id;
        public short getId(){ return this.Id ;}
        public void  setId(short id){this.Id = id;}
    }

    public static class TMAC{
        short MACIndex;
        boolean IsContain;
        List<TId> Idlist;
        public boolean getMark(){return this.IsContain;}
        public void setMark(boolean Isc){ this.IsContain = Isc;}
        public short getMACIndex(){ return this.MACIndex ;}
        public void  setMACIndex(short M){this.MACIndex = M;}
    }

    public static class TPoint{
        float x = 0;
        float y = 0;
        float z = 0;
        public float getX(){return this.x;}
        public float getY(){return this.y;}
        public float getZ(){return this.z;}
        public void setX(float x1){this.x = x1;}
        public void setY(float y1){this.y = y1;}
        public void setZ(float z1){this.z = z1;}
        public String toString(){
            return String.format( "x = %.2f,y = %.2f,z = %.2f",this.x,this.y,this.z );
        }
    }

    public static class TBlueTooth implements Comparable{
        private char[] BleId = new char[12];
        /*private char[] uuid  = new char[32];
        private char[] Info  = new char[20];*/
        private TPoint point = new TPoint();
        private float Rn = 0;    //aver, 观测噪声R
        private double Var = 0;  //      kf
        private short A = 0;     // A , Count
        private float weight = 0;//sum   协方差更新p
        private short Rssi = 0;  //result
        private double dis = 0;
        private short nfloor;
        private boolean IsExist = false; //delete


        public TPoint getPoint(){return this.point;}
        public char[] getBleId(){ return this.BleId;}
        public double getDis() {return dis;}
        /*public char[] getBleuuid(){ return this.uuid;}
                public char[] getInfo() { return this.Info;}*/
        public float getRn(){return this.Rn;}
        public double getVar() {return this.Var;}
        public short getA(){return this.A;}
        public float getWeight(){return this.weight;}
        public short getRssi(){ return this.Rssi;}
        public short getNfloor( ){ return this.nfloor;}
        public boolean getIsExist() { return  this.IsExist;}

        public void setBleId(char[] bleId){ System.arraycopy(bleId,0,this.BleId,0,12  );}
        public void setDis(double dis) {this.dis = dis;}
        /*public void setBleuuid(char[] uuid){ System.arraycopy(uuid,0,this.uuid,0,18  );}
                public void setInfo(char[] info1){ System.arraycopy(info1,0,this.Info,0,20 );}*/
        public void setRssi(short Rssi1){ this.Rssi = Rssi1;}
        public void setRn(float rn){this.Rn = rn;}
        public void setVar(double var){this.Var = var;}
        public void setA(short a){this.A = a;}
        public void setWeight(float weight){this.weight =  weight;}
        public void setNfloor(short nf){this.nfloor = nf;}
        public void setIsExist(boolean ie){ this.IsExist = ie;}
        public void setPoint(TPoint p){
            this.point.setZ( p.getZ() );
            this.point.setX( p.getX() );
            this.point.setY( p.getY() );
        }
        public String toString(){
                return String.valueOf( BleId) + "|" + Rssi;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj instanceof TBlueTooth) {
                TBlueTooth objCompare  =(TBlueTooth)obj;
                if (objCompare.getRssi() < this.getRssi()) {
                    return -1;
                }else if(objCompare.getRssi() > this.getRssi()){
                    return 1;
                }else {
                    return 0;
                }

            }
            return 0;
        }
    }

    public static class TPathRgn{
        TNavHotMsg startID;
        TNavHotMsg endID;
        public void setStartID(TNavHotMsg s){this.startID = s;}
        public void setEndID(TNavHotMsg e){this.endID = e;}
        public TNavHotMsg getStartID(){ return this.startID;}
        public TNavHotMsg getEndID(){ return this.endID;}
    }

    public static class TNavHotMsg implements Comparable{
        int             Id;
        TPoint          point;          //logicPoint
        TPoint          realPoint;      //realPoint
        char            IdType;         //IDType
        String          PostInfo;       //Postion Info
        String          AutoPostInfo;   //Automatic broadcast info
        char            Region;         //belong to the area(domestic ,international)
        char            SecurityDoor;   //SecurityDoor (inside,in,outside)
        char            Partition;      //division interval
        String          ChanalName;     //ChanalName
        String          IsLink;         //Is Link floor
        String          LinkID;         //floor link id
        char            NotPaintPath;   //Is Link Label20160920
        char            IsLocation;     //Is Location point
        Integer         nFloor;         //pathtest 171023
        boolean         Searched;       //pathtest 171023

        public int getId(){return this.Id;}
        public void setPoint(TPoint p){
            this.point = p;
        }
        public int getFloor(){return this.nFloor;}
        public TPoint getPoint(){
            return this.point;
        }
        public void setnFloor(int floor){ this.nFloor = floor;}
        @Override
        public int compareTo(Object obj) {
            if (obj instanceof TNavHotMsg) {
                int oid = ((TNavHotMsg)obj).getId();
                int tid = this.getId();
                if (oid > tid) {
                    return -1;
                }else {
                    return 0;
                }

            }
            return 0;
        }
    }

    //Ble 
    public static class TBleFinger{
        short index;
        short Rssi;
        float Varrss;
        float Weight;
        public short getR(){
            return this.Rssi;
        }
        public short getIndex(){
            return this.index;
        }
        public float getVarrss(){return this.Varrss;}
        public float getWeight(){return this.Weight;}
        public void setRssi(short rssi){
            this.Rssi = rssi;
        }
        public void setIndex(short index1){
            this.index = index1;
        }
        public void setVarrss(float varrss){this.Varrss = varrss;}
        public void setWeight(float weight){this.Weight = weight;}
    }

    public static class TCBleFinger{
        List<TBleFinger> blelist;
    }

    public static class TIdBleFinger implements Comparable{
        int Id;
        int MaxC ;       //max numben
        int MaxC_R ;     //min var of max number
        int MinR ;       //min var
        int MinR_C ;     //max num of min var
        boolean IsContain;  //is the Id needed
        List<TCBleFinger> Cblelist;

        public void setMaxC(int maxC){ this.MaxC = maxC; }
        public int getMaxC(){return this.MaxC;}
        public void setMaxC_R(int maxC_R){ this.MaxC_R = maxC_R; }
        public int getMaxC_R(){return this.MaxC_R;}
        public void setMinR(int minR){ this.MinR = minR; }
        public int getMinR(){return this.MinR;}
        public void setMinR_C(int minR_C){ this.MinR_C = minR_C; }
        public int getMinR_C(){return this.MinR_C;}
        public boolean getMark( ){ return this.IsContain;}
        public void setMark(boolean Isc){ this.IsContain = Isc;}
        public void Clean(){
            this.MaxC = 0;
            this.MaxC_R = 0;
            this.MinR = 0;
            this.MinR_C = 0;
            this.IsContain = false;
        }
        public void setId(int id){
            this.Id = id;
        }
        public int getId(){
            return this.Id;
        }
        @Override
        public int compareTo(Object obj) {
            if (obj instanceof TIdBleFinger) {
                TIdBleFinger cobj = (TIdBleFinger) obj;
                if (cobj.Id > this.Id) {
                    return -1;
                } else if (cobj.Id < this.Id) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }

    public static class TBleTable {
        char[] Blemmid = new char[10];
        List<TId> Idlist;
        public char[] getmmid(){   return this.Blemmid;    }
    }

    //Recieve Ble
    public static class TRealBleFinger implements Comparable{
        char[] Blemmid = new char[10];
        short index;
        short Rssi;
        float Weight = 1;                                   //weight
        float Average;                                     //average
        public List<Integer> Rssilist;

        public char[] getmmid()             {return this.Blemmid;}
        public void setmmid(char[] mmid)    {System.arraycopy(mmid,0,this.Blemmid,0,10);}
        public short getR()                 {return this.Rssi;}
        public short getIndex()             {return this.index;}
        public float getWeight()            {return this.Weight;}
        public float getAverage()          {return this.Average; }
        public void setRssi(short rssi)     {this.Rssi = rssi;}
        public void setIndex(short index1)  {this.index = index1;}
        public void setWeight(float weight) {this.Weight = weight;}
        public void setAverage(float aver) {this.Average = aver;}
        public String toString(){
            String tem = String.format( "|%d|%.2f|%.2f",Rssi,Average,Weight );
            return String.valueOf( Blemmid ) + tem;
        }
        @Override
        public int compareTo(Object obj){
            if (obj instanceof TRealBleFinger){
                if (((TRealBleFinger)obj).getR() - this.getR() > 0) {
                    return - 1;
                }else if (((TRealBleFinger)obj).getR() - this.getR() < 0){
                    return 1;
                }else {
                    return 0;
                }
            }
            return 0;
        }
    }

}
