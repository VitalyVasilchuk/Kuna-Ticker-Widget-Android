package apps.basilisk.kunatickerwidget.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.tools.LoaderData;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.tools.Utils;
import apps.basilisk.kunatickerwidget.entity.Order;

public class OrdersFragment extends Fragment implements Observer {
    private static final String TAG = "OrdersFragment";
    private static final String ARG_MARKET = "market";
    private static final String LIST_DATA = "listData";

    private static String market;

    private ArrayList<HashMap<String, String>> listData;
    private ListView listView;
    private LoaderData loaderData;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String deletedEntryId;

    public OrdersFragment() {
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
                loaderData.loadOrders(market);
            }
        });

        ListAdapter adapter;
        adapter = new SimpleAdapter(getContext(), listData, R.layout.item_order,
                new String[]{"side", "id", "date", "time", "price", "volume", "funds"},
                new int[]{
                        R.id.text_side, R.id.text_id, R.id.text_date, R.id.text_time,
                        R.id.text_price, R.id.text_volume, R.id.text_funds
                }) {
            @Override
            public View getView(int position, View convertView, final ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                TextView textType = v.findViewById(R.id.text_side);
                int colorType = (textType.getText().toString().equals("sell")) ? getResources().getColor(R.color.color_ask) : getResources().getColor(R.color.color_bid);
                textType.setTextColor(colorType);

                ImageView imageOrderDelete = v.findViewById(R.id.image_order_delete);
                imageOrderDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View parentRow = (View) v.getParent();
                        if (parentRow != null) {
                            TextView textId = parentRow.findViewById(R.id.text_id);
                            TextView textSide = parentRow.findViewById(R.id.text_side);
                            TextView textPrice = parentRow.findViewById(R.id.text_price);
                            TextView textVolume = parentRow.findViewById(R.id.text_volume);
                            TextView textFunds = parentRow.findViewById(R.id.text_funds);

                            String currencyTrade = "";
                            String currencyBase = "";

                            String confirmationText = "" + getString(R.string.confirm_delete_order) + "\n" +
                                    "ID " + textId.getText().toString() + "\n" +
                                    textSide.getText().toString() + " " +
                                    textVolume.getText().toString() + " " + currencyTrade + "\n" +
                                    getString(R.string.confirmation_order_price) + " " + textPrice.getText().toString() + " " + currencyBase + "\n" +
                                    getString(R.string.confirmation_order_amount) + " " + textFunds.getText().toString() + " " + currencyBase;

                            deletedEntryId = textId.getText().toString();

                            AlertDialog.Builder builder;
                            builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(R.string.delete_entry)
                                    .setMessage(confirmationText)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            loaderData.deleteOrder(deletedEntryId);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                });
                return v;
            }
        };

        listView.setAdapter(adapter);

        if (listData.size() == 0 && savedInstanceState == null) {
            swipeRefreshLayout.setRefreshing(true);
            loaderData.loadOrders(market);
        }

        return rootView;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof Pair) {
            String keyName = (String) ((Pair) o).first;
            switch (keyName) {
                case "ERROR_ORDERS":
                case "ERROR_ADDED_ORDER":
                case "ERROR_DELETED_ORDER":
                    String message = (String) ((Pair) o).second;
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;

                case "RESULT_ORDERS":
                    List<Order> orders = (List<Order>) ((Pair) o).second;
                    if (orders != null) {
                        Date dateAt;
                        String dateOperation = "";
                        String timeOperation = "";

                        listData.clear();
                        for (Order order : orders) {
                            try {
                                dateAt = Utils.parseRFC3339Date(order.getCreatedAt());
                                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                                dateOperation = df.format(dateAt);
                                df.applyPattern("HH:mm:ss");
                                timeOperation = df.format(dateAt);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("side", order.getSide());
                            hashMap.put("id", order.getId().toString());
                            hashMap.put("date", dateOperation);
                            hashMap.put("time", timeOperation);
                            hashMap.put("price", Utils.getFormattedValue(order.getPrice()));
                            hashMap.put("volume", Utils.getFormattedValue(order.getVolume()));
                            hashMap.put("funds", Utils.getFormattedValue(order.getFunds()));
                            listData.add(0, hashMap);
                        }
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    }
}
