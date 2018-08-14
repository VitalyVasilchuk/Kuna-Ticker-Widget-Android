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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.activity.DetailActivity;
import apps.basilisk.kunatickerwidget.entity.OfferList;
import apps.basilisk.kunatickerwidget.tools.LoaderData;
import apps.basilisk.kunatickerwidget.tools.Utils;

public class BuyFragment extends Fragment implements Observer {
    private static final String TAG = "BuyFragment";
    private static final String LIST_DATA = "listData";

    private static String currencyTrade;
    private static String currencyBase;
    private static String market;

    private ArrayList<HashMap<String, String>> listData;
    private ListView listView;
    private LoaderData loaderData;
    private SwipeRefreshLayout swipeRefreshLayout;

    public BuyFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        currencyTrade = bundle.getString(DetailActivity.ARG_CURRENCY_TRADE);
        currencyBase = bundle.getString(DetailActivity.ARG_CURRENCY_BASE);
        market = currencyTrade + currencyBase;

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
                loaderData.loadOffers(market);
            }
        });

        ListAdapter adapter;
        adapter = new SimpleAdapter(getContext(), listData, R.layout.item_offer,
                new String[]{"price", "vol", "amount"},
                new int[]{R.id.text_price, R.id.text_volume, R.id.text_amount});
        listView.setAdapter(adapter);

        if (listData.size() == 0 && savedInstanceState == null) {
            swipeRefreshLayout.setRefreshing(true);
            loaderData.loadOffers(market);
        }

        return rootView;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof Pair) {
            String keyName = (String) ((Pair) o).first;
            switch (keyName) {
                case "ERROR_OFFERS":
                    String message = (String) ((Pair) o).second;
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;

                case "RESULT_OFFERS":
                    OfferList offers = (OfferList) ((Pair) o).second;
                    if (offers != null) {
                        List<List<String>> bids = offers.getBids();
                        BigDecimal price, vol, amount;
                        listData.clear();
                        for (List<String> bid : bids) {
                            price = new BigDecimal(bid.get(0));
                            vol = new BigDecimal(bid.get(1));
                            amount = price.multiply(vol);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("price", Utils.getFormattedValue(bid.get(0)));
                            hashMap.put("vol", Utils.getFormattedValue(bid.get(1)));
                            hashMap.put("amount", Utils.getFormattedValue(amount.toPlainString()));

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
