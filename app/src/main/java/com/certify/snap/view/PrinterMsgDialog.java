package com.certify.snap.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.util.Log;

import com.certify.snap.R;
import com.certify.snap.printer.BasePrint;
import com.certify.snap.printer.Common;

public class PrinterMsgDialog {
    private final Context mContext;
    private ProgressDialog mProgressDialog;
    private PrinterMsgHandle mHandle;
    private Activity mActivity;

    public PrinterMsgDialog(Context context) {
        mContext = context;
    }

    /**
     * set handle
     */
    public void setHandle(PrinterMsgHandle handle) {
        mHandle = handle;
    }

    /**
     * show message
     */
    public void showStartMsgDialog(final String message) {

        if (message.contains(mContext.getString(R.string.succeeded))) {
            close();
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton(mContext.getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        BasePrint.cancel();
                        Message msg = mHandle.obtainMessage(Common.MSG_PRINT_CANCEL);
                        mHandle.sendMessage(msg);
                    }
                });

        mProgressDialog.show();

    }

    /**
     * show the end message
     */
    public void showPrintCompleteMsgDialog(final String message) {
        try {
            if (message.contains(mContext.getString(R.string.succeeded))) {
                close();
                return;
            }
            if (mActivity != null && mActivity.isDestroyed() && mActivity.isFinishing()) return;
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            mProgressDialog = new ProgressDialog(mContext);

            mProgressDialog.setMessage(mContext.getString(R.string.close_connect));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(message);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setButton(mContext.getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
            mProgressDialog.show();
        } catch (Exception e) {
            Log.e("PrintMsgDialog", "Error in displaying progress dialog");
        }
    }

    /**
     * update complete dialog's message
     */
    public void setMessage(String msg) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(msg);
        }
    }

    /**
     * show message
     */
    public void showMsgNoButton(final String title, final String message) {
        if (message.contains(mContext.getString(R.string.succeeded))) {
            close();
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
        }

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

    }

    /**
     * close dialog
     */
    public void close() {
        if (mActivity != null && mActivity.isDestroyed() && mActivity.isFinishing()) return;
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * show alert dialog
     */
    public void showAlertDialog(String title, final String msg) {
        if (msg.contains(mContext.getString(R.string.succeeded))) {
            close();
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.button_ok, null);
        dialog.show();
    }

    public void disableCancel() {
        mProgressDialog.getButton(mProgressDialog.BUTTON_POSITIVE).setEnabled(
                false);
    }

    public void setActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }
}
