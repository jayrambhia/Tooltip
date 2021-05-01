package com.fenchtose.tooltip_demo;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fenchtose.tooltip.Tooltip;
import com.fenchtose.tooltip.TooltipAnimation;

public class TooltipFragmentDemo extends Fragment {

    private ViewGroup mRootLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tooltip_fragment_demo_layout, container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTooltip(v);
            }
        });

        mRootLayout = (ViewGroup)view;
    }

    private void showCustomTooltip(View anchor) {
        View content = LayoutInflater.from(getContext()).inflate(R.layout.item_tooltip_view_1, null);
        Resources res = getResources();

        int tooltipPadding = res.getDimensionPixelOffset(R.dimen.tooltip_padding);
        int tooltipColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        int tipSizeSmall = res.getDimensionPixelSize(R.dimen.tip_dimen_small);
        int tipRadius = res.getDimensionPixelOffset(R.dimen.tip_radius);

        new Tooltip.Builder(getActivity())
            .anchor(anchor, Tooltip.BOTTOM)
            .animate(new TooltipAnimation(TooltipAnimation.SCALE_AND_FADE, 400))
            .autoAdjust(true)
            .withPadding(tooltipPadding)
//            .withMargin(6)
            .content(content)
            .cancelable(true)
            .checkForPreDraw(true)
            .withTip(new Tooltip.Tip(tipSizeSmall, tipSizeSmall, tooltipColor))
            .into(mRootLayout)
            .debug(true)
            .show();
    }
}
