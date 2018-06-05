package com.accelerate.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accelerate.activity.AbstractActivity;
import com.accelerate.utils.LogUtil;
import com.boost.booster.clean.R;
import com.zhy.android.percent.support.PercentLinearLayout;

public class TitleView extends PercentLinearLayout implements View.OnClickListener {

    private static final int SIZE_NONE = 0;
    private String mTitle = null;
    private int mTitleTextSize;

    @SuppressWarnings("unused")
    private View mStatusView;
    private ImageView mBackImageBtn;
    private TextView mTitleTextView;
    private Drawable mBackBtnDrawable = null;
    private ImageView mSettingBtn;

    private OnTitleClickListener mOnClickListener;

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.title_layout, this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleView);
        mTitle = a.getString(R.styleable.TitleView_titleTx);
        mTitleTextSize = a.getInt(R.styleable.TitleView_titleSize, SIZE_NONE);
        mBackBtnDrawable = a.getDrawable(R.styleable.TitleView_backDrawable);
        a.recycle();
    }

    @SuppressWarnings({"ResourceType", "DanglingJavadoc"})
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LogUtil.i(getClass().getName() + " onFinishInflate()");
        AbstractActivity context = (AbstractActivity) getContext();
        context.mTitleLayout = this;
        mBackImageBtn = (ImageView) findViewById(R.id.back_btn);
        mTitleTextView = (TextView) findViewById(R.id.title);
//        mStatusView = findViewById(R.id.status_bar);
        mSettingBtn = (ImageView) findViewById(R.id.title_setting);
        //指定字体
//        final Typeface typeface =
//        mTitleTextView.setTypeface(typeface);
//        mTitleTextView.setPaintFlags(mTitleTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        if (!TextUtils.isEmpty(mTitle)) {
            mTitleTextView.setText(mTitle);
            if (mTitleTextSize != SIZE_NONE) {
                mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTitleTextSize);
            }
        }
        if (mBackBtnDrawable != null) {
            mBackImageBtn.setImageDrawable(mBackBtnDrawable);
        }
        mTitleTextView.setOnClickListener(this);
        findViewById(R.id.back_btn_layout).setOnClickListener(this);
        mBackImageBtn.setOnClickListener(this);
        findViewById(R.id.setting_btn_layout).setOnClickListener(this);
        mSettingBtn.setOnClickListener(this);

        setOnClickListener(this);

        context.initTitle();

        // 设置状态栏高度
//        int statusheight = ScreenUtils.getStatusHeight2(getContext());
//        LogUtil.i("statusheight -> " + statusheight);
//        if (statusheight == 0) {
//            return;
//        }
//
//        /**
//         * TitleView布局文件中status_bar的高度
//         */
//        LayoutParams lp = (LayoutParams) mStatusView.getLayoutParams();
//        lp.height = statusheight;
//
////        mStatusView.requestLayout();
//        mStatusView.setLayoutParams(lp);
//
//        /**
//         * TitleView的高度
//         */
//        ViewGroup.MarginLayoutParams titleLp = (MarginLayoutParams) getLayoutParams();
//        LogUtil.i("titleLp -> " + titleLp);
//        if (titleLp == null) {
//            titleLp = new MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT);
//            LogUtil.i("titleLp.height -> " + titleLp.height);
//            titleLp.height = statusheight;
//        } else {
//            LogUtil.i("titleLp.height -> " + titleLp.height);
//            titleLp.height += statusheight;
//        }
//
////        requestLayout();
//        setLayoutParams(titleLp);
    }

    public String getTitle() {
        return mTitleTextView.getText().toString();
    }

    public void setTitle(String title) {
        if (title == null) {
            mTitle = "";
        } else {
            mTitle = title;
        }
        mTitleTextView.setText(mTitle);
    }

    public void setTitle(int titleId) {
        mTitle = getContext().getString(titleId);
        mTitleTextView.setText(mTitle);
    }

    @SuppressWarnings("unused")
    public void setStatusColor(int color) {
        mStatusView.setBackgroundColor(color);
    }

    @SuppressWarnings("unused")
    public void setBackDrawable(Drawable drawable) {
        mBackBtnDrawable = drawable;
        mBackImageBtn.setImageDrawable(mBackBtnDrawable);
    }

    @SuppressWarnings("unused")
    public void setBackDrawable(int drawableId) {
        mBackImageBtn.setBackgroundResource(drawableId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTitleTextView.setText(mTitle);
    }

    public void setOnTitleClickListener(OnTitleClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.title:
            case R.id.back_btn_layout:
            case R.id.back_btn:
                if (mOnClickListener != null) {
                    mOnClickListener.onTitleBack();
                }
                break;
            case R.id.setting_btn_layout:
            case R.id.title_setting:
                if (mOnClickListener != null) {
                    mOnClickListener.onTitleSetting();
                }
                break;
            default:
                break;
        }
    }

    public void setBackBtnVisibility(int backBtnVisibility) {
        mBackImageBtn.setVisibility(backBtnVisibility);
    }

    public interface OnTitleClickListener {
        /**
         * click back
         */
        void onTitleBack();

        /**
         * click Setting
         */
        void onTitleSetting();
    }

    public void setSettingBtnResource(int resid) {
        mSettingBtn.setBackgroundResource(resid);
        mSettingBtn.setVisibility(VISIBLE);
    }

    public void setSettingBtnVisibility(int visibility) {
        mSettingBtn.setVisibility(visibility);
    }

}
