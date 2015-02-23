package com.fang.contact;

import android.graphics.Bitmap;

/**
 * 通讯录数据结构
 * @author fang
 *
 */
public class ContactInfo {

	/** 通讯录项目 */
	public static final String CONTACT_ID = "id";
	public static final String CONTACT_NAME = "name";
	public static final String CONTACT_NUMBER = "number";
	public static final String CONTACT_PHOTO = "photo";
	public static final String CONTACT_SORTKEY = "sortkey";
	
	private int ID;
	private String name;
	private String number;
	private String sort_key;
	private Bitmap bitmap;
	private boolean isSelect;
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getSort_key() {
		return sort_key;
	}
	public void setSort_key(String sort_key) {
		this.sort_key = sort_key;
	}
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	
}
