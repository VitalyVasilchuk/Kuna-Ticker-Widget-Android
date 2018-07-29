package apps.basilisk.kunatickerwidget.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.tools.CoinCatalog;
import apps.basilisk.kunatickerwidget.tools.LoaderData;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.Session;
import apps.basilisk.kunatickerwidget.database.DatabaseAdapter;
import apps.basilisk.kunatickerwidget.entity.Ticker;
import apps.basilisk.kunatickerwidget.widget.TickerAppWidget;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, Observer {
    private static final String TAG = "MainActivity";
    private ArrayList<HashMap<String, Object>> listTickers;
    private ListView listView;
    private LoaderData loaderData;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.refresh_list);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = findViewById(R.id.list_tickers);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                HashMap<String, Object> o = (HashMap<String, Object>) adapterView.getItemAtPosition(position);
                String[] arrayCurrency = ((String) o.get("market")).toUpperCase().split("/");
                if (arrayCurrency.length == 2) {
                    o.put("currencyTrade", arrayCurrency[0]);
                    o.put("currencyBase", arrayCurrency[1]);
                }
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("EXTRA_DATA", o);
                startActivity(intent);
            }
        });

        listTickers = new ArrayList<>();
        prepareList(); // позволяет сделать загрузку из локальной БД, минуя запрос к серверу и при пересоздании

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean switchPriceUsd = preferences.getBoolean(SettingsActivity.APP_PREF_SWITCH_PRICE_USD, true);

        int item_ticker_layout = (switchPriceUsd) ? R.layout.item_ticker_usd : R.layout.item_ticker;
        ListAdapter simpleAdapter = new SimpleAdapter(this, listTickers, item_ticker_layout,
                new String[]{"icon_res", "title", "market", "bid", "ask",  "bid_usd", "ask_usd", "low", "high", "vol"},
                new int[]{R.id.image_icon, R.id.text_title, R.id.text_market,
                        R.id.text_bid, R.id.text_ask, R.id.text_bid_usd, R.id.text_ask_usd,
                        R.id.text_low, R.id.text_high, R.id.text_volume});

        listView.setAdapter(simpleAdapter);

        // объявление загрузчика
        loaderData = LoaderData.getInstance();
        loaderData.addObserver(this);

        if (savedInstanceState == null) {
            swipeRefreshLayout.setRefreshing(true);
            loaderData.loadTickers(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loaderData.deleteObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Session.getInstance().getPasswordValue().isEmpty())
            startActivity(new Intent(this, PinActivity.class));
    }


    private void prepareList() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean switchSortPrice = preferences.getBoolean(SettingsActivity.APP_PREF_SWITCH_SORT_PRICE, true);
        boolean switchMarketUah = preferences.getBoolean(SettingsActivity.APP_PREF_SWITCH_MARKET_UAH, false);
        boolean switchPriceUsd = preferences.getBoolean(SettingsActivity.APP_PREF_SWITCH_PRICE_USD, true);
        float rateUsdUah = Float.parseFloat(preferences.getString(SettingsActivity.APP_PREF_RATE_USDUAH, "26.25"));

        DatabaseAdapter dba = new DatabaseAdapter(this);
        dba.open();

        String[] arg = {"uah"};
        Cursor cusrorTickers = dba.getTickers(
                (switchMarketUah) ? Ticker.COL_CURR_BASE + "=?" : null,
                (switchMarketUah) ? arg : null,
                null,
                null,
                (switchSortPrice) ? Ticker.COL_CURR_BASE + " DESC, " + Ticker.COL_CURR_TRADE + " ASC" : null);

        String bidUsd;
        String askUsd;
        HashMap<String, Object> itemList;

        listTickers.clear();
        if (cusrorTickers.moveToFirst()) {
            do {
                String currencyTrade = cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_CURR_TRADE));
                String currencyBase = cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_CURR_BASE));
                CoinCatalog.Coin coin = CoinCatalog.Instance().getCoinInfo(currencyTrade);

                bidUsd = "";
                askUsd = "";
                itemList = new HashMap<>();

                itemList.put("icon_res", coin.getIconRes());
                itemList.put("title", coin.getName());
                itemList.put("market", currencyTrade + "/" + currencyBase);
                itemList.put("bid", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_BUY)));
                itemList.put("ask", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_SELL)));
                itemList.put("low", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_LOW)));
                itemList.put("high", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_HIGH)));
                itemList.put("vol", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_VOL)));
                itemList.put("last", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_LAST)));
                if (switchPriceUsd && currencyBase.equalsIgnoreCase("UAH") && rateUsdUah > 0f) {
                    try {
                        bidUsd = "($" + String.format("%.5f", Float.parseFloat(cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_BUY))) / rateUsdUah).replace(",", ".") + ")";
                        askUsd = "($" + String.format("%.5f", Float.parseFloat(cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_SELL))) / rateUsdUah).replace(",", ".") + ")";
                    } catch (NumberFormatException e) {
                        // java.lang.NumberFormatException: Invalid float: "No deals"
                    }
                }
                itemList.put("bid_usd", bidUsd);
                itemList.put("ask_usd", askUsd);

                listTickers.add(itemList);
            } while (cusrorTickers.moveToNext());
        }
        dba.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.action_accounts).setVisible(Session.getInstance().isPrivateApiPaid());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_accounts) {
            if (Session.getInstance().isPrivateApiAvailable()) {
                Intent intent = new Intent(getBaseContext(), UserInfoActivity.class);
                startActivity(intent);
            }
            else if (!Session.getInstance().isPrivateApiPaid()) {
                String[] messageArray = {
                        getString(R.string.private_api_account),
                        getString(R.string.private_api_activation_setting)
                };
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.private_api)
                        .setItems(messageArray, null)
                        //.setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.create();
                builder.show();
            }
            else if (!Session.getInstance().isCorrectKeys()) {
                String[] messageArray = {
                        getString(R.string.private_api_incorrect_key),
                };
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.private_api)
                        .setItems(messageArray, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.create();
                builder.show();
            }
        }

        if (id == R.id.action_about) {
            String[] aboutMessageArray = {
                    getString(R.string.about_subtitle),
                    "v." + BuildConfig.VERSION_NAME + "\n" + "(c) 2018 by Basilisk",
                    getString(R.string.about_agreement)};

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name)
                    .setItems(aboutMessageArray, null)
                    //.setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.create();
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        loaderData.loadTickers(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof Pair) {
            String keyName = (String) ((Pair) o).first;
            switch (keyName) {
                case "ERROR_TICKERS":
                    String message = (String) ((Pair) o).second;
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;

                case "RESULT_TICKERS":
                    prepareList();
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
            // обновление активных виджетов
            Intent intent = new Intent(getApplicationContext(), TickerAppWidget.class);
            intent.setAction(TickerAppWidget.UPDATE_ALL_WIDGETS);
            getApplicationContext().sendBroadcast(intent);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
