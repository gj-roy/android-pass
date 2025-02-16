/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featurehome.impl.onboardingtips

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.common.api.combineN
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.domain.PlanType
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.INVITE
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.NOTIFICATION_PERMISSION
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.TRIAL
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedNotificationBanner
import proton.android.pass.preferences.HasDismissedTrialBanner
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OnBoardingTipsViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val appConfig: AppConfig,
    observeInvites: ObserveInvites,
    getUserPlan: GetUserPlan,
    notificationManager: NotificationManager
) : ViewModel() {

    private val eventFlow: MutableStateFlow<OnBoardingTipsEvent> =
        MutableStateFlow(OnBoardingTipsEvent.Unknown)

    private val hasInvitesFlow: Flow<Boolean> = observeInvites()
        .map { invites -> invites.isNotEmpty() }
        .distinctUntilChanged()

    private val notificationPermissionFlow: MutableStateFlow<Boolean> = MutableStateFlow(
        notificationManager.hasNotificationPermission()
    )

    private val shouldShowAutofillFlow: Flow<Boolean> = combine(
        autofillManager.getAutofillStatus(),
        preferencesRepository.getHasDismissedAutofillBanner(),
        ::shouldShowAutofillBanner
    )

    private val shouldShowNotificationPermissionFlow: Flow<Boolean> = combine(
        notificationPermissionFlow,
        preferencesRepository.getHasDismissedNotificationBanner()
    ) { notificationPermissionEnabled, hasDismissedNotificationBanner ->
        when {
            notificationPermissionEnabled -> false
            hasDismissedNotificationBanner is HasDismissedNotificationBanner.Dismissed -> false
            else -> needsNotificationPermissions()
        }
    }.distinctUntilChanged()

    private val shouldShowTrialFlow: Flow<Boolean> = preferencesRepository
        .getHasDismissedTrialBanner()
        .map { pref ->
            when (pref) {
                HasDismissedTrialBanner.Dismissed -> false
                HasDismissedTrialBanner.NotDismissed -> true
            }
        }
        .distinctUntilChanged()

    val state: StateFlow<OnBoardingTipsUiState> = combineN(
        shouldShowAutofillFlow,
        shouldShowTrialFlow,
        hasInvitesFlow,
        shouldShowNotificationPermissionFlow,
        getUserPlan(),
        eventFlow
    ) { shouldShowAutofill, shouldShowTrial, shouldShowInvites, shouldShowNotificationPermission,
        userPlan, event ->

        val tips = getTips(
            planType = userPlan.planType,
            shouldShowTrial = shouldShowTrial,
            shouldShowAutofill = shouldShowAutofill,
            shouldShowInvites = shouldShowInvites,
            shouldShowNotificationPermission = shouldShowNotificationPermission
        )

        OnBoardingTipsUiState(
            tipsToShow = tips,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OnBoardingTipsUiState(
                tipsToShow = persistentSetOf(),
                event = OnBoardingTipsEvent.Unknown
            )
        )

    private fun getTips(
        planType: PlanType,
        shouldShowTrial: Boolean,
        shouldShowAutofill: Boolean,
        shouldShowInvites: Boolean,
        shouldShowNotificationPermission: Boolean
    ): ImmutableSet<OnBoardingTipPage> {
        val isTrial = planType is PlanType.Trial
        return when {
            shouldShowInvites -> persistentSetOf(INVITE)
            shouldShowNotificationPermission -> persistentSetOf(NOTIFICATION_PERMISSION)
            isTrial && shouldShowTrial -> persistentSetOf(TRIAL)
            shouldShowAutofill -> persistentSetOf(AUTOFILL)
            else -> persistentSetOf()
        }
    }

    private fun shouldShowAutofillBanner(
        autofillSupportedStatus: AutofillSupportedStatus,
        hasDismissedAutofillBanner: HasDismissedAutofillBanner
    ): Boolean =
        autofillSupportedStatus is AutofillSupportedStatus.Supported &&
            autofillSupportedStatus.status !is AutofillStatus.EnabledByOurService &&
            hasDismissedAutofillBanner is HasDismissedAutofillBanner.NotDismissed

    fun onClick(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            AUTOFILL -> autofillManager.openAutofillSelector()
            TRIAL -> eventFlow.update { OnBoardingTipsEvent.OpenTrialScreen }
            INVITE -> eventFlow.update { OnBoardingTipsEvent.OpenInviteScreen }
            NOTIFICATION_PERMISSION -> eventFlow.update { OnBoardingTipsEvent.RequestNotificationPermission }
        }
    }

    fun onDismiss(onBoardingTipPage: OnBoardingTipPage) = viewModelScope.launch {
        when (onBoardingTipPage) {
            AUTOFILL ->
                preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)

            TRIAL ->
                preferencesRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.Dismissed)

            INVITE -> {} // Invites cannot be dismissed
            NOTIFICATION_PERMISSION ->
                preferencesRepository.setHasDismissedNotificationBanner(HasDismissedNotificationBanner.Dismissed)
        }
    }

    fun onNotificationPermissionChanged(permission: Boolean) = viewModelScope.launch {
        notificationPermissionFlow.update { permission }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private fun needsNotificationPermissions(): Boolean =
        appConfig.androidVersion >= Build.VERSION_CODES.TIRAMISU


    fun clearEvent() {
        eventFlow.update { OnBoardingTipsEvent.Unknown }
    }
}
