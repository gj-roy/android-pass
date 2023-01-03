package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.create.alias.AliasItem
import me.proton.pass.presentation.create.alias.AliasSuffixUiModel

class CreateAliasSectionPreviewProvider :
    PreviewParameterProvider<CreateAliasSectionPreviewParameter> {
    override val values: Sequence<CreateAliasSectionPreviewParameter>
        get() = sequenceOf(
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItem = aliasItem("")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItem = aliasItem("some.alias")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItem = aliasItem("this.is.a.very.very.long.alias.that.should.appear.in.two.lines")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = false,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItem = aliasItem("some.alias")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = true,
                onInvalidAliasError = false,
                aliasItem = aliasItem("")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = true,
                aliasItem = aliasItem("invalid!alias")
            )
        )


    private fun aliasItem(alias: String) = AliasItem(
        alias = alias,
        selectedSuffix = AliasSuffixUiModel(
            suffix = "@random.suffix",
            signedSuffix = "",
            isCustom = false,
            domain = "random.suffix"
        ),
        aliasToBeCreated = "$alias@random.suffix"
    )
}

data class CreateAliasSectionPreviewParameter(
    val aliasItem: AliasItem,
    val canEdit: Boolean,
    val onAliasRequiredError: Boolean,
    val onInvalidAliasError: Boolean
)
