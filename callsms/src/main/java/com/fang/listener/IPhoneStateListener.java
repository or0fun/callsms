package com.fang.listener;

/**
 * 电话状态监听器
 * @author fang
 *
 */
public interface IPhoneStateListener {
	
	public void onResult(int callType, String number);
}
