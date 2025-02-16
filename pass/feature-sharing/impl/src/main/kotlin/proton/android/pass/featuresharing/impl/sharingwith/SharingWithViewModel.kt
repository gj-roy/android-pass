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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.usecases.ObserveInviteRecommendations
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresharing.impl.ShowEditVaultArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class SharingWithViewModel @Inject constructor(
    private val emailValidator: EmailValidator,
    private val bulkInviteRepository: BulkInviteRepository,
    observeVaultById: ObserveVaultById,
    observeInviteRecommendations: ObserveInviteRecommendations,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = ShareId(
        id = savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key)
    )
    private val showEditVault: Boolean = savedStateHandleProvider.get()
        .require(ShowEditVaultArgId.key)

    private val continueEnabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val scrollToBottomFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val showEmailNotValidFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val eventState: MutableStateFlow<SharingWithEvents> =
        MutableStateFlow(SharingWithEvents.Unknown)
    private val enteredEmailsState: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val selectedEmailIndexFlow: MutableStateFlow<Option<Int>> = MutableStateFlow(None)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var editingEmailState by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val editingEmailStateFlow: MutableStateFlow<String> =
        MutableStateFlow(editingEmailState)

    @OptIn(FlowPreview::class)
    private val debouncedEditingEmailStateFlow = editingEmailStateFlow
        .debounce(DEBOUNCE_TIMEOUT)
        .onStart { emit("") }
        .distinctUntilChanged()

    private val recommendationsFlow = debouncedEditingEmailStateFlow
        .flatMapLatest { email ->
            observeInviteRecommendations(
                shareId = shareId,
                startsWith = email.takeIfNotBlank().toOption()
            ).asLoadingResult()
        }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var checkedEmails: Set<String> by savedStateHandleProvider.get()
        .saveable { mutableStateOf(emptySet<String>()) }

    private val checkedEmailFlow = MutableStateFlow(checkedEmails)

    private val suggestionsUIStateFlow = combine(
        recommendationsFlow,
        checkedEmailFlow
    ) { result, checkedEmails ->
        when (result) {
            is LoadingResult.Error -> SuggestionsUIState.Initial
            LoadingResult.Loading -> SuggestionsUIState.Loading
            is LoadingResult.Success -> SuggestionsUIState.Content(
                groupDisplayName = result.data.groupDisplayName,
                recentEmails = result.data.recommendedEmails.map { email ->
                    email to checkedEmails.contains(email)
                }.toPersistentList(),
                planEmails = result.data.planRecommendedEmails.map { email ->
                    email to checkedEmails.contains(email)
                }.toPersistentList()
            )
        }
    }

    val editingEmail: String get() = editingEmailState

    val state: StateFlow<SharingWithUIState> = combineN(
        enteredEmailsState,
        showEmailNotValidFlow,
        observeVaultById(shareId = shareId),
        isLoadingState,
        eventState,
        suggestionsUIStateFlow,
        selectedEmailIndexFlow,
        scrollToBottomFlow,
        continueEnabledFlow
    ) { emails, isEmailNotValid, vault, isLoading, event, suggestionsUiState, selectedEmailIndex,
        scrollToBottom, continueEnabled ->
        val vaultValue = vault.value()
        SharingWithUIState(
            enteredEmails = emails.toPersistentList(),
            selectedEmailIndex = selectedEmailIndex,
            vault = vaultValue,
            showEmailNotValidError = isEmailNotValid,
            isLoading = isLoading.value() || vaultValue == null,
            event = event,
            showEditVault = showEditVault,
            suggestionsUIState = suggestionsUiState,
            scrollToBottom = scrollToBottom,
            isContinueEnabled = continueEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingWithUIState()
    )

    fun onEmailChange(value: String) {
        val sanitised = value.replace(" ", "").replace("\n", "")
        editingEmailState = sanitised
        editingEmailStateFlow.update { sanitised }
        showEmailNotValidFlow.update { false }
        selectedEmailIndexFlow.update { None }
        onChange()
    }

    fun onEmailSubmit() = viewModelScope.launch {
        if (checkValidEmail()) {
            enteredEmailsState.update {
                if (!it.contains(editingEmailState)) {
                    scrollToBottomFlow.update { true }
                    it + editingEmailState
                } else {
                    it
                }
            }
            editingEmailState = ""
            editingEmailStateFlow.update { "" }
            onChange()
        }
    }

    fun onEmailClick(index: Int) = viewModelScope.launch {
        if (selectedEmailIndexFlow.value.value() == index) {
            enteredEmailsState.update {
                if (index < 0 || index >= it.size) {
                    it
                } else {
                    val email = it[index]
                    checkedEmails = if (checkedEmails.contains(email)) {
                        checkedEmails - email
                    } else {
                        checkedEmails + email
                    }
                    checkedEmailFlow.update { checkedEmails }
                    it.filterIndexed { idx, _ -> idx != index }
                }
            }
            selectedEmailIndexFlow.update { None }
            onChange()
        } else {
            selectedEmailIndexFlow.update { index.some() }
        }
    }

    fun onContinueClick() = viewModelScope.launch {
        if (editingEmailState.isNotBlank()) {
            if (checkValidEmail()) {
                enteredEmailsState.update { it + editingEmailState }
                editingEmailState = ""
                editingEmailStateFlow.update { "" }
            } else {
                return@launch
            }
        }

        isLoadingState.update { IsLoadingState.Loading }

        val emails = enteredEmailsState.value
        bulkInviteRepository.storeAddresses(emails)
        eventState.update {
            SharingWithEvents.NavigateToPermissions(shareId = shareId)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun clearEvent() {
        eventState.update { SharingWithEvents.Unknown }
    }

    fun onItemToggle(email: String, checked: Boolean) {
        checkedEmails = if (!checked) {
            enteredEmailsState.update {
                if (!it.contains(email)) {
                    scrollToBottomFlow.update { true }
                    it + email
                } else {
                    it
                }
            }
            checkedEmails + email

        } else {
            enteredEmailsState.update {
                if (it.contains(email)) {
                    it - email
                } else {
                    it
                }
            }
            checkedEmails - email
        }
        checkedEmailFlow.update { checkedEmails }
        onChange()
    }

    fun onScrolledToBottom() = viewModelScope.launch {
        scrollToBottomFlow.update { false }
    }

    private fun onChange() {
        continueEnabledFlow.update {
            enteredEmailsState.value.isNotEmpty() || editingEmailState.isNotBlank()
        }
    }

    private fun checkValidEmail(): Boolean {
        if (editingEmailState.isBlank() || !emailValidator.isValid(editingEmailState)) {
            PassLogger.i(TAG, "Email not valid")
            showEmailNotValidFlow.update { true }
            return false
        }
        return true
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L

        private const val TAG = "SharingWithViewModel"
    }
}

