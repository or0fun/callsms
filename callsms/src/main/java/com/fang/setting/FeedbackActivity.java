package com.fang.setting;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.fang.base.BaseActivity;
import com.fang.callsms.R;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;
import com.fang.util.DebugLog;
import com.fang.util.NetWorkUtil;
import com.fang.util.Util;

public class FeedbackActivity extends BaseActivity {

	private String TAG = "FeedbackActivity";
	
	private Context mContext;
	private EditText mEditText;
	private EditText mContactEditText;
	private Button mSubmitBtn;
	private Button mCopyQQ;
	private Button mBackButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_layout);
		mContext = this;
		setTitle(mContext.getString(R.string.feedback));
		mEditText = (EditText) findViewById(R.id.feedbackContent);
		mContactEditText = (EditText) findViewById(R.id.feedbackContact);
		mSubmitBtn = (Button) findViewById(R.id.submit);
		mSubmitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (false == NetWorkUtil.isNetworkAvailable(mContext)) {
					showTip(mContext.getString(R.string.open_network));
					return;
				}
				if (mEditText.getText().toString().trim().length() == 0) {
					showTip(mContext.getString(R.string.please_input_feedback));
					return;
				}
				
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("content", mEditText.getText().toString());
					jsonObject.put("contact", mContactEditText.getText().toString());
				} catch (JSONException e) {
					DebugLog.d(TAG, e.toString());
				}

				ServerUtil.getInstance(mContext).request(NetResuestHelper.FEEDBACK, jsonObject.toString(), null);
				
				showTip(mContext.getString(R.string.thank_feedback));
			}
		});
		
		mCopyQQ = (Button) findViewById(R.id.qq);
		mCopyQQ.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Util.copy(mContext, mContext.getString(R.string.qq_group));
				showTip(mContext.getString(R.string.copied) + mContext.getString(R.string.qq_group));
			}
		});

		mBackButton = (Button) findViewById(R.id.back);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
	}

}
