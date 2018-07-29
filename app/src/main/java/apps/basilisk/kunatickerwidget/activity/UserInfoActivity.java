package apps.basilisk.kunatickerwidget.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.tools.CoinCatalog;
import apps.basilisk.kunatickerwidget.tools.LoaderData;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.tools.Utils;
import apps.basilisk.kunatickerwidget.entity.Account;
import apps.basilisk.kunatickerwidget.entity.UserInfo;

public class UserInfoActivity extends AppCompatActivity implements Observer {
    private static final String LIST_DATA = "LIST_DATA";
    private ArrayList<HashMap<String, Object>> listData;
    private ListView listView;
    private LoaderData loaderData;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwitchCompat switchEmptyBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loaderData = LoaderData.getInstance();
        loaderData.addObserver(this);

        swipeRefreshLayout = findViewById(R.id.refresh_list);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loaderData.loadUserInfo();
            }
        });

        listData = new ArrayList<>();
        listView = findViewById(R.id.list_accounts);
        ListAdapter simpleAdapter = new SimpleAdapter(this, listData, R.layout.item_account,
                new String[]{"icon_res", "title", "currency", "balance", "locked"},
                new int[]{R.id.image_icon, R.id.text_title, R.id.text_currency, R.id.text_balance, R.id.text_locked});
        listView.setAdapter(simpleAdapter);

        if (savedInstanceState == null) {
            swipeRefreshLayout.setRefreshing(true);
            loaderData.loadUserInfo();
        } else {
            listData.clear();
            listData.addAll((ArrayList<HashMap<String, Object>>) savedInstanceState.getSerializable(LIST_DATA));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loaderData.deleteObserver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_accounts, menu);

        MenuItem appBarSwitch = menu.findItem(R.id.app_bar_switch);
        switchEmptyBalance = appBarSwitch.getActionView().findViewById(R.id.switch_empty_balance);
        switchEmptyBalance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loaderData.loadUserInfo();
                String message = (isChecked) ? getString(R.string.empty_balance_show) : getString(R.string.empty_balance_hide);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(LIST_DATA, listData);
    }

    @Override
    public void update(Observable observable, Object o) {

        if (o instanceof Pair) {
            String keyName = (String) ((Pair) o).first;
            switch (keyName) {
                case "ERROR_USER_INFO":
                    String message = (String) ((Pair) o).second;
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;

                case "RESULT_USER_INFO":
                    UserInfo userInfo = (UserInfo) ((Pair) o).second;
                    List<Account> accounts = userInfo.getAccounts();
                    if (accounts != null && accounts.size() > 0) {
                        HashMap<String, Object> hashMap;
                        listData.clear();
                        CoinCatalog.Coin coin;
                        for (Account account : accounts) {
                            if (!account.getCurrency().equalsIgnoreCase("UAH") &&
                                    switchEmptyBalance != null &&
                                    !switchEmptyBalance.isChecked() &&
                                    account.getBalance().equals("0.0") &&
                                    account.getLocked().equals("0.0")) continue;
                            coin = CoinCatalog.Instance().getCoinInfo(account.getCurrency());
                            hashMap = new HashMap<>();
                            hashMap.put("icon_res", coin.getIconRes());
                            hashMap.put("title", coin.getName());
                            hashMap.put("currency", account.getCurrency());
                            hashMap.put("balance", Utils.getFormattedValue(account.getBalance()));
                            hashMap.put("locked", Utils.getFormattedValue(account.getLocked()));
                            hashMap.put("sort_weight", account.getCurrency().equalsIgnoreCase("UAH") ? "1" : "2");
                            listData.add(hashMap);
                        }

                        // сортировка списка счетов
                        // todo подключить pref
                        if (true) {
                            Collections.sort(listData, new Comparator<Map<String, Object>>() {
                                final static String COMPARE_KEY0 = "sort_weight";
                                final static String COMPARE_KEY1 = "title";

                                @Override
                                public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                                    String v1 = (String) lhs.get(COMPARE_KEY0);
                                    String v2 = (String) rhs.get(COMPARE_KEY0);
                                    int result = v1.compareTo(v2); // ascending
                                    if (result != 0) return result;

                                    v1 = (String) lhs.get(COMPARE_KEY1);
                                    v2 = (String) rhs.get(COMPARE_KEY1);
                                    result = v1.compareTo(v2); // ascending

                                    return result;
                                }
                            });
                        }
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    }
}
