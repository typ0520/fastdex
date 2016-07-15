package com.example.fertilizercrm.bean;

import com.example.fertilizercrm.role.Role;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tong on 15/12/22.
 * 厂家信息
 */
public class Factory extends BaseRoleBean {
    /**
     * address :
     * bsoid : 1
     * city : 长春市
     * code :
     * contacter : 史丹
     * createDate : 2015-12-15 14:48:39.0
     * creator : admin
     * district : 南关区
     * fax :
     * hicon :
     * introduction : 史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利史丹利
     * latitude :
     * longitude :
     * memo :
     * mobile : 13222222222
     * modifier : admin
     * modifyDate : 2015-12-15 14:48:39.0
     * phone : 0431-88888888
     * products :
     * province : 吉林省
     * sname : 史丹利
     * status : 0
     * supplierspetinfo : [{"bsoid":1,"capacity":11,"output":11,"ptype":"磷铵","sbsoid":1,"tech":"传统"}]
     * town :
     */

    private String address;
    private int bsoid;
    private String city;
    private String code;
    private String contacter;
    private String createDate;
    private String creator;
    private String district;
    private String fax;
    private String hicon;
    private String introduction;
    private String latitude;
    private String longitude;
    private String memo;
    private String mobile;
    private String modifier;
    private String modifyDate;
    private String phone;
    private String products;
    private String province;
    private String sname;
    private int status;
    private String town;
    private String balance;
    /**
     * bsoid : 1
     * capacity : 11
     * output : 11
     * ptype : 磷铵
     * sbsoid : 1
     * tech : 传统
     */

    private List<SupplierspetinfoEntity> supplierspetinfo;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setContacter(String contacter) {
        this.contacter = contacter;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setHicon(String hicon) {
        this.hicon = hicon;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
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

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public void setProducts(String products) {
        this.products = products;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setSupplierspetinfo(List<SupplierspetinfoEntity> supplierspetinfo) {
        this.supplierspetinfo = supplierspetinfo;
    }

    public String getAddress() {
        return address;
    }

    public int getBsoid() {
        return bsoid;
    }

    public String getCity() {
        return city;
    }

    public String getCode() {
        return code;
    }

    public String getContacter() {
        return contacter;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    @Override
    public String getBalance() {
        return balance;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getDistrict() {
        return district;
    }

    public String getFax() {
        return fax;
    }

    public String getHicon() {
        return hicon;
    }

    public String getIntroduction() {
        return introduction;
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

    public String getMobile() {
        return mobile;
    }

    @Override
    public String getRoleTypeStr() {
        return Role.factory.getCode() + "";
    }

    @Override
    public Role getRole() {
        return Role.factory;
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

    public String getProducts() {
        return products;
    }

    public String getProvince() {
        return province;
    }

    public String getSname() {
        return sname;
    }

    public int getStatus() {
        return status;
    }

    public String getTown() {
        return town;
    }

    public List<SupplierspetinfoEntity> getSupplierspetinfo() {
        return supplierspetinfo;
    }

    @Override
    public String toString() {
        return "Factory{" +
                "address='" + address + '\'' +
                ", bsoid=" + bsoid +
                ", city='" + city + '\'' +
                ", code='" + code + '\'' +
                ", contacter='" + contacter + '\'' +
                ", createDate='" + createDate + '\'' +
                ", creator='" + creator + '\'' +
                ", district='" + district + '\'' +
                ", fax='" + fax + '\'' +
                ", hicon='" + hicon + '\'' +
                ", introduction='" + introduction + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", memo='" + memo + '\'' +
                ", mobile='" + mobile + '\'' +
                ", modifier='" + modifier + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", phone='" + phone + '\'' +
                ", products='" + products + '\'' +
                ", province='" + province + '\'' +
                ", sname='" + sname + '\'' +
                ", status=" + status +
                ", town='" + town + '\'' +
                ", supplierspetinfo=" + supplierspetinfo +
                '}';
    }

    public static class SupplierspetinfoEntity implements Serializable {
        private int bsoid;
        private int capacity;
        private int output;
        private String ptype;
        private int sbsoid;
        private String tech;

        public void setBsoid(int bsoid) {
            this.bsoid = bsoid;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public void setOutput(int output) {
            this.output = output;
        }

        public void setPtype(String ptype) {
            this.ptype = ptype;
        }

        public void setSbsoid(int sbsoid) {
            this.sbsoid = sbsoid;
        }

        public void setTech(String tech) {
            this.tech = tech;
        }

        public int getBsoid() {
            return bsoid;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getOutput() {
            return output;
        }

        public String getPtype() {
            return ptype;
        }

        public int getSbsoid() {
            return sbsoid;
        }

        public String getTech() {
            return tech;
        }
    }
}
