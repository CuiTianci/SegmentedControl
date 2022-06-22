package com.ctc.segmentedcontrol;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SegmentedControl extends View {

    private static final int FONT_SIZE = 16; // 文字尺寸。
    private static final int RADIUS = 24;
    private static final float BORDER_WIDTH_IN_DP = 1.0f; // 边框宽度。
    private static final long ANIMATION_DURATION = 300; // 动画时长。
    private static final int VALUE_UNSET = -10;
    private String[] titles;
    private int currentIndex = 0;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int borderColor = Color.parseColor("#C9D4E2"); // 边框颜色。
    private final int controlColor = Color.parseColor("#2988FF"); // 选中背景（按钮）颜色。
    private final int selectedTextColor = Color.parseColor("#FFFFFF"); // 选中文字颜色。
    private final int unselectedTextColor = Color.parseColor("#802988FF"); // 未选中文字颜色。
    private final Rect titleRectForHeight = new Rect();
    private SelectionChangeListener selectionChangeListener;
    private float currentControlStart = VALUE_UNSET; // 记录当前选中按钮的起始位置，用来做动画。
    private ValueAnimator controlStartXAnimator;


    public SegmentedControl(Context context) {
        this(context, null);
    }

    public SegmentedControl(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SegmentedControl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 需要先调用 setTitles 方法。
        if (titles == null || titles.length < 2) return;
        decorateBorder();
        float borderWidth = dp(BORDER_WIDTH_IN_DP);
        canvas.drawRoundRect(borderWidth / 2, borderWidth / 2, getWidth() - borderWidth / 2, getHeight() - borderWidth / 2, dp(RADIUS), dp(RADIUS), paint);
        decorateControl();
        float controlWidth = ((float) getWidth() / titles.length);
        if (currentControlStart == VALUE_UNSET) currentControlStart = currentIndex * controlWidth;
        canvas.drawRoundRect(currentControlStart, 0, controlWidth + currentControlStart, getHeight(), dp(RADIUS), dp(RADIUS), paint);
        decorateSelectedText();
        drawTitles(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (titles == null || titles.length < 2) return true;
        if (event.getPointerCount() > 1) return true;
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
            event.getPointerCoords(0, pointerCoords);
            // 忽略垂直方向上超出 View 的点击，不考虑 padding 因素。
            if (pointerCoords.y > getHeight() || pointerCoords.y < 0) return true;
            // 计算 touch 区域，所属 index 范围。
            int touchAreaIndex = (int) (pointerCoords.x / (getWidth() / titles.length));
            // 避免极端计算情况（因为存在多处 float 与 int 的转换），超出 index 范围。重复点击同一区域无效。
            if (touchAreaIndex >= 0 && touchAreaIndex <= titles.length - 1 && currentIndex != touchAreaIndex) {
                currentIndex = touchAreaIndex; // 记录当前选中。
                // 选中变化回调。
                if (selectionChangeListener != null)
                    selectionChangeListener.onSelectionChanged(currentIndex);
                // 动画部分。
                if (controlStartXAnimator != null) controlStartXAnimator.cancel();
                float controlWidth = ((float) getWidth() / titles.length);
                controlStartXAnimator = ValueAnimator.ofFloat(currentControlStart, currentIndex * controlWidth);
                controlStartXAnimator.setDuration(ANIMATION_DURATION);
                controlStartXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                controlStartXAnimator.addUpdateListener(animation -> {
                    currentControlStart = (float) (animation.getAnimatedValue());
                    invalidate();
                });
                controlStartXAnimator.start();
            }
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (controlStartXAnimator != null) controlStartXAnimator.cancel();
    }

    /**
     * 设置 titles。
     */
    public void setTitles(@NonNull String[] titles) {
        this.setTitles(titles, 0);
    }

    /**
     * 设置 titles。
     * @param initIndex 初始选中索引。
     */
    public void setTitles(@NonNull String[] titles, int initIndex) {
        if (titles.length < 2) {
            throw new IllegalArgumentException("At least 2 titles should be provided.");
        }
        for (String title : titles) {
            if (TextUtils.isEmpty(title)) {
                throw new IllegalArgumentException("Any of the titles must not be empty.");
            }
        }
        currentIndex = initIndex;
        this.titles = titles;
        invalidate();
    }

    public void setSelectionChangeListener(SelectionChangeListener selectionChangeListener) {
        this.selectionChangeListener = selectionChangeListener;
    }

    /**
     * 获取当前选中索引。
     */
    public int getSelectedIndex() {
        return currentIndex;
    }

    private void drawTitles(Canvas canvas) {
        decorateUnselectedText(); // 用来设置文字大小，支持后续测量。
        float[] titleWidths = new float[titles.length];
        for (int i = 0; i < titles.length; i++) {
            float titleWidth = paint.measureText(titles[i]);
            titleWidths[i] = titleWidth;
        }
        float controlWidth = (float) getWidth() / titles.length;
        // 获取任意 title 的首个字母的 bound，旨在获取文字高度。
        paint.getTextBounds(titles[0], 0, 1, titleRectForHeight);
        // 获取文字顶部坐标。
        float titleBaseLineY = ((float) getHeight() + titleRectForHeight.height()) / 2;
        for (int i = 0; i < titles.length; i++) {
            boolean isSelected = currentIndex == i;
            if (isSelected) decorateSelectedText();
            else decorateUnselectedText();
            // 定位并绘制文字。
            float titleHCenter = (i + 0.5f) * controlWidth;
            float titleLeft = titleHCenter - (titleWidths[i] / 2);
            canvas.drawText(titles[i], titleLeft, titleBaseLineY, paint);
        }
    }

    private void decorateBorder() {
        paint.setColor(borderColor);
        paint.setStrokeWidth(dp(BORDER_WIDTH_IN_DP));
        paint.setStyle(Paint.Style.STROKE);
    }

    private void decorateControl() {
        paint.setColor(controlColor);
        paint.setStyle(Paint.Style.FILL);
    }

    private void decorateSelectedText() {
        paint.setColor(selectedTextColor);
        paint.setTextSize(sp(FONT_SIZE));
    }

    private void decorateUnselectedText() {
        paint.setColor(unselectedTextColor);
        paint.setTextSize(sp(FONT_SIZE));
    }

    private int dp(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int sp(float sp) {
        final float scale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    public interface SelectionChangeListener {
        void onSelectionChanged(int index);
    }
}
