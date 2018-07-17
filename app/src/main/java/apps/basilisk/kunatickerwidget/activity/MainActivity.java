package apps.basilisk.kunatickerwidget.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.CoinCatalog;
import apps.basilisk.kunatickerwidget.LoaderData;
import apps.basilisk.kunatickerwidget.R;
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

        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = findViewById(R.id.list_tickers);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                HashMap<String, Object> o = (HashMap<String, Object>) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("EXTRA_DATA", o);
                startActivity(intent);
            }
        });

        listTickers = new ArrayList<>();
        prepareList();

        ListAdapter simpleAdapter = new SimpleAdapter(this, listTickers, R.layout.item_ticker,
                new String[]{"icon_res", "title", "market", "bid", "ask", "low", "high", "vol"},
                new int[]{R.id.image_icon, R.id.text_title, R.id.text_market, R.id.text_bid,
                        R.id.text_ask, R.id.text_low, R.id.text_high, R.id.text_volume});

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

    private void prepareList() {
        HashMap<String, Object> itemList;

        DatabaseAdapter dba = new DatabaseAdapter(this);
        dba.open();

        Cursor cusrorTickers = dba.getTickers(null, null, null, null,
                Ticker.COL_CURR_BASE + " DESC, " + Ticker.COL_CURR_TRADE + " ASC");

        listTickers.clear();
        if (cusrorTickers.moveToFirst()) {
            do {
                String currencyTrade = cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_CURR_TRADE));
                String currencyBase = cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_CURR_BASE));
                CoinCatalog.Coin coin = CoinCatalog.Instance().getCoinInfo(currencyTrade);

                itemList = new HashMap<>();
                itemList.put("icon_res", coin.getIconRes());
                itemList.put("title",  coin.getName());
                itemList.put("market", currencyTrade + "/" + currencyBase);
                itemList.put("bid", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_BUY)));
                itemList.put("ask", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_SELL)));
                itemList.put("low", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_LOW)));
                itemList.put("high", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_HIGH)));
                itemList.put("vol", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_VOL)));
                itemList.put("last", cusrorTickers.getString(cusrorTickers.getColumnIndex(Ticker.COL_LAST)));

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
        if (o instanceof Map) {
            prepareList();
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

            // обновление активных виджетов
            Intent intent = new Intent(getApplicationContext(), TickerAppWidget.class);
            intent.setAction(TickerAppWidget.UPDATE_ALL_WIDGETS);
            getApplicationContext().sendBroadcast(intent);
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}
