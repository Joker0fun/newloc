package cn.chigoo.loc.BleLoc;

/**
 * Created by Administrator on 2018/6/12.
 */

public class BleLocType {
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
}
