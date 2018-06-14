package com.example.lukab.seechange_streaming.ui.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.util.Log
import com.example.lukab.seechange_streaming.R


class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        initSummary()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        updatePreference(findPreference(key), key)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun initSummary() {
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference: Preference = preferenceScreen.getPreference(i)
            if (preference is PreferenceCategory) {
                val preferenceGroup: PreferenceCategory = preference

                for (j in 0 until preferenceGroup.preferenceCount) {
                    val singlePref: Preference = preferenceGroup.getPreference(j)
                    initPreference(singlePref, singlePref.key)
                }
            } else {
                initPreference(preference, preference.key)
            }
        }
    }

    private fun initPreference(preference: Preference?, key: String?) {
        val sharedPreferences: SharedPreferences = preferenceManager.sharedPreferences
        preference?.summary = sharedPreferences.getString(key, "Unknown")
    }

    private fun updatePreference(preference: Preference?, key: String?) {
        val sharedPreferences: SharedPreferences = preferenceManager.sharedPreferences
        preference?.summary = sharedPreferences.getString(key, "Unknown")
    }
}
