package apps.basilisk.kunatickerwidget.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import apps.basilisk.kunatickerwidget.DateParser;
import apps.basilisk.kunatickerwidget.LoaderData;
import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.entity.OfferList;
import apps.basilisk.kunatickerwidget.entity.Trade;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        HashMap<String, String> itemMap = (HashMap<String, String>) intent.getSerializableExtra("EXTRA_DATA");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(itemMap.get("title"));
        //getSupportActionBar().setSubtitle(getString(R.string.market_last_price) + ": " + itemMap.get("last"));

        ((ImageView) findViewById(R.id.image_icon)).setImageResource(Integer.parseInt(String.valueOf(itemMap.get("icon_res"))));
        ((TextView) findViewById(R.id.text_bid)).setText(itemMap.get("bid"));
        ((TextView) findViewById(R.id.text_ask)).setText(itemMap.get("ask"));
        ((TextView) findViewById(R.id.text_low)).setText(itemMap.get("low"));
        ((TextView) findViewById(R.id.text_high)).setText(itemMap.get("high"));
        ((TextView) findViewById(R.id.text_volume)).setText(itemMap.get("vol"));
        ((TextView) findViewById(R.id.text_market)).setText(itemMap.get("market"));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), itemMap.get("market"));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
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

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements Observer {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_MARKET = "market";
        private static final String LIST_DATA = "listData";

        private int sectionNumber;
        private static String market;

        private ArrayList<HashMap<String, String>> listData;
        private ListView listView;
        private LoaderData loaderData;
        private SwipeRefreshLayout swipeRefreshLayout;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String market) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_MARKET, market);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            listData = new ArrayList<>();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            market = getArguments().getString(ARG_MARKET).replace("/", "");
            //Log.d(TAG, "onCreateView(), sectionNumber = " + sectionNumber);

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            listView = rootView.findViewById(R.id.list_view);

            swipeRefreshLayout = rootView.findViewById(R.id.refresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (sectionNumber == 3) loaderData.loadTrades(market);
                    else loaderData.loadOffers(market);
                }
            });

            ListAdapter adapter;

            switch (sectionNumber) {
                case 1:
                case 2:
                    adapter = new SimpleAdapter(getContext(), listData, R.layout.item_bid,
                            new String[]{"price", "vol", "amount"},
                            new int[]{R.id.text_price, R.id.text_vol, R.id.text_amount});

                    listView.setAdapter(adapter);
                    break;

                case 3:
                    adapter = new SimpleAdapter(getContext(), listData, R.layout.item_trade,
                            new String[]{"id", "date", "time", "price", "vol", "amount"},
                            new int[]{R.id.text_id, R.id.text_date, R.id.text_time, R.id.text_price, R.id.text_vol, R.id.text_amount});

                    listView.setAdapter(adapter);
                    break;
            }

            // объявление загрузчика
            loaderData = new LoaderData();
            loaderData.addObserver(this);

            if (listData.size() == 0 && savedInstanceState == null) {
                swipeRefreshLayout.setRefreshing(true);
                if (sectionNumber == 3) loaderData.loadTrades(market);
                else loaderData.loadOffers(market);
            }

            return rootView;
        }

        @Override
        public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
            // вызывается после onCreateView()
            super.onViewStateRestored(savedInstanceState);
            //Log.d(TAG, "onViewStateRestored(), sectionNumber = " + sectionNumber);
            if (savedInstanceState != null && savedInstanceState.getSerializable(LIST_DATA) != null) {
                listData.clear();
                listData.addAll((ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(LIST_DATA));
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            //Log.d(TAG, "onSaveInstanceState(), sectionNumber = " + sectionNumber);
            outState.putSerializable(LIST_DATA, listData);
        }


        @Override
        public void update(Observable observable, Object o) {

            if (o instanceof Trade[]) {
                Trade[] trades = (Trade[]) o;
                if (trades != null && trades.length > 0) {

                    HashMap<String, String> hashMap;
                    Date dateAt = null;
                    String dateTrade = "";
                    String timeTrade = "";
                    listData.clear();
                    for (Trade trade : trades) {
                        try {
                            dateAt = DateParser.parseRFC3339Date(trade.getCreatedAt());
                            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                            dateTrade = df.format(dateAt);
                            df.applyPattern("HH:mm:ss");
                            timeTrade = df.format(dateAt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        hashMap = new HashMap<>();
                        hashMap.put("id", String.valueOf(trade.getId()));
                        hashMap.put("date", dateTrade);
                        hashMap.put("time", timeTrade);
                        hashMap.put("price", (trade.getPrice() + "0000000000").substring(0, 10));
                        hashMap.put("vol", (trade.getVolume() + "0000000000").substring(0, 10));
                        hashMap.put("amount", (trade.getFunds() + "0000000000").substring(0, 10));
                        listData.add(hashMap);
                    }
                }
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

            if (o instanceof OfferList) {
                OfferList offers = (OfferList) o;
                if (offers != null) {
                    HashMap<String, String> hashMap;
                    List<List<String>> asks = offers.getAsks();
                    List<List<String>> bids = offers.getBids();

                    listData.clear();

                    if (sectionNumber == 1) {
                        Formatter formatter = new Formatter();
                        for (List<String> bid : bids) {
                            hashMap = new HashMap<>();
                            hashMap.put("price", (bid.get(0) + "0000000000").substring(0, 10));
                            hashMap.put("vol", (bid.get(1) + "0000000000").substring(0, 10));
                            hashMap.put("amount", String.format("%.2f",
                                    Float.parseFloat(bid.get(0)) * Float.parseFloat(bid.get(1)))
                                    .replace(",", "."));
                            listData.add(hashMap);
                        }
                    }

                    if (sectionNumber == 2) {
                        for (int i = asks.size() - 1; i >= 0; i--) {
                            List<String> ask = asks.get(i);
                            hashMap = new HashMap<>();
                            hashMap.put("price", (ask.get(0) + "0000000000").substring(0, 10));
                            hashMap.put("vol", (ask.get(1) + "0000000000").substring(0, 10));
                            hashMap.put("amount", String.format("%.2f",
                                    Float.parseFloat(ask.get(0)) * Float.parseFloat(ask.get(1)))
                                    .replace(",", "."));
                            listData.add(hashMap);
                        }
                    }

                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
   }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] nameTabs = {getString(R.string.tab_buy), getString(R.string.tab_sell), getString(R.string.tab_trades)};
        String market;

        public SectionsPagerAdapter(FragmentManager fm, String market) {
            super(fm);
            this.market = market;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //Log.d(TAG, "getItem()");
            return PlaceholderFragment.newInstance(position + 1, market);
        }

        @Override
        public int getCount() {
            return nameTabs.length;
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
