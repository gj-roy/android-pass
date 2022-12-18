package me.proton.pass.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.proton.android.pass.preferences.UserPreferencesRepository
import me.proton.android.pass.preferences.value
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.some
import javax.inject.Inject

@HiltViewModel
class NavHomeViewModel @Inject constructor(
    preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    private val shouldAuthenticateState: Flow<Boolean> = combine(
        preferenceRepository.getBiometricLockState(),
        preferenceRepository.getHasAuthenticated()
    ) { biometricLock, hasAuthenticated ->
        biometricLock.value() && !hasAuthenticated.value()
    }

    val navHomeUiState: SharedFlow<NavHomeUiState> = combine(
        shouldAuthenticateState.asResultWithoutLoading(),
        preferenceRepository.getHasCompletedOnBoarding().asResultWithoutLoading()
    ) { shouldAuthenticate, hasCompletedOnBoarding ->
        NavHomeUiState(
            shouldAuthenticate = when (shouldAuthenticate) {
                is Result.Success -> shouldAuthenticate.data.some()
                else -> None
            },
            hasCompletedOnBoarding = when (hasCompletedOnBoarding) {
                is Result.Success -> hasCompletedOnBoarding.data.value().some()
                else -> None
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NavHomeUiState.Initial
        )
}

@Immutable
data class NavHomeUiState(
    val shouldAuthenticate: Option<Boolean>,
    val hasCompletedOnBoarding: Option<Boolean>
) {
    companion object {
        val Initial = NavHomeUiState(None, None)
    }
}
