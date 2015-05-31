package com.fang.net;

import android.content.Context;
import android.util.Log;

import com.fang.common.CustomConstant;
import com.fang.security.AESUtil;
import com.fang.security.SecurityHelper;
import com.fang.util.JsonUtil;
import com.fang.util.NetWorkUtil;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.Util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ServerUtil implements Runnable {

	public final String TAG = ServerUtil.class.getSimpleName();

	private Context mContext = null;
	private Thread mThread;
	private static volatile ServerUtil mInstance = null;
	private AESUtil defaultAesutil;
	/** 用户标识 */
	private String mUserID;
	/** 未上传成功的数据 */
	private List<NameValuePair> mParamsBuffer = new ArrayList<NameValuePair>();

    private Object mLock;

	private ServerUtil(Context cxt) {
		mContext = cxt;

		defaultAesutil = new AESUtil();
		mThread = new Thread(this);
		mUserID = SharedPreferencesHelper.getString(mContext,
				SharedPreferencesHelper.USER_ID, null);
		mParamsBuffer = JsonUtil.getNameValuePairList(SharedPreferencesHelper
				.getString(mContext, SharedPreferencesHelper.OFFLINE_DATA));
		mThread.start();
	}

	public static ServerUtil getInstance(Context cxt) {
        ServerUtil instance = mInstance;
		if (instance == null) {
			synchronized (ServerUtil.class) {
                instance = mInstance;
				if (instance == null) {
					if (cxt != null) {
                        mInstance = instance = new ServerUtil(cxt);
					}
				}
			}
		}
		return instance;
	}

	/**
	 * 发送请求
	 * 
	 * @param request
	 * @param listener
	 */
	public void request(final NetRequest request, final NetRequestListener listener) {
		if (null == request) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				NetRequestResult result = new NetRequestResult();
				result.setRequestCode(request.getRequestCode());
				// 网络不可用
				if (!NetWorkUtil.isNetworkAvailable(mContext)) {
					result.setResultCode(NetRequestResultCode.NETWORK_NOT_AVAILABLE);
					if (request.getRequestType() == NetRequestConstant.TYPE_POST) {
						addOfflineData(request.getValue());
					}
				}
				//正常的上传地址，不是获取用户ID的情况下，当用户ID不存在时先存进离线数据
				else if (mUserID == null && request.getUrl().equals(getPostUrl())
						&& request.getValue() != null && !request.getValue().getName().equals(NetResuestHelper.USER_ID)) {
					result.setResultCode(NetRequestResultCode.NO_USER_ID);
					if (request.getRequestType() == NetRequestConstant.TYPE_POST) {
						addOfflineData(request.getValue());
					}
				} else {
					int type = request.getRequestType();
					NetRequestResult r = null;
					if (NetRequestConstant.TYPE_GET == type) {
						r = getHttpRequest(request.getUrl());
					} else if (NetRequestConstant.TYPE_POST == type) {
						// 加密处理
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						NameValuePair nl = request.getValue();
						if (null != nl) {
							params.add(new BasicNameValuePair(SecurityHelper
									.getMD5(nl.getName()), defaultAesutil
									.encrypt(nl.getValue())));
							if (!nl.getName().equals(NetResuestHelper.USER_ID)) {
								params.add(new BasicNameValuePair(SecurityHelper
										.getMD5(NetResuestHelper.USER), defaultAesutil
										.encrypt(mUserID)));
							}else {
								String mtype = android.os.Build.MODEL; // 手机型号
								params.add(new BasicNameValuePair(SecurityHelper
										.getMD5(NetResuestHelper.MODEL), defaultAesutil
										.encrypt(mtype)));
							}
							//加上版本和渠道
							params.add(new BasicNameValuePair(SecurityHelper
									.getMD5(NetResuestHelper.VERSION), defaultAesutil
									.encrypt(Util.getVersionName(mContext))));
							params.add(new BasicNameValuePair(SecurityHelper
									.getMD5(NetResuestHelper.CHANNEL), defaultAesutil
									.encrypt(CustomConstant.sPACKAGE_CHANNEL)));
							r = postHttpRequest(request.getUrl(), params);
						}
					}
					if (null != r) {
						result.setResultCode(r.getResultCode());
						// 解密
						result.setValue(defaultAesutil.decrypt(r.getValue()));
					}
				}
				if (null != listener) {
					listener.onResult(result);
				}
			}
		}).start();
	}

	/**
	 * 请求数据
	 * 
	 * @param key
	 * @param listener
	 */
	public void request(String key, String value, NetRequestListener listener) {
		if (null != key) {
			NetRequest request = new NetRequest();
			request.setUrl(getPostUrl());
			request.setRequestType(NetRequestConstant.TYPE_POST);
			request.setRequestCode(NetResuestHelper.getRequestCode());
			final NameValuePair nl = new BasicNameValuePair(key, value);
			request.setValue(nl);
			request(request, listener);
		}
	}
	/**
	 * 请求数据
	 * 
	 * @param key
	 * @param listener
	 */
	public void request(String key, NetRequestListener listener) {
		if (null != key) {
			NetRequest request = new NetRequest();
			request.setUrl(getPostUrl());
			request.setRequestType(NetRequestConstant.TYPE_POST);
			request.setRequestCode(NetResuestHelper.getRequestCode());
			final NameValuePair nl = new BasicNameValuePair(key, "");
			request.setValue(nl);
			request(request, listener);
		}
	}
	/**
	 * 请求数据
	 * 
	 * @param nameValuePair
	 * @param listener
	 */
	public void request(NameValuePair nameValuePair, NetRequestListener listener) {
		if (null == nameValuePair) {
			NetRequest request = new NetRequest();
			request.setUrl(getPostUrl());
			request.setRequestType(NetRequestConstant.TYPE_POST);
			request.setRequestCode(NetResuestHelper.getRequestCode());
			request.setValue(nameValuePair);
			request(request, listener);
		}
	}

	/**
	 * GET 请求实现
	 * 
	 * @param url
	 * @return
	 */
	private NetRequestResult getHttpRequest(String url) {
		return httpRequest(url, null);
	}

	/**
	 * POST 请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private NetRequestResult postHttpRequest(String url,
			List<NameValuePair> params) {
		return httpRequest(url, params);
	}
	/**
	 * 网络请求
	 * @param url
	 * @param params
	 * @return
	 */
	private NetRequestResult httpRequest(String url,
			List<NameValuePair> params) {
		NetRequestResult result = new NetRequestResult();
		String strResult = "";
		try {
			HttpClient httpClient = CustomHttpClient.getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			if (null != params) {
				httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int status = httpResponse.getStatusLine().getStatusCode();
			result.setResultCode(status);
			if (status == HttpStatus.SC_OK) {
				strResult = getRealString(EntityUtils.toString(httpResponse
						.getEntity()));
				result.setResultCode(NetRequestResultCode.HTTP_OK);
			}
		} catch (ConnectionPoolTimeoutException e) {
			Log.e(TAG, e.toString());
			result.setResultCode(NetRequestResultCode.CONNECTION_POLL_TIMEOUT);
		} catch (ConnectTimeoutException e) {
			Log.e(TAG, e.toString());
			result.setResultCode(NetRequestResultCode.CONNECT_TIMEOUT);
		} catch (SocketTimeoutException e) {
			Log.e(TAG, e.toString());
			result.setResultCode(NetRequestResultCode.SOCKET_TIMEOUT);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString());
			result.setResultCode(NetRequestResultCode.CLIENT_PROTOCOL);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			result.setResultCode(NetRequestResultCode.IO_ERROR);
		}
		result.setValue(strResult);
		return result;
	}
	@Override
	public void run() {
		while (true) {
			try {
				Thread.currentThread();
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 去除无用的前缀
	 * 
	 * @param str
	 * @return
	 */
	private String getRealString(String str) {
		if (str != null) {
			int len = str.length();
			return str.substring(len % 16);
		}
		return "";
	}

	/**
	 * 获取用户ID
	 */
	private void getUSerID(final Context context) {
		ServerUtil server = ServerUtil.getInstance(context);
		server.request(NetResuestHelper.USER_ID, new NetRequestListener() {
			@Override
			public void onResult(NetRequestResult result) {
				if (result.getResultCode() == NetRequestResultCode.HTTP_OK) {
					mUserID = result.getValue();
					SharedPreferencesHelper.setString(context,
							SharedPreferencesHelper.USER_ID, mUserID);
					checkOffLineData(context);
				}
			}
		});
	}

	/**
	 * 添加未成功上传的数据
	 * 
	 * @param param
	 */
	private void addOfflineData(NameValuePair param) {
		synchronized (mLock) {
			mParamsBuffer.add(param);
			SharedPreferencesHelper.setString(mContext,
					SharedPreferencesHelper.OFFLINE_DATA,
					JsonUtil.getJsonString(mParamsBuffer));
		}
	}

	/**
	 * 获取用户ID
	 * 
	 * @return
	 */
	public void checkUserID(final Context context) {
		if (null == mUserID) {
			getUSerID(context);
		}
		;
	}

	/**
	 * 获取用户ID
	 * 
	 * @return
	 */
	public void checkOffLineData(final Context context) {
		if (null == mParamsBuffer) {
			synchronized (mLock) {
				if (mParamsBuffer.size() > 0) {
					if (NetWorkUtil.isNetworkAvailable(context)) {
						request((NameValuePair)mParamsBuffer.remove(0), null);
					}
				}
			}
		}
	}
	/**
	 * 获取用户ID
	 * @return
	 */
	public String getUserID() {
		return mUserID;
	}

    /**
     * 获取api地址
     * @return
     */
    private String getPostUrl() {
        if (CustomConstant.DEBUG) {
            return  CustomConstant.DEFAULT_POST_URL_TEST;
        }
        return CustomConstant.DEFAULT_POST_URL;
    }
}
