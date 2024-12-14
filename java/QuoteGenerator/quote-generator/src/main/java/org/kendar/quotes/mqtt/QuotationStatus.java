package org.kendar.quotes.mqtt;

public class QuotationStatus {
    private String symbol;
    private double price;
    private int volume;

    public QuotationStatus(String symbol, double price, int volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "QuotationStatus{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", volume=" + volume +
                '}';
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
