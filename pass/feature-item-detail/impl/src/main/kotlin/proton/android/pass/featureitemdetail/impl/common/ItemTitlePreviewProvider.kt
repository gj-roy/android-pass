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

package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault

class ThemeItemTitleProvider : ThemePairPreviewProvider<ItemTitleInput>(ItemTitlePreviewProvider())

class ItemTitlePreviewProvider : PreviewParameterProvider<ItemTitleInput> {
    override val values: Sequence<ItemTitleInput>
        get() = sequence {
            yield(
                ItemTitleInput(
                    vault = null,
                    isPinned = false,
                )
            )
            yield(
                ItemTitleInput(
                    vault = Vault(
                        shareId = ShareId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                    ),
                    isPinned = false,
                )
            )
            yield(
                ItemTitleInput(
                    vault = null,
                    isPinned = true,
                )
            )
            yield(
                ItemTitleInput(
                    vault = Vault(
                        shareId = ShareId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                    ),
                    isPinned = true,
                )
            )
        }
}

data class ItemTitleInput(
    val title: String = "A really long title to check if the element is multiline",
    val note: String = "Note body",
    val vault: Vault?,
    val isPinned: Boolean,
)
