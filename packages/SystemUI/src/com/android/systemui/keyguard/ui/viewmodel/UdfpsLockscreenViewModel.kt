/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.keyguard.ui.viewmodel

import android.content.Context
import androidx.annotation.ColorInt
import com.android.settingslib.Utils.getColorAttrDefaultColor
import com.android.systemui.R
import com.android.systemui.keyguard.domain.interactor.BurnInOffsets
import com.android.systemui.keyguard.domain.interactor.KeyguardTransitionInteractor
import com.android.systemui.keyguard.domain.interactor.UdfpsKeyguardInteractor
import com.android.systemui.keyguard.shared.model.KeyguardState
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/** View-model for UDFPS lockscreen views. */
@ExperimentalCoroutinesApi
open class UdfpsLockscreenViewModel(
    context: Context,
    lockscreenColorResId: Int,
    alternateBouncerColorResId: Int,
    transitionInteractor: KeyguardTransitionInteractor,
) {
    private val toLockscreen: Flow<TransitionViewModel> =
        transitionInteractor.anyStateToLockscreenTransition.map {
            TransitionViewModel(
                alpha =
                    if (it.from == KeyguardState.AOD) {
                        it.value // animate
                    } else {
                        1f
                    },
                scale = 1f,
                color = getColorAttrDefaultColor(context, lockscreenColorResId),
            )
        }

    private val toAlternateBouncer: Flow<TransitionViewModel> =
        transitionInteractor.anyStateToAlternateBouncerTransition.map {
            TransitionViewModel(
                alpha = 1f,
                scale =
                    if (visibleInKeyguardState(it.from)) {
                        1f
                    } else {
                        it.value
                    },
                color = getColorAttrDefaultColor(context, alternateBouncerColorResId),
            )
        }

    private val fadeOut: Flow<TransitionViewModel> =
        merge(
                transitionInteractor.anyStateToGoneTransition,
                transitionInteractor.anyStateToAodTransition,
                transitionInteractor.anyStateToOccludedTransition,
                transitionInteractor.anyStateToPrimaryBouncerTransition,
                transitionInteractor.anyStateToDreamingTransition,
            )
            .map {
                TransitionViewModel(
                    alpha =
                        if (visibleInKeyguardState(it.from)) {
                            1f - it.value
                        } else {
                            0f
                        },
                    scale = 1f,
                    color =
                        if (it.from == KeyguardState.ALTERNATE_BOUNCER) {
                            getColorAttrDefaultColor(context, alternateBouncerColorResId)
                        } else {
                            getColorAttrDefaultColor(context, lockscreenColorResId)
                        },
                )
            }

    private fun visibleInKeyguardState(state: KeyguardState): Boolean {
        return when (state) {
            KeyguardState.OFF,
            KeyguardState.DOZING,
            KeyguardState.DREAMING,
            KeyguardState.AOD,
            KeyguardState.PRIMARY_BOUNCER,
            KeyguardState.GONE,
            KeyguardState.OCCLUDED -> false
            KeyguardState.LOCKSCREEN,
            KeyguardState.ALTERNATE_BOUNCER -> true
        }
    }

    val transition: Flow<TransitionViewModel> =
        merge(
            toAlternateBouncer,
            toLockscreen,
            fadeOut,
        )
    val visible: Flow<Boolean> = transition.map { it.alpha != 0f }
}

@ExperimentalCoroutinesApi
class FingerprintViewModel
@Inject
constructor(
    val context: Context,
    transitionInteractor: KeyguardTransitionInteractor,
    interactor: UdfpsKeyguardInteractor,
) :
    UdfpsLockscreenViewModel(
        context,
        android.R.attr.textColorPrimary,
        com.android.internal.R.attr.materialColorOnPrimaryFixed,
        transitionInteractor,
    ) {
    val dozeAmount: Flow<Float> = interactor.dozeAmount
    val burnInOffsets: Flow<BurnInOffsets> = interactor.burnInOffsets

    // Padding between the fingerprint icon and its bounding box in pixels.
    val padding: Flow<Int> =
        interactor.scaleForResolution.map { scale ->
            (context.resources.getDimensionPixelSize(R.dimen.lock_icon_padding) * scale)
                .roundToInt()
        }
}

@ExperimentalCoroutinesApi
class BackgroundViewModel
@Inject
constructor(
    val context: Context,
    transitionInteractor: KeyguardTransitionInteractor,
) :
    UdfpsLockscreenViewModel(
        context,
        com.android.internal.R.attr.colorSurface,
        com.android.internal.R.attr.materialColorPrimaryFixed,
        transitionInteractor,
    )

data class TransitionViewModel(
    val alpha: Float,
    val scale: Float,
    @ColorInt val color: Int,
)
