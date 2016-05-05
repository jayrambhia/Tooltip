package com.fenchtose.tooltip_demo;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fenchtose.tooltip.Tooltip;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

    private ViewGroup root;

    private int tooltipColor;
    private int tooltipSize;
    private int tooltipPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (ViewGroup) findViewById(R.id.root);

        tooltipSize = getResources().getDimensionPixelOffset(R.dimen.tooltip_width);
        tooltipColor = ContextCompat.getColor(this, R.color.colorPrimary);
        tooltipPadding = getResources().getDimensionPixelOffset(R.dimen.tooltip_padding);

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
                showTooltip(v, R.string.bottom_auto_adjust, Tooltip.BOTTOM, true, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.bottom_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.bottom_no_auto_adjust, Tooltip.BOTTOM, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.bottom, Tooltip.BOTTOM, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.top_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.top_auto_adjust, Tooltip.TOP, true, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.top_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.top_no_auto_adjust, Tooltip.TOP, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.top, Tooltip.TOP, false, tooltipSize,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.right_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.right_auto_adjust, Tooltip.RIGHT, true,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.right_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.right_no_auto_adjust, Tooltip.RIGHT, false,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.right, Tooltip.RIGHT, false,
                        tooltipSize, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        findViewById(R.id.left_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.left_auto_adjust, Tooltip.LEFT, true,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.left_no_auto_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.left_no_auto_adjust, Tooltip.LEFT, false,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tooltipSize);
            }
        });

        findViewById(R.id.left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, R.string.left, Tooltip.LEFT, false,
                        tooltipSize, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }

    private void showTooltip(@NonNull View anchor, @StringRes int resId,
                             @Tooltip.Position int position, boolean autoAdjust,
                             int width, int height) {
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.tooltip_textview, null);
        textView.setText(resId);
        textView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        showTooltip(anchor, textView, position, autoAdjust, tooltipColor);
    }

    private void showTooltip(@NonNull View anchor, @NonNull View content,
                             @Tooltip.Position int position, boolean autoAdjust,
                             int tipColor) {
        new Tooltip.Builder(this)
                .anchor(anchor, position)
                .autoAdjust(autoAdjust)
                .content(content)
                .withTip(new Tooltip.Tip(60, 60, tipColor))
                .into(root)
                .debug(true)
                .show();
    }

    private void showCustomTooltip(@NonNull View anchor) {
        View content = getLayoutInflater().inflate(R.layout.item_tooltip_view_1, null);

        new Tooltip.Builder(this)
                .anchor(anchor, Tooltip.BOTTOM)
                .autoAdjust(true)
                .withPadding(tooltipPadding)
                .content(content)
                .withTip(new Tooltip.Tip(60, 60, tooltipColor))
                .into(root)
                .debug(true)
                .show();
    }
}
