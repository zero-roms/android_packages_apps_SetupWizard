/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.settings

import android.content.Context
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import lineageos.hardware.LineageHardwareManager
import lineageos.providers.LineageSettings
import org.lineageos.setupwizard.DISABLE_NAV_KEYS
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class LineageSettingsActivity : BaseSetupWizardActivity() {

    private val metricsItem by lazy { toggle(R.id.metrics_item) }
    private val navKeysItem by lazy { toggle(R.id.nav_keys_item) }

    private var supportsKeyDisabler = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ZeroS: setup does not offer the statistics switch; statistics stay off (from 0.5.1
        // they are ZeroS statistics, sent to ZeroS's server, and can be turned on later in
        // Settings > Security & privacy > More security & privacy > Trust).
        // That leaves this page nothing to show unless the device can disable its navigation
        // keys, so it is skipped otherwise.
        supportsKeyDisabler = isKeyDisablerSupported(this)
        if (!supportsKeyDisabler) {
            Log.v(TAG, "Nothing to show, skipping LineageSettingsActivity")
            SetupWizardUtils.disableComponent(this, LineageSettingsActivity::class.java)
            finishAction(RESULT_SKIP)
            return
        }
        metricsItem.isVisible = false

        navKeysItem.isVisible = true
        navKeysItem.setOnCheckedChangeListener { isChecked ->
            SetupWizardApp.settingsBundle.putBoolean(DISABLE_NAV_KEYS, isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDisableNavkeysOption()
    }

    override val layoutResId = R.layout.setup_lineage_settings

    override val titleResId = R.string.setup_services

    override val iconResId = R.drawable.logo

    override val itemEntriesResId = R.xml.lineage_settings_items

    private fun updateDisableNavkeysOption() {
        if (supportsKeyDisabler) {
            val myPageBundle = SetupWizardApp.settingsBundle
            val enabled =
                LineageSettings.System.getIntForUser(
                    contentResolver,
                    LineageSettings.System.FORCE_SHOW_NAVBAR,
                    0,
                    UserHandle.USER_CURRENT,
                ) != 0
            val checked =
                if (myPageBundle.containsKey(DISABLE_NAV_KEYS)) {
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS)
                } else {
                    enabled
                }
            navKeysItem.isChecked = checked
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked)
        }
    }

    companion object {
        private const val TAG = "LineageSettingsActivity"

        private fun isKeyDisablerSupported(context: Context): Boolean {
            val hardware = LineageHardwareManager.getInstance(context)
            return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE)
        }
    }
}
