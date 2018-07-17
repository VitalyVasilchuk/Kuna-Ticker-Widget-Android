package apps.basilisk.kunatickerwidget.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.entity.OfferList;
import apps.basilisk.kunatickerwidget.entity.TickerList;
import apps.basilisk.kunatickerwidget.entity.Trade;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KunaService {
    String ENDPOINT = "https://kuna.io/api/v2/";
    String TAG = "KunaService";

    /* Public API */
    @GET("tickers")
    Call<Map<String, TickerList>> getTickers();

    @GET("trades/")
    Call<Trade[]> getTrades(@Query("market") String market);

    @GET("depth/")
    Call<OfferList> getOffers(@Query("market") String market);

    /* Private API */

    class Factory {

        public static KunaService create() {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                    .readTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS);

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(interceptor).build();
            }

            OkHttpClient client = builder.build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(KunaService.ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            return retrofit.create(KunaService.class);
        }

        public static Call<Map<String, TickerList>> getTickers() {
            KunaService service = create();
            Call<Map<String, TickerList>> call = service.getTickers();
            return call;
        }

        public static Call<Trade[]> getTrades(String market) {
            return create().getTrades(market);
        }

        public static Call<OfferList> getOffers(String market) {
            return  create().getOffers(market);
        }
    }
}
