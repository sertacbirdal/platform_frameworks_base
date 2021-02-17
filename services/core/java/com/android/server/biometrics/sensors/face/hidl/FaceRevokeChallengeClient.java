/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.biometrics.sensors.face.hidl;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.biometrics.face.V1_0.IBiometricsFace;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;

import com.android.server.biometrics.sensors.RevokeChallengeClient;

/**
 * Face-specific revokeChallenge client supporting the {@link android.hardware.biometrics.face.V1_0}
 * HIDL interface.
 */
public class FaceRevokeChallengeClient extends RevokeChallengeClient<IBiometricsFace> {

    private static final String TAG = "FaceRevokeChallengeClient";

    FaceRevokeChallengeClient(@NonNull Context context,
            @NonNull LazyDaemon<IBiometricsFace> lazyDaemon, @NonNull IBinder token,
            @NonNull String owner, int sensorId) {
        super(context, lazyDaemon, token, owner, sensorId);
    }

    @Override
    protected void startHalOperation() {
        try {
            getFreshDaemon().revokeChallenge();
        } catch (RemoteException e) {
            Slog.e(TAG, "revokeChallenge failed", e);
        }
    }
}
