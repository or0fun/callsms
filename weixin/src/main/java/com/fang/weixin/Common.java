package com.fang.weixin;

import android.content.Context;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by benren.fj on 6/12/15.
 */
public class Common {

    private static IWXAPI api;

    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static IWXAPI getApi() {
        return api;
    }

    public static void init(Context context) {
        api = WXAPIFactory.createWXAPI(context, Constants.APP_ID);
        api.registerApp(Constants.APP_ID);
    }
}
