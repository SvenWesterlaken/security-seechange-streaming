package com.example.lukab.seechange_streaming.ui.fragments

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.viewModel.UserSettingsViewModel
import java.text.SimpleDateFormat
import java.util.*


class SettingsFragment : PreferenceFragment(), LifecycleOwner, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var userSettingsViewModel: UserSettingsViewModel
    private lateinit var lifeCycleRegistry: LifecycleRegistry
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        this.lifeCycleRegistry = LifecycleRegistry(this)
        this.lifeCycleRegistry.markState(Lifecycle.State.CREATED)
        this.userSettingsViewModel = UserSettingsViewModel(activity.application, "svenwesterlaken")
        this.sharedPreferences = preferenceManager.sharedPreferences
        initSummary()
    }

    override fun getLifecycle(): Lifecycle {
        return this.lifeCycleRegistry
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        this.sharedPreferences = sharedPreferences!!
        updatePreference(findPreference(key), key)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        this.lifeCycleRegistry.markState(Lifecycle.State.RESUMED)
    }

    override fun onStart() {
        super.onStart()
        this.lifeCycleRegistry.markState(Lifecycle.State.STARTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.lifeCycleRegistry.markState(Lifecycle.State.DESTROYED)
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
        if(key.equals("pref_avatar")) {
            preference?.summary = formatAvatarDate(key)
        } else {
            preference?.summary = sharedPreferences.getString(key, "Unknown")
        }

    }

    private fun updatePreference(preference: Preference?, key: String?) {
        when {
            key.equals("pref_username") -> {
                val updatedPreference = sharedPreferences.getString(key, null)

                userSettingsViewModel.updatePublicName(updatedPreference).observe(this, Observer<Boolean> { succeeded ->
                    if(succeeded!!) {
                        preference?.summary = updatedPreference
                    } else {
                        sharedPreferences.edit().putString(key, preference!!.summary.toString()).apply()
                    }

                })
            }
            key.equals("pref_slogan") -> {
                val updatedPreference = sharedPreferences.getString(key, null)

                userSettingsViewModel.updateSlogan(updatedPreference).observe(this, Observer<Boolean> { succeeded ->
                    if(succeeded!!) {
                        preference?.summary = updatedPreference
                    } else {
                        sharedPreferences.edit().putString(key, preference!!.summary.toString()).apply()
                    }
                })
            }
            key.equals("pref_avatar") -> {
                preference?.summary = formatAvatarDate(key)
            }
            else -> preference?.summary = sharedPreferences.getString(key, "Unknown")
        }

    }

    private fun formatAvatarDate(key: String?): String {
        val milliseconds = sharedPreferences.getLong(key, 0)
        val dateFormat = SimpleDateFormat("MMMM d yyyy, HH:mm", Locale.US)

        return if (milliseconds < 1) {
            "No avatar set"
        } else {
            "Last Modified: ${dateFormat.format(Date(milliseconds))}"
        }
    }
}
