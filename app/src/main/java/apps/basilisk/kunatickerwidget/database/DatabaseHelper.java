package apps.basilisk.kunatickerwidget.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import apps.basilisk.kunatickerwidget.entity.Ticker;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "appstore.db"; // название БД
    private static final int SCHEMA = 1; // версия базы данных

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE " +
                        Ticker.TABLE + "(" +
                            Ticker.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            Ticker.COL_TIMESTAMP + " INTEGER DEFAULT 0," +
                            Ticker.COL_CURR_TRADE + " TEXT," +
                            Ticker.COL_CURR_BASE + " TEXT," +
                            Ticker.COL_BUY + " TEXT," +
                            Ticker.COL_SELL + " TEXT," +
                            Ticker.COL_LAST + " TEXT," +
                            Ticker.COL_LOW + " TEXT," +
                            Ticker.COL_HIGH + " TEXT," +
                            Ticker.COL_VOL + " TEXT," +
                            Ticker.COL_PRICE + " TEXT" +
                        ")"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Ticker.TABLE);
        onCreate(sqLiteDatabase);
    }
}
