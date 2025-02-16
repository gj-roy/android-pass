/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "ProtonBottomSheetBackHandler"

@[Composable OptIn(ExperimentalMaterialApi::class)]
fun ProtonBottomSheetBackHandler(
    bottomSheetState: ModalBottomSheetState,
    coroutineScope: CoroutineScope,
) {
    if (bottomSheetState.isVisible) {
        BackHandler {
            coroutineScope.launch {
                try {
                    bottomSheetState.hide()
                } catch (e: CancellationException) {
                    PassLogger.d(TAG, e, "Bottom sheet hidden animation interrupted")
                }
            }
        }
    }
}
