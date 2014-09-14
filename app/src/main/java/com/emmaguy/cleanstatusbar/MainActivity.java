package com.emmaguy.cleanstatusbar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.emmaguy.cleanstatusbar.prefs.TimePreference;


public class MainActivity extends Activity {
    public static final String PREFS_KEY_API_VALUE = "api_level";
    public static final String PREFS_KEY_CLOCK_TIME = "clock_time";
    public static final String PREFS_KEY_BACKGROUND_COLOUR = "background_colour";

    public static final int VERSION_CODE_L = 21; // TODO: change to Build.VERSION_CODES.L when it's released

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSwitch();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    private void initSwitch() {
        Switch masterSwitch = new Switch(this);
        masterSwitch.setChecked(CleanStatusBarService.isRunning());
        masterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startService(MainActivity.this);
                } else {
                    stopService(MainActivity.this);
                }
            }
        });

        final ActionBar bar = getActionBar();
        final ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.master_switch_margin_right);
        bar.setCustomView(masterSwitch, lp);
        bar.setDisplayShowCustomEnabled(true);
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, CleanStatusBarService.class));
    }

    public static void startService(Context context) {
        context.startService(new Intent(context, CleanStatusBarService.class));
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);

            initSummary();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(findPreference(key));

            if (CleanStatusBarService.isRunning()) {
                stopService(getActivity());
                startService(getActivity());
            }
        }

        protected void initSummary() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initPrefsSummary(getPreferenceScreen().getPreference(i));
            }
        }

        protected void initPrefsSummary(Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initPrefsSummary(cat.getPreference(i));
                }
            } else {
                updatePrefsSummary(p);
            }
        }

        protected void updatePrefsSummary(Preference pref) {
            if (pref == null) {
                return;
            }

            if (pref instanceof ListPreference) {
                ListPreference lst = (ListPreference) pref;
                String currentValue = lst.getValue();

                int index = lst.findIndexOfValue(currentValue);
                CharSequence[] entries = lst.getEntries();
                if (index >= 0 && index < entries.length) {
                    pref.setSummary(entries[index]);
                }
            } else if (pref instanceof TimePreference) {
                if (pref.getKey().equals(PREFS_KEY_CLOCK_TIME)) {
                    String time = getPreferenceManager().getSharedPreferences().getString(PREFS_KEY_CLOCK_TIME, TimePreference.DEFAULT_TIME_VALUE);
                    pref.setSummary(time);
                }
            }
        }
    }
}
