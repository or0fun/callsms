package com.fang.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fang.base.RequestUrl;
import com.fang.call.CallRecordDialog;
import com.fang.callsms.R;
import com.fang.common.controls.CustomEditText;
import com.fang.common.util.Patterns;
import com.fang.contact.ContactHelper;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.Util;

/**
 * 搜索模块
 * Created by benren.fj on 6/21/15.
 */
public class SearchView implements View.OnClickListener {

    Context mContext;
    View mRootView;
    //搜索框
    CustomEditText mSearchEditView;
    //搜索按钮
    Button mSearchBtn;
    // 号码信息对话框
    CallRecordDialog mCallRecordDialog;
    Bitmap mContactBitmap;

    //缓存号码
    String mNumberString = "";

    public SearchView(Context context, View view) {
        mContext = context;
        mRootView = view;
        mSearchBtn = (Button) mRootView.findViewById(R.id.searchBtn);
        mSearchBtn.setOnClickListener(this);
        mSearchEditView = (CustomEditText) mRootView.findViewById(R.id.search);
        mSearchEditView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    searchBtnClick();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.searchBtn) {
            searchBtnClick();
        }
    }

    public void show(int visibility) {
        mRootView.setVisibility(visibility);
    }

    /**
     *  搜索号码
     */
    protected void searchBtnClick() {
        String str = mSearchEditView.getText().toString().trim();
        if (str.length() < 3) {
            Util.openUrl(RequestUrl.BAIDU + str);
            return;
        }
        if (!str.matches(Patterns.PHONE_NUMBER_PATTERN) &&
                !str.matches(Patterns.NUMBER_PATTERN)) {
            Util.openUrl(RequestUrl.BAIDU + str);
            return;
        }
        if (null == mCallRecordDialog) {
            if (null == mContactBitmap) {
                mContactBitmap = BitmapFactory.decodeResource(
                        mContext.getResources(), R.drawable.contact_photo);
            }
            mCallRecordDialog = new CallRecordDialog(mContext, str,
                    ContactHelper.getPerson(mContext, str), mContactBitmap);
        } else {
            if (!str.equals(mNumberString)) {
                mCallRecordDialog.setContent(str,
                        ContactHelper.getPerson(mContext, str), mContactBitmap);
            }
        }

        SharedPreferencesHelper.getInstance().setString(SharedPreferencesHelper.NUMBER_SEARCH, str);
        mCallRecordDialog.show();

        //日志
        LogOperate.updateLog(mContext, LogCode.SEARCH_NUMBER);
    }

    public EditText getSearchEditView() {
        return mSearchEditView.getEditText();
    }

    public void setText(String text) {
        mSearchEditView.setText(text);
    }
}
