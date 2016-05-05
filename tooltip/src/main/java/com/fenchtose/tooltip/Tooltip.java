package com.fenchtose.tooltip;

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
        init(context, content, anchorView, builderListener);
    }

    private void init(@NonNull Context context, @NonNull View contentView, @NonNull View anchorView,
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

    public void setPosition(int position) {
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

    public void setAutoAdjust(boolean autoAdjust) {
        this.autoAdjust = autoAdjust;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isShowTip() {
        return showTip;
    }

    public void setShowTip(boolean showTip) {
        this.showTip = showTip;
        if (showTip && tip == null) {
            throw new NullPointerException("Tip is null");
        }
    }

    public void setTip(@Nullable Tip tip) {
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

    public static class Builder {

        private Context context;

        private ViewGroup rootView;
        private View contentView;
        private View anchorView;

        private boolean cancelable = true;
        private boolean autoAdjust = true;
        private Tip tip;
        private int padding = 0;

        private int autoCancelTime = NO_AUTO_CANCEL;

        private Tooltip tooltip;

        private Handler handler;
        private Runnable autoCancelRunnable;
        private Listener myListener;

        private Listener listener;

        private boolean debug = false;

        @Position
        private int position = TOP;

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

        public Builder content(@NonNull View view) {
            this.contentView = view;
            return this;
        }

        public Builder anchor(@NonNull View view) {
            this.anchorView = view;
            return this;
        }

        public Builder anchor(@NonNull View view, @Position int position) {
            this.anchorView = view;
            this.position = position;
            return this;
        }

        public Builder into(@NonNull ViewGroup viewGroup) {
            this.rootView = viewGroup;
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder autoAdjust(boolean autoAdjust) {
            this.autoAdjust = autoAdjust;
            return this;
        }

        public Builder withPadding(int padding) {
            this.padding = padding;
            return this;
        }

        public Builder withListener(@NonNull Listener listener) {
            this.listener = listener;
            return this;
        }

        public Builder withTip(@Nullable Tip tip) {
            this.tip = tip;
            return this;
        }

        public Builder autoCancel(int timeInMilli) {
            this.autoCancelTime = timeInMilli;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

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

        public Tooltip show() {
            tooltip = build();
            rootView.addView(tooltip, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            if (autoCancelTime > NO_AUTO_CANCEL) {
                handler.postDelayed(autoCancelRunnable, autoCancelTime);
            }

            return tooltip;
        }
    }

    public static class Tip {

        private int width;
        private int height;
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
