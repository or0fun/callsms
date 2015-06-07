package com.fang.base;

import android.content.Context;

import com.fang.util.SharedPreferencesHelper;

/**
 * Created by benren.fj on 6/7/15.
 */
public class Model {

    public final static int NUMBER_FRAGMENT = 0;
    public final static int CALL_FRAGMENT = 1;
    public final static int CONTACT_FRAGMENT = 2;
    public final static int SETTING_FRAGMENT = 3;

    public Context context;

    public Model(Context context) {
        this.context = context;
    }

    public int getCurrentFragment() {
        return SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SELECTED_PAGE, NUMBER_FRAGMENT);
    }
}
