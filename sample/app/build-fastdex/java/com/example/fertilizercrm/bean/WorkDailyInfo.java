package com.example.fertilizercrm.bean;

import java.io.Serializable;

/**
 * Created by tong on 16/1/22.
 * 工作日报信息
 */
public class WorkDailyInfo implements Serializable {

    /**
     * bsoid : 3
     * content : 今年过节不收礼，收集只收脑白金7744
     * createDate : 2016-01-08 15:55:18.0
     * creator : fuckyw
     * ddate : 2016-01-08
     * memo :
     * modifier : fuckyw
     * modifyDate : 2016-01-08 16:03:35.0
     * sbsoid : 10
     * sname : 王小贱
     * status : 0
     * stype : 3
     */

    private int bsoid;
    private String content;
    private String createDate;
    private String creator;
    private String ddate;
    private String memo;
    private String modifier;
    private String modifyDate;
    private int sbsoid;
    private String sname;
    private int status;
    private int stype;

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDdate(String ddate) {
        this.ddate = ddate;
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

    public int getBsoid() {
        return bsoid;
    }

    public String getContent() {
        return content;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getDdate() {
        return ddate;
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
}
