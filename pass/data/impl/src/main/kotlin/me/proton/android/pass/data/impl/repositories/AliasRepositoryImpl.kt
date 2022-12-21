package me.proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.proton.android.pass.data.api.repositories.AliasRepository
import me.proton.android.pass.data.impl.extensions.toDomain
import me.proton.android.pass.data.impl.remote.RemoteAliasDataSource
import me.proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import me.proton.android.pass.data.impl.responses.AliasMailboxResponse
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class AliasRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasDataSource
) : AliasRepository {

    override fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions> =
        remoteDataSource.getAliasOptions(userId, shareId)
            .map { it.toDomain() }
            .flowOn(Dispatchers.IO)

    override fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails> =
        remoteDataSource.getAliasDetails(userId, shareId, itemId)
            .map { details ->
                AliasDetails(
                    email = details.email,
                    mailboxes = mapMailboxes(details.mailboxes),
                    availableMailboxes = mapMailboxes(details.availableMailboxes)
                )
            }
            .flowOn(Dispatchers.IO)

    override fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit> {
        val request = UpdateAliasMailboxesRequest(
            mailboxIds = mailboxes.map { it.id }
        )
        return remoteDataSource.updateAliasMailboxes(userId, shareId, itemId, request)
            .map { }
            .flowOn(Dispatchers.IO)
    }

    private fun mapMailboxes(input: List<AliasMailboxResponse>): List<AliasMailbox> =
        input.map { AliasMailbox(id = it.id, email = it.email) }
}
