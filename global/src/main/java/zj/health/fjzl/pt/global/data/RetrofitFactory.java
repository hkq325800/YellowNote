package zj.health.fjzl.pt.global.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 因为需要AppContext
 * Created by ucmed on 2016/9/18.
 */
public class RetrofitFactory {
    private static RetrofitFactory instance;
    public OkHttpClient client;

    public static RetrofitFactory getInstance(Context context){
        if(instance==null)
            synchronized (RetrofitFactory.class){
                if(instance==null){
                    instance = new RetrofitFactory(context);
                }
            }
        return instance;
    }

    private RetrofitFactory(final Context context) {
        //cache url
        File httpCacheDirectory = new File(context.getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        //logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);//BASIC只有两行BODY有具体的传出传入数据
        client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {

                        CacheControl.Builder cacheBuilder = new CacheControl.Builder();
                        cacheBuilder.maxAge(0, TimeUnit.SECONDS);
                        cacheBuilder.maxStale(365, TimeUnit.DAYS);
                        CacheControl cacheControl = cacheBuilder.build();

                        Request request = chain.request();
                        if (!isNetworkAvailable(context)) {
                            request = request.newBuilder()
                                    .cacheControl(cacheControl)
                                    .build();

                        }
                        Response originalResponse = chain.proceed(request);
                        if (isNetworkAvailable(context)) {
                            int maxAge = 0; // read from cache
                            return originalResponse.newBuilder()
                                    .removeHeader("Pragma")
                                    .header("Cache-Control", "public ,max-age=" + maxAge)
                                    .build();
                        } else {
                            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                            return originalResponse.newBuilder()
                                    .removeHeader("Pragma")
                                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                                    .build();
                        }
                    }
                })
                .addInterceptor(logging)
                .cache(cache).build();
    }

//    //cache
//    Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//
//            CacheControl.Builder cacheBuilder = new CacheControl.Builder();
//            cacheBuilder.maxAge(0, TimeUnit.SECONDS);
//            cacheBuilder.maxStale(365, TimeUnit.DAYS);
//            CacheControl cacheControl = cacheBuilder.build();
//
//            Request request = chain.request();
//            if (!isNetworkAvailable(mContext)) {
//                request = request.newBuilder()
//                        .cacheControl(cacheControl)
//                        .build();
//
//            }
//            Response originalResponse = chain.proceed(request);
//            if (isNetworkAvailable(mContext)) {
//                int maxAge = 0; // read from cache
//                return originalResponse.newBuilder()
//                        .removeHeader("Pragma")
//                        .header("Cache-Control", "public ,max-age=" + maxAge)
//                        .build();
//            } else {
//                int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
//                return originalResponse.newBuilder()
//                        .removeHeader("Pragma")
//                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
//                        .build();
//            }
//        }
//    };

    /**
     * 判断网络是否可用
     * @param context 上下文环境
     * @return boolean
     */
    private boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                return info.isAvailable();
            }
        }
        return false;
    }
}
