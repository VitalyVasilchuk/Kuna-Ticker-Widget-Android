package apps.basilisk.kunatickerwidget.entity;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfferList {

    @SerializedName("timestamp")
    @Expose
    private Integer timestamp;
    @SerializedName("asks")
    @Expose
    private List<List<String>> asks = null;
    @SerializedName("bids")
    @Expose
    private List<List<String>> bids = null;

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public List<List<String>> getAsks() {
        return asks;
    }

    public void setAsks(List<List<String>> asks) {
        this.asks = asks;
    }

    public List<List<String>> getBids() {
        return bids;
    }

    public void setBids(List<List<String>> bids) {
        this.bids = bids;
    }

    @Override
    public String toString() {
        return "OfferList{" +
                "timestamp=" + timestamp +
                ", asks=" + asks +
                ", bids=" + bids +
                '}';
    }
}