package com.example.fertilizercrm.bean;

import java.io.Serializable;

/**
 * Created by tong on 16/1/22.
 * 收款信息
 */
public class Receivable implements Serializable {
    /**
     * amount : 1
     * bsoid : 1
     * createDate : 2016-01-02 15:01:04.0
     * creator : fuck1
     * cuserid : 1
     * cusername : 李老二
     * cusertype : 7
     * memo :
     * modifier :
     * modifyDate :
     * muse : 提货
     * paytype : 现金
     * sbsoid : 0
     * sname :
     * status : 0
     * userid : 1
     * usertype : 5
     */

    private double amount;
    private int bsoid;
    private String createDate;
    private String creator;
    private int cuserid;
    private String cusername;
    private int cusertype;
    private String memo;
    private String modifier;
    private String modifyDate;
    private String muse;
    private String paytype;
    private int sbsoid;
    private String sname;
    private int status;
    private int userid;
    private int usertype;

    public void setAmount(double amount) {
        this.amount = amount;
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

    public void setCuserid(int cuserid) {
        this.cuserid = cuserid;
    }

    public void setCusername(String cusername) {
        this.cusername = cusername;
    }

    public void setCusertype(int cusertype) {
        this.cusertype = cusertype;
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

    public void setMuse(String muse) {
        this.muse = muse;
    }

    public void setPaytype(String paytype) {
        this.paytype = paytype;
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

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public double getAmount() {
        return amount;
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

    public int getCuserid() {
        return cuserid;
    }

    public String getCusername() {
        return cusername;
    }

    public int getCusertype() {
        return cusertype;
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

    public String getMuse() {
        return muse;
    }

    public String getPaytype() {
        return paytype;
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

    public int getUserid() {
        return userid;
    }

    public int getUsertype() {
        return usertype;
    }
}
