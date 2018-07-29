package apps.basilisk.kunatickerwidget.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.tools.LoaderData;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.tools.Utils;
import apps.basilisk.kunatickerwidget.entity.Trade;

public class TradesFragment extends Fragment implements Observer {
    private static final String TAG = "TradesFragment";
    private static final String ARG_MARKET = "market";
    private static final String LIST_DATA = "listData";

    private static String market;

    private ArrayList<HashMap<String, String>> listData;
    private ListView listView;
    private LoaderData loaderData;
    private SwipeRefreshLayout swipeRefreshLayout;

    public TradesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        market = getArguments().getString(ARG_MARKET).replace("/", "");
        listData = new ArrayList<>();

        // объявление загрузчика
        loaderData = LoaderData.getInstance();
        loaderData.addObserver(this);
        if (BuildConfig.DEBUG) Log.d(TAG, "countObservers = " + loaderData.countObservers());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loaderData.deleteObserver(this);
        if (BuildConfig.DEBUG) Log.d(TAG, "countObservers = " + loaderData.countObservers());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(LIST_DATA, listData);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        // вызывается после onCreateView()
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getSerializable(LIST_DATA) != null) {
            listData.clear();
            listData.addAll((ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(LIST_DATA));
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        listView = rootView.findViewById(R.id.list_view);

        swipeRefreshLayout = rootView.findViewById(R.id.refresh_list);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loaderData.loadTrades(market);
            }
        });

        ListAdapter adapter = new SimpleAdapter(getContext(), listData, R.layout.item_trade,
                new String[]{"id", "date", "time", "price", "vol", "amount"},
                new int[]{R.id.text_id, R.id.text_date, R.id.text_time, R.id.text_price, R.id.text_volume, R.id.text_amount});
        listView.setAdapter(adapter);

        if (listData.size() == 0 && savedInstanceState == null) {
            swipeRefreshLayout.setRefreshing(true);
            loaderData.loadTrades(market);
        }

        return rootView;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof Pair) {
            String keyName = (String) ((Pair) o).first;
            switch (keyName) {
                case "ERROR_TRADES":
                    String message = (String) ((Pair) o).second;
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;

                case "RESULT_TRADES":
                    Trade[] trades = (Trade[]) ((Pair) o).second;
                    if (trades != null && trades.length > 0) {
                        Date dateAt;
                        String dateOperation = "";
                        String timeOperation = "";

                        listData.clear();
                        for (Trade trade : trades) {
                            try {
                                dateAt = Utils.parseRFC3339Date(trade.getCreatedAt());
                                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                                dateOperation = df.format(dateAt);
                                df.applyPattern("HH:mm:ss");
                                timeOperation = df.format(dateAt);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", String.valueOf(trade.getId()));
                            hashMap.put("date", dateOperation);
                            hashMap.put("time", timeOperation);
                            hashMap.put("price", Utils.getFormattedValue(trade.getPrice()));
                            hashMap.put("vol", Utils.getFormattedValue(trade.getVolume()));
                            hashMap.put("amount", Utils.getFormattedValue(trade.getFunds()));
                            listData.add(hashMap);
                        }
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    }
}
