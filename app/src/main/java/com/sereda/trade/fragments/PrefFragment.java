package com.sereda.trade.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sereda.trade.MainActivity;
import com.sereda.trade.R;

public class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ListPreference prefRefTime = (ListPreference) findPreference("ref_time");

        String refreshTime = sp.getString("ref_time", "");
        switch (refreshTime) {
            case "30":
                prefRefTime.setSummary(getResources().getString(R.string.ref_time_30));
                break;
            case "60":
                prefRefTime.setSummary(getResources().getString(R.string.ref_time_60));
                break;
            case "300":
                prefRefTime.setSummary(getResources().getString(R.string.ref_time_300));
                break;
        }

        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Preference preference;

        switch (key) {
            case "ref_time":
                preference = findPreference(key);
                String refreshTime = sp.getString(key, "");
                switch (refreshTime) {
                    case "30":
                        preference.setSummary(getActivity().getResources().getString(R.string.ref_time_30));
                        break;
                    case "60":
                        preference.setSummary(getActivity().getResources().getString(R.string.ref_time_60));
                        break;
                    case "300":
                        preference.setSummary(getActivity().getResources().getString(R.string.ref_time_300));
                        break;
                }
                restartHandler(refreshTime);
        }
    }

    private void restartHandler(String refreshTime) {
        if (null != MainActivity.handler && null != MainActivity.runnable) {
            MainActivity.handler.removeCallbacks(MainActivity.runnable);
            if (!refreshTime.isEmpty()) {
                MainActivity.handler.postDelayed(MainActivity.runnable, 1000 * Integer.parseInt(refreshTime));
            }
        }
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
}
