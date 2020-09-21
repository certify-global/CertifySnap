package com.certify.snap.bluetooth.printer.toshiba;

import java.util.HashMap;
import java.util.Map;

public class PrintData {

	private Map< String , String > m_objectDataList = null;
	private String m_lfmFileFullPath = "";
	private int printCount = 1;
	private int mCurrentIssueMode = 1;
	
	private String mStatusMessage = "";
	private long mResult = 0;
	private int mIssueMode = 0;
	
	public PrintData( ) {
		setObjectDataList(new HashMap<String , String>());
	}
	
	protected void finalize() {
		setObjectDataList(null);
	}

	/**
	 * @param m_objectDataList the objectDataList to set
	 */
	public void setObjectDataList(Map< String , String > m_objectDataList) {
		this.m_objectDataList = m_objectDataList;
	}

	/**
	 * @return the m_objectDataList
	 */
	public Map< String , String > getObjectDataList() {
		return m_objectDataList;
	}

	/**
	 * @param printCount the printCount to set
	 */
	public void setPrintCount(int printCount) {
		if ( printCount < 1 ) {
			printCount = 1;
		}
		this.printCount = printCount;
	}

	/**
	 * @return the printCount
	 */
	public int getPrintCount() {
		return printCount;
	}

	/**
	 * @param lfmFileFullPath the lfmFileFullPath to set
	 */
	public void setLfmFileFullPath(String lfmFileFullPath) {
		this.m_lfmFileFullPath = lfmFileFullPath;
	}

	/**
	 * @return the lfmFileFullPath
	 */
	public String getLfmFileFullPath() {
		return m_lfmFileFullPath;
	}

	/**
	 * @param mCurrentIssueMode the mCurrentIssueMode to set
	 */
	public void setCurrentIssueMode(int mCurrentIssueMode) {
		this.mCurrentIssueMode = mCurrentIssueMode;
	}

	/**
	 * @return the mCurrentIssueMode
	 */
	public int getCurrentIssueMode() {
		return mCurrentIssueMode;
	}

	/**
	 * @param statusMessage the mStatusMessage to set
	 */
	public void setStatusMessage(String statusMessage) {
		this.mStatusMessage = statusMessage;
	}

	/**
	 * @return the mStatusMessage
	 */
	public String getStatusMessage() {
		return mStatusMessage;
	}

	/**
	 * @param mResult the mResult to set
	 */
	public void setResult(long mResult) {
		this.mResult = mResult;
	}

	/**
	 * @return the mResult
	 */
	public long getResult() {
		return mResult;
	}

	public void setIssueMode(int issueMode) {
		this.mIssueMode = issueMode;
	}

	/**
	 * @return the mIssueMode
	 */
	public int getIssueMode() {
		return mIssueMode;
	}
	
}
