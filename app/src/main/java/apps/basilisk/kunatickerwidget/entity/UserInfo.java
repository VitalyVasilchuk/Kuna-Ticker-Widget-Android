package apps.basilisk.kunatickerwidget.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserInfo {

    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("activated")
    @Expose
    private Boolean activated;
    @SerializedName("accounts")
    @Expose
    private List<Account> accounts = null;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "email='" + email + '\'' +
                ", activated=" + activated +
                ", accounts=" + accounts +
                '}';
    }
}
