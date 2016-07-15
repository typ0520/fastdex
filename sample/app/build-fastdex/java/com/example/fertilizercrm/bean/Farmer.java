package com.example.fertilizercrm.bean;

import com.example.fertilizercrm.role.Role;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tong on 15/12/23.
 * 农户信息
 */
public class Farmer implements RoleBean {
    /**
     * address :
     * balance : 0
     * birthday :
     * brand :
     * bsoid : 1
     * city : 长春市
     * cname : 李老二
     * contacter : 李老二
     * createDate : 2015-12-19 14:34:07.0
     * creator : admin
     * ctype :
     * customerplantinfo : [{"allarea":11,"bsoid":1,"cbsoid":1,"crop":"玉米","ownarea":11}]
     * district : 南关区
     * hobby :
     * memo :
     * modifier : admin
     * modifyDate : 2015-12-19 14:34:07.0
     * phone : 13843060712
     * province : 吉林省
     * status : 0
     * town :
     * village :
     */

    private String address;
    private String balance;
    private String birthday;
    private String brand;
    private int bsoid;
    private String city;
    private String cname;
    private String contacter;
    private String createDate;
    private String creator;
    private String ctype;
    private String district;
    private String hobby;
    private String memo;
    private String modifier;
    private String modifyDate;
    private String phone;
    private String province;
    private int status;
    private String town;
    private String village;


    @Override
    public String getMobile() {
        return null;
    }

    @Override
    public String getCompany() {
        return cname;
    }

    @Override
    public String getRoleTypeStr() {
        return Role.farmer.getCode() + "";
    }

    @Override
    public Role getRole() {
        return Role.farmer;
    }

    @Override
    public int getBsoid() {
        return bsoid;
    }

    @Override
    public String getContacter() {
        return contacter;
    }


    /**
     * allarea : 11
     * bsoid : 1
     * cbsoid : 1
     * crop : 玉米
     * ownarea : 11
     */

    private List<CustomerplantinfoEntity> customerplantinfo;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCname(String cname) {
        this.cname = cname;
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

    public void setCtype(String ctype) {
        this.ctype = ctype;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
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

    public void setProvince(String province) {
        this.province = province;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public void setCustomerplantinfo(List<CustomerplantinfoEntity> customerplantinfo) {
        this.customerplantinfo = customerplantinfo;
    }

    public String getAddress() {
        return address;
    }

    public String getBalance() {
        return balance;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getBrand() {
        return brand;
    }

    public String getCity() {
        return city;
    }

    public String getCname() {
        return cname;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getCtype() {
        return ctype;
    }

    public String getDistrict() {
        return district;
    }

    public String getHobby() {
        return hobby;
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

    public String getProvince() {
        return province;
    }

    public int getStatus() {
        return status;
    }

    public String getTown() {
        return town;
    }

    public String getVillage() {
        return village;
    }

    public List<CustomerplantinfoEntity> getCustomerplantinfo() {
        return customerplantinfo;
    }

    public static class CustomerplantinfoEntity implements Serializable {
        private int allarea;
        private int bsoid;
        private int cbsoid;
        private String crop;
        private int ownarea;

        public void setAllarea(int allarea) {
            this.allarea = allarea;
        }

        public void setBsoid(int bsoid) {
            this.bsoid = bsoid;
        }

        public void setCbsoid(int cbsoid) {
            this.cbsoid = cbsoid;
        }

        public void setCrop(String crop) {
            this.crop = crop;
        }

        public void setOwnarea(int ownarea) {
            this.ownarea = ownarea;
        }

        public int getAllarea() {
            return allarea;
        }

        public int getBsoid() {
            return bsoid;
        }

        public int getCbsoid() {
            return cbsoid;
        }

        public String getCrop() {
            return crop;
        }

        public int getOwnarea() {
            return ownarea;
        }
    }

    @Override
    public String toString() {
        return "Farmer{" +
                "address='" + address + '\'' +
                ", balance='" + balance + '\'' +
                ", birthday='" + birthday + '\'' +
                ", brand='" + brand + '\'' +
                ", bsoid=" + bsoid +
                ", city='" + city + '\'' +
                ", cname='" + cname + '\'' +
                ", contacter='" + contacter + '\'' +
                ", createDate='" + createDate + '\'' +
                ", creator='" + creator + '\'' +
                ", ctype='" + ctype + '\'' +
                ", district='" + district + '\'' +
                ", hobby='" + hobby + '\'' +
                ", memo='" + memo + '\'' +
                ", modifier='" + modifier + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", phone='" + phone + '\'' +
                ", province='" + province + '\'' +
                ", status=" + status +
                ", town='" + town + '\'' +
                ", village='" + village + '\'' +
                ", customerplantinfo=" + customerplantinfo +
                '}';
    }
}
