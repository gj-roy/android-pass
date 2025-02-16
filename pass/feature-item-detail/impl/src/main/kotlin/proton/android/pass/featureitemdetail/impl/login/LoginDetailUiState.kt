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

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.common.api.Option
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemdetail.impl.common.ItemDetailEvent
import proton.android.pass.featureitemdetail.impl.common.ShareClickAction

sealed interface LoginDetailUiState {

    @Stable
    object NotInitialised : LoginDetailUiState

    @Stable
    object Error : LoginDetailUiState

    @Stable
    object Pending : LoginDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val passwordScore: PasswordScore?,
        val vault: Vault?,
        val totpUiState: TotpUiState?,
        val linkedAlias: Option<LinkedAliasItem>,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
        val canPerformItemActions: Boolean,
        val customFields: ImmutableList<CustomFieldUiContent>,
        val shareClickAction: ShareClickAction,
        val itemActions: ItemActions,
        val event: ItemDetailEvent,
        val isPinningFeatureEnabled: Boolean,
    ) : LoginDetailUiState
}

sealed interface TotpUiState {
    object Hidden : TotpUiState
    object Limited : TotpUiState

    @Stable
    data class Visible(
        val code: String,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : TotpUiState
}

@Stable
data class LinkedAliasItem(
    val shareId: ShareId,
    val itemId: ItemId
)

@Stable
sealed interface CustomFieldUiContent {

    @Stable
    sealed interface Limited : CustomFieldUiContent {
        val label: String

        @Stable
        data class Text(override val label: String) : Limited

        @Stable
        data class Hidden(override val label: String) : Limited

        @Stable
        data class Totp(override val label: String) : Limited
    }

    @Stable
    data class Text(val label: String, val content: String) : CustomFieldUiContent

    @Stable
    data class Hidden(val label: String, val content: HiddenState) : CustomFieldUiContent

    @Stable
    data class Totp(
        val label: String,
        val code: String,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : CustomFieldUiContent
}
