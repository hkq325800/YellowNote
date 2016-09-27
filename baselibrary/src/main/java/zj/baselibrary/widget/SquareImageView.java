package zj.baselibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import zj.baselibrary.R;

public class SquareImageView extends ImageView {

	private int widthWeight = 1;
	private int heightWeight = 1;

	public SquareImageView(Context context) {
		super(context);
	}

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.SquareImageView);
		widthWeight = ta.getInteger(R.styleable.SquareImageView_width_weight,
				widthWeight);
		heightWeight = ta.getInteger(R.styleable.SquareImageView_height_weight,
				heightWeight);
		ta.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (widthMode == MeasureSpec.EXACTLY
				&& heightMode != MeasureSpec.EXACTLY) {
			int width = MeasureSpec.getSize(widthMeasureSpec);

			int height = width;
			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height,
						MeasureSpec.getSize(heightMeasureSpec));
			}
			setMeasuredDimension(width, (width * heightWeight) / widthWeight);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
