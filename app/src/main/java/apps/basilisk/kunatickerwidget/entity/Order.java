package apps.basilisk.kunatickerwidget.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Order {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("side")
    @Expose
    private String side;
    @SerializedName("ord_type")
    @Expose
    private String ordType;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("avg_price")
    @Expose
    private String avgPrice;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("market")
    @Expose
    private String market;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("volume")
    @Expose
    private String volume;
    @SerializedName("remaining_volume")
    @Expose
    private String remainingVolume;
    @SerializedName("executed_volume")
    @Expose
    private String executedVolume;
    @SerializedName("trades_count")
    @Expose
    private Integer tradesCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getOrdType() {
        return ordType;
    }

    public void setOrdType(String ordType) {
        this.ordType = ordType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(String avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getRemainingVolume() {
        return remainingVolume;
    }

    public void setRemainingVolume(String remainingVolume) {
        this.remainingVolume = remainingVolume;
    }

    public String getExecutedVolume() {
        return executedVolume;
    }

    public void setExecutedVolume(String executedVolume) {
        this.executedVolume = executedVolume;
    }

    public Integer getTradesCount() {
        return tradesCount;
    }

    public void setTradesCount(Integer tradesCount) {
        this.tradesCount = tradesCount;
    }

    public String getFunds() {
        return String.valueOf(Float.parseFloat(price) * Float.parseFloat(volume));
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", side='" + side + '\'' +
                ", ordType='" + ordType + '\'' +
                ", price='" + price + '\'' +
                ", avgPrice='" + avgPrice + '\'' +
                ", state='" + state + '\'' +
                ", market='" + market + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", volume='" + volume + '\'' +
                ", remainingVolume='" + remainingVolume + '\'' +
                ", executedVolume='" + executedVolume + '\'' +
                ", tradesCount=" + tradesCount +
                '}';
    }
}
