package com.example.fertilizercrm.bean;

import java.io.Serializable;

/**
 * Created by tong on 16/1/22.
 * 签到信息
 */
public class SignInfo implements Serializable {
    /**
     * address : 上海市浦东新区Y212(紫薇路)
     * bsoid : 23
     * createDate : 2016-01-22 15:49:29.0
     * creator : fuckyw
     * latitude : 31.211231
     * longitude : 121.62983
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
    private String latitude;
    private String longitude;
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

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
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

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
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

    public String getProvince() {
        String result = "";
        try {
            if (address.indexOf("省") != -1) {
                result = address.substring(0,address.indexOf("省") + 1);
            }
            else if (address.indexOf("市") != -1) {
                result = address.substring(0,address.indexOf("市") + 1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getCity() {
        String result = "";
        try {
            if (address.indexOf("省") != -1) {
                if (address.indexOf("市") != -1) {
                    result = address.substring(address.indexOf("省") + 1,address.indexOf("市") + 1);
                }
            }
            else if (address.indexOf("市") != -1) {
                if (address.indexOf("区") != -1) {
                    result = address.substring(address.indexOf("市") + 1,address.indexOf("区") + 1);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }
}
