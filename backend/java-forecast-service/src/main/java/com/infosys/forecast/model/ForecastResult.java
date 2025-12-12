package com.infosys.forecast.model;

public class ForecastResult {
    private Long productId;
    private Double predicted;
    private String forecastDate;

    public ForecastResult() {}

    public ForecastResult(Long productId, Double predicted, String forecastDate) {
        this.productId = productId;
        this.predicted = predicted;
        this.forecastDate = forecastDate;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Double getPredicted() { return predicted; }
    public void setPredicted(Double predicted) { this.predicted = predicted; }

    public String getForecastDate() { return forecastDate; }
    public void setForecastDate(String forecastDate) { this.forecastDate = forecastDate; }
}
