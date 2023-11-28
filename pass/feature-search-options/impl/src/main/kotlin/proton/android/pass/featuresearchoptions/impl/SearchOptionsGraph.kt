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

package proton.android.pass.featuresearchoptions.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet

enum class SortingLocation {
    Home,
    Autofill;
}

object SortingLocationNavArgId : NavArgId {
    override val key = "sortingLocation"
    override val navType = NavType.StringType
}

object SortingBottomsheet : NavItem(
    baseRoute = "searchoptions/sorting/bottomsheet",
    navArgIds = listOf(SortingLocationNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(location: SortingLocation): String =
        "$baseRoute/${location.name}"
}

object SearchOptionsBottomsheet : NavItem(
    baseRoute = "searchoptions/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

object FilterBottomsheet : NavItem(
    baseRoute = "searchoptions/filter/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

fun NavGraphBuilder.searchOptionsGraph(
    onNavigateEvent: (SearchOptionsNavigation) -> Unit
) {
    bottomSheet(SearchOptionsBottomsheet) {
        SearchOptionsBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
    bottomSheet(SortingBottomsheet) {
        SortingBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
    bottomSheet(FilterBottomsheet) {
        FilterBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
}

