package com.kerchin.yellownote.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kerchin.yellownote.R;

/**
 * 2015-9-4，金锁很紧张，紫薇抢食快，为我的鱼儿赋诗一首：紫非嫣然出清水，薇瞰浮游间其中。
 *
 * @author MaTianyu
 *         2014-2-25下午2:36:30
 */
public class Main extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
    }

    public void clickSingle(View view) {
        startActivity(new Intent(this, SingleTest.class));
    }

    public void clickCascade(View view) {
        startActivity(new Intent(this, CascadeTest.class));
    }
}
