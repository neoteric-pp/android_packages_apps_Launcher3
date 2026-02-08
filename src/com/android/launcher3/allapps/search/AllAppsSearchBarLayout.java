/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.launcher3.allapps.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Insettable;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.ActivityAllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.graphics.ThemeManager;
import com.android.launcher3.qsb.AssistantIconView;

/**
 * Layout wrapper for the All Apps search bar that includes icons like the dock QSB.
 */
public class AllAppsSearchBarLayout extends FrameLayout implements SearchUiManager, Insettable {

    private final int mContentOverlap;

    private AppsSearchContainerLayout mSearchInput;
    private ViewGroup mInner;

    public AllAppsSearchBarLayout(Context context) {
        this(context, null);
    }

    public AllAppsSearchBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsSearchBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContentOverlap = getResources().getDimensionPixelSize(R.dimen.all_apps_search_bar_content_overlap);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSearchInput = findViewById(R.id.search_box_input);
        mInner = findViewById(R.id.search_box_inner);

        setupAppearance();

        // Set up click on the whole search bar to focus the input
        mInner.setOnClickListener(v -> mSearchInput.showKeyboard());
    }

    private void setupAppearance() {
        Context context = getContext();
        ThemeManager themeManager = ThemeManager.INSTANCE.get(context);
        boolean isThemedIcons = themeManager.isMonoThemeEnabled();
        boolean isMusicSearch = Utilities.isMusicSearchEnabled(context);

        // Update background based on theme
        mInner.setBackgroundResource(isThemedIcons ?
                R.drawable.bg_all_apps_searchbox_google_themed :
                R.drawable.bg_all_apps_searchbox_google);

        // Update Google icon
        ImageView gIcon = findViewById(R.id.search_box_g_icon);
        gIcon.setImageResource(isThemedIcons ?
                R.drawable.ic_super_g_themed : R.drawable.ic_super_g_color);

        // Update mic/music icon
        AssistantIconView micIcon = findViewById(R.id.search_box_mic_icon);
        if (isMusicSearch) {
            micIcon.setImageResource(isThemedIcons ?
                    R.drawable.ic_music_themed : R.drawable.ic_music_color);
        } else {
            micIcon.setImageResource(isThemedIcons ?
                    R.drawable.ic_mic_themed : R.drawable.ic_mic_color);
        }
        micIcon.setListener(context);

        // Update lens icon
        ImageButton lensIcon = findViewById(R.id.search_box_lens_icon);
        lensIcon.setImageResource(isThemedIcons ?
                R.drawable.ic_lens_themed : R.drawable.ic_lens_color);

        // Enable lens if GSA is available
        if (Utilities.isGSAEnabled(context)) {
            lensIcon.setVisibility(View.VISIBLE);
            lensIcon.setOnClickListener(v -> {
                Intent lensIntent = new Intent(Intent.ACTION_VIEW)
                        .setComponent(new ComponentName(Utilities.GSA_PACKAGE, Utilities.LENS_ACTIVITY))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setData(Uri.parse(Utilities.LENS_URI))
                        .putExtra("LensHomescreenShortcut", true);
                context.startActivity(lensIntent);
            });
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Shift down to overlap with the content below
        offsetTopAndBottom(mContentOverlap);
    }

    // SearchUiManager implementation - delegate to mSearchInput

    @Override
    public void initializeSearch(ActivityAllAppsContainerView<?> appsView) {
        mSearchInput.initializeSearch(appsView);
    }

    @Override
    public void resetSearch() {
        mSearchInput.resetSearch();
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent event) {
        mSearchInput.preDispatchKeyEvent(event);
    }

    @Override
    public ExtendedEditText getEditText() {
        return mSearchInput;
    }

    @Override
    public void setInsets(Rect insets) {
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = insets.top;
        requestLayout();
    }
}
