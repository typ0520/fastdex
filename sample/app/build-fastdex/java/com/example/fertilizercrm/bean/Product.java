package com.example.fertilizercrm.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by tong on 15/12/21.
 * 产品信息
 */
public class Product implements Serializable {
    /**
     * brand :
     * bsoid : 1
     * createDate : 2015-12-18 21:47:12.0
     * creator : admin
     * ctname : 喷浆
     * factory : 史丹利
     * format :
     * inprice : 0
     * memo :
     * modifier : admin
     * modifyDate : 2015-12-18 22:00:57.0
     * outprice1 : 0
     * outprice2 : 0
     * picture1 :
     * picture2 :
     * picture3 :
     * pname : 史丹利复合肥
     * ptname : 复合肥
     * ratio : ##
     * status : 0
     * stock : 0
     * tname : 氯基复合肥
     * userid : 1
     * usertype : 5
     */

    private String brand;
    private int bsoid;
    private String createDate;
    private String creator;
    private String ctname;
    private String factory;
    private String format;
    private double inprice;
    private String memo;
    private String modifier;
    private String modifyDate;
    private BigDecimal outprice1;
    private BigDecimal outprice2;
    private String picture1;
    private String picture2;
    private String picture3;
    private String pname;
    private String ptname;
    private String ratio;
    private int status;
    private int stock;
    private String tname;
    private int userid;
    private int usertype;

    //收发货交易用到的数据===
    private String outaddress;
    private String inaddress;
    private String saleprice;
    private int num = 1;
    private PriceType priceType = PriceType.defaultType();

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setBsoid(int bsoid) {
        this.bsoid = bsoid;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCtname(String ctname) {
        this.ctname = ctname;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setInprice(double inprice) {
        this.inprice = inprice;
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

    public void setOutprice1(BigDecimal outprice1) {
        this.outprice1 = outprice1;
    }

    public void setOutprice2(BigDecimal outprice2) {
        this.outprice2 = outprice2;
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

    public void setPtname(String ptname) {
        this.ptname = ptname;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public String getBrand() {
        return brand;
    }

    public int getBsoid() {
        return bsoid;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getCtname() {
        return ctname;
    }

    public String getFactory() {
        return factory;
    }

    public String getFormat() {
        return format;
    }

    public double getInprice() {
        return inprice;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        if (priceEqualsToSaleprice()) {
            this.setSaleprice(this.getFormatPrice(priceType));
        }
        this.priceType = priceType;
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

    public BigDecimal getOutprice1() {
        return outprice1;
    }

    public BigDecimal getOutprice2() {
        return outprice2;
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

    public String getPtname() {
        return ptname;
    }

    public String getRatio() {
        return ratio == null ? "" : ratio.replaceAll("#","-");
    }

    public int getStatus() {
        return status;
    }

    public int getStock() {
        return stock;
    }

    public String getTname() {
        return tname;
    }

    public int getUserid() {
        return userid;
    }

    public int getUsertype() {
        return usertype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return bsoid == product.bsoid;
    }

    @Override
    public int hashCode() {
        return bsoid;
    }

    public String getOutaddress() {
        return outaddress;
    }

    public void setOutaddress(String outaddress) {
        this.outaddress = outaddress;
    }

    public String getInaddress() {
        return inaddress;
    }

    public void setInaddress(String inaddress) {
        this.inaddress = inaddress;
    }

    public String getSaleprice() {
        return saleprice;
    }

    public void setSaleprice(String saleprice) {
        this.saleprice = saleprice;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    /**
     * 获取定价
     * @param priceType
     * @return
     */
    public BigDecimal getPrice(PriceType priceType) {
        if (priceType == PriceType.DELIVERY) {
            return outprice1;
        }
        if (priceType == PriceType.PICKUP) {
            return outprice2;
        }
        return null;
    }

    public BigDecimal getPrice() {
        return getPrice(priceType);
    }

    /**
     * 获取格式化的定价
     * @param priceType
     * @return
     */
    public String getFormatPrice(PriceType priceType) {
        BigDecimal result = getPrice(priceType);
        if (result != null) {
            return new DecimalFormat("0.00").format(result);
        }
        return "0.00";
    }

    public String getFormatPrice() {
        return getFormatPrice(this.priceType);
    }

    /**
     * 计算应收定价  数量 x 定价
     * @param priceType
     * @return
     */
    public BigDecimal getTotalPrice(PriceType priceType) {
        BigDecimal result = BigDecimal.ZERO;
        try {
            BigDecimal price = getPrice(priceType);
            if (price != null) {
                BigDecimal quantit = new BigDecimal(this.getNum());
                result = price.multiply(quantit);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public BigDecimal getTotalPrice() {
        return this.getTotalPrice(this.priceType);
    }

    public String getFormatTotalPrice() {
        BigDecimal result = this.getTotalPrice();
        if (result != null) {
            return new DecimalFormat("0.00").format(result);
        }
        return "0.00";
    }
//
//    //计算应收总价  数量 x 定价
//    public String getTotalInprice() {
//        DecimalFormat decimalFormat = new DecimalFormat("0.0");
//        BigDecimal result = BigDecimal.ZERO;
//        try {
//            BigDecimal price = new BigDecimal(this.getInprice());
//            BigDecimal quantit = new BigDecimal(this.getNum());
//            result = price.multiply(quantit);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        return decimalFormat.format(result);
//    }

    //计算销售总额
    public String getTotalSaleprice() {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        BigDecimal result = BigDecimal.ZERO;
        try {
            BigDecimal price = new BigDecimal(getSaleprice());
            BigDecimal quantit = new BigDecimal(getNum());
            result = price.multiply(quantit);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return decimalFormat.format(result);
    }

    /**
     * 定价和售价是否相等
     * @return
     */
    public boolean priceEqualsToSaleprice() {
        if (this.getFormatPrice() != null
                && this.getSaleprice() != null
                && this.getFormatPrice().equals(this.getSaleprice())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Product{" +
                "brand='" + brand + '\'' +
                ", bsoid=" + bsoid +
                ", createDate='" + createDate + '\'' +
                ", creator='" + creator + '\'' +
                ", ctname='" + ctname + '\'' +
                ", factory='" + factory + '\'' +
                ", format='" + format + '\'' +
                ", inprice=" + inprice +
                ", memo='" + memo + '\'' +
                ", modifier='" + modifier + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", outprice1=" + outprice1 +
                ", outprice2=" + outprice2 +
                ", picture1='" + picture1 + '\'' +
                ", picture2='" + picture2 + '\'' +
                ", picture3='" + picture3 + '\'' +
                ", pname='" + pname + '\'' +
                ", ptname='" + ptname + '\'' +
                ", ratio='" + ratio + '\'' +
                ", status=" + status +
                ", stock=" + stock +
                ", tname='" + tname + '\'' +
                ", userid=" + userid +
                ", usertype=" + usertype +
                ", outaddress='" + outaddress + '\'' +
                ", inaddress='" + inaddress + '\'' +
                ", saleprice='" + saleprice + '\'' +
                ", num=" + num +
                '}';
    }
}
