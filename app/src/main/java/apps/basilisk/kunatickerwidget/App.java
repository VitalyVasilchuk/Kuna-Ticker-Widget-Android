package apps.basilisk.kunatickerwidget;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import apps.basilisk.kunatickerwidget.tools.LoaderData;

public class App extends Application {

    private static final String TAG = "App";
    private static Context context;

    public void onCreate() {
        super.onCreate();
        // получение контекста, для последующей передачи в синглтоны и другие компоненты
        App.context = getApplicationContext();

        // инициализация синглтонов текущей сессии и загрузчика данных
        Session.initInstance();
        LoaderData.initInstance();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate()");
    }

    public static Context getAppContext() {
        return App.context;
    }
}
