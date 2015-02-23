package com.fang.express;

import com.fang.net.ResponseInfo;

/**
 * 快递信息
 * @author fang
 *
 */
public class ExpressInfo extends ResponseInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8988994631433972109L;

	private String company;
	 
	private String number;

	private String info;

	private boolean isChanged;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean flag) {
		this.isChanged = flag;
	}
}
