//package zj.health.fjzl.pt.global.data;
//
//import org.json.JSONObject;
//
//import retrofit2.Call;
//import retrofit2.http.Field;
//import retrofit2.http.FormUrlEncoded;
//import retrofit2.http.POST;
//import zj.health.fjzl.pt.pathology.UrlCollection;
//import zj.health.fjzl.pt.pathology.data.model.RPDetailModel;
//import zj.health.fjzl.pt.pathology.data.model.RPZDBGModel;
//import zj.health.fjzl.pt.pathology.data.model.RemotePathologyModel;
//
///**
// * Created by ucmed on 2016/10/11.
// */
//
//public interface RemotePathologyApi {
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<RemotePathologyModel> getRemoteList(@Field("requestData") String requestData);
//
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<RPDetailModel> getRemoteDetail(@Field("requestData") String requestData);
//
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<JSONObject> getCaptcha(@Field("requestData") String requestData);
//
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<JSONObject> refuseCase(@Field("requestData") String requestData);
//
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<RPZDBGModel> getReportDetail(@Field("requestData") String requestData);
//
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<JSONObject> commitReport(@Field("requestData") String requestData);
//
//    @FormUrlEncoded
//    @POST(UrlCollection.POST)
//    Call<JSONObject> agreeCase(@Field("requestData") String requestData);
//}
