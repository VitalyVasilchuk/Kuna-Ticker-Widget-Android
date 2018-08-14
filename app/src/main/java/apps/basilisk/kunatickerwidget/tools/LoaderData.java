package apps.basilisk.kunatickerwidget.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.api.KunaService;
import apps.basilisk.kunatickerwidget.database.DatabaseAdapter;
import apps.basilisk.kunatickerwidget.entity.Deal;
import apps.basilisk.kunatickerwidget.entity.ErrorMessage;
import apps.basilisk.kunatickerwidget.entity.OfferList;
import apps.basilisk.kunatickerwidget.entity.Order;
import apps.basilisk.kunatickerwidget.entity.Ticker;
import apps.basilisk.kunatickerwidget.entity.TickerList;
import apps.basilisk.kunatickerwidget.entity.Trade;
import apps.basilisk.kunatickerwidget.entity.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoaderData extends Observable {

    private static final String TAG = "LoaderData";
    private static LoaderData instance;
    private static long timestamp;

    // объявление приватного конструктора запрещает создание экземпляра класса из вне
    private LoaderData() {
    }

    public static void initInstance() {
        if (instance == null) {
            instance = new LoaderData();
            timestamp = System.currentTimeMillis();
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "initInstance(" + timestamp + ")");
    }

    public static LoaderData getInstance() {
        //todo разобраться с "I/UrgentSignal: getInstance(): Creating UrgentSignalsProcessor instance"
        if (BuildConfig.DEBUG) Log.d(TAG, "getInstance(" + timestamp + ")");
        return instance;
    }

    private void pushResponseResult(String keyName, Object object) {
/*
        HashMap<String, Object> hashMapResult = new HashMap<>();
        hashMapResult.put(keyName, object);
*/
        setChanged();
        notifyObservers(new Pair<>("RESULT_" + keyName, object));
    }

    private void pushResponseError(String keyName, Response response) {
        String message = "error #" + response.code() + ": " + response.message(); // HTTP error
        try {
            Gson gson = new Gson();
            String errorString = response.errorBody().string();
            if (errorString != null && !errorString.isEmpty()) {
                ErrorMessage errorMessage = gson.fromJson(errorString, ErrorMessage.class);
                if (errorMessage != null)
                    message += "\n(" + errorMessage.getError().getMessage() + " (" + errorMessage.getError().getCode() + "))"; // API error
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG) Log.d(TAG, message);
        setChanged();
        notifyObservers(new Pair<>("ERROR_" + keyName, message));
    }

    private void pushResponseFailure(String keyName, String message) {
        setChanged();
        notifyObservers(new Pair<>("ERROR_" + keyName, message));
    }

    public void loadTickers(final Context context) {
        Call<Map<String, TickerList>> call = KunaService.Factory.getTickers();
        call.enqueue(new Callback<Map<String, TickerList>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, TickerList>> call,
                                   @NonNull Response<Map<String, TickerList>> response) {
                if (response.isSuccessful()) {
                    Map<String, TickerList> mapTickers = response.body();
                    saveTickerList(mapTickers, context);
                    pushResponseResult("TICKERS", mapTickers);
                } else {
                    pushResponseError("TICKERS", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, TickerList>> call, @NonNull Throwable t) {
                pushResponseFailure("TICKERS", "loadTickers().onFailure: " + t.getMessage());
            }
        });
    }

    public void loadOffers(final String market) {
        Call<OfferList> call = KunaService.Factory.getOffers(market);
        call.enqueue(new Callback<OfferList>() {
            @Override
            public void onResponse(@NonNull Call<OfferList> call, @NonNull Response<OfferList> response) {
                if (response.isSuccessful()) {
                    //OfferList offers = response.body();
                    pushResponseResult("OFFERS", response.body());
                } else
                    pushResponseError("OFFERS", response);
            }

            @Override
            public void onFailure(@NonNull Call<OfferList> call, @NonNull Throwable t) {
                pushResponseFailure("OFFERS","loadOffers().onFailure: " + t.getMessage());
            }
        });
    }

    public void loadTrades(final String market) {
        Call<Trade[]> call = KunaService.Factory.getTrades(market);
        call.enqueue(new Callback<Trade[]>() {
            @Override
            public void onResponse(@NonNull Call<Trade[]> call, @NonNull Response<Trade[]> response) {
                if (response.isSuccessful()) {
                    //Trade[] trades = response.body();
                    pushResponseResult("TRADES", response.body());
                } else
                    pushResponseError("TRADES", response);
            }

            @Override
            public void onFailure(@NonNull Call<Trade[]> call, @NonNull Throwable t) {
                pushResponseFailure("TRADES", "loadTrades().onFailure: " + t.getMessage());
            }
        });
    }

    public void loadUserInfo() {
        Call<UserInfo> call = KunaService.Factory.getUserInfo();
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    //UserInfo userInfo = response.body();
                    pushResponseResult("USER_INFO", response.body());
                } else
                    pushResponseError("USER_INFO", response);
            }

            @Override
            public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                pushResponseFailure("USER_INFO", "loadUserInfo().onFailure: " + t.getMessage());
            }
        });
    }

    public void loadOrders(String market) {
        Call<List<Order>> call = KunaService.Factory.getOrders(market);
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    //List<Order> orders = response.body();
                    pushResponseResult("ORDERS", response.body());
                }
                else {
                    pushResponseError("ORDERS", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                pushResponseFailure("ORDERS", "loadOrders().onFailure: " + t.getMessage());
            }
        });
    }

    public void loadDeals(String market) {
        Call<List<Deal>> call = KunaService.Factory.getDeals(market);
        call.enqueue(new Callback<List<Deal>>() {
            @Override
            public void onResponse(@NonNull Call<List<Deal>> call, @NonNull Response<List<Deal>> response) {
                if (response.isSuccessful()) {
                    //List<Deal> deals = response.body();
                    pushResponseResult("DEALS", response.body());
                } else
                    pushResponseError("DEALS", response);
            }

            @Override
            public void onFailure(@NonNull Call<List<Deal>> call, @NonNull Throwable t) {
                pushResponseFailure("DEALS", "loadDeals().onFailure: " + t.getMessage());
            }
        });
    }
    public  void addOrder(String market, String price, String side, String volume) {
        Call<Order> call = KunaService.Factory.addOrder(market, price, side, volume);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                if (response.isSuccessful()) {
                    Order order = response.body();
                    pushResponseResult("ADDED_ORDER", order);
                    loadOrders(order.getMarket());
                }
                else
                    pushResponseError("ADDED_ORDER", response);
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                pushResponseFailure("ADDED_ORDER", "addOrder().onFailure: " + t.getMessage());
            }
        });

    }

    public void deleteOrder(String id) {
        Call<Order> call = KunaService.Factory.deleteOrder(id);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                if (response.isSuccessful()) {
                    Order order = response.body();
                    pushResponseResult("DELETED_ORDER", order);
                    loadOrders(order.getMarket());
                }
                else
                    pushResponseError("DELETED_ORDER", response);
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                pushResponseFailure("DELETED_ORDER", "deleteOrder().onFailure: " + t.getMessage());
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
