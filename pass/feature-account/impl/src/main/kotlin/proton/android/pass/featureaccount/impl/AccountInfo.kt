package proton.android.pass.featureaccount.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun AccountInfo(modifier: Modifier = Modifier, state: AccountUiState) {
    Column(
        modifier = modifier.roundedContainer(PassTheme.colors.inputBorder)
    ) {
        SettingOption(
            text = state.email ?: "",
            label = stringResource(R.string.account_username_label),
            isLoading = state.isLoadingState.value()
        )
        Divider()
        SettingOption(
            text = state.plan ?: "",
            label = stringResource(R.string.account_subscription_label),
            isLoading = state.isLoadingState.value()
        )
    }
}

@Preview
@Composable
fun AccountInfoPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountInfo(
                state = AccountUiState(
                    "myemail@proton.me",
                    "free",
                    IsLoadingState.NotLoading
                )
            )
        }
    }
}
