package com.sereda.trade.data;

public class BarChartData {
    private int index;
    private String date;
    private double close;

    public BarChartData(int index, String date, double close) {
        this.index = index;
        this.date = date;
        this.close = close;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
