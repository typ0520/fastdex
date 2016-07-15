package com.example.fertilizercrm.role;

/**
 * Created by tong on 15/12/7.
 * 肥肥的角色枚举
 */
public enum  Role {
    factory("厂家",5),
    factory_salesman("厂家业务员",3),
    yiji("批发商",1),
    yiji_salesman("批发商业务员",4),
    erji("零售商",2),
    erji_salesman("零售商业务员",6),
    farmer("农民",7);

    //1批发商、2零售商、3厂家业务员，4批发商业务员，5厂家、6零售商业务员   7农民
    private String description;
    private int code;

    Role(String description,int code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public String getCodeStr() {
        return code + "";
    }

    /**
     * 是否是厂家
     * @return
     */
    public boolean isFactory() {
        return this == factory;
    }

    /**
     * 是否是代理商
     * @return
     */
    public boolean isAgent() {
        return this == yiji || this == erji;
    }

    /**
     * 是否是业务员
     * @return
     */
    public boolean isSalesman() {
        if (this == factory_salesman
                || this == yiji_salesman
                || this == erji_salesman) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前角色对应的业务员角色
     * @return
     */
    public Role subRole() {
        if (this == factory) {
            return factory_salesman;
        }
        else if (this == yiji) {
            return yiji_salesman;
        }
        else if (this == erji) {
            return erji_salesman;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Role{" +
                "description='" + description + '\'' +
                ", code=" + code +
                '}';
    }

    public static Role valueOf(int code) {
        for (Role role : values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        return null;
    }

    private static Role currentRole = null;

    public static Role currentRole() {
        return currentRole != null ? currentRole : defaultRole();
    }

    public static void setCurrentRole(Role currentRole) {
        Role.currentRole = currentRole;
    }

    public static Role defaultRole() {
        return Role.yiji;
    }
}
