package com.fang.weixin;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.fang.common.controls.CustomDialog;
import com.fang.common.util.BaseUtil;
import com.fang.zxing.encoding.EncodingHandler;

/**
 * Created by benren.fj on 6/13/15.
 */
public class ShareHandler implements View.OnClickListener {

    private CustomDialog dialog;

    private String text;

    private String url;
    private String title;
    private String description;
    private Bitmap thumb;

    private TYPE type;
    private Context context;

    private enum TYPE {
        TEXT, WEBPAGE
    }

    public ShareHandler(Context context) {
        this.context = context;
    }

    public void share(String text) {
        this.text = text;
        type = TYPE.TEXT;
        showDialog(context);
    }

    public void share(String url, String title, String description, Bitmap thumb) {
        this.url = url;
        this.title = title;
        this.title = title;
        this.thumb = thumb;
        type = TYPE.WEBPAGE;
        showDialog(context);
    }

    private void showDialog(Context context) {
        if (null != dialog) {
            dialog.show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.share, null);
        rootView.findViewById(R.id.weixin).setOnClickListener(this);
        rootView.findViewById(R.id.timeline).setOnClickListener(this);
        rootView.findViewById(R.id.copylink).setOnClickListener(this);
        rootView.findViewById(R.id.qrcode).setOnClickListener(this);

        dialog = new CustomDialog.Builder(context).setContentView(rootView).create();
        dialog.show();
    }

    private void shareToWeixin(boolean isTimeline) {
        if (type.equals(TYPE.TEXT)) {
            Share.shareText(text, isTimeline);
        } else if (type.equals(TYPE.WEBPAGE)) {
            Share.shareWebPage(url, title, description, thumb, isTimeline);
        }
    }

    private void copyLink() {
        if (type.equals(TYPE.TEXT)) {
            BaseUtil.copy(context, text);
        } else if (type.equals(TYPE.WEBPAGE)) {
            BaseUtil.copy(context, url);
        }
    }
    private void generateQrcode() {
        Bitmap bitmap = null;
        try {
            if (type.equals(TYPE.TEXT)) {
                bitmap = EncodingHandler.createQRCode(text, 100);
            } else if (type.equals(TYPE.WEBPAGE)) {
                bitmap = EncodingHandler.createQRCode(url, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != bitmap) {
            ImageView imageView = new ImageView(context);
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
