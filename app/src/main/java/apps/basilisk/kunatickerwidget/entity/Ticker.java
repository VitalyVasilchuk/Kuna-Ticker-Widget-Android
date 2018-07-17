package apps.basilisk.kunatickerwidget.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ticker {
    // Таблица
    public static final String TABLE = "ticker";
    // Колонки таблицы
    public static final String COL_ID = "_id";
    public static final String COL_TIMESTAMP = "timestamp";
    /*public static final String COL_CURR_PAIR = "currency_pair";*/
    public static final String COL_CURR_TRADE = "currency_trade";
    public static final String COL_CURR_BASE = "currency_base";
    public static final String COL_BUY = "buy";
    public static final String COL_SELL = "sell";
    public static final String COL_LAST = "last";
    public static final String COL_LOW = "low";
    public static final String COL_HIGH = "high";
    public static final String COL_VOL = "vol";
    public static final String COL_PRICE = "price";

    // поля сущности
    private long id;
    private int timestamp;
    /*private String currencyPair;*/
    private String currencyTrade;
    private String currencyBase;

    // поля сущности из JSON
    @SerializedName("buy")
    @Expose
    private String buy;
    @SerializedName("sell")
    @Expose
    private String sell;
    @SerializedName("low")
    @Expose
    private String low;
    @SerializedName("high")
    @Expose
    private String high;
    @SerializedName("last")
    @Expose
    private String last;
    @SerializedName("vol")
    @Expose
    private String vol;
    @SerializedName("price")
    @Expose
    private String price;

    public Ticker(String buy, String sell, String last, String low, String high, String vol, String price) {
        this.buy = buy;
        this.sell = sell;
        this.last = last;
        this.low = low;
        this.high = high;
        this.vol = vol;
        this.price = price;
    }

    public Ticker(long id, int timestamp, String currencyTrade, String currencyBase,
                  String buy, String sell, String low, String high, String last, String vol, String price) {
        this.id = id;
        this.timestamp = timestamp;
        this.currencyTrade = currencyTrade;
        this.currencyBase = currencyBase;
        this.buy = buy;
        this.sell = sell;
        this.low = low;
        this.high = high;
        this.last = last;
        this.vol = vol;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrencyPair(char divider) {
        return currencyTrade + divider + currencyBase;
    }

    public String getCurrencyPair() {
        return currencyTrade + currencyBase;
    }

    public String getCurrencyTrade() {
        return currencyTrade;
    }

    public void setCurrencyTrade(String currencyTrade) {
        this.currencyTrade = currencyTrade;
    }

    public String getCurrencyBase() {
        return currencyBase;
    }

    public void setCurrencyBase(String currencyBase) {
        this.currencyBase = currencyBase;
    }

    public String getBuy() {
        return (buy + "0000000000").substring(0, 10);
    }

    public void setBuy(String buy) {
        this.buy = buy;
    }

    public String getSell() {
        return (sell + "0000000000").substring(0, 10);
    }

    public void setSell(String sell) {
        this.sell = sell;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getVol() {
        return vol;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Ticker{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", currencyTrade='" + currencyTrade + '\'' +
                ", currencyBase='" + currencyBase + '\'' +
                ", buy='" + buy + '\'' +
                ", sell='" + sell + '\'' +
                ", low='" + low + '\'' +
                ", high='" + high + '\'' +
                ", last='" + last + '\'' +
                ", vol='" + vol + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}