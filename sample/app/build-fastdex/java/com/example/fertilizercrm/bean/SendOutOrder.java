package com.example.fertilizercrm.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by tong on 15/12/25.
 * 发货单数据
 */
public class SendOutOrder implements Serializable {

    /**
     * amount : 0
     * bsoid : 8
     * channel :
     * character :
     * contacter :
     * countnumber : 1
     * countprice : 0
     * counttype : 1
     * countweight : 1
     * createDate : 2016-01-23 10:00:12.0
     * creator : testyj
     * damaged :
     * expend : 0
     * freight : 0
     * fuserid : 5
     * fusertype : 1
     * incomeamount : 1.8
     * memo :
     * modifier : testyj
     * modifyDate : 2016-01-23 10:22:48.0
     * nowStatus : 2
     * numbers : JH20160123100012227
     * pdate : 2016-01-23
     * phone :
     * pricerole : 送到
     * productdata : [{"amount":0,"brand":"双赢","factory":"双赢化肥厂","format":"50","inaddress":"吉林长春市","num":1,"outaddress":"吉林长春市","pbsoid":4,"picture1":"/attached/1_0/image/product/20160123/20160123083901_226.jpg","picture2":"","picture3":"","pname":"传统肥料","price":0,"saleamount":1.8,"saleprice":1.85}]
     * ptypes : 0
     * saleamount : 1.8
     * status : 0
     * tuserid : 2
     * tusername : 测试厂家
     * tusertype : 5
     * weight : 0
     */

    private double amount;
    private int bsoid;
    private String channel;
    private String character;
    private String contacter;
    private int countnumber;
    private int countprice;
    private int counttype;
    private int countweight;
    private String createDate;
    private String creator;
    private String damaged;
    private int expend;
    private int freight;
    private int fuserid;
    private int fusertype;
    private double incomeamount;
    private String memo;
    private String modifier;
    private String modifyDate;
    private int nowStatus;
    private String numbers;
    private String pdate;
    private String phone;
    private String pricerole;
    private int ptypes;
    private double saleamount;
    private int status;
    private int tuserid;
    private String tusername;
    private int tusertype;
    private int weight;
    /**
     * amount : 0
     * brand : 双赢
     * factory : 双赢化肥厂
     * format : 50
     * inaddress : 吉林长春市
     * num : 1
     * outaddress : 吉林长春市
     * pbsoid : 4
     * picture1 : /attached/1_0/image/product/20160123/20160123083901_226.jpg
     * picture2 :
     * picture3 :
     * pname : 传统肥料
     * price : 0
     * saleamount : 1.8
     * saleprice : 1.85
     */

    private List<ProductdataEntity> productdata;

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public void setContacter(String contacter) {
        this.contacter = contacter;
    }

    public void setCountnumber(int countnumber) {
        this.countnumber = countnumber;
    }

    public void setCountprice(int countprice) {
        this.countprice = countprice;
    }

    public void setCounttype(int counttype) {
        this.counttype = counttype;
    }

    public void setCountweight(int countweight) {
        this.countweight = countweight;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDamaged(String damaged) {
        this.damaged = damaged;
    }

    public void setExpend(int expend) {
        this.expend = expend;
    }

    public void setFreight(int freight) {
        this.freight = freight;
    }

    public void setFuserid(int fuserid) {
        this.fuserid = fuserid;
    }

    public void setFusertype(int fusertype) {
        this.fusertype = fusertype;
    }

    public void setIncomeamount(double incomeamount) {
        this.incomeamount = incomeamount;
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

    public void setNowStatus(int nowStatus) {
        this.nowStatus = nowStatus;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPricerole(String pricerole) {
        this.pricerole = pricerole;
    }

    public void setPtypes(int ptypes) {
        this.ptypes = ptypes;
    }

    public void setSaleamount(double saleamount) {
        this.saleamount = saleamount;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTuserid(int tuserid) {
        this.tuserid = tuserid;
    }

    public void setTusername(String tusername) {
        this.tusername = tusername;
    }

    public void setTusertype(int tusertype) {
        this.tusertype = tusertype;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setProductdata(List<ProductdataEntity> productdata) {
        this.productdata = productdata;
    }

    public double getAmount() {
        return amount;
    }

    public int getBsoid() {
        return bsoid;
    }

    public String getChannel() {
        return channel;
    }

    public String getCharacter() {
        return character;
    }

    public String getContacter() {
        return contacter;
    }

    public int getCountnumber() {
        return countnumber;
    }

    public int getCountprice() {
        return countprice;
    }

    public int getCounttype() {
        return counttype;
    }

    public int getCountweight() {
        return countweight;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getDamaged() {
        return damaged;
    }

    public int getExpend() {
        return expend;
    }

    public int getFreight() {
        return freight;
    }

    public int getFuserid() {
        return fuserid;
    }

    public int getFusertype() {
        return fusertype;
    }

    public double getIncomeamount() {
        return incomeamount;
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

    public int getNowStatus() {
        return nowStatus;
    }

    public String getNumbers() {
        return numbers;
    }

    public String getPdate() {
        return pdate;
    }

    public String getPhone() {
        return phone;
    }

    public String getPricerole() {
        return pricerole;
    }

    public int getPtypes() {
        return ptypes;
    }

    public double getSaleamount() {
        return saleamount;
    }

    public int getStatus() {
        return status;
    }

    public int getTuserid() {
        return tuserid;
    }

    public String getTusername() {
        return tusername;
    }

    public int getTusertype() {
        return tusertype;
    }

    public int getWeight() {
        return weight;
    }

    public List<ProductdataEntity> getProductdata() {
        return productdata;
    }

    public static class ProductdataEntity {
        private BigDecimal amount;
        private String brand;
        private String factory;
        private String format;
        private String inaddress;
        private int num;
        private String outaddress;
        private int pbsoid;
        private String picture1;
        private String picture2;
        private String picture3;
        private String pname;
        private double price;
        private double saleamount;
        private double saleprice;

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public void setInaddress(String inaddress) {
            this.inaddress = inaddress;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public void setOutaddress(String outaddress) {
            this.outaddress = outaddress;
        }

        public void setPbsoid(int pbsoid) {
            this.pbsoid = pbsoid;
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

        public void setPname(String pname) {
            this.pname = pname;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setSaleamount(double saleamount) {
            this.saleamount = saleamount;
        }

        public void setSaleprice(double saleprice) {
            this.saleprice = saleprice;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getBrand() {
            return brand;
        }

        public String getFactory() {
            return factory;
        }

        public String getFormat() {
            return format;
        }

        public String getInaddress() {
            return inaddress;
        }

        public int getNum() {
            return num;
        }

        public String getOutaddress() {
            return outaddress;
        }

        public int getPbsoid() {
            return pbsoid;
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

        public String getPname() {
            return pname;
        }

        public double getPrice() {
            return price;
        }

        public double getSaleamount() {
            return saleamount;
        }

        public double getSaleprice() {
            return saleprice;
        }
    }
}
