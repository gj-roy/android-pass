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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVaultStatus
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class CanShareVaultImpl @Inject constructor(
    private val getVaultById: GetVaultById,
    private val getShareById: GetShareById,
    private val getUserPlan: GetUserPlan,
) : CanShareVault {

    override suspend fun invoke(shareId: ShareId): CanShareVaultStatus {
        val vault = runCatching { getVaultById(shareId = shareId).first() }.getOrElse {
            PassLogger.w(TAG, "canShare vault not found")
            PassLogger.w(TAG, it)
            return CanShareVaultStatus.CannotShare(CanShareVaultStatus.CannotShareReason.Unknown)
        }

        return invoke(vault)
    }

    override suspend fun invoke(vault: Vault): CanShareVaultStatus {
        val share = runCatching { getShareById(shareId = vault.shareId) }.getOrElse {
            PassLogger.w(TAG, "canShare share not found")
            PassLogger.w(TAG, it)
            return CanShareVaultStatus.CannotShare(CanShareVaultStatus.CannotShareReason.Unknown)
        }

        return when {
            getUserPlan().first().isBusinessPlan -> {
                CanShareVaultStatus.CanShare(invitesRemaining = share.remainingInvites)
            }

            !share.hasRemainingInvites -> {
                CanShareVaultStatus.CannotShare(CanShareVaultStatus.CannotShareReason.NotEnoughInvites)
            }

            vault.isOwned -> {
                CanShareVaultStatus.CanShare(invitesRemaining = share.remainingInvites)
            }

            vault.role == ShareRole.Admin -> {
                CanShareVaultStatus.CanShare(invitesRemaining = share.remainingInvites)
            }

            else -> {
                CanShareVaultStatus.CannotShare(CanShareVaultStatus.CannotShareReason.NotEnoughPermissions)
            }
        }
    }

    companion object {
        private const val TAG = "CanShareVaultImpl"
    }

}
