package com.fang.listener;

/**
 * 下载监听
 * @author fang
 *
 */
public interface IDownloadListener {

	public static final int HTTP_OK = 0;
	public static final int HTTP_EXCEPTION = 1;
	public static final int SDCARD_NOT_AVAILABLE = 2;
	public static final int TIME_OUT = 3;
	
	public void onResult(int result);
}
