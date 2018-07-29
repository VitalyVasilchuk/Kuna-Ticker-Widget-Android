package apps.basilisk.kunatickerwidget.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.tools.CoinCatalog;
import apps.basilisk.kunatickerwidget.tools.LoaderData;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.activity.DetailActivity;
import apps.basilisk.kunatickerwidget.database.DatabaseAdapter;
import apps.basilisk.kunatickerwidget.entity.Ticker;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TickerAppWidgetConfigureActivity TickerAppWidgetConfigureActivity}
 */
public class TickerAppWidget extends AppWidgetProvider {

    private static final String TAG = "TickerAppWidget";
    public static final String UPDATE_ALL_WIDGETS = "UPDATE_ALL_WIDGETS";
    public static final String RELOAD_ALL_WIDGETS = "RELOAD_ALL_WIDGETS";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // определение рынка, связанного с виджетом
        String textMarket = TickerAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        String[] itemMarket = textMarket.split("/");

        if (itemMarket.length == 2) {
            // запрос информации по торгуемой валюте
            CoinCatalog.Coin coin = CoinCatalog.Instance().getCoinInfo(itemMarket[0]);
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            // запрос тикера для рынка (пара торговая и базовая валюта)
            DatabaseAdapter dba = new DatabaseAdapter(context);
            dba.open();
            Ticker ticker = dba.getTicker(itemMarket[0], itemMarket[1]);
            dba.close();

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ticker_app_widget);
            views.setImageViewResource(R.id.image_icon, coin.getIconRes());
            views.setTextViewText(R.id.text_title, textMarket);
            views.setTextViewText(R.id.text_bid, ticker.getBuy());
            views.setTextViewText(R.id.text_ask, ticker.getSell());
            //views.setTextViewText(R.id.text_date_time, DateFormat.format("dd.MM.yyyy HH:mm", ticker.getTimestamp() * 1000L));
            views.setTextViewText(R.id.text_date_time, df.format(ticker.getTimestamp() * 1000L));

            views.setViewVisibility(R.id.image_update, View.VISIBLE);
            views.setViewVisibility(R.id.progress_bar, View.INVISIBLE);

            // launch configuration activity on icon click
            Intent intentConfig = new Intent(context, TickerAppWidgetConfigureActivity.class);
            intentConfig.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntentConfig = PendingIntent.getActivity(context, appWidgetId, intentConfig, 0);
            views.setOnClickPendingIntent(R.id.image_icon, pendingIntentConfig);

            // update on click icon
            Intent intentUpdate = new Intent(context, TickerAppWidget.class);
            intentUpdate.setAction(RELOAD_ALL_WIDGETS);
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, 0);
            views.setOnClickPendingIntent(R.id.image_update, pendingIntentUpdate);

            // переход на детализирующую активити
            HashMap<String, Object> mapDetail = new HashMap();
            mapDetail.put("market", ticker.getCurrencyPair('/'));
            mapDetail.put("icon_res",coin.getIconRes());
            mapDetail.put("title", coin.getName());
            mapDetail.put("bid", ticker.getBuy());
            mapDetail.put("ask", ticker.getSell());
            mapDetail.put("low", ticker.getLow());
            mapDetail.put("high", ticker.getHigh());
            mapDetail.put("vol", ticker.getVol());
            mapDetail.put("last", ticker.getLast());
            mapDetail.put("currencyTrade", ticker.getCurrencyTrade());
            mapDetail.put("currencyBase", ticker.getCurrencyBase());

            // активити и ее параметры
            Intent intentDetail = new Intent(context, DetailActivity.class);
            intentDetail.putExtra("EXTRA_DATA", mapDetail);

            // формирование стека вызова
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(DetailActivity.class);
            stackBuilder.addNextIntent(intentDetail);

            PendingIntent stackPendingIntent =
                    stackBuilder.getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);

/*
            PendingIntent pendingIntentActivity = PendingIntent.getActivity(context, appWidgetId,
                    intentDetail, PendingIntent.FLAG_UPDATE_CURRENT);
*/

            views.setOnClickPendingIntent(R.id.text_bid, stackPendingIntent/*pendingIntentActivity*/);
            views.setOnClickPendingIntent(R.id.text_ask, stackPendingIntent/*pendingIntentActivity*/);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            TickerAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(final Context context) {
        // Enter relevant functionality for when the first widget is created
        if (BuildConfig.DEBUG) Log.d(TAG, "onEnabled()");
        super.onEnabled(context);

        // настройка AlarmManager для периодической загрузки и обновления виджетов
        Intent intent = new Intent(context, TickerAppWidget.class);
        intent.setAction(RELOAD_ALL_WIDGETS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), TimeUnit.MINUTES.toMillis(5), pendingIntent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (BuildConfig.DEBUG) Log.d(TAG, "onDisabled()");
        super.onDisabled(context);

        // отключение AlarmManager для периодической загрузки и обновления виджетов
        Intent intent = new Intent(context, TickerAppWidget.class);
        intent.setAction(UPDATE_ALL_WIDGETS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReceive().action = " + intent.getAction());
        super.onReceive(context, intent);

        String action = intent.getAction().toUpperCase();
        Bundle extras = intent.getExtras();
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        switch (action) {

            case UPDATE_ALL_WIDGETS:
                ComponentName componentWidget = new ComponentName(context.getPackageName(), getClass().getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentWidget);
                for (int appWidgetId : appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
                break;

            case RELOAD_ALL_WIDGETS:
                if (extras != null) {
                    // подмена иконки обновления индикатором загрузки
                    int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ticker_app_widget);
                    views.setViewVisibility(R.id.image_update, View.INVISIBLE);
                    views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }

                LoaderData loaderData = LoaderData.getInstance();
                loaderData.addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, Object o) {
                        if (o instanceof Pair) {
                            String keyName = (String) ((Pair) o).first;
                            if (keyName.equals("RESULT_TICKERS") || keyName.equals("ERROR_TICKERS")) {
                                Intent intent = new Intent(context, TickerAppWidget.class);
                                intent.setAction(UPDATE_ALL_WIDGETS);
                                context.sendBroadcast(intent);

                                observable.deleteObserver(this);
                            }
                        }
                    }
                });
                loaderData.loadTickers(context);
                break;
        }
    }

}

