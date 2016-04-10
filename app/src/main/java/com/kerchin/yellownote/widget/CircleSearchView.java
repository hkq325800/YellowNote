package com.kerchin.yellownote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjj.sva.JJSearchView;
import com.cjj.sva.anim.JJBaseController;
import com.cjj.sva.anim.controller.JJCircleToLineAlphaController;
import com.kerchin.yellownote.R;

public class CircleSearchView extends RelativeLayout {
    private Context mContext;
    private JJSearchView mSearchView;
    private EditText mEditText;
    private TextView mSearchViewWeGotTxt;
    private ImageView mSearchViewDownImg, mSearchViewUpImg;
    private UpAndDownListener listener;

    public CircleSearchView(Context context) {
        this(context, null);
    }

    public CircleSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    public interface UpAndDownListener {
        public void upClick();

        public void downClick();
    }

    public void setText(String str) {
        mSearchViewWeGotTxt.setText(str);
    }

    public void setUpAndDownClick(UpAndDownListener listener) {
        this.listener = listener;
    }

    private void initLayout(Context context) {
        this.mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.widget_circle_search, this);
        mSearchViewWeGotTxt = (TextView) findViewById(R.id.mSearchViewWeGotTxt);
        mSearchViewDownImg = (ImageView) findViewById(R.id.mSearchViewDownImg);
        mSearchViewUpImg = (ImageView) findViewById(R.id.mSearchViewUpImg);
        mSearchView = (JJSearchView) findViewById(R.id.mSearchView);
        mSearchView.setController(new JJCircleToLineAlphaController());
        mEditText = (EditText) findViewById(R.id.mSearchViewEdt);
        mSearchViewUpImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.upClick();
            }
        });
        mSearchViewDownImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.downClick();
            }
        });
        mSearchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Trace.show(mContext, mSearchView.getState()+"");
                if (mSearchView.getState() == JJBaseController.STATE_ANIM_NONE
                        || mSearchView.getState() == JJBaseController.STATE_ANIM_STOP) {
                    mSearchView.startAnim();
                    mEditText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEditText.setVisibility(View.VISIBLE);
                        }
                    }, 600);
                    mEditText.bringToFront();
                } else if (mSearchView.getState() == JJBaseController.STATE_ANIM_START) {
//                    Trace.show(mContext, "正在搜索", Toast.LENGTH_LONG);
                    mSearchView.resetAnim();
                    mEditText.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}