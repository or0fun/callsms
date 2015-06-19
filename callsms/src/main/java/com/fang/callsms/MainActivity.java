package com.fang.callsms;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fang.base.BaseActivity;
import com.fang.base.BaseFragment;
import com.fang.base.Model;
import com.fang.call.CallFragment;
import com.fang.call.CallHelper;
import com.fang.common.CustomConstant;
import com.fang.common.util.DebugLog;
import com.fang.common.util.MIUIHelper;
import com.fang.contact.ContactFragment;
import com.fang.datatype.CallFrom;
import com.fang.datatype.ExtraName;
import com.fang.listener.IDownloadListener;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.number.NumberFragment;
import com.fang.push.ActionType;
import com.fang.receiver.MainService;
import com.fang.receiver.PhoneReceiver;
import com.fang.setting.SettingFragment;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.Util;
import com.fang.version.UpdateVersion;
import com.fang.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity implements OnClickListener {

	private final String TAG = "MainActivity";
	
	private ViewPager mViewPager;
	/** 电话按钮 */
	protected LinearLayout mCallTab;
	/** 号码通按钮 */
	protected LinearLayout mNumberTab;
	/** 联系人按钮 */
	protected LinearLayout mContactTab;
	/** 设置按钮 */
	protected LinearLayout mSettingTab;

	/** 电话记录页面 */
	protected CallFragment mCallFragment;
	/** 通讯录页面 */
	protected ContactFragment mContactFragment;
	/** 号码通页面 */
	protected NumberFragment mNumberFragment;
	/** 设置页面 */
	protected SettingFragment mSettingFragment;

	/** 标题栏 */
	protected View mTitleBar;
	/** 正在显示dFragment */
	protected BaseFragment mShowingFragment;
	/** Fragment list */
	protected List<Fragment> mFragmentList;
	/** ImageView list */
	protected List<TextView> mTextViewList;

	protected ImageView mCallIcon;
	protected ImageView mContactIcon;
	protected ImageView mNumberIcon;
	protected ImageView mSettingIcon;

	protected TextView mCallTitleTextView;
	protected TextView mContactTitleTextView;
	protected TextView mNumberTitleTextView;
	protected TextView mSettingTitleTextView;

	protected final int REQUEST_CODE_SEARCH = 0;
	/** 检查是否更新的消息 */
	protected final int MSG_CHECK_VERSION = 1;
	/** 创建快捷方式 */
	protected final int MSG_CREATE_SHORT = 2;
	/** 提示开启悬浮窗 */
	protected final int MSG_SHOW_FLOAT = 3;
    /** 创建扫一扫快捷方式 */
    protected final int MSG_CREATE_SHORT_SCAN = 4;
	
	private AsyncQueryHandler mAsyncQuery;

    private Model mModel;
	
	protected Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_CHECK_VERSION:
				checkUpdateVersion(false);
				break;
			case MSG_CREATE_SHORT:
                Util.showCreateShortDialog(mContext);
				break;
			case MSG_SHOW_FLOAT:
                Util.showOpenFloatDialog(mContext);
                break;
            case MSG_CREATE_SHORT_SCAN:
                Util.createShortCut(mContext, mContext.getString(R.string.scan_short), CallFrom.SCAN);
                break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTitleBar = findViewById(R.id.titleBar);

        mModel = new Model(mContext);

		mCallFragment = new CallFragment();
        mCallFragment.setModel(mModel);
		mSettingFragment = new SettingFragment();
        mCallFragment.setModel(mModel);
		mContactFragment = new ContactFragment();
        mCallFragment.setModel(mModel);
		mNumberFragment = new NumberFragment();
        mCallFragment.setModel(mModel);

		mFragmentList = new ArrayList<>();
		mFragmentList.add(mNumberFragment);
		mFragmentList.add(mCallFragment);
		mFragmentList.add(mContactFragment);
		mFragmentList.add(mSettingFragment);

		mCallTab = (LinearLayout) findViewById(R.id.callTab);
		mNumberTab = (LinearLayout) findViewById(R.id.numberTab);
		mContactTab = (LinearLayout) findViewById(R.id.contactTab);
		mSettingTab = (LinearLayout) findViewById(R.id.settingTab);
		mCallTab.setOnClickListener(this);
		mNumberTab.setOnClickListener(this);
		mContactTab.setOnClickListener(this);
		mSettingTab.setOnClickListener(this);

		mCallIcon = (ImageView) findViewById(R.id.callIcon);
		mNumberIcon = (ImageView) findViewById(R.id.numberIcon);
		mContactIcon = (ImageView) findViewById(R.id.contactIcon);
		mSettingIcon = (ImageView) findViewById(R.id.settingIcon);

		mCallTitleTextView = (TextView) findViewById(R.id.callTitle);
		mNumberTitleTextView = (TextView) findViewById(R.id.numberTitle);
		mContactTitleTextView = (TextView) findViewById(R.id.contactTitle);
		mSettingTitleTextView = (TextView) findViewById(R.id.settingTitle);

		mTextViewList = new ArrayList<TextView>();
		mTextViewList.add(mNumberTitleTextView);
		mTextViewList.add(mCallTitleTextView);
		mTextViewList.add(mContactTitleTextView);
		mTextViewList.add(mSettingTitleTextView);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setAdapter(new MyViewPagerAdapter(getFragmentManager()));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				pageSelected(index);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
//		mShowHotAppButton = (Button)findViewById(R.id.show_apps_button);
//		mShowHotAppButton.setOnClickListener(this);
//		mShowHotAppButton.setVisibility(View.GONE);
		//默认选择号码通
		int selected = SharedPreferencesHelper.getInstance().getInt(SharedPreferencesHelper.SELECTED_PAGE, Model.NUMBER_FRAGMENT);
		mViewPager.setCurrentItem(selected);
		pageSelected(selected);

		//创建快捷方式
		if (SharedPreferencesHelper.getInstance().getBoolean(SharedPreferencesHelper.FIRST_TIME_OPEN, true)) {
			if (MIUIHelper.getInstance().isMiUIV5() || MIUIHelper.getInstance().isMiUIV6()) {
				mHandler.sendEmptyMessageDelayed(MSG_SHOW_FLOAT, CustomConstant.FIVE_SECONDS);
			}else {
				mHandler.sendEmptyMessageDelayed(MSG_CREATE_SHORT, CustomConstant.FIVE_SECONDS);
			}
			SharedPreferencesHelper.getInstance().setBoolean(SharedPreferencesHelper.FIRST_TIME_OPEN, false);
		}

        //扫一扫快捷方式
        if (SharedPreferencesHelper.getInstance().getBoolean(SharedPreferencesHelper.SCAN, true)) {
            mHandler.sendEmptyMessageDelayed(MSG_CREATE_SHORT_SCAN, CustomConstant.FIVE_SECONDS);
            SharedPreferencesHelper.getInstance().setBoolean(SharedPreferencesHelper.SCAN, false);
        }

        //  处理消息
        handleIntent(getIntent());
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHandler.sendEmptyMessage(MSG_CHECK_VERSION);
		if (CallHelper.isMissedIncomingCall(getIntent())) {
			mViewPager.setCurrentItem(Model.CALL_FRAGMENT);
			mShowingFragment = mContactFragment;
		}

		startService(new Intent(this, MainService.class));

		//移除弹窗
		sendBroadcast(new Intent(PhoneReceiver.ACTION_REMOVE));

	}

	@Override
	public void onBackPressed() {
		if (mShowingFragment == mSettingFragment) {
			super.onBackPressed();
		}else if (mShowingFragment == mCallFragment){
			if (false == mCallFragment.onBackPressed()) {
				super.onBackPressed();
			}
		}else if (mShowingFragment == mNumberFragment){
			if (false == mNumberFragment.onBackPressed()) {
				super.onBackPressed();
			}
		}else if (mShowingFragment == mContactFragment){
			if (false == mContactFragment.onBackPressed()) {
				super.onBackPressed();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class MyViewPagerAdapter extends FragmentPagerAdapter {
		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {

			return mFragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return mFragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "";
		}

	}

	@Override
	public void onClick(View view) {
		if (view == mCallTab) {
			mViewPager.setCurrentItem(Model.CALL_FRAGMENT);
		} else if (view == mNumberTab) {
			mViewPager.setCurrentItem(Model.NUMBER_FRAGMENT);
		} else if (view == mContactTab) {
			mViewPager.setCurrentItem(Model.CONTACT_FRAGMENT);
		} else if (view == mSettingTab) {
			mViewPager.setCurrentItem(Model.SETTING_FRAGMENT);
		} 
//		else if (view == mShowHotAppButton) {
//	        Ads.showAppWall(MainActivity.this, ADHelper.TAG_LIST);
//		}
	}

	/**
	 * 选择页面后的界面显示
	 * 
	 * @param index
	 */
	protected void pageSelected(int index) {
		for (TextView textView : mTextViewList) {
			textView.setTextColor(mContext.getResources()
					.getColor(R.color.hint));
		}
		mCallIcon.setImageResource(R.drawable.call_nor);
		mNumberIcon.setImageResource(R.drawable.sms_nor);
		mSettingIcon.setImageResource(R.drawable.setting_nor);
		mContactIcon.setImageResource(R.drawable.contact_nor);
		
		mCallFragment.setSelected(false);
		mContactFragment.setSelected(false);
		mNumberFragment.setSelected(false);
		mSettingFragment.setSelected(false);

		if (index == Model.CALL_FRAGMENT) {
			mTitleBar.setVisibility(View.GONE);
			mCallIcon.setImageResource(R.drawable.call_foucs);
			mShowingFragment = mCallFragment;
		} else if (index == Model.NUMBER_FRAGMENT) {
			mTitleBar.setVisibility(View.GONE);
			mNumberIcon.setImageResource(R.drawable.sms_foucs);
			mShowingFragment = mNumberFragment;
		} else if (index == Model.CONTACT_FRAGMENT) {
			mTitleBar.setVisibility(View.GONE);
			mContactIcon.setImageResource(R.drawable.contact_foucs);
			mContactFragment.updateContacts(true);
			mShowingFragment = mContactFragment;
		} else if (index == Model.SETTING_FRAGMENT) {
			mTitleBar.setVisibility(View.VISIBLE);
			mSettingIcon.setImageResource(R.drawable.setting_foucs);
			mShowingFragment = mSettingFragment;
		}
		
		SharedPreferencesHelper.getInstance().setInt(SharedPreferencesHelper.SELECTED_PAGE, index);
		mShowingFragment.setSelected(true);
		mTextViewList.get(index).setTextColor(mContext.getResources().getColor(
				R.color.blue));
		
		if (mShowingFragment.isNeedLoading()) {
            mShowingFragment.showLoading();
		}
	}
	
	/**
	 * 更新版本
	 */
	public void checkUpdateVersion(boolean manual) {
		long now = new Date().getTime();
		long last = SharedPreferencesHelper.getInstance().getLong(
                SharedPreferencesHelper.LAUNCH_LAST_TIME, 0);
		if (manual || now - last > CustomConstant.ONE_DAY ||
                SharedPreferencesHelper.getInstance().getBoolean(SharedPreferencesHelper.UPDATE_VERSION, false)) {
			UpdateVersion.checkVersion(mContext, manual, mDownloadListener);

            //保持时间
            SharedPreferencesHelper.getInstance().setLong(
                    SharedPreferencesHelper.LAUNCH_LAST_TIME, new Date().getTime());

            SharedPreferencesHelper.getInstance().setBoolean(SharedPreferencesHelper.UPDATE_VERSION, false);

            //日志
            LogOperate.updateLog(mContext, LogCode.ACTIVE_APP);
		}
	}

    /**
     * 处理传进来的Intent
     * @param intent
     */
    private void handleIntent(Intent intent) {

        if (null != intent) {
            int callFrom = intent.getIntExtra(ExtraName.CALL_FROM, CallFrom.LOCAL);
            switch (callFrom) {
                case CallFrom.SCAN:
                    //扫描二维码
                    Intent openCameraIntent = new Intent(MainActivity.this,
                            CaptureActivity.class);
                    startActivityForResult(openCameraIntent, 0);
                    break;
                case CallFrom.WEATHER_NOTIFICATION_CLICK:
                    //天气通知栏
                    LogOperate.updateLog(mContext, LogCode.WEATHER_NOTIFICATION_CLICK);
                    break;
                case CallFrom.NOTIFICATION_CLICK:
                    //通知栏
                    int task = intent.getIntExtra(ExtraName.TASK_ACTION, 0);
                    DebugLog.d(TAG, "handleIntent: task" + task);
                    if (task > 0) {
                        // 消息推送 通知栏点击
                        LogOperate.updateLog(mContext, LogCode.PUSH_REQUEST_NOTIFICATION_CLICK);
                        if (ActionType.NO_ACTION == task) {

                        } else if (ActionType.UPDATE_ACTION == task) {
                            checkUpdateVersion(true);
                        } else if (ActionType.URL_ACTION == task) {
                            Util.openUrl(intent.getStringExtra(ExtraName.URL));
                        }
                    }
                    break;
            }
        }
    }

	/**
	 * 下载监听
	 */
	private IDownloadListener mDownloadListener = new IDownloadListener() {
		@Override
		public void onResult(int result) {
			switch (result) {
			case IDownloadListener.HTTP_EXCEPTION:
				showTip(getString(R.string.download_http_error));
				break;
			case IDownloadListener.SDCARD_NOT_AVAILABLE:
				showTip(getString(R.string.download_sdcard_error));
				break;
			case IDownloadListener.TIME_OUT:
				showTip(getString(R.string.download_timeout_error));
				break;
			default:
				break;
			}
		}
	};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(ExtraName.RESULT);
            mViewPager.setCurrentItem(Model.NUMBER_FRAGMENT);
            pageSelected(Model.NUMBER_FRAGMENT);

            mNumberFragment.setResultText(scanResult);

        }
    }
}
