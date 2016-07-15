package com.example.fertilizercrm.bean;

import com.example.fertilizercrm.role.Role;

/**
 * Created by tong on 16/2/20.
 */
public class OutsideSystemChannel extends BaseRoleBean {
    private int bsoid;
    private String name;
    private String mobile;
    private String contacter;
    private boolean select;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String getContacter() {
        return contacter;
    }

    @Override
    public String getRoleTypeStr() {
        return null;
    }

    @Override
    public Role getRole() {
        return null;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }


    public void setContacter(String contacter) {
        this.contacter = contacter;
    }

    @Override
    public int getBsoid() {
        return bsoid;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    @Override
    public String toString() {
        return "OutsideSystemChannel{" +
                "bsoid=" + bsoid +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", contacter='" + contacter + '\'' +
                ", select=" + select +
                '}';
    }
}
