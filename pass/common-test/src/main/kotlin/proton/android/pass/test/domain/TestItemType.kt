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

package proton.android.pass.test.domain

import proton.android.pass.domain.ItemType
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.test.TestUtils

object TestItemType {
    fun login(
        username: String? = null,
        password: String? = null,
        websites: List<String> = emptyList(),
        packageInfoSet: Set<PackageInfo> = emptySet(),
    ): ItemType.Login = ItemType.Login(
        username = username ?: TestUtils.randomString(),
        password = password ?: TestUtils.randomString(),
        websites = websites,
        packageInfoSet = packageInfoSet,
        primaryTotp = TestUtils.randomString(),
        customFields = emptyList(),
        passkeys = emptyList()
    )
}
