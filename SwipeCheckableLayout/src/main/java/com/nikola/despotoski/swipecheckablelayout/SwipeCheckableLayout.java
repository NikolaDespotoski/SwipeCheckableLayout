package com.nikola.despotoski.swipecheckablelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

/**
 * Created by nikola on 9/15/14.
 */
public class SwipeCheckableLayout extends ViewGroup implements Checkable {


    public interface OnSwipeListener{
        public void onSwipe(boolean isChecked, float offset);
        public void onSwipeStateChanged(int state);
    }

    private static final float DEFAULT_CHECKING_SENSITIVITY = 0.0f;
    private float mSensitivity = DEFAULT_CHECKING_SENSITIVITY;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private OnSwipeListener mSwiperListener;
    private ViewDragHelper mViewDragHelper;
    private boolean mChecked = false;
    private View mHiddenView;
    public SwipeCheckableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);

    }

    public SwipeCheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);

    }
    private void init(Context context, AttributeSet attrs, int defStyle){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeCheckableLayout, 0, defStyle);
        mSensitivity = a.getFloat(R.styleable.SwipeCheckableLayout_sensitivity, DEFAULT_CHECKING_SENSITIVITY);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.checked});
        mChecked = a.getBoolean(0, mChecked);
        a.recycle();
        setupViewDragHelper();
    }
    public void setOnSwipeListener(OnSwipeListener listener){
        mSwiperListener = listener;
    }
    public void setCheckableSensitivity(float sensitivity){
        if(sensitivity < 0 || sensitivity > 1.0){
            throw new IllegalArgumentException("Checkable sensitivity must be between 0.0 and 1.0, not "+sensitivity);
        }
        mSensitivity = sensitivity;
    }
    private void setupViewDragHelper(){
        mViewDragHelper = ViewDragHelper.create(this, new SwipeDragHelper());
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mViewDragHelper.processTouchEvent(ev);
        return true;
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof SwipeCheckableLayout.LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new SwipeCheckableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new SwipeCheckableLayout.LayoutParams(getContext(), attrs);
    }
    private float getHiddenViewVisibilityOffset() {
        return ((LayoutParams)  mHiddenView.getLayoutParams()).onScreenVisibility;
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        int contentTop = paddingTop;
        final int contentLeft = getWidth() + paddingLeft;
        final int contentWidth = r - l - contentLeft - getPaddingRight();

        final int parentLeft = getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();
            mHiddenView = getChildAt(0);
            mHiddenView.layout(-mHiddenView.getMeasuredWidth(), contentTop, contentTop, mHiddenView.getMeasuredHeight());
            float onScreen = (float) (contentWidth + mHiddenView.getLeft()) / mHiddenView.getMeasuredWidth();
            SwipeCheckableLayout.LayoutParams params = (LayoutParams) mHiddenView.getLayoutParams();
            params.onScreenVisibility = onScreen;
            Rect rect = new Rect();
            mHiddenView.getLocalVisibleRect(rect);
            Log.i("onLayout", " top: " + rect.top + " left: " + rect.left + " bottom: " + rect.bottom + " right: " + rect.bottom);

        for (int i = 1; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = parentLeft;
                int childTop = parentTop;

                final int gravity = lp.gravity;

                if (gravity != -1) {
                    Log.i("onLayout"," "+(gravity == GravityCompat.START || gravity == Gravity.RIGHT));
                    final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                   switch (horizontalGravity) {
                        case Gravity.LEFT:
                            childLeft = parentLeft + lp.leftMargin;
                            break;
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                    lp.leftMargin - lp.rightMargin;
                            break;
                         case Gravity.RIGHT:
                         case GravityCompat.START:
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        default:
                            childLeft = parentLeft + lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = parentTop + lp.topMargin;
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                    lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = parentBottom - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = parentTop + lp.topMargin;
                    }
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }

        }
    }


    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean b) {
        if (b != mChecked) {
            mChecked = b;
            refreshDrawableState();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int desiredWidth = 300;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        double desiredHeight = 300;
        double height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.max(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        int widthUsed = 0;
        int heightUsed = 0;

        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec,heightMeasureSpec);
            heightUsed = Math.max(heightUsed, getMeasuredHeightWithMargins(child));
            if(BuildConfig.DEBUG) {
                widthUsed += getMeasuredWidthWithMargins(child);
                Log.i("measuringChild ", MeasureSpec.toString(widthMeasureSpec) + " " + i + " w:" + widthUsed + " h: " + heightUsed);
            }
        }
        setMeasuredDimension(resolveSize(widthSize, widthMeasureSpec), resolveSize(heightUsed, heightMeasureSpec));
    }
    private void setHiddenViewOffset(float offset){
        //TODO listener;
        Log.i("setHiddenViewOffset()", " "+offset);
        ((LayoutParams) mHiddenView.getLayoutParams()).onScreenVisibility = offset;
    }

    private int getWidthWithMargins(View child) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getWidth() + lp.leftMargin + lp.rightMargin;
    }

    private int getHeightWithMargins(View child) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
    }

    private int getMeasuredWidthWithMargins(View child) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
    }

    private int getMeasuredHeightWithMargins(View child) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
    }
    public void toggle() {
        setChecked(!mChecked);
    }
    public void toggle(boolean animate) {
        mViewDragHelper.smoothSlideViewTo(mHiddenView, mHiddenView.getWidth(), mHiddenView.getTop());
        setChecked(!mChecked);
    }
    public View getHiddenView() {
        return mHiddenView;
    }

    @Override
    public int[] onCreateDrawableState(int state) {
        final int[] drawableState = super.onCreateDrawableState(state + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mViewDragHelper.cancel();
            return false;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);

    }
    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    void printViewRect(String oldNew, View v){
        Rect rect = new Rect();
        v.getLocalVisibleRect(rect);
        Log.i("Rect "+oldNew," top: "+rect.top + " left: "+rect.left + " bottom: "+rect.bottom + " right: "+rect.bottom );
    }
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private static final int[] LAYOUT_ATTRS = new int[] { android.R.attr.layout_gravity };
        public int gravity = -1;
        public float onScreenVisibility = 0f;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(Context c, AttributeSet attrs, int gravity) {
            super(c, attrs);
            this.gravity = gravity;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(MarginLayoutParams source, int gravity) {
            super(source);
            this.gravity = gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source, int gravity) {
            super(source);
            this.gravity = gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }

    private class SwipeDragHelper extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            dispatchSwipeStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float offset;
            final int childWidth =  mHiddenView.getWidth();
            for(int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if(child != changedView)
                    child.offsetLeftAndRight(dx);
            }
            offset = (float) (childWidth +  mHiddenView.getLeft()) / childWidth;
            dispatchOnSwipeEvent(offset);
            setHiddenViewOffset(offset);
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final float offset = getHiddenViewVisibilityOffset();
           // final int childWidth = releasedChild.getWidth();
            mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), releasedChild.getTop());
            invalidate();
            Log.i("onViewReleased", "offset: "+offset);
            mViewDragHelper.smoothSlideViewTo(mHiddenView, -mHiddenView.getMeasuredWidth(), mHiddenView.getTop());
            boolean respectSensitivity = offset >= mSensitivity;
            boolean defaultSensitivity = mSensitivity != DEFAULT_CHECKING_SENSITIVITY;
            if(respectSensitivity && !defaultSensitivity) {
                setChecked(!isChecked());
            }else if(defaultSensitivity){
                setChecked(!isChecked());
            }
        }




        @Override
        public int getViewHorizontalDragRange(View child) {
            return  mHiddenView.getWidth();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound =  mHiddenView.getWidth();
            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
            return newLeft;
        }

    }

    private void dispatchSwipeStateChanged(int state) {
        if(mSwiperListener != null){
            mSwiperListener.onSwipeStateChanged(state);
        }
    }

    private void dispatchOnSwipeEvent(float offset) {
        if(mSwiperListener != null){
            mSwiperListener.onSwipe(isChecked(), offset);
        }
    }
}


