package apps.basilisk.kunatickerwidget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import apps.basilisk.kunatickerwidget.entity.Ticker;

public class DatabaseAdapter {
    private static final String TAG = "DatabaseAdapter";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseAdapter(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public DatabaseAdapter open() {
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long getCountTicker() {
        return DatabaseUtils.queryNumEntries(database, Ticker.TABLE);
    }

    public long deleteTicker(long id) {
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return database.delete(Ticker.TABLE, whereClause, whereArgs);
    }

    public long insertTicker(Ticker ticker) {
        ContentValues cv = new ContentValues();

        cv.put(Ticker.COL_TIMESTAMP, ticker.getTimestamp());
        cv.put(Ticker.COL_CURR_TRADE, ticker.getCurrencyTrade());
        cv.put(Ticker.COL_CURR_BASE, ticker.getCurrencyBase());
        cv.put(Ticker.COL_BUY, ticker.getBuy());
        cv.put(Ticker.COL_SELL, ticker.getSell());
        cv.put(Ticker.COL_LAST, ticker.getLast());
        cv.put(Ticker.COL_LOW, ticker.getLow());
        cv.put(Ticker.COL_HIGH, ticker.getHigh());
        cv.put(Ticker.COL_VOL, ticker.getVol());
        cv.put(Ticker.COL_PRICE, ticker.getPrice());

        return database.insert(Ticker.TABLE, null, cv);
    }

    public long updateTicker(Ticker ticker) {
        String whereClause = Ticker.COL_ID + "=" + String.valueOf(ticker.getId());
        ContentValues cv = new ContentValues();

        cv.put(Ticker.COL_TIMESTAMP, ticker.getTimestamp());
        cv.put(Ticker.COL_CURR_TRADE, ticker.getCurrencyTrade());
        cv.put(Ticker.COL_CURR_BASE, ticker.getCurrencyBase());
        cv.put(Ticker.COL_BUY, ticker.getBuy());
        cv.put(Ticker.COL_SELL, ticker.getSell());
        cv.put(Ticker.COL_LAST, ticker.getLast());
        cv.put(Ticker.COL_LOW, ticker.getLow());
        cv.put(Ticker.COL_HIGH, ticker.getHigh());
        cv.put(Ticker.COL_VOL, ticker.getVol());
        cv.put(Ticker.COL_PRICE, ticker.getPrice());

        return database.update(Ticker.TABLE, cv, whereClause, null);
    }

    public Cursor getTickers(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        String[] columns = new String[]{
                Ticker.COL_ID,
                Ticker.COL_TIMESTAMP,
                Ticker.COL_CURR_TRADE,
                Ticker.COL_CURR_BASE,
                Ticker.COL_BUY,
                Ticker.COL_SELL,
                Ticker.COL_LAST,
                Ticker.COL_LOW,
                Ticker.COL_HIGH,
                Ticker.COL_VOL,
                Ticker.COL_PRICE
        };
        return database.query(Ticker.TABLE, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public Cursor getTickers() {
        return getTickers(null, null, null, null, null);
    }

    public Ticker getTicker(String currencyTrade, String currencyBase) {
        Ticker ticker = null;
        String query = String.format("SELECT * FROM %s WHERE %s=? AND %s=?",
                Ticker.TABLE, Ticker.COL_CURR_TRADE, Ticker.COL_CURR_BASE);

        Cursor cursor = database.rawQuery(query, new String[]{currencyTrade, currencyBase});
        ArrayList<Ticker> tickers = cursorToTickerList(cursor);
        cursor.close();
        if (tickers != null && tickers.size() > 0) ticker = tickers.get(0);

        return ticker;
    }

    public ArrayList<Ticker> getTickerList(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        ArrayList<Ticker> tickers;

        Cursor cursor = getTickers(selection, selectionArgs, groupBy, having, orderBy);
        tickers = cursorToTickerList(cursor);

        return tickers;
    }

    public ArrayList<Ticker> getTickerList() {
        return getTickerList(null, null, null, null, null);
    }

    private ArrayList<Ticker> cursorToTickerList(Cursor cursor) {
        ArrayList<Ticker> tickers = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                tickers.add(
                        new Ticker(
                                cursor.getLong(cursor.getColumnIndex(Ticker.COL_ID)),
                                cursor.getInt(cursor.getColumnIndex(Ticker.COL_TIMESTAMP)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_CURR_TRADE)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_CURR_BASE)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_BUY)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_SELL)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_LOW)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_HIGH)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_LAST)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_VOL)),
                                cursor.getString(cursor.getColumnIndex(Ticker.COL_PRICE))
                        )
                );
            } while (cursor.moveToNext());
        }

        return tickers;
    }


}
