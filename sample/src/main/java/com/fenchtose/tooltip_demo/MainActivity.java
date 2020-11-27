package com.fenchtose.tooltip_demo;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.fenchtose.tooltip.Tooltip;
import com.fenchtose.tooltip.TooltipAnimation;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

    private ViewGroup root;

    private int tooltipColor;
    private int tooltipSize;
    private int tooltipPadding;

    private int tipSizeSmall;
    private int tipSizeRegular;
    private int tipRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (ViewGroup) findViewById(R.id.root);

        Resources res = getResources();

        tooltipSize = res.getDimensionPixelOffset(R.dimen.tooltip_width);
        tooltipColor = ContextCompat.getColor(this, R.color.colorPrimary);
        tooltipPadding = res.getDimensionPixelOffset(R.dimen.tooltip_padding);

        tipSizeSmall = res.getDimensionPixelSize(R.dimen.tip_dimen_small);
        tipSizeRegular = res.getDimensionPixelSize(R.dimen.tip_dimen_regular);
        tipRadius = res.getDimensionPixelOffset(R.dimen.tip_radius);

        final View bottomButton = findViewById(R.id.button_bottom);
        if (bottomButton != null) {
            bottomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCustomTooltip(v);
                }
            });
        }

        findViewById(R.id.bottom_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.bottom_auto_adjust, Tooltip.BOTTOM, true,
                        TooltipAnimation.SCALE, false,
                        tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.bottom_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.bottom_no_auto_adjust, Tooltip.BOTTOM, false,
                        TooltipAnimation.SCALE, false,
                        tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.bottom, Tooltip.BOTTOM, false,
                        TooltipAnimation.SCALE, true,
                        tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.top_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.top_auto_adjust, Tooltip.TOP, true,
                        TooltipAnimation.NONE, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.top_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.top_no_auto_adjust, Tooltip.TOP, false,
                        TooltipAnimation.NONE, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.top, Tooltip.TOP, false,TooltipAnimation.NONE, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.right_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.right_auto_adjust, Tooltip.RIGHT, true,
                        TooltipAnimation.REVEAL, true,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.right_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.right_no_auto_adjust, Tooltip.RIGHT, false,
                        TooltipAnimation.REVEAL, true,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.right, Tooltip.RIGHT, false,
                        TooltipAnimation.REVEAL, false,
                        tooltipSize, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.left_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.left_auto_adjust, Tooltip.LEFT, true,
                        TooltipAnimation.SCALE_AND_FADE, true,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.left_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.left_no_auto_adjust, Tooltip.LEFT, false,
                        TooltipAnimation.SCALE_AND_FADE, true,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.left, Tooltip.LEFT, false,
                        TooltipAnimation.SCALE_AND_FADE, false,
                        tooltipSize, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.demo_action) {
            showMenuTooltip(findViewById(R.id.demo_action));
        } else if (item.getItemId() == R.id.second_action) {
            openSecondActivity();
        }
        return true;
    }

    private void openSecondActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void showTooltip(@NonNull View anchor, @StringRes int resId,
                             @Tooltip.Position int position, boolean autoAdjust,
                             @TooltipAnimation.Type int type, boolean hideWhenAnimating,
                             int width, int height) {
        ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.tooltip_textview, null);
        TextView textView = (TextView) contentView.getChildAt(0);
        textView.setText(resId);
        textView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        showTooltip(anchor, contentView, position, autoAdjust, type, hideWhenAnimating, tooltipColor);
    }

    private void showTooltip(@NonNull View anchor, @NonNull View content,
                             @Tooltip.Position int position, boolean autoAdjust,
                             @TooltipAnimation.Type int type, boolean hideWhenAnimating,
                             int tipColor) {

        new Tooltip.Builder(this)
                .anchor(anchor, position)
                .animate(new TooltipAnimation(type, 500, hideWhenAnimating))
                .autoAdjust(autoAdjust)
                .content(content)
                .withTip(new Tooltip.Tip(tipSizeRegular, tipSizeRegular, tipColor))
                .into(root)
                .debug(true)
                .show();
    }

    private void showCustomTooltip(@NonNull View anchor) {
        View content = getLayoutInflater().inflate(R.layout.item_tooltip_view_1, null);

        final Tooltip customTooltip = new Tooltip.Builder(this)
                .anchor(anchor, Tooltip.BOTTOM)
                .animate(new TooltipAnimation(TooltipAnimation.SCALE_AND_FADE, 400, true))
                .autoAdjust(true)
                .withPadding(tooltipPadding)
                .content(content)
                .cancelable(false)
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

    private void showMenuTooltip(@NonNull View anchor) {
        ViewGroup content = (ViewGroup) getLayoutInflater().inflate(R.layout.tooltip_textview, null);
        TextView textView = (TextView) content.getChildAt(0);
        textView.setText(R.string.menu_tooltip);

        new Tooltip.Builder(this)
                .anchor(anchor, Tooltip.BOTTOM)
                .animate(new TooltipAnimation(TooltipAnimation.REVEAL, 400))
                .autoAdjust(true)
                .autoCancel(2000)
                .content(content)
                .withPadding(getResources().getDimensionPixelOffset(R.dimen.menu_tooltip_padding))
                .withTip(new Tooltip.Tip(tipSizeSmall, tipSizeSmall, tooltipColor))
                .into(root)
                .debug(true)
                .show();
    }
}
