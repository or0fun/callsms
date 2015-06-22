package com.fang.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fang.base.BaseFragment;
import com.fang.base.RequestUrl;
import com.fang.base.WEActivity;
import com.fang.callsms.MainActivity;
import com.fang.callsms.R;
import com.fang.common.util.DebugLog;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.weixin.WXConstants;

/**
 * 设置页面
 * @author fang
 *
 */
public class SettingFragment extends BaseFragment implements OnClickListener {

	protected Context mContext;
    private final String TAG = "SettingFragment";

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        if (isViewCreated()) {
            return mView;
        }

        View rootView = inflater.inflate(R.layout.setting_layout, container, false);
		Button feedbackBtn = (Button)rootView.findViewById(R.id.feedback);
		feedbackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(mContext, FeedbackActivity.class));
			}
		});

        TextView version = (TextView) rootView.findViewById(R.id.version);
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version.setText(mContext.getString(R.string.app_name) + " V"+ info.versionName + " | ");
        } catch (PackageManager.NameNotFoundException e) {
            DebugLog.e(TAG, e.toString());
        }
        // 立即更新
        rootView.findViewById(R.id.check_update).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).checkUpdateVersion(true);
                //记录日志
                LogOperate.updateLog(mContext, LogCode.MANUAL_CHECK_UPDATE);
            }
        });

        rootView.findViewById(R.id.share).setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.share == id) {
            ((WEActivity)getActivity()).getShareHandler().share(RequestUrl.DOWNLOAD,
                    mContext.getString(R.string.share_title),
                    mContext.getString(R.string.share_description),
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.we108x108), WXConstants.SHARE_ALL);
        }
    }
}
