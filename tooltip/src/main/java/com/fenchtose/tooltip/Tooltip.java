package com.fenchtose.tooltip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Jay Rambhia on 5/4/16.
 */
@SuppressLint("ViewConstructor")
public class Tooltip extends ViewGroup {

    private static final String TAG = "Tooltip";

    public static final int NO_AUTO_CANCEL = 0;

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

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;

    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Position {}

    private Tooltip(@NonNull Context context, @NonNull View content, @NonNull View anchorView,
                    @NonNull Listener builderListener) {
        super(context);
        init(content, anchorView, builderListener);
    }

    private void init(@NonNull View contentView, @NonNull View anchorView,
                      @NonNull Listener builderListener) {

        this.contentView = contentView;
        this.anchorView = anchorView;
        this.builderListener = builderListener;

        tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setColor(0xffffffff);
        tipPaint.setStyle(Paint.Style.FILL);

        tipPath = new Path();

        LayoutParams params = contentView.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }

        this.addView(contentView, params);

        setCancelable(isCancelable);
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (debug) {
            Log.i(TAG, "l: " + l + ", t: " + t + ", r: " + r + ", b: " + b);
        }
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
            Log.d(TAG, "child w: " + w + " h: " + h);
            Log.d(TAG, "left: " + left + ", top: " + top);
        }

        tipPath.reset();

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
                    int px = left + w + tip.getHeight();
                    int py = top + h/2;
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
                    int px = left - tip.getHeight();
                    int py = top + h/2;
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
                    int px = left + w / 2;
                    int py = top + h + tip.getHeight();
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
                    int px = left + w / 2;
                    int py = top - tip.getHeight();
                    tipPath.moveTo(px, py);
                    tipPath.lineTo(px - tip.getWidth() / 2, py + tip.getHeight());
                    tipPath.lineTo(px + tip.getWidth() / 2, py + tip.getHeight());
                    tipPath.lineTo(px, py);

                    if (debug) {
                        Log.i(TAG, "px: " + px + ", py: " + py);
                    }
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
        }

        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (debug) {
            Log.i(TAG, "cavas w: " + canvas.getWidth() + ", h: " + canvas.getHeight());
        }

        if (showTip) {
            canvas.drawPath(tipPath, tipPaint);
        }
    }

    private void setPosition(int position) {
        this.position = position;
    }

    public boolean isCancelable() {
        return isCancelable;
    }

    public void setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
        if (isCancelable) {
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } else {
            this.setOnClickListener(null);
        }
    }

    private void setAutoAdjust(boolean autoAdjust) {
        this.autoAdjust = autoAdjust;
    }

    private void setPadding(int padding) {
        this.padding = padding;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isShowTip() {
        return showTip;
    }

    private void setShowTip(boolean showTip) {
        this.showTip = showTip;
        if (showTip && tip == null) {
            throw new NullPointerException("Tip is null");
        }
    }

    private void setTip(@Nullable Tip tip) {
        this.showTip = (tip != null);
        this.tip = tip;
        if (tip != null) {
            tipPaint.setColor(tip.getColor());
        }

        if (debug) {
            Log.d(TAG, "show tip: " + showTip);
        }
    }

    public void dismiss() {
        this.removeView(contentView);
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeView(this);

        builderListener.onDismissed();

        if (listener != null) {
            listener.onDismissed();
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Builder class for {@link Tooltip}. Builder has the responsibility of creating the Tooltip
     * and adding/displaying it in the {@link #rootView}.
     */
    public static class Builder {

        private Context context;

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
         * Margin from the anchor.
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
                        tooltip.dismiss();
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
         * Margin from the anchor.
         */
        public Builder withPadding(int padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Attach dismiss listener
         * @param listener
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
        public Builder withTip(@Nullable Tip tip) {
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

            tooltip = new Tooltip(context, contentView, anchorView, myListener);
            tooltip.setDebug(debug);
            tooltip.setPosition(position);
            tooltip.setCancelable(cancelable);
            tooltip.setAutoAdjust(autoAdjust);
            tooltip.setPadding(padding);
            tooltip.setListener(listener);
            tooltip.setTip(tip);

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
            rootView.addView(tooltip, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            if (autoCancelTime > NO_AUTO_CANCEL) {
                handler.postDelayed(autoCancelRunnable, autoCancelTime);
            }

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
        private int width;

        /**
         * length of the perpendicular from top vertex to the base
         */
        private int height;

        /**
         * color of the tip.
         */
        private int color;

        public Tip(int width, int height, int color) {
            this.width = width;
            this.height = height;
            this.color = color;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getColor() {
            return color;
        }
    }

    public interface Listener {
        void onDismissed();
    }
}
