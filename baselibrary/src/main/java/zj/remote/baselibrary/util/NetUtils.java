//package zj.remote.baselibrary.util;
//
//import android.util.Log;
//
//import com.squareup.okhttp.FormEncodingBuilder;
//import com.squareup.okhttp.MediaType;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * okhttp操作库
// * com.netease.hzinstitute.myemmagee.utils
// * Created by hzhuangkeqing on 2015/8/5 0005.
// */
//public class NetUtils {
//    private static final String LOG_TAG = "NetUtils ";
//    private static final String Token = "9ef46f13891f401924f9908377bc2ee2";
//    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//    public static String get(String uri) throws IOException {
//        OkHttpClient client = new OkHttpClient();
//        client.setConnectTimeout(10, TimeUnit.SECONDS);
//        Request request = new Request.Builder()
//                .url(uri)
//                .build();
//        Response response = client.newCall(request).execute();
//        String result = response.body().string();
//        Log.d(LOG_TAG + "get", /*"uri " + uri +*/ " code " + String.valueOf(response.code()) + " response " + result);
//        return result;
//    }
//
//    public static String post(String send, String url) throws IOException {
//        //String url = "http://10.240.154.90/monkey/public/getPost";
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body = RequestBody.create(JSON, send);
//        Request request = new Request.Builder()
//                .header("Authorization", "Token token=" + Token)
//                .url(url)
//                .post(body)
//                .build();
//        Response response = client.newCall(request).execute();
//        //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//        String result = response.body().string();
//        Log.d("NetUtils post", send + /*" $$$ " + url +*/ " $$$ " + result);
//        return result;
//    }
//
//    public static String simplePost(Map<String, String> paramsMap, String url) throws IOException {
//        //String url = "http://10.240.154.90/monkey/public/getPost";
//        OkHttpClient client = new OkHttpClient();
//        //创建一个FormBody.Builder
//        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
//
//        for (String key : paramsMap.keySet()) {
//            //追加表单信息
//            formEncodingBuilder.add(key, paramsMap.get(key));
//        }
//        RequestBody body = formEncodingBuilder.build();
//        Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//        Response response = client.newCall(request).execute();
//        //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//        String result = response.body().string();
//        Log.d("NetUtils post", paramsMap + /*" $$$ " + url +*/ " $$$ " + result);
//        return result;
//    }
//
//    public static String postData(byte[] send, String url, String token) throws IOException {
//        //String url = "http://10.240.154.90/monkey/public/getPost";
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body;
//        if (send != null) {
//            body = RequestBody.create(MediaType.parse("application/data"), send);
//        } else {
//            body = RequestBody.create(MediaType.parse("application/data"), "");
//        }
//        Request request = new Request.Builder()
//                .addHeader("Authorization", token)
//                .addHeader("Content-Type", "application/octet-stream")
//                .addHeader("Content-Length", String.valueOf(send == null ? 0 : send.length))
//                .url(url)
//                .post(body)
//                .build();
//        Response response = client.newCall(request).execute();
//        //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//        String result = response.body().string();
//        Log.d("NetUtils post", send + /*" $$$ " + url +*/ " $$$ " + result);
//        return result;
//    }
//
//    //将local指向的文件上传//转为一个后台线程处理
//    public static void postExtra(String local) throws IOException {
//        //String uri = MyApplication.getContext().getString(R.string.server_url) + "/tasks/extras";
//        final String url = "http://10.240.154.90/monkey/public/getPost";
//        final String IMGUR_CLIENT_ID = "9199fdef135c122";
//        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
//        /*final OkHttpClient client = new OkHttpClient();
//        File file = new File(local);
//
//        RequestBody requestBody = new MultipartBuilder()
//                .type(MultipartBuilder.FORM)
//                .addFormDataPart("title", "Square Logo")//
//                .addFormDataPart("image", file.getName(),
//                        RequestBody.create(MEDIA_TYPE_PNG, file))
//                .build();
//
//        Request request = new Request.Builder()
//                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)//
//                .url(url)
//                .header("Token", Token)
//                .post(requestBody)
//                .build();
//
//        Response response = client.newCall(request).execute();
//        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);*/
//
//        //response.body().string()应为服务器上图片的url
//        Log.d("NetUtils postExtra", local /*+ " $$$ " + uri + " $$$ " + response.body().string()*/);
//        //post("{\"a\":\"包含testId和图片url的json\"}", "对接图片的接口");
//    }
//}
