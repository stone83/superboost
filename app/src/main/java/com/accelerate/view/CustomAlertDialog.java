package com.accelerate.view;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.accelerate.utils.DimenUtils;
import com.boost.booster.clean.R;
import com.ccmt.library.util.ViewUtil;

import static android.view.View.GONE;

/**
 * 公用的基本Dialog，便于统一样式
 * Created by yangxl on 2016/8/19.
 */
public class CustomAlertDialog extends AlertDialog implements DialogInterface {

    @SuppressWarnings("unused")
    protected static final float DIALOG_RATIO_TO_SCREEN_WIDTH = 0.8f;

    //对话框title布局参数
    private static final int DIALOG_TITLE_MARGIN_LEFT = 24;
    private static final int DIALOG_TITLE_TOTAL_HEIGTH = 50;
    private static final int DIALOG_TITLE_SIZE = 16;
    private static final int DIALOG_TITLE_COLOR = 0xFF333333;

    private static final int DIALOG_TITLE_SEPRATOR_COLOR = 0x33000000;
    private static final int DIALOG_TITLE_SEPRATOR_SIZE = 1;

    private View mContentView;

    /**
     * title布局
     */
    private LinearLayout mTitleLayout;
    private TextView mTitleText;

    /**
     * 两个按钮布局
     */
    private LinearLayout mButtonLayout;
    private LinearLayout mPositiveButtonLayout;
    private LinearLayout mNegativeButtonLayout;
    private View mSplitLineView;
    private Button mPositiveButton;
    private Button mNegativeButton;

    /**
     * 对话框内容布局
     */
    private FrameLayout mMessageLayout;

    /**
     * 通用的对话框内容容器
     */
    private LinearLayout mCommonMessageContainer;

    private TextView mMessageText;
    private CheckBox mNoPrompCheckBox;

    /**
     * 对话框关闭时的回调
     */
    private DialogCloseListener mDialogCloseListener;
    private View mHoriSplitLineView;

    private CustomAlertDialog(Context context) {
        this(context, R.style.custom_alter_dialog_style);
    }

    @SuppressWarnings("ConstantConditions")
    private CustomAlertDialog(Context context, int theme) {
        super(context, theme);

        // 设置对话框背景透明度 以及属性
        if (theme == R.style.custom_alter_dialog_style) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0.6f;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
//            FrameLayout root = (FrameLayout) findViewById(R.id.previewSV).getParent();
//            FrameLayout root = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.activity_main, null);

//            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

            Log.i("MyLog", "lp.dimAmount -> " + lp.dimAmount);
            Log.i("MyLog", "lp.alpha -> " + lp.alpha);
            Log.i("MyLog", "lp.screenBrightness -> " + lp.screenBrightness);
            Log.i("MyLog", "lp.buttonBrightness -> " + lp.buttonBrightness);

            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

//        lp.dimAmount = 0f;
            lp.format = PixelFormat.TRANSPARENT;
            lp.alpha = 0.6F;
//        lp.screenBrightness = 0.6F;
//        lp.buttonBrightness = 1F;
//            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
            window.setAttributes(lp);
//            windowManager.addView(root, lp);
        }
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.custom_alert_dialog_layout, null);
        FrameLayout.LayoutParams layoutParams = //
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        mTitleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mButtonLayout = (LinearLayout) view.findViewById(R.id.btn_layout);

        mPositiveButton = (Button) view.findViewById(R.id.btn_positive);
        mNegativeButton = (Button) view.findViewById(R.id.btn_negative);
        mSplitLineView = view.findViewById(R.id.seprator);
        mHoriSplitLineView = view.findViewById(R.id.hori_seprator);
        mPositiveButtonLayout = (LinearLayout) view.findViewById(R.id.btn_positive_layout);
        mNegativeButtonLayout = (LinearLayout) view.findViewById(R.id.btn_negative_layout);

        mMessageLayout = (FrameLayout) view.findViewById(R.id.content_layout);
        mMessageText = (TextView) view.findViewById(R.id.content);
        mNoPrompCheckBox = (CheckBox) view.findViewById(R.id.no_promp_checkbox);

        mCommonMessageContainer = (LinearLayout) view.findViewById(R.id.common_message_container);
        setView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(LayoutInflater.from(getContext()).inflate(layoutResID, null));
    }

    @Override
    public void setContentView(@NonNull View view) {
        mContentView = view;

        FrameLayout rootView = new FrameLayout(getContext());

        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

//        rootView.addView(mContentView, new FrameLayout.LayoutParams(
//                (int) (DimenUtils.getScreenWidth() * DIALOG_RATIO_TO_SCREEN_WIDTH),
//                RelativeLayout.LayoutParams.MATCH_PARENT));
        rootView.addView(mContentView);

//        mContentView.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        super.setContentView(rootView);
    }

    @SuppressWarnings("unused")
    public View getContentView() {
        return mContentView;
    }

    @Override
    public void dismiss() {
        dismiss(null, 0);
    }

    @SuppressWarnings("WeakerAccess")
    public void dismiss(OnClickListener buttonListener, int button_identifier) {
        if (buttonListener != null) {
            buttonListener.onClick(this, button_identifier);
        }
        super.dismiss();
    }

    @Override
    public void setOnDismissListener(final OnDismissListener listener) {
        super.setOnDismissListener(arg0 -> {
            if (listener != null) {
                listener.onDismiss(arg0);
            }
        });
    }

    public void setCustomTitle(View titleView) {
        mTitleLayout.removeAllViews();
        mTitleLayout.addView(titleView);
        mTitleLayout.setVisibility(View.VISIBLE);
    }

    public void setMessage(CharSequence message) {
        mMessageLayout.setVisibility(View.VISIBLE);
        mMessageText.setText(message);
    }

    private void setNoPrompCheckBox(boolean isShown,
                                    CompoundButton.OnCheckedChangeListener changedLister) {
        if (isShown) {
            mNoPrompCheckBox.setVisibility(View.VISIBLE);
            mNoPrompCheckBox.setChecked(false);
            mNoPrompCheckBox.setOnCheckedChangeListener(changedLister);
        }
    }

    public void setIcon(int iconId) {

    }

    public void setIcon(Drawable icon) {

    }

    private void setGoneButton(final int button_identifier) {
        switch (button_identifier) {
            case DialogInterface.BUTTON_POSITIVE:
                mPositiveButtonLayout.setVisibility(GONE);
                mSplitLineView.setVisibility(GONE);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mSplitLineView.setVisibility(GONE);
                mNegativeButtonLayout.setVisibility(GONE);
                break;
        }
        if (mPositiveButtonLayout.getVisibility() == GONE &&
                mNegativeButtonLayout.getVisibility() == GONE) {
            //不要GONE，会影响布局
            mHoriSplitLineView.setVisibility(View.INVISIBLE);
        }
    }

    public void setButton(final int button_identifier, CharSequence buttonText,
                          final OnClickListener buttonListener) {
        switch (button_identifier) {
            case DialogInterface.BUTTON_POSITIVE:
                mButtonLayout.setVisibility(View.VISIBLE);
                mPositiveButton.setText(buttonText);
                mPositiveButton.setOnClickListener(v -> CustomAlertDialog.this.dismiss(buttonListener, button_identifier));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonLayout.setVisibility(View.VISIBLE);
                mNegativeButton.setText(buttonText);
                mNegativeButton.setOnClickListener(v -> {
                    if (buttonListener != null) {
                        buttonListener.onClick(CustomAlertDialog.this,
                                button_identifier);
                    }
                    CustomAlertDialog.this.dismiss();
                });
                break;
        }
    }

    private void setMessageView(View view) {
        mMessageLayout.removeAllViews();
        mMessageLayout.setVisibility(View.VISIBLE);
        mMessageLayout.addView(view);
    }

    private void fixCommonMessagePadding() {
        //@note title不可见时，则要设置messagelayout与dialog上边距
//        mCommonMessageContainer.setPadding(0,
//                getContext().getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_top)
//                , 0, 0);

        // 适配,15dip在720*1280下的像素为30px,再用ViewUtil.obtainViewPx()方法算出在当前手机下应该为多少像素.
        mCommonMessageContainer.setPadding(0,
                ViewUtil.obtainViewPx(getContext(), 30, false)
                , 0, 0);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            mTitleLayout.setVisibility(GONE);
            //@note title不可见时，则要设置messagelayout与dialog上边距
            fixCommonMessagePadding();
            return;
        }
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleText.setText(title);
    }

    /**
     * 创建一个默认的TitleView
     */
    private static View createDefaultTitleView(Context context, String title) {

        FrameLayout titleLayout = new FrameLayout(context);
        titleLayout.setMinimumHeight(DimenUtils.dp2px(context, DIALOG_TITLE_TOTAL_HEIGTH));

        TextView textView = new TextView(context);
        FrameLayout.LayoutParams textParms =
                new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParms.leftMargin = DimenUtils.dp2px(context, DIALOG_TITLE_MARGIN_LEFT);
        textParms.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        textView.setLayoutParams(textParms);
        textView.setTextColor(DIALOG_TITLE_COLOR);
        textView.setTextSize(DIALOG_TITLE_SIZE);
        textView.setText(title);

        View line = new View(context);
        FrameLayout.LayoutParams lineParms =
                new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DIALOG_TITLE_SEPRATOR_SIZE);
        lineParms.gravity = Gravity.BOTTOM;
        line.setLayoutParams(lineParms);
        line.setBackgroundColor(DIALOG_TITLE_SEPRATOR_COLOR);
//        textView.setTypeface(TypefaceUtils.load(context.getAssets(),
//                AppConfig.OPENSANSLIGHT_FONT_PATH));

        titleLayout.addView(textView);
        titleLayout.addView(line);
        titleLayout.setVisibility(View.VISIBLE);
        return titleLayout;
    }

    private static class CustomParams {
        Context mContext;
        int theme;
        CharSequence title;
        View customTitleView;
        CharSequence message;
        int iconId;
        Drawable icon;
        CharSequence positiveButtonText;
        OnClickListener positiveButtonListener;

        CharSequence negativeButtonText;
        OnClickListener negativeButtonListener;
        boolean mCancelable = true;
        boolean mCanceledOnTouchOutside = true;
        OnCancelListener mOnCancelListener;
        OnDismissListener mOnDismissListener;
        OnKeyListener mOnKeyListener;
        View mMessageView;

        boolean isNoPrompCheckBoxShown;
        CompoundButton.OnCheckedChangeListener noPrompChangedLister;

        /**
         * 是否为全局对话框，全局对话框主要给后台服务使用
         */
        boolean isGlobalDialog;

        CustomParams(Context context, int theme) {
            this.mContext = context;
            this.theme = theme;
        }

        @SuppressWarnings("ConstantConditions")
        void apply(CustomAlertDialog dialog) {
            if (customTitleView != null) {
                dialog.setCustomTitle(customTitleView);
            } else {

                dialog.setTitle(title);
                if (iconId >= 0) {
                    dialog.setIcon(iconId);
                } else if (icon != null) {
                    dialog.setIcon(icon);
                }
            }
            if (mMessageView != null) {
                dialog.setMessageView(mMessageView);
            } else if (message != null) {
                dialog.setMessage(message);
            }

            if (positiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        positiveButtonText, positiveButtonListener);
            } else {
                dialog.setGoneButton(DialogInterface.BUTTON_POSITIVE);
            }

            if (negativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        negativeButtonText, negativeButtonListener);
            } else {
                dialog.setGoneButton(DialogInterface.BUTTON_NEGATIVE);
            }

            dialog.setNoPrompCheckBox(isNoPrompCheckBoxShown, noPrompChangedLister);

            if (isGlobalDialog) {
                int type = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    type = WindowManager.LayoutParams.TYPE_TOAST;
                } else {
                    type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                dialog.getWindow().setType(type);
            }
        }
    }

    public static class Builder {
        private CustomParams P;

        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int theme) {
            P = new CustomParams(context, theme);
        }

        public Context getContext() {
            return P.mContext;
        }

        public Builder setTitle(int titleId) {
            try {
                P.title = P.mContext.getString(titleId);
            } catch (Exception e) {
            }
            return this;
        }

        public Builder setTitle(CharSequence title) {
            P.title = title;
            return this;
        }

        public Builder setCustomTitle(View customTitleView) {
            P.customTitleView = customTitleView;
            return this;
        }

        //定义一个默认的custom title样式
        public Builder setCustomTitle(Context context, String title) {
            P.customTitleView = createDefaultTitleView(context, title);
            return this;
        }

        public Builder setMessage(int messageId) {
            try {
                P.message = P.mContext.getString(messageId);
            } catch (Exception e) {
            }
            return this;
        }

        public Builder setMessage(CharSequence message) {
            P.message = message;
            return this;
        }

        public Builder setIcon(int iconId) {
            P.iconId = iconId;
            return this;
        }

        public Builder setIcon(Drawable icon) {
            P.icon = icon;
            return this;
        }

        public Builder setPositiveButton(int textId,
                                         final OnClickListener listener) {
            P.positiveButtonText = P.mContext.getString(textId);
            P.positiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text,
                                         final OnClickListener listener) {
            P.positiveButtonText = text;
            P.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId,
                                         final OnClickListener listener) {
            P.negativeButtonText = P.mContext.getText(textId);
            P.negativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text,
                                         final OnClickListener listener) {
            P.negativeButtonText = text;
            P.negativeButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            P.mCanceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        /**
         * 调用该方法后,默认的message对应的TextView和CheckBox都会被删除,只有指定的view占着message区域.
         *
         * @param view
         * @return
         */
        public Builder setMessageView(View view) {
            P.mMessageView = view;
            return this;
        }

        /**
         * 调用该方法后,默认的message对应的TextView和CheckBox都会被删除,只有指定的view占着message区域.
         *
         * @param layoutResID
         * @return
         */
        public Builder setMessageView(int layoutResID) {
            P.mMessageView = LayoutInflater.from(getContext()).inflate(layoutResID, null);
            return this;
        }

        public Builder setGlobalDialog(boolean isGlobalDialog) {
            P.isGlobalDialog = isGlobalDialog;
            return this;
        }

        public Builder setNoPrompCheckBoxShown(
                boolean isShown, CompoundButton.OnCheckedChangeListener changedLister) {
            P.isNoPrompCheckBoxShown = isShown;
            P.noPrompChangedLister = changedLister;
            return this;
        }

        public CustomAlertDialog create() {
            final CustomAlertDialog dialog = P.theme == 0 ? new CustomAlertDialog(P.mContext)
                    : new CustomAlertDialog(P.mContext, P.theme);
            P.apply(dialog);
            dialog.setCancelable(P.mCancelable);
            dialog.setCanceledOnTouchOutside(P.mCanceledOnTouchOutside);
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (!P.mCancelable) {
                dialog.setOnKeyListener((dialog1, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    if (P.mOnKeyListener != null) {
                        P.mOnKeyListener.onKey(dialog1, keyCode, event);
                    }
                    dialog1.dismiss();
                    return false;
                });
            } else {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }

        public CustomAlertDialog show() {
            CustomAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    public String mExtraData;

    public void setExtraData(String extraData) {
        this.mExtraData = extraData;
    }

    public String getExtraData() {
        return mExtraData;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDialogCloseListener != null) {
            mDialogCloseListener.onBackPress();
        }
    }

    public void setDialogCloseListener(DialogCloseListener listener) {
        mDialogCloseListener = listener;
    }

    public interface DialogCloseListener {
        void onBackPress();
    }
}
