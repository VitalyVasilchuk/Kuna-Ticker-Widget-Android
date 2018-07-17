package apps.basilisk.kunatickerwidget.entity;

import android.text.format.DateFormat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class TickerList {

    @SerializedName("at")
    @Expose
    private Integer at;
    @SerializedName("ticker")
    @Expose
    private Ticker ticker;

    public Integer getAt() {
        return at;
    }

    public void setAt(Integer at) {
        this.at = at;
    }

    public String getAt(String format) {
        return (String) DateFormat.format(format, (at*1000L));
    }

    public String getFormattedAt() {
        return getAt("dd.MM.yyyy HH:mm:ss");
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

}