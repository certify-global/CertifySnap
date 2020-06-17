package com.certify.snap.activity;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.certify.callback.JSONObjectCallback;
import com.certify.snap.R;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class ConnectivityStatusActivity extends AppCompatActivity implements JSONObjectCallback{

    LinearLayout mRelativeConnectivity;
    private TextView mMacTv, mIPAddress, mNetmaskTv, mGatewayTv, mDns1Tv, mDns2Tv, mEthernetIpTv, mSsidTv;
    private RadioButton mRbInternetConnectivity, mRbcloudConnectivity;
    DhcpInfo dhcpInfo;
    WifiManager wifi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_connectivity);

        Util.getDeviceHealthCheck((JSONObjectCallback) this,this);
        initView();
        if (!Util.isNetworkOff(ConnectivityStatusActivity.this)){
            mRbInternetConnectivity.setChecked(true);
        } else {
            mRbInternetConnectivity.setChecked(false);
        }
        wifi= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        dhcpInfo=wifi.getDhcpInfo();
        getConnectionData();
        
    }

    @SuppressWarnings("deprecation")
    private void getConnectionData() {
        String macAddress = getMacAddress();
        mMacTv.setText(macAddress);
        if (dhcpInfo != null){
            mIPAddress.setText(String.valueOf(Formatter.formatIpAddress(dhcpInfo.ipAddress)));
            mNetmaskTv.setText(String.valueOf(Formatter.formatIpAddress(dhcpInfo.netmask)));
            mGatewayTv.setText(String.valueOf(Formatter.formatIpAddress(dhcpInfo.gateway)));
            mDns1Tv.setText(String.valueOf(Formatter.formatIpAddress(dhcpInfo.dns1)));
            mDns2Tv.setText(String.valueOf(Formatter.formatIpAddress(dhcpInfo.dns2)));
            if (wifi.getConnectionInfo().getSSID().equalsIgnoreCase("<unknown ssid>")){
                mSsidTv.setText(" ");
            } else {
                mSsidTv.setText(wifi.getConnectionInfo().getSSID().replace("\"", ""));
            }

            mEthernetIpTv.setText("dhcp");
        }

    }

    private void initView() {
        mRelativeConnectivity = findViewById(R.id.linear_connectivity);
        mMacTv = findViewById(R.id.tv_mac);
        mIPAddress = findViewById(R.id.tv_ipAddress);
        mNetmaskTv = findViewById(R.id.tv_netmask);
        mGatewayTv = findViewById(R.id.tv_gateway);
        mDns1Tv = findViewById(R.id.tv_dns1);
        mDns2Tv = findViewById(R.id.tv_dns2);
        mEthernetIpTv = findViewById(R.id.tv_ethernetIp);
        mSsidTv = findViewById(R.id.tv_ssid);
        mRbInternetConnectivity = findViewById(R.id.radio_connectivity);
        mRbcloudConnectivity = findViewById(R.id.radio_cloud);
    }

    public void onConnectivityClick(View v) {
        switch (v.getId()) {
            case R.id.connectivity_back:
                finish();
                break;
        }

    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("eth0")) continue;

                Log.i("Shailendra dhcp", nif.getDisplayName());
                Log.i("Shailendra dhcp1", nif.getName());
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "02:00:00:00:00:00";
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }

            JSONObject json = new JSONObject(reportInfo);
            if (json.getInt("responseCode") == 1){
                Log.i("Shailendra res", "responseCode");
                mRbcloudConnectivity.setChecked(true);

            } else {
                mRbcloudConnectivity.setChecked(false);
            }

        } catch (Exception e) {
            Logger.error("onJSONObjectListener(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }
}
