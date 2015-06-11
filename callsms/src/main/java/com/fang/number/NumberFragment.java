package com.fang.number;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fang.base.BaseFragment;
import com.fang.business.BusinessHelper;
import com.fang.call.CallRecordDialog;
import com.fang.callsms.R;
import com.fang.common.CustomConstant;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.DebugLog;
import com.fang.common.util.Patterns;
import com.fang.common.util.StringUtil;
import com.fang.contact.ContactHelper;
import com.fang.datatype.ExtraName;
import com.fang.express.ExpressListActivity;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.span.MySpan;
import com.fang.util.MessageWhat;
import com.fang.util.NetWorkUtil;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.Util;
import com.fang.weather.WeatherHelper;
import com.fang.zxing.activity.CaptureActivity;

public class NumberFragment extends BaseFragment implements OnClickListener {

	private final String TAG = "NumberFragment";
	//显示结果
	TextView mResultTextView;
	//搜索框
	EditText mSearchEditView;
	//搜索按钮
	Button mSearchBtn;
	//订餐
	LinearLayout mFoodListLayout;
	//订酒店
	LinearLayout mHouseListLayout;
	//快递
	LinearLayout mExpressListLayout;
	//客服
	LinearLayout mServiceListLayout;
	//快递追踪
	Button mSearchExpressBtn;
    // 天气列表
    LinearLayout mWeatherList;
    // 城市
    TextView mWeatherCity;
    //今天日期
    TextView mToday;
    TextView mHistory;
	//缓存号码
	String mNumberString = "";
	//缓存信息
	String mNumberInfoString = "";
	//粘贴板里的数据
	String mPasteNumberString;
    //农历更新时间
    long mLastNongliUpdateTime = 0;
    //天气更新时间
    long mLastWeatherUpdateTime = 0;

    // 号码信息对话框
    CallRecordDialog mCallRecordDialog;

	protected Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageWhat.NET_REQUEST_NUMBER:
				if (null != msg.obj) {
					mNumberInfoString = (String) msg.obj;
					mResultTextView.setText(mNumberInfoString);
				}
				break;
            case MessageWhat.NET_REQUEST_WEATHER:
                if (null != msg.obj) {
                    handlerWeather((String) msg.obj);
                }
                break;
                case MessageWhat.NET_REQUEST_NONGLI:
                    if (null != msg.obj) {
                        handlerNongli((String) msg.obj);
                    }
                    break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        if (isViewCreated()) {
            return mView;
        }

        View rootView = inflater.inflate(R.layout.number_layout, container,
				false);
		mResultTextView = (TextView) rootView.findViewById(R.id.result);
        MySpan.formatTextView(mContext, mResultTextView, mNumberInfoString, false);

		mSearchBtn = (Button) rootView.findViewById(R.id.searchBtn);
		mSearchBtn.setOnClickListener(this);
		mSearchEditView = (EditText) rootView.findViewById(R.id.search);
		mSearchEditView.setOnKeyListener(new View.OnKeyListener() {
		    @Override
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					searchBtnClick();
				}
		        return false;
		    }
		});
		
		mFoodListLayout = (LinearLayout) rootView.findViewById(R.id.foodlistBtn);
		mFoodListLayout.setOnClickListener(this);
		mHouseListLayout = (LinearLayout) rootView.findViewById(R.id.houselistBtn);
		mHouseListLayout.setOnClickListener(this);
		mExpressListLayout = (LinearLayout) rootView.findViewById(R.id.expresslistBtn);
		mExpressListLayout.setOnClickListener(this);
		mServiceListLayout = (LinearLayout) rootView.findViewById(R.id.servicelistBtn);
		mServiceListLayout.setOnClickListener(this);
		
		mSearchExpressBtn = (Button) rootView.findViewById(R.id.searchExpressBtn);
		mSearchExpressBtn.setOnClickListener(this);

        mWeatherList = (LinearLayout) rootView.findViewById(R.id.weatherList);
        mWeatherCity = (TextView) rootView.findViewById(R.id.weather_city);

        mToday = (TextView) rootView.findViewById(R.id.today);
        mHistory = (TextView) rootView.findViewById(R.id.history);
        mHistory.setOnClickListener(this);

        rootView.findViewById(R.id.clearBtn).setOnClickListener(this);
        rootView.findViewById(R.id.scan).setOnClickListener(this);

        // 显示农历和天气
        handlerNongli(SharedPreferencesHelper.getInstance().getString(SharedPreferencesHelper.NONGLI));
        handlerWeather(SharedPreferencesHelper.getInstance().getString(SharedPreferencesHelper.WEATHER));

        return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		DebugLog.d(TAG, "onResume");
		String str = BaseUtil.paste(mContext);
		if (str != null && str.matches(Patterns.NUMBER_PATTERN) && !str.equals(mPasteNumberString)) {
			mSearchEditView.setText(str);
			mPasteNumberString = str;
		} else {
            str = SharedPreferencesHelper.getInstance().getString(SharedPreferencesHelper.NUMBER_SEARCH);
            mSearchEditView.setText(str);
        }

        searchWeather();

        searchNongli();
	}

	@Override
	public boolean onBackPressed() {

		return super.onBackPressed();
	}


	@Override
	public void onClick(View view) {
        int id = view.getId();
		if (view == mFoodListLayout) {
			mContext.startActivity(new Intent(NumberServiceHelper.ACTION_FOOD));
		}else if (view == mHouseListLayout) {
			mContext.startActivity(new Intent(NumberServiceHelper.ACTION_HOUSE));
		}else if (view == mExpressListLayout) {
			mContext.startActivity(new Intent(NumberServiceHelper.ACTION_EXPRESS));
		}else if (view == mServiceListLayout) {
			mContext.startActivity(new Intent(NumberServiceHelper.ACTION_SERVICE));
		}else if (view == mSearchBtn) {
			searchBtnClick();
		}else if (view == mSearchExpressBtn) {
			mContext.startActivity(new Intent(mContext, ExpressListActivity.class));
		} else if (id == R.id.clearBtn) {
            mSearchEditView.setText("");
            LogOperate.updateLog(mContext, LogCode.CLEAR_NUMBER);
        } else if (id == R.id.scan) {//扫一扫
            Intent openCameraIntent = new Intent(mContext,
                    CaptureActivity.class);
            startActivityForResult(openCameraIntent, 0);
            LogOperate.updateLog(mContext, LogCode.SCAN);
        } else if (id == R.id.history) {
            Util.openUrl(CustomConstant.HISTORY_OF_TODAY);
        }
	}
	
	/**
	 *  搜索号码
	 */
	protected void searchBtnClick() {
		String str = mSearchEditView.getText().toString();
		if (!str.equals(mNumberString) || StringUtil.isEmpty(mNumberInfoString)) {
            if (null == mCallRecordDialog) {
                mCallRecordDialog = new CallRecordDialog(mContext, str,
                        ContactHelper.getPerson(mContext, str),
                        BitmapFactory.decodeResource(
                                mContext.getResources(), R.drawable.contact_photo));
            } else {
                mCallRecordDialog.setContent(str,
                        ContactHelper.getPerson(mContext, str),
                        BitmapFactory.decodeResource(
                                mContext.getResources(), R.drawable.contact_photo));
            }
            mCallRecordDialog.show();

			mResultTextView.setText(mContext.getString(R.string.number_seaching));
			mNumberString = String.format("%s", str);
			BusinessHelper.getNumberInfo(mContext, mNumberString, myHandler);
		}
        SharedPreferencesHelper.getInstance().setString(SharedPreferencesHelper.NUMBER_SEARCH, str);
		//日志
		LogOperate.updateLog(mContext, LogCode.SEARCH_NUMBER);
	}

    private void searchWeather() {
        if (System.currentTimeMillis() - mLastWeatherUpdateTime  < CustomConstant.QUARTER_HOUR) {
            DebugLog.d(TAG, "searchWeather: the time is too short");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 最多7天
                String weather = NetWorkUtil.getInstance().searchWeather(7);
                if (!StringUtil.isEmpty(weather)) {
                    mLastWeatherUpdateTime = System.currentTimeMillis();
                    myHandler.sendMessage(myHandler.obtainMessage(MessageWhat.NET_REQUEST_WEATHER, weather));
                }
            }
        }).start();
    }


    /**
     * 查农历
     */
    private void searchNongli() {
        if (System.currentTimeMillis() - mLastNongliUpdateTime < CustomConstant.QUARTER_HOUR) {
            DebugLog.d(TAG, "searchNongli: the time is too short");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String nongli = NetWorkUtil.getInstance().searchNongli();
                if (!StringUtil.isEmpty(nongli)) {
                    mLastNongliUpdateTime = System.currentTimeMillis();
                    myHandler.sendMessage(myHandler.obtainMessage(MessageWhat.NET_REQUEST_NONGLI, nongli));
                }
            }
        }).start();
    }

    /**
     * 处理获取到的天气信息
     * @param weather
     */
    private void handlerWeather(String weather) {
        if(!StringUtil.isEmpty(weather)) {
            SharedPreferencesHelper.getInstance().setString(SharedPreferencesHelper.WEATHER, weather);
            int index = weather.indexOf(" ");
            if (index > 0) {
                String city = weather.substring(0, index);
                weather = weather.substring(index + 1);
                mWeatherCity.setText( city + " " +  mContext.getString(R.string.number_weather));
                String[] str = weather.split("\\|");

                mWeatherList.removeAllViews();
                for (int i = 0; i < str.length; i++) {
                    mWeatherList.addView(WeatherHelper.createWeatherItem(mContext, str[i], i));
                }
            }
        }
    }

    /**
     * 处理获取到的农历信息
     * @param nongli
     */
    private void handlerNongli(final String nongli) {
        if (!StringUtil.isEmpty(nongli)) {
            StringBuilder today = new StringBuilder();
            today.append("今天是");
            if (nongli.trim().length() > 0) {
                today.append(nongli.replace("\n", "<br/>"));
                mToday.setText(Html.fromHtml(today.toString()));

                SharedPreferencesHelper.getInstance().setString(SharedPreferencesHelper.NONGLI, nongli);
            }
        }
    }

    /**
     * 外界调用
     * @param scanResult
     */
    public void setResultText(String scanResult) {
        mNumberInfoString = scanResult;
        MySpan.formatTextView(mContext, mResultTextView, scanResult, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(ExtraName.RESULT);
            setResultText(scanResult);

        }
    }
}
