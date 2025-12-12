package com.infosys.forecast.model;

public class TransactionData {
    private Long productId;
    private Double sold;
    private String date;

    public TransactionData() {}

    public TransactionData(Long productId, Double sold, String date) {
        this.productId = productId;
        this.sold = sold;
        this.date = date;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Double getSold() { return sold; }
    public void setSold(Double sold) { this.sold = sold; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
