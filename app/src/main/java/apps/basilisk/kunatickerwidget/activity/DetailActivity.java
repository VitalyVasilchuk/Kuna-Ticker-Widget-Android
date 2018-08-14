package apps.basilisk.kunatickerwidget.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.Session;
import apps.basilisk.kunatickerwidget.entity.Account;
import apps.basilisk.kunatickerwidget.entity.UserInfo;
import apps.basilisk.kunatickerwidget.fragment.BuyFragment;
import apps.basilisk.kunatickerwidget.fragment.DealsFragment;
import apps.basilisk.kunatickerwidget.fragment.OrdersFragment;
import apps.basilisk.kunatickerwidget.fragment.SellFragment;
import apps.basilisk.kunatickerwidget.fragment.TradesFragment;
import apps.basilisk.kunatickerwidget.tools.LoaderData;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private static final int REQUEST_CODE_NEW_ORDER = 1;

    public static final String ARG_CURRENCY_TRADE = "currencyTrade";
    public static final String ARG_CURRENCY_BASE = "currencyBase";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private FloatingActionButton fab;
    private byte touchCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        final HashMap<String, String> itemMap = (HashMap<String, String>) intent.getSerializableExtra("EXTRA_DATA");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(itemMap.get("title"));
            //getSupportActionBar().setSubtitle(getString(R.string.market_last_price) + ": " + itemMap.get("last"));
        }

        ((ImageView) findViewById(R.id.image_icon)).setImageResource(Integer.parseInt(String.valueOf(itemMap.get("icon_res"))));
        ((TextView) findViewById(R.id.text_bid)).setText(itemMap.get("bid"));
        ((TextView) findViewById(R.id.text_ask)).setText(itemMap.get("ask"));
        ((TextView) findViewById(R.id.text_low)).setText(itemMap.get("low"));
        ((TextView) findViewById(R.id.text_high)).setText(itemMap.get("high"));
        ((TextView) findViewById(R.id.text_volume)).setText(itemMap.get("vol"));
        ((TextView) findViewById(R.id.text_market)).setText(itemMap.get("market"));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                itemMap, Session.getInstance().isPrivateApiPaid());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        if (Session.getInstance().isPrivateApiPaid()) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        } else {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        fab = findViewById(R.id.fab_order_insert);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Session.getInstance().isPrivateApiAvailable()) {
                    LoaderData loaderData = LoaderData.getInstance();
                    loaderData.addObserver(new Observer() {
                        @Override
                        public void update(Observable observable, Object o) {
                            String balanceTrade = "0.00";
                            String balanceBase = "0.00";
                            if (o instanceof Pair) {
                                String keyName = (String) ((Pair) o).first;
                                switch (keyName) {
                                    case "ERROR_USER_INFO":
                                        String message = (String) ((Pair) o).second;
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                        break;

                                    case "RESULT_USER_INFO":
                                        UserInfo userInfo = (UserInfo) ((Pair) o).second;
                                        List<Account> accounts = userInfo.getAccounts();
                                        if (accounts != null && accounts.size() > 0) {
                                            for (Account account : accounts) {
                                                if (account.getCurrency().equalsIgnoreCase(itemMap.get("currencyTrade"))) {
                                                    balanceTrade = account.getBalance();
                                                }
                                                if (account.getCurrency().equalsIgnoreCase(itemMap.get("currencyBase"))) {
                                                    balanceBase = account.getBalance();
                                                }
                                            }
                                            itemMap.put("balanceTrade", balanceTrade);
                                            itemMap.put("balanceBase", balanceBase);
                                            Intent intent = new Intent(getApplicationContext(), NewOrderActivity.class);
                                            intent.putExtra("EXTRA_DATA", itemMap);
                                            startActivityForResult(intent, REQUEST_CODE_NEW_ORDER);
                                        }
                                        break;
                                }
                                observable.deleteObserver(this);
                            }
                        }
                    });
                    loaderData.loadUserInfo();
                } else if (!Session.getInstance().isPrivateApiPaid()) {
                    String[] messageArray = {
                            getString(R.string.private_api_order),
                            getString(R.string.private_api_activation_menu) + " \"" +
                                    getString(R.string.action_subscription) + "\""
                    };
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(DetailActivity.this);
                    builder.setTitle(R.string.private_api)
                            .setItems(messageArray, null)
                            //.setMessage(message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    builder.create();
                    builder.show();
                } else if (!Session.getInstance().isCorrectKeys()) {
                    String[] messageArray = {
                            getString(R.string.private_api_incorrect_key),
                    };
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(DetailActivity.this);
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
        });

        // временная "активация" private API
/*
        touchCounter = 0;
        ImageView imageView = findViewById(R.id.image_icon);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Session.getInstance().isPrivateApiAvailable() && itemMap.get("market").equalsIgnoreCase("kun/btc")) {
                    if (++touchCounter == 10) {
                        touchCounter = 0;
                        Session.getInstance().setPrivateApiPaid(true);
                        Toast.makeText(DetailActivity.this, "a private API is available", Toast.LENGTH_SHORT).show();
                        //finish();
                    }
                }
            }
        });
*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fab.setVisibility(Session.getInstance().isPrivateApiAvailable() ? View.VISIBLE : View.GONE);
        if (Session.getInstance().getPasswordValue().isEmpty())
            startActivity(new Intent(this, PinActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_ORDER:
                if (resultCode == RESULT_OK && data != null) {
                    LoaderData.getInstance().addOrder(
                            data.getStringExtra("market"),
                            data.getStringExtra("price"),
                            data.getStringExtra("side"),
                            data.getStringExtra("volume")
                    );
                }
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] nameTabs = {getString(R.string.tab_buy), getString(R.string.tab_sell), getString(R.string.tab_trades), getString(R.string.tab_orders), getString(R.string.tab_deals)};
        String market;
        String currencyTrade;
        String currencyBase;
        boolean privateApi;

        public SectionsPagerAdapter(FragmentManager fm, HashMap<String, String> itemMap, boolean privateApi) {
            super(fm);
            this.currencyTrade = itemMap.get(ARG_CURRENCY_TRADE);
            this.currencyBase = itemMap.get(ARG_CURRENCY_BASE);
            this.privateApi = privateApi;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position + 1) {
                case 1:
                    fragment = new BuyFragment();
                    break;
                case 2:
                    fragment = new SellFragment();
                    break;
                case 3:
                    fragment = new TradesFragment();
                    break;
                case 4:
                    fragment = new OrdersFragment();
                    break;
                case 5:
                    fragment = new DealsFragment();
                    break;
            }
            Bundle args = new Bundle();
            args.putString(ARG_CURRENCY_TRADE, currencyTrade);
            args.putString(ARG_CURRENCY_BASE, currencyBase);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return (privateApi) ? nameTabs.length : nameTabs.length - 2;
        }

        @Override
        public String getPageTitle(int position) {
            return nameTabs[position];
        }

        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }
    }

}
