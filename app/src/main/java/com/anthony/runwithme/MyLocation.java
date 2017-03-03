package com.anthony.runwithme;

import com.baidu.location.Poi;

import java.util.List;

/**
 * Created by asus on 2016/12/1.
 */

public class MyLocation {
    //定位时间
    String time;
    //返回的状态码
    int errorcode;
    //纬度
    double latitude;
    //经度
    double lontitude;
    //半径
    float radius;

    //地址
    String addr;
    //describe
    String describe;
    //位置语义化信息
    String locationdescribe;

    List<Poi> list;

    public MyLocation() {
    }

    public MyLocation(String addr, String describe, int errorcode, double latitude, List<Poi> list, String locationdescribe, double lontitude, float radius, String time)
    {
        this.addr = addr;
        this.describe = describe;
        this.errorcode = errorcode;
        this.latitude = latitude;
        this.list = list;
        this.locationdescribe = locationdescribe;
        this.lontitude = lontitude;
        this.radius = radius;
        this.time = time;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public List<Poi> getList() {
        return list;
    }

    public void setList(List<Poi> list) {
        this.list = list;
    }

    public String getLocationdescribe() {
        return locationdescribe;
    }

    public void setLocationdescribe(String locationdescribe) {
        this.locationdescribe = locationdescribe;
    }

    public double getLongitude() {
        return lontitude;
    }

    public void setLontitude(double lontitude) {
        this.lontitude = lontitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MyLocation{" +
                "addr='" + addr + '\'' +
                ", time='" + time + '\'' +
                ", errorcode=" + errorcode +
                ", latitude=" + latitude +
                ", lontitude=" + lontitude +
                ", radius=" + radius +
                ", describe='" + describe + '\'' +
                ", locationdescribe='" + locationdescribe + '\'' +
                ", list=" + list +
                '}';
    }
}
