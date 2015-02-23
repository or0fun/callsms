package com.fang.callsms;

public class MySMSMessage {
	private String mSenderString;
	private String mBodyString;
	private String mTimeString;
	private int mID;
	
	public MySMSMessage(String sender, String body, String time, int id) {
		mSenderString = sender;
		mBodyString = body;
		mTimeString = time;
		mID = id;
	}
	
	public int getmID() {
		return mID;
	}

	public void setmID(int mID) {
		this.mID = mID;
	}

	public String getBodyString() {
		return mBodyString;
	}
	public void setBodyString(String bodyString) {
		this.mBodyString = bodyString;
	}
	public String getTimeString() {
		return mTimeString;
	}
	public void setTimeString(String timeString) {
		this.mTimeString = timeString;
	}
	public String getSenderString() {
		return mSenderString;
	}
	public void setSenderString(String senderString) {
		this.mSenderString = senderString;
	}
}
