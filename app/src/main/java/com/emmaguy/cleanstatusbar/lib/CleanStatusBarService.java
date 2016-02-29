package com.emmaguy.cleanstatusbar.lib;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import com.emmaguy.cleanstatusbar.lib.config.DefaultStatusBarConfig;
import com.emmaguy.cleanstatusbar.lib.config.KitKatStatusBarConfig;
import com.emmaguy.cleanstatusbar.lib.config.LollipopStatusBarConfig;
import com.emmaguy.cleanstatusbar.lib.config.MStatusBarConfig;
import com.emmaguy.cleanstatusbar.lib.config.StatusBarConfig;
import com.emmaguy.cleanstatusbar.lib.widgets.StatusBarView;

public class CleanStatusBarService extends Service {

    private static boolean sIsRunning = false;

    private static StatusBarView sStatusBarView;

    private WindowManager mWindowManager;
    private StatusBarConfig mStatusBarConfig;

    public static final String key_api_level = "api_level";
    public static final String key_clock_time = "clock_time";
    public static final String key_kit_kat_gradient = "enable_kitkat_gradient";
    public static final String key_m_light_status_bar = "enable_m_light_mode";
    public static final String key_background_colour = "background_colour";
    public static final String key_signal_3g = "signal_network_icon";
    public static final String key_signal_wifi = "signal_wifi";
    public static final String key_gps = "gps";

    public CleanStatusBarService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        sIsRunning = true;
    }

    public static Intent newIntent(Context context, int api_level, boolean enable_m_light_mode, boolean kit_kat_gradient, int background_colour, String clock_time, boolean signal_wifi, int signal_3g, boolean gps) {
        Intent intent = new Intent(context, CleanStatusBarService.class);
        intent.putExtra(key_api_level, api_level);
        intent.putExtra(key_m_light_status_bar, enable_m_light_mode);
        intent.putExtra(key_kit_kat_gradient, kit_kat_gradient);
        intent.putExtra(key_background_colour, background_colour);
        intent.putExtra(key_clock_time, clock_time);
        intent.putExtra(key_signal_wifi, signal_wifi);
        intent.putExtra(key_signal_3g, signal_3g);
        intent.putExtra(key_gps, gps);
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int apiValue = intent.getIntExtra(key_api_level, 0);
        final boolean isLightModeEnabled = intent.getBooleanExtra(key_m_light_status_bar, false);
        final boolean isKitKatGradientEnabled = intent.getBooleanExtra(key_kit_kat_gradient, false);
        final int backgroundColor = intent.getIntExtra(key_background_colour, 0);
        String clockTime = intent.getStringExtra(key_clock_time);
        if (clockTime == null) {
            clockTime = "12:00";
        }
        final boolean showWifiIcon = intent.getBooleanExtra(key_signal_wifi, false);
        final int show3gIcon = intent.getIntExtra(key_signal_3g, -1);
        final boolean showGpsIcon = intent.getBooleanExtra(key_gps, false);

        if (apiValue == Build.VERSION_CODES.M) {
            mStatusBarConfig = new MStatusBarConfig(getResources(), getAssets(), isLightModeEnabled);
        } else if (apiValue == Build.VERSION_CODES.LOLLIPOP) {
            mStatusBarConfig = new LollipopStatusBarConfig(getResources(), getAssets());
        } else if (apiValue == Build.VERSION_CODES.KITKAT) {
            mStatusBarConfig = new KitKatStatusBarConfig(getResources(), getAssets(), isKitKatGradientEnabled);
        } else {
            mStatusBarConfig = new DefaultStatusBarConfig(getResources(), getAssets());
        }

        if (sStatusBarView == null) {
            sStatusBarView = new StatusBarView(this);
            mWindowManager.addView(sStatusBarView, getWindowManagerParams());
        }
        sStatusBarView.setStatusBarConfig(mStatusBarConfig,
                backgroundColor,
                clockTime,
                showWifiIcon,
                show3gIcon,
                showGpsIcon);

        return super.onStartCommand(intent, flags, startId);
    }

    private WindowManager.LayoutParams getWindowManagerParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT); // must be translucent to support KitKat gradient
        params.gravity = Gravity.TOP;
        params.height = mStatusBarConfig.getStatusBarHeight();
        return params;
    }

    @Override
    public void onDestroy() {
        sIsRunning = false;

        if (sStatusBarView != null) {
            mWindowManager.removeView(sStatusBarView);
            sStatusBarView = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static boolean isRunning() {
        return sIsRunning;
    }
}
