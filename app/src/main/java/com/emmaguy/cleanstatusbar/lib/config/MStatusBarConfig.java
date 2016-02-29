package com.emmaguy.cleanstatusbar.lib.config;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.emmaguy.cleanstatusbar.lib.R;

public class MStatusBarConfig extends LollipopStatusBarConfig {
    private final boolean mLightModeEnabled;

    public MStatusBarConfig(Resources r, AssetManager a, boolean lightModeEnabled) {
        super(r, a);
        mLightModeEnabled = lightModeEnabled;
    }

    @Override
    public int getForegroundColour() {
        if (mLightModeEnabled) {
            return mResources.getColor(R.color.m_light_mode_semi_transparent_black);
        }
        return super.getForegroundColour();
    }
}
