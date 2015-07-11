package com.fang.life;

import android.content.Context;
import android.util.Log;

import com.fang.common.base.Global;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.StringUtil;
import com.fang.database.NumberDatabaseManager;
import com.fang.datatype.ExtraName;
import com.fang.net.NetRequestListener;
import com.fang.net.NetRequestResult;
import com.fang.net.NetRequestResultCode;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;
import com.fang.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务号码帮助类
 * @author fang
 *
 */
public class NumberServiceHelper {

    private static final String TAG = "NumberServiceHelper";

	public static final String PARAM_NAME = "name";
	public static final String PARAM_NUMBER = "number";
	public static final String PARAM_ICON = "icon";
	
	public static final String ACTION_FOOD ="com.fang.number.action.food";
	public static final String ACTION_HOUSE ="com.fang.number.action.house";
	public static final String ACTION_EXPRESS ="com.fang.number.action.express";
	public static final String ACTION_SERVICE ="com.fang.number.action.service";
	
	/**
	 * 订餐电话
	 */
	public static final Map<String,String> NUMBERS_FOOD = new HashMap<String,String>(){//构造方法
		{
		put("肯德基","4008823823");
		put("麦当劳","4008517517");
		put("必胜客","4008123123");
		put("吉野家","4008197197");
		put("呷哺呷哺","4008177878");
		put("海底捞","4008107107");
		put("真功夫","4006927927");
		}
	};
	/**
	 * 订酒店电话
	 */
	public static final Map<String,String> NUMBERS_HOUSE = new HashMap<String,String>(){//构造方法
		{
		put("汉庭酒店","4008121121");
		put("锦江之星酒店","4008209999");
		put("布丁酒店","4008802802");
		put("如家酒店","4008203333");
		put("7天连锁酒店","4008740087");
		put("格林豪泰酒店","4006998998");
		put("鲜屋酒店","4007114000");
		}
	};
	/**
	 * 快递电话
	 */
	public static final Map<String,String> NUMBERS_EXPRESS = new HashMap<String,String>(){//构造方法
		{
		put("顺丰快递","95338");
		put("EMS","11183");
		put("UPS快递","4008208388");
		put("联邦快递","4008891888");
		put("申通快递","4008895543");
		put("圆通快递","02169777888");
		put("韵达快递","4008216789");
		put("中通快递","4008270270");
		put("宅急送","4006789000");
		put("中铁快运","95572");
		put("德邦物流","4008305555");
		put("如风达","4000106660");
		put("汇通快递","02162963636");
		put("全峰快递","4001000001");
		put("天天快递","4001888888");
		}
	};
	/**
	 * 客服电话
	 */
	public static final Map<String,String> NUMBERS_SERVICE = new HashMap<String,String>(){//构造方法
		{
		put("支付宝客服","95188");
		put("淘宝客服","057188158198");
		put("京东商城客服","4006065500");
		put("亚马逊客服","4008105666");
		put("QQ客服","075583765566");
		put("微信客服","075561369988");
		put("三星客服","4008105858");
		put("联想客服","4009908888");
		}
	};

    /**
     * 获取离线数据
     * @param context
     */
    public static void getNumberInfo(final Context context) {
        int startID = SharedPreferencesHelper.getInstance().getInt(SharedPreferencesHelper.OFFLINE_NUMBER_STARTID, 0);
        getNumberInfo(context, startID);
    }
    /**
     * 获取离线数据
     * @param context
     * @param startID
     */
    private static void getNumberInfo(final Context context, final int startID) {
        BaseUtil.excute(new Runnable() {
            @Override
            public void run() {
                ServerUtil server = ServerUtil.getInstance(context);
                server.request(NetResuestHelper.OFFLINE_NUMBER, String.valueOf(startID), mNetRequestListener);
            }
        });
    }

    /**
     * 网络监听
     */
    protected static NetRequestListener mNetRequestListener = new NetRequestListener() {
        @Override
        public void onResult(NetRequestResult result) {
            if (null == result) {
                return;
            }
            if (result.getResultCode() == NetRequestResultCode.HTTP_OK) {

                JSONArray infos;
                int startID = SharedPreferencesHelper.getInstance().getInt(SharedPreferencesHelper.OFFLINE_NUMBER_STARTID, 0);
                try {
                    infos = new JSONArray(result.getValue());
                    if (null != infos) {
                        JSONObject infoObject;
                        String number, info;
                        for (int i = 0; i < infos.length(); i++) {
                            infoObject = (JSONObject) infos.get(i);
                            number = infoObject.getString(ExtraName.PARAM_NUMBER);
                            info = infoObject.getString(ExtraName.PARAM_INFO);
                            if (StringUtil.isEmpty(NumberDatabaseManager.getInstance(Global.application).query(
                                    number))) {
                                NumberDatabaseManager.getInstance(Global.application).update(number, info);
                            }
                            startID = infoObject.getInt(ExtraName.PARAM_ID);
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                } finally {
                    SharedPreferencesHelper.getInstance().setInt(SharedPreferencesHelper.OFFLINE_NUMBER_STARTID, startID);
                }
            }
        }
    };
}
