package com.android.settings.ext;

import android.content.Context;
import android.content.Intent;
import android.ext.settings.BoolSetting;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.widget.FooterPreference;

public abstract class BoolSettingFragment extends DashboardFragment implements ExtSettingPrefController<BoolSetting> {

    private static final String TAG = BoolSettingFragment.class.getSimpleName();

    protected SwitchPreferenceCompat mainSwitch;
    protected boolean invertSetting;

    private ExtSettingControllerHelper<BoolSetting> helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new ExtSettingControllerHelper<>(requireContext(), getSetting());

        getActivity().setTitle(getTitle());

        Context ctx = requireContext();

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(ctx);

        var mainSwitch = new SwitchPreferenceCompat(ctx);
        mainSwitch.setTitle(getMainSwitchTitle());

        this.mainSwitch = mainSwitch;
        refreshMainSwitch();

        mainSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean state = (boolean) newValue;

            if (invertSetting) {
                state = !state;
            }

            if (interceptMainSwitchChange(state)) {
                return false;
            }

            if (!getSetting().put(requireContext(), state)) {
                return false;
            }

            onMainSwitchChanged(state);

            return true;
        });

        screen.addPreference(mainSwitch);

        addExtraPrefs(screen);

        FooterPreference footer = makeFooterPref(new FooterPreference.Builder(ctx));

        if (footer != null) {
            screen.addPreference(footer);
        }

        setPreferenceScreen(screen);
    }

    protected abstract BoolSetting getSetting();

    protected abstract CharSequence getTitle();

    protected CharSequence getMainSwitchTitle() {
        return getText(R.string.bool_setting_enable);
    }

    protected CharSequence getMainSwitchSummary() {
        return null;
    }

    protected void addExtraPrefs(PreferenceScreen screen) {}

    protected FooterPreference makeFooterPref(FooterPreference.Builder builder) {
        return null;
    }

    protected static void setFooterPrefLearnMoreUri(FooterPreference p, Uri uri) {
        p.setLearnMoreAction(v -> {
            var intent = new Intent(Intent.ACTION_VIEW, uri);
            p.getContext().startActivity(intent);
        });
    }

    protected boolean interceptMainSwitchChange(boolean newValue) {
        return false;
    }

    protected void onMainSwitchChanged(boolean state) {}

    protected void refreshMainSwitch() {
        boolean state = getSetting().get(requireContext());
        if (invertSetting) {
            state = !state;
        }
        mainSwitch.setChecked(state);

        CharSequence mainSwitchSummary = getMainSwitchSummary();
        if (mainSwitchSummary != null) {
            mainSwitch.setSummary(mainSwitchSummary);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        helper.onResume(this);
        refreshMainSwitch();
    }

    @Override
    public void onPause() {
        super.onPause();

        helper.onPause(this);
    }

    @Override
    public void accept(BoolSetting setting) {
        refreshMainSwitch();
    }

    @Override
    public int getMetricsCategory() {
        return METRICS_CATEGORY_UNKNOWN;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return 0;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    protected final CharSequence resText(int res) {
        return requireContext().getText(res);
    }
}
