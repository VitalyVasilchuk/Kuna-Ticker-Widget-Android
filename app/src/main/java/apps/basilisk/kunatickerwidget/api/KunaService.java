package apps.basilisk.kunatickerwidget.api;

import android.util.Log;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.Session;
import apps.basilisk.kunatickerwidget.entity.Deal;
import apps.basilisk.kunatickerwidget.entity.OfferList;
import apps.basilisk.kunatickerwidget.entity.Order;
import apps.basilisk.kunatickerwidget.entity.TickerList;
import apps.basilisk.kunatickerwidget.entity.Trade;
import apps.basilisk.kunatickerwidget.entity.UserInfo;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface KunaService {
    String ENDPOINT = "https://kuna.io/api/v2/";
    String TAG = "KunaService";

    /*********
    Public API
    **********/
    @GET("tickers")
    Call<Map<String, TickerList>> getTickers();

    @GET("trades/")
    Call<Trade[]> getTrades(@Query("market") String market);

    @GET("depth/")
    Call<OfferList> getOffers(@Query("market") String market);

    /**********
    Private API
    ***********/

    // Информация о пользователе и активах
    @GET("/api/v2/members/me")
    Call<UserInfo> getUserInfo(
            @Query("access_key") String accessKey,
            @Query("tonce") long tonce
    );

    // Активные ордера пользователя
    @GET("/api/v2/orders")
    Call<List<Order>> getOrders (
            @Query("access_key") String accessKey,
            @Query("market") String market,
            @Query("tonce") long tonce
    );

    // История сделок пользователя
    @GET("/api/v2/trades/my")
    Call<List<Deal>> getDeals (
            @Query("access_key") String accessKey,
            @Query("market") String market,
            @Query("tonce") long tonce
    );

    // Выставление ордера
    @POST("/api/v2/orders")
    Call<Order> addOrder (
            @Query("access_key") String accessKey,
            @Query("market") String market,
            @Query("price") String price,
            @Query("side") String side,
            @Query("tonce") long tonce,
            @Query("volume") String volume
    );

    // Отмена ордера
    @POST("/api/v2/order/delete")
    Call<Order> deleteOrder(
            @Query("access_key") String accessKey,
            @Query("id") String id,
            @Query("tonce") long tonce
    );

    class Factory {

        private static String accessKey;
        private static String secretKey;

        private static KunaService create() {
            accessKey = Session.getInstance().getPublicKey();
            secretKey = Session.getInstance().getPrivateKey();

            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                    .readTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS);

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(interceptor).build();
            }

            // перехватчик для подписания запроса, содержащего параметр "access_key"
            // подпись генерируется по алгоритму HEX(HMAC-SHA256("HTTP-verb|URI|params", secret_key))
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    String method = request.method();
                    String uri = request.url().uri().getPath();
                    String query = request.url().uri().getQuery();
                    String message = method + "|" + uri.substring(0, uri.length()) + "|" + query;
                    if(BuildConfig.DEBUG) Log.d(TAG, message);

                    if (message.contains("access_key") && !secretKey.isEmpty()) {
                        HttpUrl url = request.url().newBuilder().addQueryParameter("signature", getSignature(message)).build();
                        request = request.newBuilder().url(url).build();
                    }
                    return chain.proceed(request);
                }
            });

            OkHttpClient client = builder.build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(KunaService.ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            return retrofit.create(KunaService.class);
        }

        private static String getSignature(String message) {
            String result = null;

            try {
                Mac hasher = Mac.getInstance("HmacSHA256");
                hasher.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
                byte[] hash = hasher.doFinal(message.getBytes());
                result = byteArrayToHex(hash);
            }
            catch (NoSuchAlgorithmException e) {}
            catch (InvalidKeyException e) {}

            return result;
        }

        private static String byteArrayToHex(byte[] a) {
            StringBuilder sb = new StringBuilder(a.length * 2);
            for(byte b: a)
                sb.append(String.format("%02x", b));
            return sb.toString();
        }


        public static Call<Map<String, TickerList>> getTickers() {
            return create().getTickers();
        }

        public static Call<Trade[]> getTrades(String market) {
            return create().getTrades(market.toLowerCase());
        }

        public static Call<OfferList> getOffers(String market) {
            return  create().getOffers(market.toLowerCase());
        }

        public static Call<UserInfo> getUserInfo() {
            return create().getUserInfo(accessKey, System.currentTimeMillis());
        }

        public static Call<List<Order>> getOrders(String market) {
            return create().getOrders(accessKey, market.toLowerCase(), System.currentTimeMillis()
            );
        }

        public static Call<List<Deal>> getDeals(String market) {
            return create().getDeals(
                    accessKey, market.toLowerCase(), System.currentTimeMillis()
            );
        }

        public static Call<Order> addOrder(String market, String price, String side, String volume) {
            return create().addOrder(
                    accessKey, market.toLowerCase(), price, side, System.currentTimeMillis(), volume
            );
        }

        public static Call<Order> deleteOrder(String id) {
            return create().deleteOrder(accessKey, id, System.currentTimeMillis());
        }

    }
}
