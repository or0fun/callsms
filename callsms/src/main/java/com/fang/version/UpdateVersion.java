package com.fang.version;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.fang.callsms.R;
import com.fang.common.CustomConstant;
import com.fang.controls.CustomDialog;
import com.fang.listener.IDownloadListener;
import com.fang.net.CustomHttpClient;
import com.fang.net.NetRequest;
import com.fang.net.NetRequestConstant;
import com.fang.net.NetRequestListener;
import com.fang.net.NetRequestResult;
import com.fang.net.NetRequestResultCode;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;
import com.fang.util.DebugLog;
import com.fang.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 版本更新类
 * 
 * @author fang
 * 
 */
public class UpdateVersion {

	private String TAG = "UpdateVersion";

	private Context mContext;
	/** 版本号 */
	private final String VERSION_CODE = "verCode";
	/** 版本名称 */
	private final String VERSION_NAME = "verName";
	/** 版本下载地址 */
	private final String VERSION_ADDRESS = "addr";
	/** 更新内容 */
	private final String VERSION_CONTENT = "content";
	/** 更新消息 */
	private final int MSG_EVENT_UPDATE = 1;
	/** 下载进度 */
	private final int MSG_DOWNLOAD_STATUS = 2;
	/** 安装消息 */
	private final int MSG_EVENT_INSTALL = 3;
	/** 取消下载 */
	private boolean mStopDownload = false;

	/** 进度条 */
	private ProgressDialog mProgressBar;
	private MyHandlerThread mHandlerThread;
	private Handler mHandler;
	/** 下载标志 */
	private long mDownloadRefrence = 0;

	/** 下载监听 */
	IDownloadListener mDownloadListener;

    /**  手动下载 */
    private static boolean mManualCheck = false;

	public UpdateVersion(Context ctx, IDownloadListener downloadListener) {
		mContext = ctx;
		mDownloadListener = downloadListener;
		mHandlerThread = new MyHandlerThread("myHanler");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper(), mHandlerThread);

	}

	/**
	 * 检查版本号
	 */
	public static void checkVersion(final Context context, final boolean manualCheck,
			final IDownloadListener downloadListener) {

		new Thread() {
			@Override
			public void run() {
				super.run();
				UpdateVersion uv = new UpdateVersion(context, downloadListener);
				uv.getLatestVerFromServer(manualCheck);
			}
		}.start();
	}

	/**
	 * 从服务器获取最新版本号
	 * 
	 * @return
	 */
	protected boolean getLatestVerFromServer(boolean manualCheck) {
        mManualCheck = manualCheck;
		try {
			ServerUtil server = ServerUtil.getInstance(mContext);
			NetRequest request = new NetRequest();
			request.setUrl(CustomConstant.VERSION_GET_URL);
			request.setRequestType(NetRequestConstant.TYPE_GET);
			request.setRequestCode(NetResuestHelper.getRequestCode());
			server.request(request, mNetRequestListener);
		} catch (Exception e) {
			DebugLog.d(TAG, e.toString());
			return false;
		}
		return true;
	}

	/**
	 * 下载新版本
	 * 
	 * @param newVerName
	 * @param addr
	 */
	private void doNewVersionUpdate(final Context context, String newVerName,
			final String addr, final String content) {

		Dialog dialog = new CustomDialog.Builder(context)
				.setTitle(context.getString(R.string.findnewversion))
				.setMessage(content)
				.setPositiveButton(context.getString(R.string.update),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								downloadApk(context, addr, content);
								dialog.dismiss();
							}
						})
				.setNegativeButton(context.getString(R.string.no_update),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						}).create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	/**
	 * 交给系统下载APK
	 * 
	 * @param context
	 * @param url
	 * @param content
	 */
	private void downloadApk(final Context context, final String url,
			final String content) {
		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(url);
		DownloadManager.Request request = new Request(uri);
		request.setTitle(context.getString(R.string.app_name)
				+ context.getString(R.string.update));
		request.setDescription(content);
		
		context.registerReceiver(mDownloadBroadcastReceiver,
				new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		mDownloadRefrence = downloadManager.enqueue(request);
	}

	/**
	 * 下载文件
	 * 
	 * @param url
	 */
	private void downFile(final Context context, final String url) {
		new Thread() {
			public void run() {
				String state = Environment.getExternalStorageState();
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					HttpClient client = CustomHttpClient.getHttpClient();
					HttpGet get = new HttpGet(url);
					HttpResponse response;
					try {
						response = client.execute(get);
						HttpEntity entity = response.getEntity();
						long fileLength = entity.getContentLength();
						InputStream is = entity.getContent();
						FileOutputStream fileOutputStream = null;
						int length = (int) (fileLength / 1024);
						if (is != null) {
							File file = new File(
									Environment.getExternalStorageDirectory(),
									context.getString(R.string.apk_name));
							fileOutputStream = new FileOutputStream(file);
							byte[] buf = new byte[1024];
							int ch = -1;
							int downloadedLen = 0;
							mHandler.sendMessage(mHandler.obtainMessage(
									MSG_DOWNLOAD_STATUS, downloadedLen, length));
							while ((ch = is.read(buf)) != -1 && !mStopDownload) {
								downloadedLen += 1;
								fileOutputStream.write(buf, 0, ch);
								mHandler.sendMessage(mHandler.obtainMessage(
										MSG_DOWNLOAD_STATUS, downloadedLen,
										length));
							}
						}
						fileOutputStream.flush();
						if (fileOutputStream != null) {
							fileOutputStream.close();
						}
						if (mProgressBar != null && mProgressBar.isShowing()) {
							mProgressBar.cancel();
						}
						if (!mStopDownload) {
							mHandler.sendMessage(mHandler.obtainMessage(
									MSG_DOWNLOAD_STATUS, length, length));
							// 直接安装
							Util.installAPK(context, Uri.fromFile(new File(
									Environment.getExternalStorageDirectory(),
									context.getString(R.string.apk_name))));
						}
					} catch (Exception e) {
						if (null != e) {
							DebugLog.d(TAG, e.toString());
						}
						if (null != mDownloadListener) {
							mDownloadListener
									.onResult(IDownloadListener.HTTP_EXCEPTION);
						}
					}
				} else {
					if (null != mDownloadListener) {
						mDownloadListener
								.onResult(IDownloadListener.SDCARD_NOT_AVAILABLE);
					}
				}
				// 进度条消失
				if (mProgressBar != null && mProgressBar.isShowing()) {
					mProgressBar.cancel();
				}
			}
		}.start();
	}

	/**
	 * 网络监听
	 */
	protected NetRequestListener mNetRequestListener = new NetRequestListener() {
		@Override
		public void onResult(NetRequestResult result) {
			if (null == result) {
				return;
			}
			if (result.getResultCode() == NetRequestResultCode.HTTP_OK) {

				JSONObject verjson = null;
				int newVerCode = -1;
				try {
					verjson = new JSONObject(result.getValue());
				} catch (JSONException e) {
					Log.e(TAG, "Error parsing data " + e.toString());
				}
				if (verjson != null) {
					try {
						Message msg = new Message();
						msg.obj = verjson;
						msg.what = MSG_EVENT_UPDATE;
						mHandler.sendMessage(msg);
					} catch (Exception e) {
						DebugLog.d(TAG, e.toString());
						return;
					}
				}
			}
		}
	};

	private class MyHandlerThread extends HandlerThread implements Callback {

		public MyHandlerThread(String name) {
			super(name);
		}

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_EVENT_UPDATE:

                JSONObject jsonObj = (JSONObject) msg.obj;
                try {
                    int newVerCode = Integer.parseInt(jsonObj.getString(VERSION_CODE));
                    if (Util.getVersionCode(mContext) < newVerCode) {
                        doNewVersionUpdate(mContext,
                                jsonObj.getString(VERSION_NAME),
                                jsonObj.getString(VERSION_ADDRESS),
                                jsonObj.getString(VERSION_CONTENT));
                    } else if (mManualCheck) {
                        Toast.makeText(mContext, "已经是最新版本了。", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    DebugLog.d(TAG, e.toString());
                }
				break;
			case MSG_DOWNLOAD_STATUS:
				if (null != mProgressBar && mProgressBar.isShowing()) {
					mProgressBar.setProgress(msg.arg1 * 100 / msg.arg2);
					mProgressBar.setMessage(mContext
							.getString(R.string.waiting));
					if (msg.arg2 >= 1024) {
						if (msg.arg1 >= 1024) {
							mProgressBar.setProgressNumberFormat(String.format(
									"%.2fM/%.2fM", msg.arg1 * 1.0 / 1024,
									msg.arg2 * 1.0 / 1024));
						} else {
							mProgressBar.setProgressNumberFormat(String.format(
									"%dK/%.2fM", msg.arg1,
									msg.arg2 * 1.0 / 1024));
						}
					} else {
						mProgressBar.setProgressNumberFormat(String.format(
								"%dK/%dK", msg.arg1, msg.arg2));
					}
				}
				break;
			case MSG_EVENT_INSTALL:
				// 直接安装
				Util.installAPK(mContext, Uri.fromFile(new File((String)msg.obj)));
				break;
			}
			return true;
		}
	}

	/**
	 * 监听系统下载广播
	 */
	private BroadcastReceiver mDownloadBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			long reference = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (mDownloadRefrence == reference) {

				Query myDownloadQuery = new Query();
				myDownloadQuery.setFilterById(reference);
				DownloadManager downloadManager = (DownloadManager) context
						.getSystemService(Context.DOWNLOAD_SERVICE);

				Cursor myDownload = downloadManager.query(myDownloadQuery);
				if (myDownload.moveToFirst()) {
					int fileNameIdx = myDownload
							.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);

					String fileName = myDownload.getString(fileNameIdx);
					DebugLog.d(TAG, fileName);

					mHandler.sendMessage(mHandler.obtainMessage(MSG_EVENT_INSTALL, fileName));
					
					
				}
				myDownload.close();
				
				context.unregisterReceiver(mDownloadBroadcastReceiver);
				
			}
		}

	};
}
