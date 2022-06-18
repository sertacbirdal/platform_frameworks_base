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

package com.android.internal.app;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.internal.R;

import java.util.Locale;
import java.util.Set;

/**
 * This adapter extends basic SuggestedLocaleAdapter. In addition to the base functionality, it
 * shows language name not only in the native locale, but in secondary locale as well. Secondary
 * locale is passed as a constructor parameter.
 */
public class BilingualSuggestedLocaleAdapter extends SuggestedLocaleAdapter {

    private final Locale mSecondaryLocale;
    private final int mSecondaryLocaleTextDir;

    public BilingualSuggestedLocaleAdapter(
            Set<LocaleStore.LocaleInfo> localeOptions,
            boolean countryMode,
            Locale secondaryLocale) {
        super(localeOptions, countryMode);
        mSecondaryLocale = secondaryLocale;
        if (TextUtils.getLayoutDirectionFromLocale(secondaryLocale) == View.LAYOUT_DIRECTION_RTL) {
            mSecondaryLocaleTextDir = View.TEXT_DIRECTION_RTL;
        } else {
            mSecondaryLocaleTextDir = View.TEXT_DIRECTION_LTR;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null && super.mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        int itemType = getItemViewType(position);
        switch (itemType) {
            case TYPE_HEADER_SUGGESTED: // intentional fallthrough
            case TYPE_HEADER_ALL_OTHERS:
                // Covers both null, and "reusing" a wrong kind of view
                if (!(convertView instanceof TextView)) {
                    convertView =
                            mInflater.inflate(
                                    R.layout.language_picker_bilingual_section_header,
                                    parent,
                                    false);
                }
                TextView textView = (TextView) convertView;
                if (itemType == TYPE_HEADER_SUGGESTED) {
                    setHeaderText(
                            textView,
                            R.string.language_picker_section_suggested_bilingual,
                            R.string.region_picker_section_suggested_bilingual);
                } else {
                    setHeaderText(
                            textView,
                            R.string.language_picker_section_all,
                            R.string.region_picker_section_all);
                }
                break;
            default:
                // Covers both null, and "reusing" a wrong kind of view
                if (!(convertView instanceof ViewGroup)) {
                    convertView =
                            mInflater.inflate(
                                    R.layout.language_picker_bilingual_item, parent, false);
                }

                LocaleStore.LocaleInfo item = (LocaleStore.LocaleInfo) getItem(position);
                setLocaleToListItem(convertView, item);
        }
        return convertView;
    }

    private void setHeaderText(
            TextView textView, int languageStringResourceId, int regionStringResourceId) {
        if (mCountryMode) {
            setTextTo(textView, regionStringResourceId);
        } else {
            setTextTo(textView, languageStringResourceId);
        }
    }

    private void setLocaleToListItem(View itemView, LocaleStore.LocaleInfo localeInfo) {
        if (localeInfo == null) {
            throw new NullPointerException("Cannot set locale, locale info is null.");
        }

        TextView textNative = (TextView) itemView.findViewById(R.id.locale_native);
        textNative.setText(localeInfo.getLabel(mCountryMode));
        textNative.setTextLocale(localeInfo.getLocale());
        textNative.setContentDescription(localeInfo.getContentDescription(mCountryMode));

        TextView textSecondary = (TextView) itemView.findViewById(R.id.locale_secondary);
        textSecondary.setText(localeInfo.getLocale().getDisplayLanguage(mSecondaryLocale));
        textSecondary.setTextDirection(mSecondaryLocaleTextDir);
        if (mCountryMode) {
            int layoutDir = TextUtils.getLayoutDirectionFromLocale(localeInfo.getParent());
            //noinspection ResourceType
            itemView.setLayoutDirection(layoutDir);
            textNative.setTextDirection(
                    layoutDir == View.LAYOUT_DIRECTION_RTL
                            ? View.TEXT_DIRECTION_RTL
                            : View.TEXT_DIRECTION_LTR);
        }
    }
}
