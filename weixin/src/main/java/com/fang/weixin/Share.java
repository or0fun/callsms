package com.fang.weixin;

import android.graphics.Bitmap;

import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;

/**
 * Created by benren.fj on 6/12/15.
 */
public class Share {

    /**
     * 分享文字
     *
     * @param text
     * @param isTimelineCb
     */
    public static void shareText(String text, boolean isTimelineCb) {
        if (text == null || text.length() == 0) {
            return;
        }

        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = text;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Common.buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        // 调用api接口发送数据到微信
        Common.getApi().sendReq(req);
    }

    /**
     * 分享网页
     *
     * @param url
     * @param title
     * @param description
     * @param thumb
     * @param isTimelineCb
     */
    public static void shareWebPage(String url, String title, String description, Bitmap thumb, boolean isTimelineCb) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;
        msg.thumbData = Util.bmpToByteArray(thumb, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Common.buildTransaction("webpage");
        req.message = msg;
        req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        Common.getApi().sendReq(req);

    }

}
