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
    private EditText mSearchViewEdt;
    private TextView mSearchViewWeGotTxt;
    private ImageView mSearchViewDownImg, mSearchViewUpImg;
    private RelativeLayout mSearchViewReL;
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
        mSearchViewEdt.addTextChangedListener(textWatcher);
    }

    public void setUpAndDownClick(UpAndDownListener listener) {
        this.listener = listener;
    }

    public void setSearchClick(SearchClickListener listener) {
        searchListener = listener;
    }
    
    public void clearEditText(){
        mSearchViewEdt.setText("");
    }

    private void initLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_circle_search, this);
        mSearchViewReL = (RelativeLayout) findViewById(R.id.mSearchViewReL);
        mSearchViewWeGotTxt = (TextView) findViewById(R.id.mSearchViewWeGotTxt);
        mSearchViewDownImg = (ImageView) findViewById(R.id.mSearchViewDownImg);
        mSearchViewUpImg = (ImageView) findViewById(R.id.mSearchViewUpImg);
        mSearchView = (JJSearchView) findViewById(R.id.mSearchView);
        mSearchView.setController(new HKQCircleToClearLineController());
        mSearchViewEdt = (EditText) findViewById(R.id.mSearchViewEdt);
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
                if (mSearchView.getState() == JJBaseController.STATE_ANIM_NONE
                        || mSearchView.getState() == JJBaseController.STATE_ANIM_STOP) {
                    mSearchView.startAnim();
//                    mSearchViewReL.setAlpha(0);
//                    mSearchViewReL.setVisibility(VISIBLE);
//                    mSearchViewReL.animate().alpha(1).setDuration(1200).start();
                    mSearchViewEdt.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSearchViewEdt.setVisibility(View.VISIBLE);
                        }
                    }, 600);
                    mSearchViewEdt.bringToFront();
                } else if (mSearchView.getState() == JJBaseController.STATE_ANIM_START) {
                    searchListener.searchClick(mSearchViewEdt, mSearchViewEdt.getText().toString());
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
        mSearchViewEdt.setText("");
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
            mSearchViewEdt.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSearchViewEdt.setVisibility(View.VISIBLE);
                    mSearchViewEdt.bringToFront();
                    mSearchViewEdt.requestFocusFromTouch();
                }
            }, 600);//60s为动画持续的时间
        }
    }

    //滑动过时调用
    public void resetSearch() {
        mSearchView.resetAnim();
        mSearchViewEdt.setVisibility(View.INVISIBLE);
    }
}