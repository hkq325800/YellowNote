package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by Kerchin on 2016/4/5 0005.
 */
public class ShareSuggetService {

    public static void setUnableToSuggest(String txtUser){
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        query.getFirstInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    avObject.put("isAbleToSuggest", false);
                    avObject.saveInBackground();
                } else {
                    Trace.e("设置isAbleToSuggest false失败");
                    e.printStackTrace();
                }
            }
        });
    }

    public static void pushSuggest(String txtUser, String msg, String contact) throws AVException {
        AVObject suggest = new AVObject("Suggest");
        suggest.put("user_tel", txtUser);
        suggest.put("suggest_msg", msg);
        suggest.put("suggest_contact", contact);
        suggest.save();
    }

    public static AVObject isAbleToSuggest(String txtUser) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        return query.getFirst();
    }
}
