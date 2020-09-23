package com.certify.snap.printer.usb;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionData {
	private int mIssueMode;
	private String mPortSetting;
	private AtomicBoolean mIsOpen = new AtomicBoolean(false);
	
	public ConnectionData(){
		
	}

	/**
	 * @param mIssueMode the mIssueMode to set
	 */
	public void setIssueMode(int mIssueMode) {
		this.mIssueMode = mIssueMode;
	}

	/**
	 * @return the mIssueMode
	 */
	public int getIssueMode() {
		return mIssueMode;
	}

	/**
	 * @param mPortSetting the mPortSetting to set
	 */
	public void setPortSetting(String mPortSetting) {
		this.mPortSetting = mPortSetting;
	}

	/**
	 * @return the mPortSetting
	 */
	public String getPortSetting() {
		return mPortSetting;
	}

	/**
	 * @param mIsOpen the mIsOpen to set
	 */
	public void setIsOpen(AtomicBoolean mIsOpen) {
		this.mIsOpen = mIsOpen;
	}

	/**
	 * @return the mIsOpen
	 */
	public AtomicBoolean getIsOpen() {
		return mIsOpen;
	}
	
}
