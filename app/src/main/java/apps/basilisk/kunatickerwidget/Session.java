package apps.basilisk.kunatickerwidget;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import apps.basilisk.kunatickerwidget.activity.SettingsActivity;
import apps.basilisk.kunatickerwidget.api.NbuService;
import apps.basilisk.kunatickerwidget.entity.RateNbu;
import apps.basilisk.kunatickerwidget.tools.Enigma;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Session {
    private static final String TAG = "Session";

    public static final String APP_PREF_PUBLIC_KEY = "public_key";
    public static final String APP_PREF_PRIVATE_KEY = "private_key";
    public static final String APP_PREF_PASSWORD_SALT = "password_salt";
    public static final String APP_PREF_PASSWORD_HASH = "password_hash";
    public static final String APP_PREF_TRIAL_DATE_END = "trial_date_end";

    private static Session instance;
    private String passwordValue;
    private boolean privateApiPaid;

    private Session() {
        passwordValue = "";
        privateApiPaid = false;

        // обработка первого запуска приложения
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        if (sharedPref.getBoolean("first_run", true)) {
            try {
                // генерация ключей в хранилище
                Enigma.generateSecret(App.getAppContext());
                sharedPref.edit().putBoolean("first_run", false).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // запрос курса НБУ для рынка USD/UAH
        Call<RateNbu[]> call = NbuService.Factory.getExchangeRate("USD");
        call.enqueue(new Callback<RateNbu[]>() {
            @Override
            public void onResponse(Call<RateNbu[]> call, Response<RateNbu[]> response) {
                if (response.isSuccessful()) {
                    RateNbu[] rates = response.body();
                    if (rates != null && rates.length > 0) {
                        if (BuildConfig.DEBUG) Log.d(TAG, rates[0].toString());
                        sharedPref.edit().putString(SettingsActivity.APP_PREF_RATE_USDUAH, String.valueOf(rates[0].getRate())).apply();
                    }
                } else {
                }
            }

            @Override
            public void onFailure(Call<RateNbu[]> call, Throwable t) {
            }
        });

    }

    public static void initInstance() {
        if (instance == null) instance = new Session();
        if (BuildConfig.DEBUG) Log.d(TAG, "initInstance()");
    }

    public static Session getInstance() {
        //if (instance == null) initInstance();
        return instance;
    }

    public void setPasswordValue(String passwordValue) {
        this.passwordValue = passwordValue;
    }

    public String getPasswordValue() {
        return passwordValue;
    }

    public String getPublicKey() {
        String result = "";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        String encryptedString = preferences.getString(APP_PREF_PUBLIC_KEY, "");
        if (!encryptedString.isEmpty() && !passwordValue.isEmpty()) {
            try {
                byte[] encryptedBytes = Enigma.easDecrypt(
                        App.getAppContext(), Base64.decode(encryptedString, Base64.NO_WRAP), passwordValue);
                result = new String(encryptedBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String getPrivateKey() {
        String result = "";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        String encryptedString = preferences.getString(APP_PREF_PRIVATE_KEY, "");
        if (!encryptedString.isEmpty() && !passwordValue.isEmpty()) {
            try {
                byte[] encryptedBytes = Enigma.easDecrypt(
                        App.getAppContext(), Base64.decode(encryptedString, Base64.NO_WRAP), passwordValue);
                result = new String(encryptedBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String getPasswordSalt() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        return preferences.getString(APP_PREF_PASSWORD_SALT, "");
    }

    public String getPasswordHash() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        return preferences.getString(APP_PREF_PASSWORD_HASH, "");
    }

    public boolean isPrivateApiPaid() {
        //if (BuildConfig.DEBUG) return true;
        return privateApiPaid;
    }

    public void setPrivateApiPaid(boolean privateApiPaid) {
        this.privateApiPaid = privateApiPaid;
        Log.d(TAG, "privateApiPaid = " + this.privateApiPaid);
    }

    public boolean isCorrectKeys() {
        String publicKey = getPublicKey();
        String privateKey = getPrivateKey();
        return (!publicKey.equals(privateKey) && publicKey.length() == 40 && privateKey.length() == 40);
    }

    public boolean isPrivateApiAvailable() {
        return Session.getInstance().isCorrectKeys() && Session.getInstance().isPrivateApiPaid();
    }

/*
    public String checkPrivateApiPaid() {
        boolean result = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        String stringTrial = preferences.getString(APP_PREF_TRIAL_DATE_END, "");
        if (!stringTrial.isEmpty()) {
            Date dateTrial, today;
            try {
                dateTrial = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(stringTrial);
                today = new Date();
                result = !today.after(dateTrial);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.privateApiPaid = result;
        return stringTrial;
    }
*/
}
