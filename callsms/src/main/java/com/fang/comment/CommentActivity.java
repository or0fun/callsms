package com.fang.comment;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fang.base.BaseActivity;
import com.fang.call.CallHelper;
import com.fang.callsms.R;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;
import com.fang.common.util.DebugLog;
import com.fang.util.MessageWhat;
import com.fang.util.NetWorkUtil;

/**
 * 吐槽
 * @author fang
 *
 */
public class CommentActivity extends BaseActivity {

	private String TAG = "CommentActivity";
	
	private Button mBackButton;
	private Button mSubmitBtn;
	private EditText mCommentContenteEditText;
	private TextView mComments;
	
	private String mNumberString;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MessageWhat.MSG_SHOW_COMMENTS:
				if (null != msg.obj) {
                    if (msg.obj instanceof String) {
                        String comment = CommentHelper.parseAllComments((String)msg.obj);
                        if (!TextUtils.isEmpty(comment)) {
                            findViewById(R.id.comment_title).setVisibility(View.VISIBLE);
                            mComments.setText(comment);
                        } else {
                            findViewById(R.id.comment_title).setVisibility(View.GONE);
                        }
                    }
				} else {
                    findViewById(R.id.comment_title).setVisibility(View.GONE);
                }
				break;

			default:
				break;
			}
			
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_layout);
		Intent intent = getIntent();
		if (null != intent) {
			mNumberString = intent.getStringExtra(CallHelper.PARAM_NUMBER);
		}
		setTitle(mContext.getString(R.string.comment_title) + mNumberString);

		mBackButton = (Button) findViewById(R.id.back);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		mCommentContenteEditText = (EditText) findViewById(R.id.commentContent);
		mComments = (TextView) findViewById(R.id.comments);
		mSubmitBtn = (Button) findViewById(R.id.submit);
		mSubmitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (false == NetWorkUtil.isNetworkConnected(mContext)) {
					showTip(mContext.getString(R.string.open_network));
					return;
				}
				if (mCommentContenteEditText.getText().toString().trim().length() == 0) {
					showTip(mContext.getString(R.string.please_input_feedback));
					return;
				}
				
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("content", mCommentContenteEditText.getText().toString());
					jsonObject.put("number", mNumberString);
				} catch (JSONException e) {
					DebugLog.d(TAG, e.toString());
				}

				ServerUtil.getInstance(mContext).request(NetResuestHelper.COMMENT, jsonObject.toString(), null);
				
				showTip(mContext.getString(R.string.comment_thank));
				
				//日志
				LogOperate.updateLog(mContext, LogCode.COMMENT_SUBMIT);
				
				//刷新
				getComments(mNumberString);
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		//刷新
		getComments(mNumberString);
	}
	
	/**
	 * 获取吐槽
	 * @param number
	 */
	protected void getComments(String number) {
		CommentHelper.getComments(mContext, number, mHandler);
	}
}
