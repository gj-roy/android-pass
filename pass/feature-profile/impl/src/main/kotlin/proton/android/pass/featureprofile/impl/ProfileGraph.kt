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

package proton.android.pass.featureprofile.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureprofile.impl.applock.AppLockBottomsheet
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Profile : NavItem(baseRoute = "profile", isTopLevel = true)
object FeedbackBottomsheet : NavItem(
    baseRoute = "feedback/bottomsheet",
    isBottomsheet = true
)

object AppLockBottomsheet : NavItem(
    baseRoute = "applock/bottomsheet",
    isBottomsheet = true
)

sealed interface ProfileNavigation {
    object Account : ProfileNavigation
    object AppLock : ProfileNavigation
    object List : ProfileNavigation
    object CreateItem : ProfileNavigation
    object Settings : ProfileNavigation
    object Feedback : ProfileNavigation
    object Report : ProfileNavigation
    object FeatureFlags : ProfileNavigation
    object Upgrade : ProfileNavigation
    object Finish : ProfileNavigation
    object CloseBottomSheet : ProfileNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(
    onNavigateEvent: (ProfileNavigation) -> Unit
) {
    composable(Profile) {
        BackHandler { onNavigateEvent(ProfileNavigation.Finish) }
        ProfileScreen(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(FeedbackBottomsheet) {
        FeedbackBottomsheet(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(AppLockBottomsheet) {
        AppLockBottomsheet(onClose = { onNavigateEvent(ProfileNavigation.CloseBottomSheet) })
    }
}
