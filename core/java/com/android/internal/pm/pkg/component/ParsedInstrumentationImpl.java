/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.internal.pm.pkg.component;

import static com.android.internal.pm.parsing.pkg.PackageImpl.sForInternedString;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DataClass;
import com.android.internal.util.Parcelling.BuiltIn.ForInternedString;

/** @hide */
@DataClass(genGetters = true, genSetters = true, genBuilder = false, genParcelable = false)
@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class ParsedInstrumentationImpl extends ParsedComponentImpl implements
        ParsedInstrumentation, Parcelable {

    @Nullable
    @DataClass.ParcelWith(ForInternedString.class)
    private String targetPackage;
    @Nullable
    @DataClass.ParcelWith(ForInternedString.class)
    private String targetProcesses;
    private boolean handleProfiling;
    private boolean functionalTest;

    public ParsedInstrumentationImpl() {
    }

    public ParsedInstrumentationImpl setTargetPackage(@Nullable String targetPackage) {
        this.targetPackage = TextUtils.safeIntern(targetPackage);
        return this;
    }

    public ParsedInstrumentationImpl setTargetProcesses(@Nullable String targetProcesses) {
        this.targetProcesses = TextUtils.safeIntern(targetProcesses);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Instrumentation{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        ComponentName.appendShortString(sb, getPackageName(), getName());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        sForInternedString.parcel(this.targetPackage, dest, flags);
        sForInternedString.parcel(this.targetProcesses, dest, flags);
        dest.writeBoolean(this.handleProfiling);
        dest.writeBoolean(this.functionalTest);
    }

    protected ParsedInstrumentationImpl(Parcel in) {
        super(in);
        this.targetPackage = sForInternedString.unparcel(in);
        this.targetProcesses = sForInternedString.unparcel(in);
        this.handleProfiling = in.readByte() != 0;
        this.functionalTest = in.readByte() != 0;
    }

    @NonNull
    public static final Parcelable.Creator<ParsedInstrumentationImpl> CREATOR =
            new Parcelable.Creator<ParsedInstrumentationImpl>() {
                @Override
                public ParsedInstrumentationImpl createFromParcel(Parcel source) {
                    return new ParsedInstrumentationImpl(source);
                }

                @Override
                public ParsedInstrumentationImpl[] newArray(int size) {
                    return new ParsedInstrumentationImpl[size];
                }
            };



    // Code below generated by codegen v1.0.23.
    //
    // DO NOT MODIFY!
    // CHECKSTYLE:OFF Generated code
    //
    // To regenerate run:
    // $ codegen $ANDROID_BUILD_TOP/frameworks/base/core/java/com/android/internal/pm/pkg/component/ParsedInstrumentationImpl.java
    //
    // To exclude the generated code from IntelliJ auto-formatting enable (one-time):
    //   Settings > Editor > Code Style > Formatter Control
    //@formatter:off


    @DataClass.Generated.Member
    public ParsedInstrumentationImpl(
            @Nullable String targetPackage,
            @Nullable String targetProcesses,
            boolean handleProfiling,
            boolean functionalTest) {
        this.targetPackage = targetPackage;
        this.targetProcesses = targetProcesses;
        this.handleProfiling = handleProfiling;
        this.functionalTest = functionalTest;

        // onConstructed(); // You can define this method to get a callback
    }

    @DataClass.Generated.Member
    public @Nullable String getTargetPackage() {
        return targetPackage;
    }

    @DataClass.Generated.Member
    public @Nullable String getTargetProcesses() {
        return targetProcesses;
    }

    @DataClass.Generated.Member
    public boolean isHandleProfiling() {
        return handleProfiling;
    }

    @DataClass.Generated.Member
    public boolean isFunctionalTest() {
        return functionalTest;
    }

    @DataClass.Generated.Member
    public @NonNull ParsedInstrumentationImpl setHandleProfiling( boolean value) {
        handleProfiling = value;
        return this;
    }

    @DataClass.Generated.Member
    public @NonNull ParsedInstrumentationImpl setFunctionalTest( boolean value) {
        functionalTest = value;
        return this;
    }

    @DataClass.Generated(
            time = 1701445763455L,
            codegenVersion = "1.0.23",
            sourceFile = "frameworks/base/core/java/com/android/internal/pm/pkg/component/ParsedInstrumentationImpl.java",
            inputSignatures = "private @android.annotation.Nullable @com.android.internal.util.DataClass.ParcelWith(com.android.internal.util.Parcelling.BuiltIn.ForInternedString.class) java.lang.String targetPackage\nprivate @android.annotation.Nullable @com.android.internal.util.DataClass.ParcelWith(com.android.internal.util.Parcelling.BuiltIn.ForInternedString.class) java.lang.String targetProcesses\nprivate  boolean handleProfiling\nprivate  boolean functionalTest\npublic static final @android.annotation.NonNull android.os.Parcelable.Creator<com.android.internal.pm.pkg.component.ParsedInstrumentationImpl> CREATOR\npublic  com.android.internal.pm.pkg.component.ParsedInstrumentationImpl setTargetPackage(java.lang.String)\npublic  com.android.internal.pm.pkg.component.ParsedInstrumentationImpl setTargetProcesses(java.lang.String)\npublic  java.lang.String toString()\npublic @java.lang.Override int describeContents()\npublic @java.lang.Override void writeToParcel(android.os.Parcel,int)\nclass ParsedInstrumentationImpl extends com.android.internal.pm.pkg.component.ParsedComponentImpl implements [com.android.internal.pm.pkg.component.ParsedInstrumentation, android.os.Parcelable]\n@com.android.internal.util.DataClass(genGetters=true, genSetters=true, genBuilder=false, genParcelable=false)")
    @Deprecated
    private void __metadata() {}


    //@formatter:on
    // End of generated code

}
