package com.kerchin.yellownote.widget;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjj.sva.JJSearchView;
import com.cjj.sva.anim.JJBaseController;
import com.cjj.sva.anim.controller.HKQCircleToClearLineController;
import com.cjj.sva.anim.controller.JJCircleToLineAlphaController;
import com.kerchin.yellownote.R;

public class CircleSearchView extends RelativeLayout {
    private JJSearchView mSearchView;
    private EditText mEditText;
    private TextView mSearchViewWeGotTxt;
    private ImageView mSearchViewDownImg, mSearchViewUpImg;
    private UpAndDownListener listener;
    private SearchClickListener searchListener;

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
        void upClick();

        void downClick();
    }

    public interface SearchClickListener {
        void searchClick(EditText editText, String text);
    }

    public void setEditTextWatcher(TextWatcher textWatcher) {
        mEditText.addTextChangedListener(textWatcher);
    }

    public void setUpAndDownClick(UpAndDownListener listener) {
        this.listener = listener;
    }

    public void setSearchClick(SearchClickListener listener) {
        searchListener = listener;
    }

    private void initLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_circle_search, this);
        mSearchViewWeGotTxt = (TextView) findViewById(R.id.mSearchViewWeGotTxt);
        mSearchViewDownImg = (ImageView) findViewById(R.id.mSearchViewDownImg);
        mSearchViewUpImg = (ImageView) findViewById(R.id.mSearchViewUpImg);
        mSearchView = (JJSearchView) findViewById(R.id.mSearchView);
        mSearchView.setController(new HKQCircleToClearLineController());
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
//                if (mSearchView.getState() == JJBaseController.STATE_ANIM_NONE
//                        || mSearchView.getState() == JJBaseController.STATE_ANIM_STOP) {
//                    mSearchView.startAnim();
//                    mEditText.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mEditText.setVisibility(View.VISIBLE);
//                        }
//                    }, 600);
//                    mEditText.bringToFront();
                /*} else */
                if (mSearchView.getState() == JJBaseController.STATE_ANIM_START) {
                    searchListener.searchClick(mEditText, mEditText.getText().toString());
                }
            }
        });
    }

    /**
     * 设置结果文字
     *
     * @param str 结果文字
     */
    public void setText(String str) {
        mSearchViewWeGotTxt.setText(str);
    }

    public void setEditEmpty() {
        mEditText.setText("");
    }

    public void setUpEnable(boolean isEnable) {
        mSearchViewUpImg.setEnabled(isEnable);
    }

    public void setDownEnable(boolean isEnable) {
        mSearchViewDownImg.setEnabled(isEnable);
    }

    //滑动到时调用
    public void startSearch() {
        if (mSearchView.getState() == JJBaseController.STATE_ANIM_NONE
                || mSearchView.getState() == JJBaseController.STATE_ANIM_STOP) {
            mSearchView.startAnim();
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEditText.setVisibility(View.VISIBLE);
                    mEditText.bringToFront();
                    mEditText.requestFocusFromTouch();
                }
            }, 600);//60s为动画持续的时间
        }
    }

    //滑动过时调用
    public void resetSearch() {
        mSearchView.resetAnim();
        mEditText.setVisibility(View.INVISIBLE);
    }
}