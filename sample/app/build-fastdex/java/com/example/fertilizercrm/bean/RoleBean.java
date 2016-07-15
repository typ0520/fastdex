package com.example.fertilizercrm.bean;

import com.example.fertilizercrm.role.Role;

import java.io.Serializable;

/**
 * Created by tong on 15/12/23.
 * 角色数据bean需要实现这个接口
 */
public interface RoleBean extends Serializable {

    int getBsoid();

    String getContacter();

    String getBalance();

    String getMobile();

    String getCompany();

    String getRoleTypeStr();

    Role getRole();
}
