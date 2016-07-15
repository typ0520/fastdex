package com.example.fertilizercrm.bean;

import java.io.Serializable;

/**
 * Created by tong on 15/10/18.
 * 省市区信息
 */
public class AreaInfo implements Serializable {
    private int provinceId;
    private String provinceName;

    private int cityId;
    private String cityName;

    private int areaId;
    private String areaName;

    private int bsoid;

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String joinParams() {
        return getAreaName();
    }

    public int getBsoid() {
        return bsoid;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    @Override
    public String toString() {
        return "AreaInfo{" +
                "provinceId=" + provinceId +
                ", provinceName='" + provinceName + '\'' +
                ", cityId=" + cityId +
                ", cityName='" + cityName + '\'' +
                ", areaId=" + areaId +
                ", areaName='" + areaName + '\'' +
                ", bsoid=" + bsoid +
                '}';
    }
}
