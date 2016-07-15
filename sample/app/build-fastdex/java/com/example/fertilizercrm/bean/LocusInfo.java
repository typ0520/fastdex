package com.example.fertilizercrm.bean;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by tong on 16/1/16.
 * 轨迹信息
 */
public class LocusInfo {
    /**
     * address : 上海市浦东新区Y212(紫薇路)
     * bsoid : 3328
     * createDate : 2016-01-10 21:26:37.0
     * creator : fuckyw
     * latitude : 31.211228
     * longitude : 121.629751
     * memo :
     * modifier :
     * modifyDate :
     * sbsoid : 10
     * sname : 王小贱
     * status : 0
     * stype : 3
     */

    private String address;
    private int bsoid;
    private String createDate;
    private String creator;
    private double latitude;
    private double longitude;
    private String memo;
    private String modifier;
    private String modifyDate;
    private int sbsoid;
    private String sname;
    private int status;
    private int stype;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public void setSbsoid(int sbsoid) {
        this.sbsoid = sbsoid;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStype(int stype) {
        this.stype = stype;
    }

    public String getAddress() {
        return address;
    }

    public int getBsoid() {
        return bsoid;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMemo() {
        return memo;
    }

    public String getModifier() {
        return modifier;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public int getSbsoid() {
        return sbsoid;
    }

    public String getSname() {
        return sname;
    }

    public int getStatus() {
        return status;
    }

    public int getStype() {
        return stype;
    }

    public LatLng getLatLng() {
      return new LatLng(latitude,longitude);
    }
}
