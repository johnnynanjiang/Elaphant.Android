package com.breadwallet.presenter.activities.did;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.settings.BaseSettingsActivity;

public class DidDetailActivity extends BaseSettingsActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_did_detail_layout;
    }

    @Override
    public int getBackButtonId() {
        return R.id.back_button;
    }
}