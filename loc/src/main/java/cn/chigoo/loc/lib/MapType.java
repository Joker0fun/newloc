package cn.chigoo.loc.lib;

import java.util.HashMap;
import java.util.List;
import cn.chigoo.loc.lib.LocType.*;

/*
 * Created by Administrator on 2018/3/14.
*/

public class MapType {
    public static class MapPoint{
        double cx = 0.0;
        double cy = 0.0;

        public void setX(double x){
            this.cx = x;
        }
        public void setY(double y){
            this.cy = y;
        }
        public double getX(){
            return this.cx;
        }
        public double getY(){
            return this.cy;
        }
        public void MapOrigin(MapPoint mapPoint){
            this.cy = mapPoint.cy;
            this.cx = mapPoint.cx;
        }
    }

    public static class MapInfo{
        String FilePath;    //地图路径
        String MapName;     //地图名称
        Integer MapHeight;  //地图高度
        Integer MapWidth;   //地图宽度
        public String getFilePath(){
            return this.FilePath;
        }
        public String getMapName(){
            return this.MapName;
        }
        public Integer getMapHeight(){return this.MapHeight;}
        public Integer getMapWidth(){return this.MapWidth;}
        public void setFilePath(String fpath){
            this.FilePath = fpath;
        }
        public void setMapName(String mapName){
            this.MapName = mapName;
        }
        public void setMapHeight(Integer mapHeight){this.MapHeight = mapHeight;}
        public void setMapWidth(Integer mapWidth){this.MapWidth = mapWidth;}
    }

    public static class MapMsg{
        public MapInfo mapInfo;             //地图信息
        public TPoint m_dPoint;             //地图原点
        public double MapZoom;              //地图缩放比例
        private String Picfile;              //存放图片
        public TPoint RB;                   //

        public void setpicfile(String picfile){this.Picfile = picfile;}
        public String getpicfile(){return this.Picfile;}
    }
    //CoordM2R:map coordinate to rect coordinate  CoordR2M: rect coordinate to map coordinate ;
    public enum  CoordChangType{
        CoordM2R,CoordR2M
    }

    public static class ARegion{
        String AreaName;
        boolean MapLocType;
        HashMap<String,HashMap<String,String>> FloorListConfig;
        public List<MapMsg> FloorList;
        public List<TLocM> wifiMlist;      //MAC table
        public List<TLocIdMR> wifiidlist;  //load wififinger data
        public List<TLocMR> wifiRlist;     //load Macindex
        public List<TNavHotMsg> navlist;   //navhot
        public List<TBlueTooth> blist;     //bluetooth triglation
        public List<TBlueTooth> bletemlist;   //loadsimple
        public List<TBleTable>  bleTableList;//mmid table
        public List<TPathRgn>   pathlist;    // nav path
        public String getAreaName(){return this.AreaName;}
        public boolean getMapLocType(){return this.MapLocType;}
        public void setAreaName(String areaName){this.AreaName = areaName;}
        public void setMapLocType(boolean mapLocType){ this.MapLocType = mapLocType;}
    }

    public static class MainMsg{
        public TPoint LP;                    //Location Point
        Integer nArea;
        Integer nFloor;
        //public List<TBlueTooth> Temblist;
        public List<ARegion> AreaList;
        public Integer getnArea(){return this.nArea;}
        public Integer getnFloor(){return this.nFloor;}
        public void setnArea(Integer nArea1){this.nArea = nArea1;}
        public void setnFloor(Integer nFloor1){this.nFloor = nFloor1;}
    }
}
