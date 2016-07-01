package com.kerchin.yellownote.base;

import android.content.Context;
import android.os.Bundle;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.kerchin.yellownote.helper.sql.OrmLiteHelper;

/**
 * Created by Kerchin on 2016/6/23 0023.
 */
public abstract class MyOrmLiteBaseActivity<H extends OrmLiteHelper> extends BaseHasSwipeActivity {
    private volatile H helper;
    private volatile boolean created = false;
    private volatile boolean destroyed = false;
    private static Logger logger = LoggerFactory.getLogger(MyOrmLiteBaseActivity.class);

    public MyOrmLiteBaseActivity() {
    }

    public H getHelper() {
        if(this.helper == null) {
            if(!this.created) {
                throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
            } else if(this.destroyed) {
                throw new IllegalStateException("A call to onDestroy has already been made and the helper cannot be used after that point");
            } else {
                throw new IllegalStateException("Helper is null for some unknown reason");
            }
        } else {
            return this.helper;
        }
    }

    public ConnectionSource getConnectionSource() {
        return this.getHelper().getConnectionSource();
    }

    protected void onCreate(Bundle savedInstanceState) {
        if(this.helper == null) {
            this.helper = this.getHelperInternal(this);
            this.created = true;
        }

        super.onCreate(savedInstanceState);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.releaseHelper(this.helper);
        this.destroyed = true;
    }

    protected H getHelperInternal(Context context) {
        OrmLiteSqliteOpenHelper newHelper = OpenHelperManager.getHelper(context);
        logger.trace("{}: got new helper {} from OpenHelperManager", this, newHelper);
        //noinspection unchecked
        return (H) newHelper;
    }

    protected void releaseHelper(H helper) {
        OpenHelperManager.releaseHelper();
        logger.trace("{}: helper {} was released, set to null", this, helper);
        this.helper = null;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }
}
