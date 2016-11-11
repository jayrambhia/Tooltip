package com.fenchtose.tooltip;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Dynamically add tooltips in any ViewGroups.
 * Use {@link Builder} to generate and inject Tooltip in your layout.
 * Use {@link #dismiss()} method to manually dismiss the tooltip.
 */
@SuppressWarnings("unused")
@SuppressLint("ViewConstructor")
public class Tooltip extends ViewGroup {

    private static final String TAG = "Tooltip";

    public static final int NO_AUTO_CANCEL = 0;
    private static final int MIN_INT_VALUE = -2147483648;

    private boolean debug = false;

    private View contentView;
    private View anchorView;

    private int[] anchorLocation = new int[2];
    private int[] holderLocation = new int[2];

    @Position
    private int position;

    private boolean isCancelable = true;
    private boolean autoAdjust = true;

    private int padding;

    private Listener builderListener;
    private Listener listener;

    private Tip tip;
    private Paint tipPaint;
    private Path tipPath;
    private boolean showTip = false;

    private Point anchorPoint = new Point();
    private int[] tooltipSize = new int[2];

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Position {}

    private TooltipAnimation animation;
    private boolean animate = false;
    private boolean hasAnimatedIn = false;

    // To avoid multiple click dismiss error (in animation)
    private boolean isDismissed = false;
    private boolean isDismissAnimationInProgress = false;

    // Coordinator anchored view BS
    /**
     * If we have made a call to {@link #doLayout(boolean, int, int, int, int)} or not
     */
    private boolean hasDrawn = false;

    /**
     * If the anchor is anchored to some view in CoordinatorLayout, we get incorrect data
     * about its position in the window. So we need to wait for a preDraw event and then
     * draw tooltip and layout its contents.
     */
    private boolean checkForPreDraw = false;

    /**
     * If the view is attached to window or not
     */
    private boolean isAttached = false;

    private Tooltip(@NonNull Builder builder) {
        super(builder.context);
        init(builder);
    }

    private void init(@NonNull Builder builder) {

        this.contentView = builder.contentView;
        this.anchorView = builder.anchorView;
        this.builderListener = builder.myListener;

        this.autoAdjust = builder.autoAdjust;
        this.position = builder.position;
        this.padding = builder.padding;
        this.checkForPreDraw = builder.checkForPreDraw;
        this.debug = builder.debug;

        // Cancelable
        this.isCancelable = builder.cancelable;

        // Animation
        this.animation = builder.animation;
        animate = (animation != null && animation.getType() != TooltipAnimation.NONE);

        tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Tip
        this.tip = builder.tip;
        this.showTip = (tip != null);
        if (tip != null) {

            tipPaint.setColor(tip.getColor());

            if (tip.getTipRadius() > 0) {
                tipPaint.setStrokeJoin(Paint.Join.ROUND);
                tipPaint.setStrokeCap(Paint.Cap.ROUND);
                tipPaint.setStrokeWidth(tip.getTipRadius());
            }
        }

        tipPaint.setColor(tip == null ? 0xffffffff : tip.getColor());

        if (debug) {
            Log.d(TAG, "show tip: " + showTip);
        }

        this.listener = builder.listener;

        tipPath = new Path();

        LayoutParams params = contentView.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }

        this.addView(contentView, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View child = getChildAt(0);
        measureChild(child, widthMeasureSpec, heightMeasureSpec);

        if (debug) {
            Log.i(TAG, "child measured width: " + child.getMeasuredWidth());
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        if (debug) {
            Log.i(TAG, "l: " + l + ", t: " + t + ", r: " + r + ", b: " + b);
        }

        if (checkForPreDraw && !hasDrawn) {
            anchorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    anchorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    anchorView.getLocationInWindow(anchorLocation);
                    Log.i(TAG, "onPreDraw: " + anchorLocation[0] + ", " + anchorLocation[1]);
                    hasDrawn = true;
                    doLayout(changed, l, t, r, b);
                    return true;
                }
            });

            return;
        }

        hasDrawn = true;
        doLayout(changed, l, t, r, b);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isCancelable) {
            dismiss(animate);
        }

        return false;
    }

    private void doLayout(boolean changed, int l, int t, int r, int b) {

        View child = getChildAt(0);

        anchorView.getLocationInWindow(anchorLocation);
        this.getLocationInWindow(holderLocation);

        int dx = anchorLocation[0] - holderLocation[0];
        int dy = anchorLocation[1] - holderLocation[1];

        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();

        int left = dx;
        int top = dy;

        if (debug) {
            Log.d(TAG, "anchor location: " + anchorLocation[0] + ", " + anchorLocation[1]);
            Log.d(TAG, "holder location: " + holderLocation[0] + ", " + holderLocation[1]);
            Log.d(TAG, "child w: " + w + " h: " + h);
            Log.d(TAG, "left: " + left + ", top: " + top);
        }

        tipPath.reset();

        int px = MIN_INT_VALUE;
        int py = MIN_INT_VALUE;

        switch (position) {
            case LEFT: {
                // to left of anchor view
                // align with horizontal axis

                int diff = (anchorView.getHeight() - h) / 2;
                // We should pad right side
                left -= (w + padding + (showTip ? tip.getHeight() : 0));
                // Top and bottom padding is not required
                top += diff;

                if (showTip) {
                    px = left + w + tip.getHeight();
                    py = top + h/2;
                    tipPath.moveTo(px, py);
                    tipPath.lineTo(px - tip.getHeight(), py + tip.getWidth()/2);
                    tipPath.lineTo(px - tip.getHeight(), py - tip.getWidth()/2);
                    tipPath.lineTo(px, py);
                }

                break;
            }

            case RIGHT: {
                // to right of anchor view
                // align with horizontal axis
                int diff = (anchorView.getHeight() - h) / 2;
                // We should pad left side
                left += (anchorView.getWidth() + padding + (showTip ? tip.getHeight() : 0));
                // Top and bottom padding is not required
                top += diff;

                if (showTip) {
                    px = left - tip.getHeight();
                    py = top + h/2;
                    tipPath.moveTo(px, py);
                    tipPath.lineTo(px + tip.getHeight(), py + tip.getWidth()/2);
                    tipPath.lineTo(px + tip.getHeight(), py - tip.getWidth()/2);
                    tipPath.lineTo(px, py);
                }

                break;
            }

            case TOP: {
                // to top of anchor view
                // align with vertical axis
                int diff = (anchorView.getWidth() - w) / 2;

                // Left and Right padding are not required.
                left += diff;

                // We should only pad bottom
                top -= (h + padding + (showTip ? tip.getHeight() : 0));

                if (showTip) {
                    px = left + w / 2;
                    py = top + h + tip.getHeight();
                    tipPath.moveTo(px, py);
                    tipPath.lineTo(px - tip.getWidth() / 2, py - tip.getHeight());
                    tipPath.lineTo(px + tip.getWidth() / 2, py - tip.getHeight());
                    tipPath.lineTo(px, py);
                }

                break;
            }

            case BOTTOM: {
                // to top of anchor view
                // align with vertical axis
                int diff = (anchorView.getWidth() - w) / 2;

                // Left and Right padding are not required.
                left += diff;

                // We should only pad top
                top += anchorView.getHeight() + padding + (showTip ? tip.getHeight() : 0);

                if (debug) {
                    Log.d(TAG, "tip top: " + top);
                }

                if (showTip) {
                    px = left + w / 2;
                    py = top - tip.getHeight();
                    tipPath.moveTo(px, py);
                    tipPath.lineTo(px - tip.getWidth() / 2, py + tip.getHeight());
                    tipPath.lineTo(px + tip.getWidth() / 2, py + tip.getHeight());
                    tipPath.lineTo(px, py);

                }

                break;
            }

        }

        if (autoAdjust) {
            switch (position) {
                case TOP:
                case BOTTOM:
                    if (left + w > r) {
                        // View is going out on the right side
                        // Add padding to the right
                        left = r - w - padding;
                    } else if (left < l) {
                        // View is going out on the left side
                        // Add padding to the left
                        left = l + padding;
                    }
                    break;

                case LEFT:
                case RIGHT:
                    if (top + h > b) {
                        // View is going out on the bottom side
                        // Add padding to bottom
                        top = b - h - padding;
                    } else if (top < t) {
                        // View is going out on the top side
                        // Add padding to top
                        top = t + padding;
                    }
                    break;
            }
        }

        if (debug) {
            Log.i(TAG, "child layout: left: " + left + " top: " + top + " right: "
                    + (left + child.getMeasuredWidth())
                    + " bottom: " + (top + child.getMeasuredHeight()));
            Log.i(TAG, "px: " + px + ", py: " + py);

        }

        // Tip was not drawn. We need to set anchor point for animation
        if (px == MIN_INT_VALUE || py == MIN_INT_VALUE) {

            if (debug) {
                Log.d(TAG, "Tip was not drawn");
            }

            switch (position) {
                case TOP:
                    px = left + child.getMeasuredWidth()/2;
                    py = top + child.getMeasuredHeight();
                    break;
                case BOTTOM:
                    px = left + child.getMeasuredWidth()/2;
                    py = top;
                    break;
                case LEFT:
                    px = left + child.getMeasuredWidth();
                    py = top + child.getMeasuredHeight();
                    break;
                case RIGHT:
                    px = left;
                    py = top + child.getMeasuredHeight()/2;
                    break;
            }
        }

        // Set anchor point
        anchorPoint.set(px, py);

        // Get Tooltip content size
        tooltipSize[0] = child.getMeasuredWidth();
        tooltipSize[1] = child.getMeasuredHeight();

        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());

        if (animate && !hasAnimatedIn) {
            hasAnimatedIn = false;
            animateIn(animation);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (debug) {
            Log.i(TAG, "canvas w: " + canvas.getWidth() + ", h: " + canvas.getHeight());
        }

        if (showTip && hasDrawn) {
            canvas.drawPath(tipPath, tipPaint);
        }
    }

    public boolean isCancelable() {
        return isCancelable;
    }


    /**
     * Dismiss and remove Tooltip from the view.
     * No animation is performed.
     */
    public void dismiss() {

        // Dismissing or already dismissed
        if (isDismissed) {
            return;
        }

        isDismissed = true;

        this.removeView(contentView);
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeView(this);

        builderListener.onDismissed();

        if (listener != null) {
            listener.onDismissed();
        }
    }

    /**
     * Dismiss and remove Tooltip from the view.
     * @param animate Animation is performed if true
     */
    public void dismiss(boolean animate) {

        // Dismissing or already dismissed
        if (isDismissed) {
            return;
        }

        if (!isAttached) {
            if (debug) {
                Log.e(TAG, "view is detached. Not animating");
            }

            return;
        }

        if (!animate || animation == null) {
            dismiss();
            return;
        }

        animateOut(animation);

    }

    private Point getAnchorPoint() {
        return anchorPoint;
    }

    private int[] getTooltipSize() {
        return tooltipSize;
    }

    private void animateIn(@NonNull TooltipAnimation animation) {

        if (!isAttached) {
            if (debug) {
                Log.e(TAG, "View is not attached. Not animating the tooltip");
            }
            return;
        }

        Point point = getAnchorPoint();
        int[] size = getTooltipSize();

        if (debug) {
            Log.d(TAG, "anchor point: " + point.x + ", " + point.y);
            Log.d(TAG, "size: " + size[0] + ", " + size[1]);
        }

        Animator animator = getAnimator(animation, point, size, true);
        if (animator != null) {
            animator.start();
        }

    }

    @Nullable
    private Animator getAnimator(@NonNull TooltipAnimation animation,
                                 @NonNull Point point, @NonNull int[] size,
                                 boolean animateIn) {

        float startAlpha = 0;
        float endAlpha = 1;

        float startScale = 0;
        float endScale = 1;

        int startRadius = 0;
        int finalRadius = Math.max(size[0], size[1]);

        if (!animateIn) {
            startAlpha = 1;
            endAlpha = 0;

            startScale = 1;
            endScale = 0;

            startRadius = finalRadius;
            finalRadius = 0;
        }

        switch (animation.getType()) {
            case TooltipAnimation.FADE:
                return AnimationUtils.fade(this, startAlpha, endAlpha, animation.getDuration());

            case TooltipAnimation.REVEAL:
                if (Build.VERSION.SDK_INT < 21) {
                    Log.e(TAG, "Reveal is supported on sdk 21 and above");
                    return null;
                }

                return AnimationUtils.reveal(this, point.x, point.y, startRadius, finalRadius,
                        animation.getDuration());

            case TooltipAnimation.SCALE:
                return getScaleAnimator(animation, size, startScale, endScale);

            case TooltipAnimation.SCALE_AND_FADE:
                Animator scaleAnimator = getScaleAnimator(animation, size, startScale, endScale);
                Animator fadeAnimator = AnimationUtils.fade(this, startAlpha, endAlpha, animation.getDuration());

                if (scaleAnimator == null) {
                    return fadeAnimator;
                }

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(scaleAnimator, fadeAnimator);

                return animatorSet;

            case TooltipAnimation.NONE:
                return null;

            default:
                return null;

        }
    }

    private void animateOut(@NonNull TooltipAnimation animation) {

        if (isDismissAnimationInProgress) {
            return;
        }

        Point point = getAnchorPoint();
        int[] size = getTooltipSize();

        if (debug) {
            Log.d(TAG, "anchor point: " + point.x + ", " + point.y);
            Log.d(TAG, "circular reveal : " + point.y + ", " + point.x);
            Log.d(TAG, "size: " + size[0] + ", " + size[1]);
        }

        Animator animator = getAnimator(animation, point, size, false);
        if (animator == null) {
            dismiss();
            return;
        }

        animator.start();
        isDismissAnimationInProgress = true;

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Nullable
    private Animator getScaleAnimator(@NonNull TooltipAnimation animation, @NonNull int size[],
                                      float startScale, float endScale) {

        switch (position) {
            case BOTTOM:
                return AnimationUtils.scaleY(contentView, size[0]/2, 0 , startScale, endScale, animation.getDuration());
            case TOP:
                return AnimationUtils.scaleY(contentView, size[0]/2, size[1] , startScale, endScale, animation.getDuration());
            case RIGHT:
                return AnimationUtils.scaleX(contentView, 0, size[1]/2, startScale, endScale, animation.getDuration());
            case LEFT:
                return AnimationUtils.scaleX(contentView, size[0], size[1]/2, startScale, endScale, animation.getDuration());
            default:
                return null;
        }
    }


    /**
     * Builder class for {@link Tooltip}. Builder has the responsibility of creating the Tooltip
     * and adding/displaying it in the {@link #rootView}.
     */
    public static class Builder {

        private final Context context;

        /**
         * ViewGroup where Tooltip is added
         */
        private ViewGroup rootView;

        /**
         * Content of the Tooltip
         */
        private View contentView;

        /**
         * Anchor of the Tooltip. This is where the tooltip is anchored.
         */
        private View anchorView;

        /**
         * Position of the Tooltip relative to the anchor. Default position is {@link #TOP}.
         * Other positions are - {@link #BOTTOM}, {@link #RIGHT}, {@link #LEFT}
         */
        @Position
        private int position = TOP;

        /**
         * Whether the tooltip should be dismissed or not if clicked outside
         */
        private boolean cancelable = true;

        /**
         * Automatically adjust tooltip layout if it's going out of screen.
         * Scenario: If tooltip is anchored with position {@link #TOP}, it will try to position itself
         * within the bounds of the view in right and left direction. It will not try to adjust itself in top
         * bottom direction.
         */
        private boolean autoAdjust = true;

        /**
         * Tip of the tooltip.
         */
        private Tip tip;

        /**
         * Margin from the anchor and screen boundaries
         */
        private int padding = 0;

        /**
         * If you want the tooltip to dismiss automatically after a certain amount of time,
         * set it in milliseconds. Values <= 0 are considered invalid and auto dismiss is turned off.
         */
        private int autoCancelTime = NO_AUTO_CANCEL;

        /**
         * Tooltip instance
         */
        private Tooltip tooltip;

        private Handler handler;
        private Runnable autoCancelRunnable;

        /**
         * Dismiss Listener for Builder
         */
        private Listener myListener;

        /**
         * Dismiss Listener for User
         */
        private Listener listener;

        private TooltipAnimation animation;
        private boolean animate;

        /**
         * If the anchor is anchored to some view in CoordinatorLayout, we get incorrect data
         * about its position in the window. So we need to wait for a preDraw event and then
         * draw tooltip.
         */
        private boolean checkForPreDraw = false;

        /**
         * Show logs
         */
        private boolean debug = false;

        public Builder(@NonNull Context context) {
            this.context = context;
            handler = new Handler();

            autoCancelRunnable = new Runnable() {
                @Override
                public void run() {
                    if (tooltip != null) {
                        tooltip.dismiss(animate);
                    }
                }
            };

            myListener = new Listener() {
                @Override
                public void onDismissed() {
                    handler.removeCallbacks(autoCancelRunnable);
                }
            };
        }

        /**
         * set tooltip's content view
         * @param view Content of the tooltip
         * @return Builder
         */
        public Builder content(@NonNull View view) {
            this.contentView = view;
            return this;
        }

        /**
         * set tooltip's anchor with position {@link #TOP}
         * @param view Anchor view
         * @return Builder
         */
        public Builder anchor(@NonNull View view) {
            this.anchorView = view;
            return this;
        }

        /**
         * Set tooltip's anchor with tooltip's relative position
         * @param view Anchor view
         * @param position position of tooltip relative to the anchor. {@link #TOP}, {@link #RIGHT},
         *                 {@link #BOTTOM}, {@link #LEFT}
         * @return Builder
         */
        public Builder anchor(@NonNull View view, @Position int position) {
            this.anchorView = view;
            this.position = position;
            return this;
        }

        /**
         * Add Tooltip in this view
         * @param viewGroup {@link ViewGroup} root view (parent view) for the tooltip
         * @return Builder
         */
        public Builder into(@NonNull ViewGroup viewGroup) {
            this.rootView = viewGroup;
            return this;
        }

        /**
         * Whether the tooltip should be dismissed or not if clicked outside. Default it true
         * @param cancelable boolean
         * @return Builder
         */
        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * Automatically adjust tooltip layout if it's going out of screen.
         * Scenario: If tooltip is anchored with position {@link #TOP}, it will try to position itself
         * within the bounds of the view in right and left direction. It will not try to adjust itself in top
         * bottom direction.
         *
         * @param autoAdjust boolean
         * @return Builder
         */
        public Builder autoAdjust(boolean autoAdjust) {
            this.autoAdjust = autoAdjust;
            return this;
        }

        /**
         * Margin from the anchor and screen boundaries
         */
        public Builder withPadding(int padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Attach dismiss listener
         * @param listener dismiss listener
         * @return Builder
         */
        public Builder withListener(@NonNull Listener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Show Tip. If null, it doesn't show the tip.
         * @param tip {@link Tip}
         * @return Builder
         */
        public Builder withTip(@NonNull Tip tip) {
            this.tip = tip;
            return this;
        }

        /**
         * If you want the tooltip to dismiss automatically after a certain amount of time,
         * set it in milliseconds. Values <= 0 are considered invalid and auto dismiss is turned off.
         *
         * Default is 0.
         *
         * @param timeInMilli dismiss time
         * @return Builder
         */
        public Builder autoCancel(int timeInMilli) {
            this.autoCancelTime = timeInMilli;
            return this;
        }

        /**
         * Set show and dismiss animation for the tooltip
         *
         * @param animation {@link TooltipAnimation} to be performed while showing and dismissing
         * @return Builder
         */
        public Builder animate(@NonNull TooltipAnimation animation) {
            this.animation = animation;
            this.animate = true;
            return this;
        }

        /**
         * If the anchor is anchored to some view in CoordinatorLayout, we get incorrect data
         * about its position in the window. So we need to wait for a preDraw event and then
         * draw tooltip.
         */
        public Builder checkForPreDraw(boolean check) {
            this.checkForPreDraw = check;
            return this;
        }

        /**
         * Show logs
         * @param debug boolean
         * @return Builder
         */
        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * Create a new instance of Tooltip. This method will throw {@link NullPointerException}
         * if {@link #anchorView} or {@link #rootView} or {@link #contentView} is not assigned.
         * @return {@link Tooltip}
         */
        public Tooltip build() {
            if (anchorView == null) {
                throw new NullPointerException("anchor view is null");
            }

            if (rootView == null) {
                throw new NullPointerException("Root view is null");
            }

            if (contentView == null) {
                throw new NullPointerException("content view is null");
            }

            tooltip = new Tooltip(this);
            return tooltip;
        }

        /**
         * Creates a new instance of Tooltip by calling {@link #build()} and adds tooltip to {@link #rootView}.
         * <br/><br/>
         * Tooltip is added to the rootView with MATCH_PARENT for width and height constraints. {@link #contentView}
         * is drawn based on its LayoutParams. If it does not contain any LayoutParams, new LayoutParams are generated
         * with WRAP_CONTENT for width and height and added to the Tooltip view.
         *
         * @return Generated {@link Tooltip}
         */
        public Tooltip show() {
            tooltip = build();

            int[] anchorLocation = new int[2];
            anchorView.getLocationInWindow(anchorLocation);
            Log.i(TAG, "anchor location before adding: " + anchorLocation[0] + ", " + anchorLocation[1]);

            rootView.addView(tooltip, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            anchorView.getLocationInWindow(anchorLocation);
            Log.i(TAG, "anchor location after adding: " + anchorLocation[0] + ", " + anchorLocation[1]);

            if (autoCancelTime > NO_AUTO_CANCEL) {
                handler.postDelayed(autoCancelRunnable, autoCancelTime);
            }

            /*if (animate && animation != null) {

                tooltip.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        tooltip.getViewTreeObserver().removeOnPreDrawListener(this);
                        tooltip.animateIn(animation);
                        return true;
                    }
                });

            }*/

            return tooltip;
        }

    }

    /**
     * Tip of the tooltip. Tip is drawn separately to accommodate custom views.
     * It has three properties. {@link #width}, {@link #height}, and {@link #color}.
     * <br/><br/>
     * Tip is drawn as an isosceles triangle. The length of the base
     * is defined by width and perpendicular length between top vertex and base is defined
     * by height.
     */
    public static class Tip {

        /**
         * length of the base of isosceles triangle
         */
        private final int width;

        /**
         * length of the perpendicular from top vertex to the base
         */
        private final int height;

        /**
         * color of the tip.
         */
        @ColorInt
        private final int color;

        /**
         * Corner radius of the tip in px
         */
        private int tipRadius;
        private static final int DEFAULT_TIP_RADIUS = 0;

        public Tip(int width, int height, int color, int tipRadius) {
            this.width = width;
            this.height = height;
            this.color = color;
            this.tipRadius = tipRadius;
        }

        public Tip(int width, int height, int color) {
            this(width, height, color, DEFAULT_TIP_RADIUS);
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @ColorInt
        public int getColor() {
            return color;
        }

        public int getTipRadius() {
            return tipRadius;
        }
    }

    /**
     * Tooltip dismiss listener. {@link #onDismissed()} is called when tooltip is dismissed.
     */
    public interface Listener {
        void onDismissed();
    }
}
