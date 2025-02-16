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

package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.BoxedIcon
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.domain.WebsiteUrl
import me.proton.core.presentation.R as CoreR

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    backgroundColor: Color = if (enabled) {
        PassTheme.colors.loginInteractionNormMinor1
    } else {
        PassTheme.colors.loginInteractionNormMinor2
    },
    foregroundColor: Color = if (enabled) {
        PassTheme.colors.loginInteractionNormMajor2
    } else {
        PassTheme.colors.loginInteractionNormMinor1
    }
) {
    BoxedIcon(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_user),
            contentDescription = null,
            tint = foregroundColor
        )
    }
}

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    website: String?,
    packageName: String?,
    size: Int = 40,
    favIconPadding: Dp = Spacing.small,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    canLoadExternalImages: Boolean,
    enabled: Boolean = true,
    backgroundColor: Color = if (enabled) {
        PassTheme.colors.loginInteractionNormMinor1
    } else {
        PassTheme.colors.loginInteractionNormMinor2
    },
    foregroundColor: Color = if (enabled) {
        PassTheme.colors.loginInteractionNormMajor2
    } else {
        PassTheme.colors.textHint
    }
) {
    if (website == null || !canLoadExternalImages) {
        FallbackLoginIcon(
            modifier = modifier,
            text = text,
            packageName = packageName,
            size = size,
            shape = shape,
            enabled = enabled,
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor
        )
    } else {
        var isLoaded by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }

        val animatedBackgroundColor by if (CROSSFADE_ENABLED) {
            animateColorAsState(
                targetValue = if (isLoaded) {
                    Color.White
                } else PassTheme.colors.loginInteractionNormMinor2,
                animationSpec = tween(
                    durationMillis = CROSSFADE_ANIMATION_MS
                ),
                label = "backgroundColor"
            )
        } else {
            remember { mutableStateOf(Color.White) }
        }

        Box(modifier = modifier.size(size.dp)) {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .clip(shape)
                    .size(size.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(WebsiteUrl(website))
                    .size(size)
                    .apply {
                        if (CROSSFADE_ENABLED) {
                            crossfade(CROSSFADE_ANIMATION_MS)
                        }
                    }
                    .build(),
                loading = {
                    TwoLetterLoginIcon(
                        text = text,
                        shape = shape,
                        backgroundColor = backgroundColor,
                        foregroundColor = foregroundColor
                    )
                },
                onError = {
                    isError = true
                },
                error = {
                    FallbackLoginIcon(
                        text = text,
                        packageName = packageName,
                        size = size,
                        shape = shape,
                        enabled = enabled,
                        backgroundColor = backgroundColor,
                        foregroundColor = foregroundColor
                    )
                },
                onSuccess = {
                    isLoaded = true
                },
                success = {
                    SubcomposeAsyncImageContent(
                        modifier = Modifier
                            .size(size.dp)
                            .border(
                                width = 1.dp,
                                color = PassTheme.colors.loginIconBorder,
                                shape = shape
                            )
                            .background(animatedBackgroundColor)
                            .padding(favIconPadding)
                    )
                },
                contentDescription = null
            )

            if (!enabled && !isError) {
                Box(
                    modifier = Modifier
                        .size(size.dp)
                        .background(
                            color = PassTheme.colors.loginIconDisabledMask,
                            shape = shape
                        )
                )
            }
        }
    }
}

@Composable
private fun FallbackLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    packageName: String?,
    size: Int = 40,
    shape: Shape,
    enabled: Boolean,
    backgroundColor: Color,
    foregroundColor: Color
) {
    if (packageName == null) {
        TwoLetterLoginIcon(
            modifier = modifier,
            text = text,
            size = size,
            shape = shape,
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor
        )
    } else {
        LinkedAppIcon(
            packageName = packageName,
            size = size,
            shape = shape,
            enabled = enabled,
            emptyContent = {
                TwoLetterLoginIcon(
                    modifier = modifier,
                    text = text,
                    size = size,
                    shape = shape,
                    backgroundColor = backgroundColor,
                    foregroundColor = foregroundColor
                )
            }
        )
    }
}

@Composable
private fun TwoLetterLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    size: Int = 40,
    shape: Shape,
    backgroundColor: Color,
    foregroundColor: Color
) {
    CircleTextIcon(
        modifier = modifier,
        text = text,
        backgroundColor = backgroundColor,
        textColor = foregroundColor,
        size = size,
        shape = shape
    )
}

@Preview
@Composable
fun LoginIconPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LoginIcon(
                text = "login text",
                website = null,
                packageName = null,
                canLoadExternalImages = false,
                enabled = input.second
            )
        }
    }
}

internal const val CROSSFADE_ANIMATION_MS = 150
internal const val CROSSFADE_ENABLED = false
