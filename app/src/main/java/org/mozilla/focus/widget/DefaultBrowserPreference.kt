/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.focus.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import androidx.lifecycle.Observer
import dagger.Lazy
import org.mozilla.focus.R
import org.mozilla.focus.utils.DialogUtils
import org.mozilla.focus.utils.Settings
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.getActivityViewModel
import org.mozilla.rocket.extension.toFragmentActivity
import org.mozilla.rocket.settings.defaultbrowser.ui.DefaultBrowserHelper
import org.mozilla.rocket.settings.defaultbrowser.ui.DefaultBrowserPreferenceViewModel
import org.mozilla.rocket.settings.defaultbrowser.ui.DefaultBrowserPreferenceViewModel.DefaultBrowserPreferenceUiModel
import javax.inject.Inject

@TargetApi(Build.VERSION_CODES.N)
class DefaultBrowserPreference : Preference {
    @Inject
    lateinit var viewModelCreator: Lazy<DefaultBrowserPreferenceViewModel>

    private lateinit var viewModel: DefaultBrowserPreferenceViewModel
    private lateinit var defaultBrowserHelper: DefaultBrowserHelper

    private var switchView: Switch? = null

    // Instantiated from XML
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        widgetLayoutResource = R.layout.preference_default_browser
        init()
    }

    // Instantiated from XML
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        widgetLayoutResource = R.layout.preference_default_browser
        init()
    }

    private fun init() {
        appComponent().inject(this)
    }

    override fun onAttachedToActivity() {
        super.onAttachedToActivity()
        viewModel = getActivityViewModel(viewModelCreator)
        defaultBrowserHelper = DefaultBrowserHelper(context.toFragmentActivity(), viewModel)
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        switchView = view.findViewById<View>(R.id.switch_widget) as Switch?

        viewModel.uiModel.observe(context.toFragmentActivity(), Observer { update(it) })
        viewModel.openDefaultAppsSettings.observe(context.toFragmentActivity(), Observer { defaultBrowserHelper.openDefaultAppsSettings() })
        viewModel.openAppDetailSettings.observe(context.toFragmentActivity(), Observer { defaultBrowserHelper.openAppDetailSettings() })
        viewModel.openSumoPage.observe(context.toFragmentActivity(), Observer { defaultBrowserHelper.openSumoPage() })
        viewModel.triggerWebOpen.observe(context.toFragmentActivity(), Observer { defaultBrowserHelper.triggerWebOpen() })
        viewModel.openDefaultAppsSettingsTutorialDialog.observe(context.toFragmentActivity(), Observer { DialogUtils.showGoToSystemAppsSettingsDialog(context, viewModel) })
        viewModel.openUrlTutorialDialog.observe(context.toFragmentActivity(), Observer { DialogUtils.showOpenUrlDialog(context, viewModel) })
        viewModel.successToSetDefaultBrowser.observe(context.toFragmentActivity(), Observer { defaultBrowserHelper.showSuccessMessage() })
        viewModel.failToSetDefaultBrowser.observe(context.toFragmentActivity(), Observer { defaultBrowserHelper.showFailMessage() })
    }

    fun update(uiModel: DefaultBrowserPreferenceUiModel) {
        switchView?.let {
            it.isChecked = uiModel.isDefaultBrowser
            Settings.updatePrefDefaultBrowserIfNeeded(context, uiModel.isDefaultBrowser, uiModel.hasDefaultBrowser)
        }
    }

    override fun onClick() {
        viewModel.performAction()
    }

    fun onFragmentResume() {
        viewModel.onResume()
    }

    fun onFragmentPause() {
        viewModel.onPause()
    }

    fun performClick() {
        viewModel.performActionFromNotification()
    }

    companion object {
        const val EXTRA_RESOLVE_BROWSER = "_intent_to_resolve_browser_"
    }
}
