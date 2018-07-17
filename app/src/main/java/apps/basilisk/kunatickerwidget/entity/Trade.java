package apps.basilisk.kunatickerwidget.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trade {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("volume")
    @Expose
    private String volume;
    @SerializedName("funds")
    @Expose
    private String funds;
    @SerializedName("market")
    @Expose
    private String market;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("side")
    @Expose
    private Object side;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getFunds() {
        return funds;
    }

    public void setFunds(String funds) {
        this.funds = funds;
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

    public Object getSide() {
        return side;
    }

    public void setSide(Object side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", price='" + price + '\'' +
                ", volume='" + volume + '\'' +
                ", funds='" + funds + '\'' +
                ", market='" + market + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", side=" + side +
                '}';
    }
}

