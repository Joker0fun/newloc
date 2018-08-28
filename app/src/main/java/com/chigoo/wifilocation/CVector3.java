package com.chigoo.wifilocation;

/**
 * Created by Administrator on 2018/8/2.
 */
public class CVector3{

    private double Vx = 0;
    private double Vy = 0;
    private double Vz = 0;

    public CVector3(){}

    public CVector3(double vx ,double vy, double vz){
        this.Vx = vx;
        this.Vy = vy;
        this.Vz = vz;
    }

    public double getVx() {
        return Vx;
    }

    public double getVy() {
        return Vy;
    }

    public double getVz() {
        return Vz;
    }

    public void setVx(double vx) {
        Vx = vx;
    }

    public void setVy(double vy) {
        Vy = vy;
    }

    public void setVz(double vz) {
        Vz = vz;
    }


}
