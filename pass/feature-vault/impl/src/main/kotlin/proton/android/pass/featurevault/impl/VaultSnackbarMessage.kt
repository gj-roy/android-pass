package proton.android.pass.featurevault.impl

import androidx.annotation.StringRes
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class VaultSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    CreateVaultSuccess(R.string.snackbar_create_vault_success, SnackbarType.SUCCESS),
    CreateVaultError(R.string.snackbar_create_vault_error, SnackbarType.ERROR),
    DeleteVaultSuccess(R.string.snackbar_delete_vault_success, SnackbarType.SUCCESS),
    DeleteVaultError(R.string.snackbar_delete_vault_error, SnackbarType.ERROR),
    CannotDeleteCurrentVault(R.string.snackbar_cannot_delete_current_vault_error, SnackbarType.ERROR),
    ChangeVaultSuccess(R.string.snackbar_change_vault_success, SnackbarType.SUCCESS),
    ChangeVaultError(R.string.snackbar_change_vault_error, SnackbarType.ERROR)
}
