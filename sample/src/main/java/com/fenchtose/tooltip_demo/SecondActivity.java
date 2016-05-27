package com.fenchtose.tooltip_demo;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fenchtose.tooltip.Tooltip;
import com.fenchtose.tooltip.TooltipAnimation;

/**
 * Created by Jay Rambhia on 5/27/16.
 */
public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";
    private CoordinatorLayout root;
    private FloatingActionButton fab;

    private int tooltipColor;
    private int tooltipSize;
    private int tooltipPadding;

    private int tipSizeSmall;
    private int tipSizeRegular;
    private int tipRadius;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        root = (CoordinatorLayout) findViewById(R.id.root_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        Resources res = getResources();

        tooltipSize = res.getDimensionPixelOffset(R.dimen.tooltip_width);
        tooltipColor = ContextCompat.getColor(this, R.color.colorPrimary);
        tooltipPadding = res.getDimensionPixelOffset(R.dimen.tooltip_padding);

        tipSizeSmall = res.getDimensionPixelSize(R.dimen.tip_dimen_small);
        tipSizeRegular = res.getDimensionPixelSize(R.dimen.tip_dimen_regular);

        tipRadius = res.getDimensionPixelOffset(R.dimen.tip_radius);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showTooltip();
            }
        }, 1000);
    }

    private void showTooltip() {
        showCustomTooltip(fab);
    }

    private void showCustomTooltip(@NonNull View anchor) {
        View content = getLayoutInflater().inflate(R.layout.item_tooltip_view_1, null);

        final Tooltip customTooltip = new Tooltip.Builder(this)
                .anchor(anchor, Tooltip.TOP)
                .animate(new TooltipAnimation(TooltipAnimation.SCALE_AND_FADE, 400))
                .autoAdjust(true)
                .withPadding(tooltipPadding)
                .content(content)
                .cancelable(false)
                .checkForPreDraw(true)
                .withTip(new Tooltip.Tip(tipSizeRegular, tipSizeRegular, tooltipColor, tipRadius))
                .into(root)
                .debug(true)
                .show();

        content.findViewById(R.id.dismiss_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTooltip.dismiss(true);
            }
        });

    }
}
