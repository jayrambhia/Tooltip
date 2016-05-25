package com.fenchtose.tooltip;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Jay Rambhia on 5/25/16.
 */
public class TooltipAnimation {

    public static final int NONE = 0;
    public static final int FADE = 1;
    public static final int REVEAL = 2;
    public static final int SCALE = 3;
    public static final int SCALE_AND_FADE = 4;

    @IntDef({NONE, FADE, REVEAL, SCALE, SCALE_AND_FADE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    @Type
    private int type;

    private int duration;

    public TooltipAnimation(@Type int type, int duration) {
        this.type = type;
        this.duration = duration;
    }

    @Type
    public int getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }
}
