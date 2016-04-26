package com.bcgdv.asia.lib.fanmenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

/**
 * Created by Ali Muzaffar on 11/04/2016.
 */
public class FanMenuButtons extends TextView {
    protected long mAnimationDuration = 200;
    protected float mAngleBetweenButtons = 10; //degrees

    protected String[] menus = null;
    protected int[] colors = null;

    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint mTxtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Drawable[] drawables = null;
    protected RectF[] mButtons = null;
    protected Bitmap[] mBitmap = null;
    protected Canvas[] mCanvas = null;
    protected Matrix[] mMatrix = null;
    protected Rect[] mTextBounds = null;

    protected float mButtonHeight = 32; //in dp
    protected float mButtonWidth = 32; //default value it will be overridden to take up available space.
    protected float mRotateAngle = mAngleBetweenButtons;
    protected float mAdditionalRotateAngle = 0; //used for touch movement.
    protected float mMinBounceBackAngle = -1000;
    protected float mMaxBounceBackAngle = 1000;

    protected float mDrawablePadding = 8;
    protected float mButtonLeftStart = 0;

    protected OnFanClickListener mClickListener;
    protected OnFanAnimationListener mAnimListener;

    private int mViewScaledTouchSlop = 0; //internal variable
    private boolean mAnimatingIn = false;
    private boolean mAnimationInProgress = false;
    private boolean mEnableTouchMovement = true;
    private boolean mRememberMovedPosition = false;

    private float dp2 = 2;
    private float dp1 = 1;

    /**
     * Constructor for FanMenuButtons
     *
     * @param context Context
     */
    public FanMenuButtons(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Constructor for FanMenuButtons
     *
     * @param context Context
     * @param attrs AttributeSet
     */
    public FanMenuButtons(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Constructor for FanMenuButtons
     *
     * @param context Context
     * @param attrs AttributeSet
     * @param defStyleAttr Style
     */
    public FanMenuButtons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initialize the FanMenuButtons widget
     *
     * @param context Context
     * @param attrs AttributeSet
     */
    protected void init(Context context, AttributeSet attrs) {
        float multi = getResources().getDisplayMetrics().density;
        dp2 *= multi;
        dp1 *= multi;
        mPaint.setAntiAlias(true);
        mTxtPaint = new Paint(getPaint());
        mTxtPaint.setColor(getTextColors().getDefaultColor());
        mTxtPaint.setAntiAlias(true);

        mButtonHeight = multi * mButtonHeight;
        mDrawablePadding = multi * mDrawablePadding;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FanMenuButtons, 0, 0);
        try {
            TypedValue outValue = new TypedValue();
            ta.getValue(R.styleable.FanMenuButtons_fanButtonColors, outValue);
            int colorsArrayResId = outValue.resourceId;

            outValue = new TypedValue();
            ta.getValue(R.styleable.FanMenuButtons_fanLeftDrawable, outValue);
            int leftDrawablesArrayResId = outValue.resourceId;

            outValue = new TypedValue();
            ta.getValue(R.styleable.FanMenuButtons_fanMenuLabels, outValue);
            int menuLabelsArrayResId = outValue.resourceId;

            try {
                menus = context.getResources().getStringArray(menuLabelsArrayResId);
            } catch (Resources.NotFoundException nfe) {
                if (isInEditMode()) {
                    menus = new String[]{"Submenu 1", "Submenu 2", "Submenu 3", "Submenu 4"};
                }
            }

            try {
                colors = new int[menus.length];
                if (isInEditMode()) {
                    int[] tempColors = new int[]{android.R.color.holo_red_dark, android.R.color.holo_blue_dark, android.R.color.holo_green_dark, android.R.color.holo_orange_dark};
                    for (int i = 0; i < menus.length; i++) {
                        colors[i] = ContextCompat.getColor(context, tempColors[i % tempColors.length]);
                    }
                } else {
                    TypedArray colorType = context.getResources().obtainTypedArray(colorsArrayResId);
                    for (int i = 0; i < colors.length; i++) {
                        colors[i] = colorType.getColor(i, colors[i]);
                    }
                    colorType.recycle();
                }
            } catch (Resources.NotFoundException nfe) {
            }

            try {
                drawables = new Drawable[menus.length];
                if (isInEditMode()) {
                    for (int i = 0; i < drawables.length; i++) {
                        drawables[i] = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_my_calendar);
                    }
                } else {
                    TypedArray icons = context.getResources().obtainTypedArray(leftDrawablesArrayResId);
                    for (int i = 0; i < drawables.length; i++) {
                        drawables[i] = icons.getDrawable(i);
                    }
                    icons.recycle();
                }
            } catch (Resources.NotFoundException nfe) {
            }

            mAnimationDuration = ta.getInt(R.styleable.FanMenuButtons_fanAnimationDuration, (int) mAnimationDuration);
            mAngleBetweenButtons = ta.getInt(R.styleable.FanMenuButtons_fanAngleBetweenButtons, (int) mAngleBetweenButtons);
            mDrawablePadding = ta.getDimension(R.styleable.FanMenuButtons_fanLeftDrawablePadding, mDrawablePadding);
            mButtonHeight = ta.getDimension(R.styleable.FanMenuButtons_fanButtonHeight, mButtonHeight);
            mEnableTouchMovement = ta.getBoolean(R.styleable.FanMenuButtons_fanEnableTouchMovement, mEnableTouchMovement);
            mRememberMovedPosition = ta.getBoolean(R.styleable.FanMenuButtons_fanRememberMovedPosition, mRememberMovedPosition);
            mMinBounceBackAngle = ta.getInt(R.styleable.FanMenuButtons_fanMinBounceBackAngle, (int) mMinBounceBackAngle);
            mMaxBounceBackAngle = ta.getInt(R.styleable.FanMenuButtons_fanMaxBounceBackAngle, (int) mMaxBounceBackAngle);

        } finally {
            ta.recycle();
        }

        mButtons = new RectF[menus.length];
        mBitmap = new Bitmap[menus.length];
        mCanvas = new Canvas[menus.length];
        mMatrix = new Matrix[menus.length];
        mTextBounds = new Rect[menus.length];

        if (!isInEditMode() && getVisibility() == View.GONE) {
            //View can't initially be GONE. This prevents on Measure and onSizeChanges from
            //being triggered and so bitmaps are not created.
            setVisibility(View.INVISIBLE);
        }

        final ViewConfiguration viewConfig = ViewConfiguration.get(context);
        mViewScaledTouchSlop = viewConfig.getScaledTouchSlop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initIfNeeded(w, h);
    }

    /**
     * Setup the widget if needed.
     *
     * @param w int, width available to the widget.
     * @param h int, height available to the widget.
     */
    protected void initIfNeeded(int w, int h) {
        if (mCanvas[0] == null) {
            mButtonLeftStart = getPaddingLeft();
            mButtonWidth = w - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < mButtons.length; i++) {
                mBitmap[i] = Bitmap.createBitmap((int) mButtonWidth, (int) (mButtonHeight + dp2), Bitmap.Config.ARGB_8888);
                mButtons[i] = new RectF(mButtonLeftStart, dp1, mButtonWidth, mButtonHeight);
                mCanvas[i] = new Canvas(mBitmap[i]);
                setupMatrix(i);
                mTextBounds[i] = new Rect();
                getPaint().getTextBounds(menus[i], 0, menus[i].length(), mTextBounds[i]);
                mCanvas[i].drawColor(Color.TRANSPARENT);
                drawBitmap(i);
            }
        }
    }

    /**
     * Setup the rotation matrix for all buttons.
     *
     * This is used in onDraw to rorate the button image to the correct angle and
     * move it to the bottom of the screen.
     */
    protected void setupMatrixs() {
        for (int i = 0; i < mButtons.length; i++) {
            setupMatrix(i);
        }
    }

    /**
     * Setup the rotation matrix for the button at index.
     *
     * This is used in onDraw to rorate the button image to the correct angle and
     * move it to the bottom of the screen.
     *
     * @param i index of the button
     */
    protected void setupMatrix(int i) {
        if (mMatrix[i] == null) {
            mMatrix[i] = new Matrix();
        }
        mMatrix[i].setRotate((i * mRotateAngle) + mAdditionalRotateAngle, mBitmap[i].getWidth() - mButtonHeight / 2, mBitmap[i].getHeight() / 2);
        //mMatrix[i].postTranslate(0, getHeight() - mButtonHeight - mPadding - getPaddingBottom());
        mMatrix[i].postTranslate(0, getHeight() - mButtonHeight - getPaddingBottom());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAnimatingIn) {
            //drawAllBitmaps();
            //Extreme optimization below instead.
            if (mButtons[menus.length - 1].left != mButtonLeftStart) {
                //if we are animating in or out the bar (growing it), we only need to
                //draw the top most bar. No point in re-drawing all the bitmaps
                int index = menus.length - 1;
                drawBitmap(index);
                canvas.drawBitmap(mBitmap[index], mMatrix[index], null);
                return;
            }
        }

        for (int i = 0; i < mButtons.length; i++) {
            canvas.drawBitmap(mBitmap[i], mMatrix[i], null);
        }

        //Debug touch events
        /*
        if (mTouchArea != null) {
            canvas.drawRect(mTouchArea, mPaint);
        }*/

    }

    /**
     * Draw all bitmaps (button images). If the images already exist, they will be wiped
     * before being redrawn.
     */
    protected void drawAllBitmaps() {
        for (int i = 0; i < mButtons.length; i++) {
            drawBitmap(i);
        }
    }

    /**
     * Draw te button bitmap for the given index. This will clear the image first.
     *
     * @param i the index of the button
     */
    protected void drawBitmap(int i) {
        mCanvas[i].drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mPaint.setColor(colors[i]);
        mPaint.setAntiAlias(true);
        mCanvas[i].drawRoundRect(mButtons[i], mButtonHeight / 2, mButtonHeight / 2, mPaint);
        if (drawables[i] != null) {
            mCanvas[i].drawText(menus[i], mButtons[i].left + mButtonHeight + mDrawablePadding, mButtons[i].centerY() + mTextBounds[i].height() / 2 - dp2, mTxtPaint);
            drawables[i].setBounds(
                    (int) (mButtons[i].left + dp1),
                    (int) (mButtons[i].top + dp1),
                    (int) (mButtons[i].left - dp1 + mButtonHeight),
                    (int) (mButtons[i].bottom - dp1));
            drawables[i].draw(mCanvas[i]);
        } else {
            mCanvas[i].drawText(menus[i], mButtons[i].left + mDrawablePadding, mButtons[i].centerY() + mTextBounds[i].height() / 2 - dp2, mTxtPaint);
        }
    }

    /**
     * Toggle visibility of the widget.
     *
     * This will animate the show and hide.
     */
    public void toggleShow() {
        if (getVisibility() == View.VISIBLE) {
            animateOut();
        } else {
            animateIn();
        }
    }

    /**
     * Animate and show the widget.
     */
    public void animateIn() {
        if (mAnimationInProgress) {
            return;
        }
        if (getBackground() != null) {
            getBackground().setAlpha(0);
        }
        mAnimationInProgress = true;
        initIfNeeded(getWidth(), getHeight());
        ValueAnimator grow = ValueAnimator.ofInt((int) mButtonWidth, (int) mButtonLeftStart);
        grow.setInterpolator(new DecelerateInterpolator());
        grow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int w = (Integer) animation.getAnimatedValue();
                for (int i = 0; i < mButtons.length; i++) {
                    mButtons[i].left = w;
                }
                invalidate();
            }
        });

        ValueAnimator alpha = ValueAnimator.ofInt(0, 255);
        alpha.setDuration(mAnimationDuration);
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int a = (Integer) animation.getAnimatedValue();
                if (getBackground() != null) {
                    getBackground().setAlpha(a);
                }
            }
        });

        ValueAnimator rotate = ValueAnimator.ofFloat(0, mAngleBetweenButtons);
        rotate.setInterpolator(new OvershootInterpolator());
        rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mAnimatingIn) {
                    //Some issue where it doesn't animate to full
                    //length, so force it to redraw.
                    drawBitmap(menus.length - 1);
                }
                mAnimatingIn = false;
                float r = (Float) animation.getAnimatedValue();
                mRotateAngle = r;
                setupMatrixs();
                invalidate();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatingIn = true;
                mRotateAngle = 0;
                setupMatrixs();
                FanMenuButtons.this.setVisibility(View.VISIBLE);
                if (mAnimListener != null) {
                    mAnimListener.onAnimateInStarted();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatingIn = false;
                for (int i = 0; i < mButtons.length; i++) {
                    mButtons[i].left = mButtonLeftStart;
                }
                mRotateAngle = mAngleBetweenButtons;
                setupMatrixs();
                mAnimationInProgress = false;
                invalidate();
                if (mAnimListener != null) {
                    mAnimListener.onAnimateInFinished();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.setDuration(mAnimationDuration);
        set.playSequentially(grow, rotate);
        set.start();
        if (getBackground() != null) {
            alpha.start();
        }
    }

    /**
     * Animate and hide the widget.
     */
    public void animateOut() {
        if (mAnimationInProgress) {
            return;
        }
        mAnimationInProgress = true;
        ValueAnimator grow = ValueAnimator.ofInt((int) mButtonLeftStart, (int) mButtonWidth);
        grow.setInterpolator(new AccelerateInterpolator());
        grow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatingIn = true;
                int w = (Integer) animation.getAnimatedValue();
                for (int i = 0; i < mButtons.length; i++) {
                    mButtons[i].left = w;
                }
                invalidate();
            }
        });

        ValueAnimator alpha = ValueAnimator.ofInt(255, 0);
        alpha.setDuration(mAnimationDuration);
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int a = (Integer) animation.getAnimatedValue();
                if (getBackground() != null) {
                    getBackground().setAlpha(a);
                }
            }
        });

        ValueAnimator rotate = ValueAnimator.ofFloat(mAngleBetweenButtons, 0);
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float r = (Float) animation.getAnimatedValue();
                mRotateAngle = r;
                setupMatrixs();
                invalidate();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatingIn = false;
                mRotateAngle = mAngleBetweenButtons;
                setupMatrixs();
                if (mAnimListener != null) {
                    mAnimListener.onAnimateOutStarted();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatingIn = false;
                for (int i = 0; i < mButtons.length; i++) {
                    mButtons[i].left = mButtonLeftStart;
                }
                mRotateAngle = 0;
                setupMatrixs();
                FanMenuButtons.this.setVisibility(View.GONE);
                mAnimationInProgress = false;
                invalidate();
                if (mAnimListener != null) {
                    mAnimListener.onAnimateOutFinished();
                }
                mAdditionalRotateAngle = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.setDuration(mAnimationDuration);
        set.playSequentially(rotate, grow);
        set.start();
        if (getBackground() != null) {
            alpha.start();
        }
    }

    /**
     * Set callback for the fan button click events.
     *
     * @param l OnFanClickListener
     */
    public void setOnFanButtonClickListener(OnFanClickListener l) {
        mClickListener = l;
    }

    /**
     * Set a callback which is informated when animations starts/ends for the widget.
     *
     * @param l OnFanAnimationListener
     */
    public void setOnFanAnimationListener(OnFanAnimationListener l) {
        mAnimListener = l;
    }

    /**
     * Set the selected state on the button with the provided index and reset all
     * the other buttons to normal state.
     *
     * @param index the index of the button to mark as selected.
     */
    private void setButtonSelected(int index) {
        if (drawables == null) {
            return; //no need to do anything
        }
        for (int i = 0; i < drawables.length; i++) {
            if (drawables[i] == null) {
                return;//no need to do anything
            }
            if (i == index) {
                drawables[i].setState(new int[]{android.R.attr.state_selected});
            } else {
                drawables[i].setState(new int[]{android.R.attr.state_focused});
            }
        }
        drawAllBitmaps();
        invalidate();
    }

    private long mDownTime = 0;
    private RectF mTouchArea = new RectF(0, 0, 0, 0);
    private boolean mPointerDown = false;
    private boolean mIsMoving = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mClickListener == null || getVisibility() == View.INVISIBLE || mAnimationInProgress) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownTime = System.currentTimeMillis();
            float x = event.getX();
            float y = event.getY();
            mTouchArea.left = x;
            mTouchArea.top = y;
            mTouchArea.right = getWidth() - getPaddingRight();
            mTouchArea.bottom = getHeight() - getPaddingBottom();
            mPointerDown = true;
            return true;

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (System.currentTimeMillis() - mDownTime < 200 && !mIsMoving) {
                //tan theta = opp/adj

                double angle = Math.toDegrees(Math.atan2(mTouchArea.height(), mTouchArea.width()));
                if (mTouchArea.width() < mButtonWidth / 2 && angle < menus.length * mRotateAngle) {
                    //Do nothing, clicked too close to the FAB.
                    if (BuildConfig.DEBUG) {
                        Log.d("TEST", "TOO CLOSE TO FAB, DO NOTHING");
                    }
                    mPointerDown = false;
                    animatedAdditionalRotationToZero();
                    return false;
                }

                if (BuildConfig.DEBUG) {
                    Log.d("TEST", "ANGLE = " + angle);
                }
                float qtrRotation = mRotateAngle / 4;
                for (int i = menus.length - 1; i >= 0; i--) {
                    float curAngle = (i * mRotateAngle) + mAdditionalRotateAngle;
                    float nextAngle = curAngle + mRotateAngle;
                    if (angle > curAngle - qtrRotation && angle < nextAngle - qtrRotation) {
                        setButtonSelected(i);
                        mClickListener.onFanButtonClicked(i);
                        mPointerDown = false;
                        animatedAdditionalRotationToZero();
                        return true;
                    }
                }

                //Clicked outside the buttons, close.
                animateOut();
            }
            mIsMoving = false;
            mPointerDown = false;
            animatedAdditionalRotationToZero();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!mEnableTouchMovement) {
                return false;
            }
            boolean isPrimMoving = isScrollGesture(event, 0, mTouchArea.left, mTouchArea.top);
            if (isPrimMoving) {
                mIsMoving = true;
                final float diffPrimX = mTouchArea.left - event.getX(0);
                final float diffPrimY = mTouchArea.top - event.getY(0);
                float moveBy = Math.abs(diffPrimX);
                boolean add = diffPrimX > 0;
                if (Math.abs(diffPrimX) < Math.abs(diffPrimY)) {
                    moveBy = Math.abs(diffPrimY);
                    add = diffPrimY < 0;
                }
                moveBy = moveBy / dp2;
                if (moveBy > 1) {
                    //treat 1 this as a half of a degree.
                    //and convert to degrees.
                    moveBy *= 0.50;
                    if (add) {
                        mAdditionalRotateAngle = -moveBy;
                    } else {
                        mAdditionalRotateAngle = moveBy;
                    }

                    setupMatrixs();
                    invalidate();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method animates the fan back to the starting position, if the appropriate flags are set.
     */
    private void animatedAdditionalRotationToZero() {
        if (mAdditionalRotateAngle == 0 || mAnimationInProgress) {
            return;
        }

        if (mRememberMovedPosition) {
            if (!(mAdditionalRotateAngle > mMaxBounceBackAngle || mAdditionalRotateAngle < mMinBounceBackAngle)) {
                return;
            }
        }

        mAnimationInProgress = true;
        ValueAnimator anim = ValueAnimator.ofFloat(mAdditionalRotateAngle, 0);
        anim.setInterpolator(new OvershootInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAdditionalRotateAngle = (Float) animation.getAnimatedValue();
                setupMatrixs();
                invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationInProgress = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.setStartDelay(mAnimationDuration / 2);
        anim.start();
    }

    /**
     * Have we moved enough to trigger a scroll
     *
     * @param event MotionEvent.
     * @param ptrIndex Pointer index.
     * @param originalX x-coordinate
     * @param originalY y-coordinate
     *
     * @return boolean true if we detect scrolling
     */
    protected boolean isScrollGesture(MotionEvent event, int ptrIndex, float originalX, float originalY) {
        float moveX = Math.abs(event.getX(ptrIndex) - originalX);
        float moveY = Math.abs(event.getY(ptrIndex) - originalY);

        if (moveX > mViewScaledTouchSlop || moveY > mViewScaledTouchSlop) {
            return true;
        }
        return false;
    }

    /**
     * An interface for click events on the buttons.
     */
    public interface OnFanClickListener {
        void onFanButtonClicked(int index);
    }

    /**
     * An interface for the animation events of the widget.
     */
    public interface OnFanAnimationListener {
        void onAnimateInStarted();

        void onAnimateOutStarted();

        void onAnimateInFinished();

        void onAnimateOutFinished();

    }
}
