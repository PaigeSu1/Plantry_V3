package com.seproject.plantry;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Go here for guidance on making the dropdown a Material 3 dropdown
        // https://developer.android.com/develop/ui/compose/components/menu
        // Material 3 dialog
        // https://developer.android.com/develop/ui/compose/components/dialog
        ListPreference themeSelection = findPreference("theme_selection");
        if (themeSelection != null) {
            themeSelection.setOnPreferenceChangeListener((preference, newValue) -> {
                String mode = (String) newValue;
                switch (mode) {
                    case "system":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                }
                requireActivity().recreate();
                return true;
            });
        }
    }
}