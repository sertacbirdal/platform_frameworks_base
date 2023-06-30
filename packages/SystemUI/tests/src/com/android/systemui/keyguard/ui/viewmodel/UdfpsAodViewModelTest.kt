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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.RoboPilotTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.bouncer.data.repository.FakeKeyguardBouncerRepository
import com.android.systemui.bouncer.data.repository.KeyguardBouncerRepository
import com.android.systemui.common.ui.data.repository.FakeConfigurationRepository
import com.android.systemui.coroutines.collectLastValue
import com.android.systemui.doze.util.BurnInHelperWrapper
import com.android.systemui.flags.FakeFeatureFlags
import com.android.systemui.flags.Flags
import com.android.systemui.keyguard.data.repository.FakeCommandQueue
import com.android.systemui.keyguard.data.repository.FakeKeyguardRepository
import com.android.systemui.keyguard.domain.interactor.BurnInInteractor
import com.android.systemui.keyguard.domain.interactor.KeyguardInteractor
import com.android.systemui.keyguard.domain.interactor.UdfpsKeyguardInteractor
import com.android.systemui.util.time.FakeSystemClock
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
@SmallTest
@RoboPilotTest
@RunWith(AndroidJUnit4::class)
class UdfpsAodViewModelTest : SysuiTestCase() {
    private val defaultPadding = 12
    private lateinit var underTest: UdfpsAodViewModel

    private lateinit var testScope: TestScope
    private lateinit var configRepository: FakeConfigurationRepository
    private lateinit var bouncerRepository: KeyguardBouncerRepository
    private lateinit var keyguardRepository: FakeKeyguardRepository
    private lateinit var fakeCommandQueue: FakeCommandQueue
    private lateinit var featureFlags: FakeFeatureFlags

    @Mock private lateinit var burnInHelper: BurnInHelperWrapper

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        overrideResource(com.android.systemui.R.dimen.lock_icon_padding, defaultPadding)
        testScope = TestScope()
        configRepository = FakeConfigurationRepository()
        keyguardRepository = FakeKeyguardRepository()
        bouncerRepository = FakeKeyguardBouncerRepository()
        fakeCommandQueue = FakeCommandQueue()
        featureFlags =
            FakeFeatureFlags().apply {
                set(Flags.REFACTOR_UDFPS_KEYGUARD_VIEWS, true)
                set(Flags.FACE_AUTH_REFACTOR, false)
            }

        val udfpsKeyguardInteractor =
            UdfpsKeyguardInteractor(
                configRepository,
                BurnInInteractor(
                    context,
                    burnInHelper,
                    testScope.backgroundScope,
                    configRepository,
                    FakeSystemClock(),
                ),
                KeyguardInteractor(
                    keyguardRepository,
                    fakeCommandQueue,
                    featureFlags,
                    bouncerRepository,
                    configRepository,
                ),
            )

        underTest =
            UdfpsAodViewModel(
                udfpsKeyguardInteractor,
                context,
            )
    }

    @Test
    fun alphaAndVisibleUpdates_onDozeAmountChanges() =
        testScope.runTest {
            val alpha by collectLastValue(underTest.alpha)
            val visible by collectLastValue(underTest.isVisible)

            keyguardRepository.setDozeAmount(0f)
            runCurrent()
            assertThat(alpha).isEqualTo(0f)
            assertThat(visible).isFalse()

            keyguardRepository.setDozeAmount(.65f)
            runCurrent()
            assertThat(alpha).isEqualTo(.65f)
            assertThat(visible).isTrue()

            keyguardRepository.setDozeAmount(.23f)
            runCurrent()
            assertThat(alpha).isEqualTo(.23f)
            assertThat(visible).isTrue()

            keyguardRepository.setDozeAmount(1f)
            runCurrent()
            assertThat(alpha).isEqualTo(1f)
            assertThat(visible).isTrue()
        }

    @Test
    fun paddingUpdates_onScaleForResolutionChanges() =
        testScope.runTest {
            val padding by collectLastValue(underTest.padding)

            configRepository.setScaleForResolution(1f)
            runCurrent()
            assertThat(padding).isEqualTo(defaultPadding)

            configRepository.setScaleForResolution(2f)
            runCurrent()
            assertThat(padding).isEqualTo(defaultPadding * 2)

            configRepository.setScaleForResolution(.5f)
            runCurrent()
            assertThat(padding).isEqualTo((defaultPadding * .5f).toInt())
        }
}
