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

package proton.android.pass.data.impl.extensions

import proton.android.pass.data.api.PendingEventItemRevision
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.impl.responses.ItemRevisionImpl

fun ItemRevision.toPendingEvent(): PendingEventItemRevision =
    PendingEventItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        keyRotation = keyRotation,
        content = content,
        state = state,
        aliasEmail = aliasEmail,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUseTime = lastUseTime,
        revisionTime = revisionTime,
        key = itemKey,
        isPinned = isPinned,
    )

fun PendingEventItemRevision.toItemRevision(): ItemRevision = ItemRevisionImpl(
    itemId = itemId,
    revision = revision,
    contentFormatVersion = contentFormatVersion,
    keyRotation = keyRotation,
    content = content,
    state = state,
    aliasEmail = aliasEmail,
    createTime = createTime,
    modifyTime = modifyTime,
    lastUseTime = lastUseTime,
    revisionTime = revisionTime,
    itemKey = key,
    isPinned = isPinned,
)
