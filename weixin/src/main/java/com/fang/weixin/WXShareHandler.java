package com.fang.weixin;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fang.common.base.Global;
import com.fang.common.controls.CustomDialog;
import com.fang.common.util.BaseUtil;
import com.fang.zxing.encoding.EncodingHandler;

/**
 * Created by benren.fj on 6/13/15.
 */
public class WXShareHandler implements View.OnClickListener {

    private CustomDialog dialog;

    private String text;

    private String url;
    private String title;
    private String description;
    private Bitmap thumb;

    private TYPE contentType;
    private Context context;

    private enum TYPE {
        TEXT, WEBPAGE
    }


    public WXShareHandler(Context context) {
        this.context = context;
    }

    public void share(String text, int shareType) {
        this.text = text;
        contentType = TYPE.TEXT;
        showDialog(context, shareType);
    }

    public void share(String url, String title, String description, Bitmap thumb, int shareType) {
        this.url = url;
        this.title = title;
        this.title = title;
        this.thumb = thumb;
        contentType = TYPE.WEBPAGE;
        showDialog(context, shareType);
    }

    private void showDialog(Context context, int shareType) {
        if (null != dialog) {
            dialog.show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.share, null);
        if (WXConstants.SHARE_ALL == shareType || 0 < (shareType & WXConstants.SHARE_WEIXIN)) {
            View view = rootView.findViewById(R.id.weixin);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(this);
        }
        if (WXConstants.SHARE_ALL == shareType ||0 < (shareType & WXConstants.SHARE_TIMELINE)) {
            View view = rootView.findViewById(R.id.timeline);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(this);
        }
        if (WXConstants.SHARE_ALL == shareType ||0 < (shareType & WXConstants.SHARE_COPY)) {
            View view = rootView.findViewById(R.id.copylink);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(this);
        }
        if (WXConstants.SHARE_ALL == shareType || 0 < (shareType & WXConstants.SHARE_QRCODE)) {
            View view = rootView.findViewById(R.id.qrcode);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(this);
        }

        dialog = new CustomDialog.Builder(context).setContentView(rootView)
        .setWidth((int)(Global.fullScreenWidth * 0.9)).create();
        dialog.show();
    }

    private void shareToWeixin(boolean isTimeline) {
        if (contentType.equals(TYPE.TEXT)) {
            WXShare.shareText(text, isTimeline);
        } else if (contentType.equals(TYPE.WEBPAGE)) {
            WXShare.shareWebPage(url, title, description, thumb, isTimeline);
        }
    }

    private void copyLink() {
        if (contentType.equals(TYPE.TEXT)) {
            BaseUtil.copy(context, text);
        } else if (contentType.equals(TYPE.WEBPAGE)) {
            BaseUtil.copy(context, url);
        }
    }
    private void generateQrcode() {
        Bitmap bitmap = null;
        try {
            if (contentType.equals(TYPE.TEXT)) {
                bitmap = EncodingHandler.createQRCode(text, (int)(Global.fullScreenWidth * 0.8));
            } else if (contentType.equals(TYPE.WEBPAGE)) {
                bitmap = EncodingHandler.createQRCode(url, (int)(Global.fullScreenWidth * 0.8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != bitmap) {
            ImageView imageView = new ImageView(context);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(lp);
            imageView.setImageBitmap(bitmap);
            new CustomDialog.Builder(context).setContentView(imageView).create().show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.weixin == id) {
            shareToWeixin(false);
            dialog.cancel();
        }else if (R.id.timeline == id) {
            shareToWeixin(true);
            dialog.cancel();
        }else if (R.id.copylink == id) {
            copyLink();
            dialog.cancel();
        }else if (R.id.qrcode == id) {
            generateQrcode();
            dialog.cancel();
        }
    }
}
