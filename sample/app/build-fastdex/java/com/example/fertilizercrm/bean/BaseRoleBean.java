package com.example.fertilizercrm.bean;

/**
 * Created by tong on 15/12/24.
 */
public abstract class BaseRoleBean implements RoleBean {
    @Override
    public int getBsoid() {
        return -1;
    }

    @Override
    public String getContacter() {
        return null;
    }

    @Override
    public String getBalance() {
        return null;
    }

    @Override
    public String getMobile() {
        return null;
    }

    @Override
    public String getCompany() {
        return "";
    }
}
