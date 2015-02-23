package com.fang.call;

/**
 * 通话记录
 * @author fang
 *
 */
public class CallRecord {
	/** 号码 */
	private String number;
	/** 类型 */
	private int type;
	/** 时间 */
	private long date;
	/** 通话时间 */
	private long duration;
	/** 联系人 */
	private String name;
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
