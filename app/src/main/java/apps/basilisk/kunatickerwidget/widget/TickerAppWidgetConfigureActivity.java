package apps.basilisk.kunatickerwidget.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.database.DatabaseAdapter;
import apps.basilisk.kunatickerwidget.entity.Ticker;
import apps.basilisk.kunatickerwidget.tools.CoinCatalog;

/**
 * The configuration screen for the {@link TickerAppWidget TickerAppWidget} AppWidget.
 */
public class TickerAppWidgetConfigureActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "TickerAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner spinnerMarkets;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TickerAppWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = spinnerMarkets.getSelectedItem().toString();
            widgetText = widgetText.substring(widgetText.indexOf("(")+1, widgetText.length()-1);
            saveTitlePref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TickerAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            // todo вынести отдельным методом в класс TickerAppWidget, вызывать из 2х мест?
            // настройка AlarmManager для периодической загрузки и обновления виджетов
            Intent intent = new Intent(context, TickerAppWidget.class);
            intent.setAction(TickerAppWidget.RELOAD_ALL_WIDGETS);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), TimeUnit.MINUTES.toMillis(5), pendingIntent);
            }

            finish();
        }
    };

    public TickerAppWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, "btc/uah");
        return titleValue;
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.ticker_app_widget_configure);
        findViewById(R.id.button_apply).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // подготовка списка рынков
        List<String> listMarkets = new ArrayList<>();
        DatabaseAdapter dba = new DatabaseAdapter(this);
        dba.open();

        Cursor cusrorTickers = dba.getTickers(null, null, null, null,
                Ticker.COL_CURR_TRADE + " ASC, " + Ticker.COL_CURR_BASE + " ASC");

        if (cusrorTickers.moveToFirst()) {
            do {
                String currencyTrade = cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_CURR_TRADE));
                String currencyBase = cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_CURR_BASE));

                //listMarkets.add(CoinCatalog.Instance().getCoinInfo(currencyTrade).getName() + " (" + currencyTrade + "/" + currencyBase + ")");
                listMarkets.add(CoinCatalog.Instance().getCoinInfo(currencyTrade).getName() + " (" + currencyTrade + "/" + currencyBase + ")");
            } while (cusrorTickers.moveToNext());
        }
        dba.close();

        // сортировка списка рынков
        Collections.sort(listMarkets, new Comparator<String>() {
            @Override
            public int compare(String v1, String v2) {
                return v1.compareTo(v2); // ascending
            }
        });

        spinnerMarkets = findViewById(R.id.spinner_markets);
        ArrayAdapter<String> adp = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_dropdown_item, listMarkets);
        spinnerMarkets.setAdapter(adp);

        String compareValue = loadTitlePref(TickerAppWidgetConfigureActivity.this, mAppWidgetId);
        compareValue = CoinCatalog.Instance().getCoinInfo(compareValue.split("/")[0]).getName() + " (" + compareValue + ")";
        if (compareValue != null) {
            int spinnerPosition = adp.getPosition(compareValue);
            spinnerMarkets.setSelection(spinnerPosition);
        }
    }
}

