package com.example.fertilizercrm.bean;

import android.text.TextUtils;

import com.example.fertilizercrm.utils.FerUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tong on 16/1/8.
 * 拜访记录信息
 */
public class VisitInfo implements Serializable {

    /**
     * bsoid : 54
     * content : ������
     * createDate : 2016-01-22 16:49:34.0
     * creator : 10
     * fuserid : 1
     * fusername : 李老二
     * fusertype : 7
     * latitude : 31.211197
     * longitude : 121.629793
     * memo :
     * modifier : fuckyw
     * modifyDate : 2016-01-22 16:49:34.0
     * picture1 : /attached/10_3/image/visit/201601/20160122164934_54.png
     * picture2 : /attached/10_3/image/visit/201601/20160122164934_314.png
     * picture3 :
     * picture4 :
     * picture5 :
     * status : 0
     * userid : 10
     * username : 王小贱
     * usertype : 3
     * visitDate : 2016-01-22
     */

    private int bsoid;
    private String content;
    private String createDate;
    private String creator;
    private int fuserid;
    private String fusername;
    private int fusertype;
    private String latitude;
    private String longitude;
    private String memo;
    private String modifier;
    private String modifyDate;
    private String picture1;
    private String picture2;
    private String picture3;
    private String picture4;
    private String picture5;
    private int status;
    private int userid;
    private String username;
    private int usertype;
    private String visitDate;

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

    public void setFuserid(int fuserid) {
        this.fuserid = fuserid;
    }

    public void setFusername(String fusername) {
        this.fusername = fusername;
    }

    public void setFusertype(int fusertype) {
        this.fusertype = fusertype;
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

    public void setPicture1(String picture1) {
        this.picture1 = picture1;
    }

    public void setPicture2(String picture2) {
        this.picture2 = picture2;
    }

    public void setPicture3(String picture3) {
        this.picture3 = picture3;
    }

    public void setPicture4(String picture4) {
        this.picture4 = picture4;
    }

    public void setPicture5(String picture5) {
        this.picture5 = picture5;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
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

    public int getFuserid() {
        return fuserid;
    }

    public String getFusername() {
        return fusername;
    }

    public int getFusertype() {
        return fusertype;
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

    public String getPicture1() {
        return picture1;
    }

    public String getPicture2() {
        return picture2;
    }

    public String getPicture3() {
        return picture3;
    }

    public String getPicture4() {
        return picture4;
    }

    public String getPicture5() {
        return picture5;
    }

    public int getStatus() {
        return status;
    }

    public int getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public int getUsertype() {
        return usertype;
    }

    public String getVisitDate() {
        return visitDate;
    }


    public List<String> getImageUrls() {
        List<String> imageList = new ArrayList<String>();

        if (!TextUtils.isEmpty(picture1)) {
            imageList.add(FerUtil.getImageUrl(picture1));
        }
        if (!TextUtils.isEmpty(picture2)) {
            imageList.add(FerUtil.getImageUrl(picture2));
        }
        if (!TextUtils.isEmpty(picture3)) {
            imageList.add(FerUtil.getImageUrl(picture3));
        }
        if (!TextUtils.isEmpty(picture4)) {
            imageList.add(FerUtil.getImageUrl(picture4));
        }
        if (!TextUtils.isEmpty(picture5)) {
            imageList.add(FerUtil.getImageUrl(picture5));
        }
        return imageList;
    }
    @Override
    public String toString() {
        return "VisitInfo{" +
                "bsoid=" + bsoid +
                ", content='" + content + '\'' +
                ", createDate='" + createDate + '\'' +
                ", creator='" + creator + '\'' +
                ", fuserid=" + fuserid +
                ", fusername='" + fusername + '\'' +
                ", fusertype=" + fusertype +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", memo='" + memo + '\'' +
                ", modifier='" + modifier + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", picture1='" + picture1 + '\'' +
                ", picture2='" + picture2 + '\'' +
                ", picture3='" + picture3 + '\'' +
                ", picture4='" + picture4 + '\'' +
                ", picture5='" + picture5 + '\'' +
                ", status=" + status +
                ", userid=" + userid +
                ", username='" + username + '\'' +
                ", usertype=" + usertype +
                ", visitDate='" + visitDate + '\'' +
                '}';
    }
}
