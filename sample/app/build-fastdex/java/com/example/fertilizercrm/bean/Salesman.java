package com.example.fertilizercrm.bean;

import com.example.fertilizercrm.role.Role;

/**
 * Created by tong on 16/1/11.
 * 业务员
 */
public class Salesman implements RoleBean {

    /**
     * brand :
     * bsoid : 10
     * coverrange1 :
     * coverrange2 :
     * coverrange3 :
     * createDate : 2016-01-01 09:40:17.0
     * creator : fuck1
     * customerlist : 1#7
     * hicon :
     * memo :
     * modifier : fuck1
     * modifyDate : 2016-01-05 11:40:06.0
     * phone : 15678912358
     * productlist : 2
     * rolecodes :
     * sname : 王小贱
     * status : 0
     * supertype : 5
     * suserid : 1
     */

    private String brand;
    private int bsoid;
    private String coverrange1;
    private String coverrange2;
    private String coverrange3;
    private String createDate;
    private String creator;
    private String customerlist;
    private String hicon;
    private String memo;
    private String modifier;
    private String modifyDate;
    private String phone;
    private String productlist;
    private String rolecodes;
    private String sname;
    private int status;
    private int supertype;
    private int suserid;

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setCoverrange1(String coverrange1) {
        this.coverrange1 = coverrange1;
    }

    public void setCoverrange2(String coverrange2) {
        this.coverrange2 = coverrange2;
    }

    public void setCoverrange3(String coverrange3) {
        this.coverrange3 = coverrange3;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCustomerlist(String customerlist) {
        this.customerlist = customerlist;
    }

    public void setHicon(String hicon) {
        this.hicon = hicon;
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

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProductlist(String productlist) {
        this.productlist = productlist;
    }

    public void setRolecodes(String rolecodes) {
        this.rolecodes = rolecodes;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSupertype(int supertype) {
        this.supertype = supertype;
    }

    public void setSuserid(int suserid) {
        this.suserid = suserid;
    }

    public String getBrand() {
        return brand;
    }

    public int getBsoid() {
        return bsoid;
    }

    @Override
    public String getContacter() {
        return getSname();
    }

    @Override
    public String getBalance() {
        return "";
    }

    @Override
    public String getMobile() {
        return phone;
    }

    @Override
    public String getCompany() {
        return sname;
    }

    @Override
    public String getRoleTypeStr() {
        if (getRole() != null) {
            return getRole().getCodeStr();
        }
        return "";
    }

    @Override
    public Role getRole() {
        return Role.currentRole().subRole();
    }

    public String getCoverrange1() {
        return coverrange1;
    }

    public String getCoverrange2() {
        return coverrange2;
    }

    public String getCoverrange3() {
        return coverrange3;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getCustomerlist() {
        return customerlist;
    }

    public String getHicon() {
        return hicon;
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

    public String getPhone() {
        return phone;
    }

    public String getProductlist() {
        return productlist;
    }

    public String getRolecodes() {
        return rolecodes;
    }

    public String getSname() {
        return sname;
    }

    public int getStatus() {
        return status;
    }

    public int getSupertype() {
        return supertype;
    }

    public int getSuserid() {
        return suserid;
    }

    @Override
    public String toString() {
        return "Salesman{" +
                "brand='" + brand + '\'' +
                ", bsoid=" + bsoid +
                ", coverrange1='" + coverrange1 + '\'' +
                ", coverrange2='" + coverrange2 + '\'' +
                ", coverrange3='" + coverrange3 + '\'' +
                ", createDate='" + createDate + '\'' +
                ", creator='" + creator + '\'' +
                ", customerlist='" + customerlist + '\'' +
                ", hicon='" + hicon + '\'' +
                ", memo='" + memo + '\'' +
                ", modifier='" + modifier + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", phone='" + phone + '\'' +
                ", productlist='" + productlist + '\'' +
                ", rolecodes='" + rolecodes + '\'' +
                ", sname='" + sname + '\'' +
                ", status=" + status +
                ", supertype=" + supertype +
                ", suserid=" + suserid +
                '}';
    }
}
