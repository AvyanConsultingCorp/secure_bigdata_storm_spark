package com.microsoft.example;

class Order {
    private int productid = 0;
    private int quantity = 0;
    private int sales = 0;
    private int refund = 0;
    private String orderdate="";

    public Order(Integer _productid, Integer _quantity, Integer _sales, Integer _refund, String _orderdate ) {
        super();
        this.productid = _productid;
        this.quantity = _quantity;
        this.sales = _sales;
        this.refund = _refund * -1;
        this.orderdate = _orderdate;
    }

    public Integer getproductid() {
        return productid;
    }

    public Integer getquantity() {
        return quantity;
    }

    public Integer getsales() {
        return sales;
    }

    public Integer getrefund() {
        return refund;
    }

    public String getorderdate() {
        return orderdate;
    }

    public void setproductid(Integer productid) {
        this.productid = productid;
    }

    public void setquantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setsales(Integer sales) {
        this.sales = sales;
    }

    public void setrefund(Integer refund) {
        this.refund = refund;
    }

    public void setorderdate(String orderdate) {
        this.orderdate = orderdate;
    }

}
