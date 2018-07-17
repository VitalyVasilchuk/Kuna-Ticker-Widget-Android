package apps.basilisk.kunatickerwidget;

import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.Observable;

import apps.basilisk.kunatickerwidget.api.KunaService;
import apps.basilisk.kunatickerwidget.database.DatabaseAdapter;
import apps.basilisk.kunatickerwidget.entity.OfferList;
import apps.basilisk.kunatickerwidget.entity.Ticker;
import apps.basilisk.kunatickerwidget.entity.TickerList;
import apps.basilisk.kunatickerwidget.entity.Trade;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoaderData extends Observable {

    private static final String TAG = "LoaderData";
    private static LoaderData instance;

    public static LoaderData getInstance() {
        if (instance == null) instance = new LoaderData();
        return instance;
    }

    public void loadTrades(final String market) {
        Call<Trade[]> call = KunaService.Factory.getTrades(market);
        call.enqueue(new Callback<Trade[]>() {
            @Override
            public void onResponse(Call<Trade[]> call, Response<Trade[]> response) {
                if (response.isSuccessful()) {
                    Trade[] trades = response.body();
                    setChanged();
                    notifyObservers(trades);
                } else
                    Log.d(TAG, "loadTrades().onResponse:" + "#" + response.code() + "\"" + response.message() + "\"");
            }

            @Override
            public void onFailure(Call<Trade[]> call, Throwable t) {
                Log.d(TAG, "loadTrades().onFailure: " + t.getMessage());
                setChanged();
                notifyObservers();
            }
        });
    }

    public void loadOffers(final String market) {
        Call<OfferList> call = KunaService.Factory.getOffers(market);
        call.enqueue(new Callback<OfferList>() {
            @Override
            public void onResponse(Call<OfferList> call, Response<OfferList> response) {
                if (response.isSuccessful()) {
                    OfferList offers = response.body();
                    setChanged();
                    notifyObservers(offers);
                } else
                    Log.d(TAG, "loadOffers().onResponse:" + "#" + response.code() + "\"" + response.message() + "\"");
            }

            @Override
            public void onFailure(Call<OfferList> call, Throwable t) {
                Log.d(TAG, "loadOffers().onFailure: " + t.getMessage());
                setChanged();
                notifyObservers();
            }
        });
    }

    public void loadTickers(final Context context) {
        Call<Map<String, TickerList>> call = KunaService.Factory.getTickers();
        call.enqueue(new Callback<Map<String, TickerList>>() {
            @Override
            public void onResponse(Call<Map<String, TickerList>> call, Response<Map<String, TickerList>> response) {
                if (response.isSuccessful()) {
                    Map<String, TickerList> mapTickers = response.body();
                    saveTickerList(mapTickers, context);
                    setChanged();
                    notifyObservers(mapTickers);
                } else {
                    Log.d(TAG, "loadTickers().onResponse:" + "#" + response.code() + "\"" + response.message() + "\"");
                }
            }

            @Override
            public void onFailure(Call<Map<String, TickerList>> call, Throwable t) {
                Log.d(TAG, "loadTickers().onFailure: " + t.getMessage());
                setChanged();
                notifyObservers();
            }
        });
    }

    private void saveTickerList(Map<String, TickerList> mapTickers, Context context) {
        if (mapTickers != null && mapTickers.size() > 0) {

            DatabaseAdapter dba = new DatabaseAdapter(context);
            dba.open();

            String currencyPair, currencyTrade, currencyBase;
            for (Map.Entry<String, TickerList> entry : mapTickers.entrySet()) {
                currencyTrade = "";
                currencyBase = "";
                Ticker tickerNew = entry.getValue().getTicker();
                tickerNew.setTimestamp(entry.getValue().getAt());
                // определение валютной пары
                currencyPair = entry.getKey();
                if (currencyPair.length() > 3) {
                    currencyTrade = currencyPair.substring(0, currencyPair.length() - 3);
                    currencyBase = currencyPair.substring(currencyTrade.length());
                }
                tickerNew.setCurrencyTrade(currencyTrade.toLowerCase());
                tickerNew.setCurrencyBase(currencyBase.toLowerCase());


                // поиск в таблице тикера по заданной валютной паре
                Ticker tickerOld = dba.getTicker(currencyTrade, currencyBase);

                if (tickerOld != null) {                // если нашли
                    tickerNew.setId(tickerOld.getId()); // устанавливаем значение идентификатора
                    dba.updateTicker(tickerNew);        // обновляем существующую запись
                } else
                    dba.insertTicker(tickerNew);        // иначе создаем новую запись
            }

            dba.close();
        }
    }

}
