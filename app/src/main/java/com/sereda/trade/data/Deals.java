package com.sereda.trade.data;

public class Deals {
    private String symbol;
    private int id;
    private int dealID;
    private String startAt;
    private String endAt;
    private boolean expirationIsFixed;
    private String duration;
    private double payMatch;
    private double payNoMatch;
    private double minStake;
    private double maxStake;

    public Deals(String symbol, int id, int dealID, String startAt, String endAt,
                 boolean expirationIsFixed, String duration, double payMatch,
                 double payNoMatch, double minStake, double maxStake) {
        this.symbol = symbol;
        this.dealID = dealID;
        this.startAt = startAt;
        this.endAt = endAt;
        this.expirationIsFixed = expirationIsFixed;
        this.duration = duration;
        this.payMatch = payMatch;
        this.payNoMatch = payNoMatch;
        this.minStake = minStake;
        this.maxStake = maxStake;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDealID() {
        return dealID;
    }

    public void setDealID(int dealID) {
        this.dealID = dealID;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public boolean isExpirationIsFixed() {
        return expirationIsFixed;
    }

    public void setExpirationIsFixed(boolean expirationIsFixed) {
        this.expirationIsFixed = expirationIsFixed;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPayMatch() {
        return payMatch;
    }

    public void setPayMatch(double payMatch) {
        this.payMatch = payMatch;
    }

    public double getPayNoMatch() {
        return payNoMatch;
    }

    public void setPayNoMatch(double payNoMatch) {
        this.payNoMatch = payNoMatch;
    }

    public double getMinStake() {
        return minStake;
    }

    public void setMinStake(double minStake) {
        this.minStake = minStake;
    }

    public double getMaxStake() {
        return maxStake;
    }

    public void setMaxStake(double maxStake) {
        this.maxStake = maxStake;
    }
}
