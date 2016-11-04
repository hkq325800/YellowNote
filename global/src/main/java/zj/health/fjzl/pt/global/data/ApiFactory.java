//package zj.health.fjzl.pt.global.data;
//
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import zj.health.fjzl.pt.AppContext;
//import zj.health.fjzl.pt.global.data.jsonconvert.JsonConverterFactory;
//import zj.health.fjzl.pt.pathology.UrlCollection;
//
///**
// * Created by ucmed on 2016/10/11.
// */
//
//public class ApiFactory {
//
//    static RemotePathologyApi remotePathologyApi;
//    static RemotePathologyApi remotePathologyJsonApi;
//
//    public static RemotePathologyApi getRemoteApi() {
//        if (remotePathologyApi == null)
//            synchronized (ApiFactory.class) {
//                if (remotePathologyApi == null) {
//                    Retrofit retrofit = new Retrofit.Builder()
//                            .baseUrl(UrlCollection.BASE_URL)
//                            .client(RetrofitFactory.getInstance(AppContext.getAppContext()).client)
//                            .addConverterFactory(GsonConverterFactory.create())
////                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                            .build();
//
//                    remotePathologyApi = retrofit.create(RemotePathologyApi.class);
//                }
//            }
//        return remotePathologyApi;
//    }
//
//    public static RemotePathologyApi getRemoteJsonApi() {
//        if (remotePathologyJsonApi == null)
//            synchronized (ApiFactory.class) {
//                if (remotePathologyJsonApi == null) {
//                    Retrofit retrofit = new Retrofit.Builder()
//                            .baseUrl(UrlCollection.BASE_URL)
//                            .client(RetrofitFactory.getInstance(AppContext.getAppContext()).client)
//                            .addConverterFactory(JsonConverterFactory.create())
////                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                            .build();
//
//                    remotePathologyJsonApi = retrofit.create(RemotePathologyApi.class);
//                }
//            }
//        return remotePathologyJsonApi;
//    }
//}
