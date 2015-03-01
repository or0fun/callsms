package com.fang.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.fang.base.BaseFragment;
import com.fang.callsms.MainActivity;
import com.fang.callsms.R;

/**
 * 设置页面
 * @author fang
 *
 */
public class SettingFragment extends BaseFragment {

	protected Context mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.setting_layout, container, false);
		Button feedbackBtn = (Button)rootView.findViewById(R.id.feedback);
		feedbackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(mContext, FeedbackActivity.class));
			}
		});

        // 立即更新
        rootView.findViewById(R.id.check_update).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).checkUpdateVersion(true);
            }
        });

//		Button adsBtn = (Button)rootView.findViewById(R.id.ads);
//		adsBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//		        Ads.showAppWall(mContext, ADHelper.TAG_LIST);
//		        LogOperate.updateLog(mContext, LogCode.ENTER_HOTAPP);
//			}
//		});
		return rootView;
	}

}
