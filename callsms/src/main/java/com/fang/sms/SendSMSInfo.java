package com.fang.sms;

import java.io.Serializable;
import java.util.List;

/**
 * 定时发送短信
 * 
 * @author fang
 * 
 */
public class SendSMSInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//收件人
	private List<String> receiverList;
	//内容
	private String content;
	//序号
	private int resultCode;
	//时间
	private long timeInMillis;

	public SendSMSInfo(List<String> receiverList, String content, int resultCode, long timeInMillis) {
		this.receiverList = receiverList;
		this.content = content;
		this.resultCode = resultCode;
		this.timeInMillis = timeInMillis;
	}

	public List<String> getmReceiverList() {
		return receiverList;
	}

	public void setReceiverList(List<String> receiverList) {
		this.receiverList = receiverList;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}

	public void setTimeInMillis(long timeInMillis) {
		this.timeInMillis = timeInMillis;
	}

}
