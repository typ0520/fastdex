package com.example.fertilizercrm.bean;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by tong on 15/12/19.
 * 登陆接口返回信息
 */
public class LoginResponse implements Serializable {


    /**
     * hpassword : 111111
     * mCompany : 二级代理
     * mID : 2
     * mImageURL :
     * mPercent :
     * mPhone : 18956786340
     * mUerType : 2
     * mUserName : 王二级
     * mtodo : 0
     * token : erji
     */

    private String hpassword;
    private String mCompany;
    private String mID;
    private String mImageURL;
    private String mPercent;
    private String mPhone;
    private String mUerType;
    private String mUserName;
    private int mtodo;
    private String token;
    private JSONObject userinfo;

    public void setHpassword(String hpassword) {
        this.hpassword = hpassword;
    }

    public void setMCompany(String mCompany) {
        this.mCompany = mCompany;
    }

    public void setMID(String mID) {
        this.mID = mID;
    }

    public void setMImageURL(String mImageURL) {
        this.mImageURL = mImageURL;
    }

    public void setMPercent(String mPercent) {
        this.mPercent = mPercent;
    }

    public void setMPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public void setMUerType(String mUerType) {
        this.mUerType = mUerType;
    }

    public void setMUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public void setMtodo(int mtodo) {
        this.mtodo = mtodo;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getHpassword() {
        return hpassword;
    }

    public String getMCompany() {
        return mCompany;
    }

    public String getMID() {
        return mID;
    }

    public String getMImageURL() {
        return mImageURL;
    }

    public double getMPercent() {
        double result = 0.d;
        try {
            result = Double.valueOf(mPercent);
        } catch (Throwable e) {

        }
        return result;
    }

    public String getMPhone() {
        return mPhone;
    }

    public String getMUerType() {
        return mUerType;
    }

    public String getMUserName() {
        return mUserName;
    }

    public int getMtodo() {
        return mtodo;
    }

    public String getToken() {
        return token;
    }

    public JSONObject getUserinfo() {
        return userinfo;
    }

    public void setUserinfo(JSONObject userinfo) {
        this.userinfo = userinfo;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "hpassword='" + hpassword + '\'' +
                ", mCompany='" + mCompany + '\'' +
                ", mID='" + mID + '\'' +
                ", mImageURL='" + mImageURL + '\'' +
                ", mPercent='" + mPercent + '\'' +
                ", mPhone='" + mPhone + '\'' +
                ", mUerType='" + mUerType + '\'' +
                ", mUserName='" + mUserName + '\'' +
                ", mtodo=" + mtodo +
                ", token='" + token + '\'' +
                ", message=" + userinfo +
                '}';
    }
}
