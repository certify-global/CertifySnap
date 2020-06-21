package com.certify.snap.activity;

import android.app.Service;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.RouteInfo;
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

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ConnectivityStatusActivity extends SettingBaseActivity implements JSONObjectCallback{

    LinearLayout mRelativeConnectivity;
    private TextView mMacTv, mIPAddress, mNetmaskTv, mGatewayTv, mDns1Tv, mDns2Tv, mEthernetIpTv, mSsidTv, mNetworkAvailableTv;
    private RadioButton mRbInternetConnectivity, mRbcloudConnectivity;
    DhcpInfo dhcpInfo;
    WifiManager wifi;
    String macAddress = "";
    String gateway = "";
    ConnectivityManager connectivityManager;
    private static String netMaskAddress;
    private String ipAddress;
    String dns1, dns2;
    Typeface rubiklight;
    private TextView mConnectedTv;
    private TextView mMacText;
    private TextView mIpAddressText;
    private TextView mNetmaskText;
    private TextView mGatewaytext;
    private TextView mDns1Text;
    private TextView mDns2Text;
    private TextView mEthernetIptext;
    private TextView mInternetConnectivityText;
    private TextView mCloudConnectivityText;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);

        Util.getDeviceHealthCheck((JSONObjectCallback) this,this);
        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        initView();
        networkAvailableSetText();
        if (!Util.isNetworkOff(ConnectivityStatusActivity.this)){
            mRbInternetConnectivity.setChecked(true);
        } else {
            mRbInternetConnectivity.setChecked(false);
        }
        
    }
    @SuppressWarnings("deprecation")
    private void networkAvailableSetText() {
        if (Util.isConnectedWifi(ConnectivityStatusActivity.this)){
            wifi= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            dhcpInfo=wifi.getDhcpInfo();
            mNetworkAvailableTv.setText(getString(R.string.wifi));
            macAddress = getMacAddress("p2p0");
            ipAddress = String.valueOf(Formatter.formatIpAddress(dhcpInfo.ipAddress));
            gateway = String.valueOf(Formatter.formatIpAddress(dhcpInfo.gateway));
            dns1 = String.valueOf(Formatter.formatIpAddress(dhcpInfo.dns1));
            dns2 = String.valueOf(Formatter.formatIpAddress(dhcpInfo.dns2));
            netMaskAddress = String.valueOf(Formatter.formatIpAddress(dhcpInfo.netmask));
            getConnectionData(macAddress, ipAddress, netMaskAddress, gateway, dns1, dns2);
            if (wifi.getConnectionInfo().getSSID().equalsIgnoreCase("<unknown ssid>")){
                mSsidTv.setText(" ");
            } else {
                mSsidTv.setText(wifi.getConnectionInfo().getSSID().replace("\"", ""));
            }
        } else if (Util.isConnectedEthernet(ConnectivityStatusActivity.this)){
            connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Service.CONNECTIVITY_SERVICE);
            mNetworkAvailableTv.setText(getString(R.string.ethernet));
            macAddress = getMacAddress("eth0");
            ipAddress = getIPAddress(true);
            gateway = enthernetGatewayData(connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork()).getRoutes());
            dnsData(connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork()).getDnsServers());

            getConnectionData(macAddress, ipAddress, netMaskAddress, gateway, dns1, dns2);
        } else if (Util.isConnectedMobile(ConnectivityStatusActivity.this)){
            wifi= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            dhcpInfo=wifi.getDhcpInfo();
            mNetworkAvailableTv.setText(getString(R.string.mobile));
            macAddress = getMacAddress("wlan0");
            ipAddress = getIPAddress(true);
            gateway = String.valueOf(Formatter.formatIpAddress(dhcpInfo.gateway));
            dns1 = String.valueOf(Formatter.formatIpAddress(dhcpInfo.dns1));
            dns2 = String.valueOf(Formatter.formatIpAddress(dhcpInfo.dns2));
            getConnectionData(macAddress, ipAddress, netMaskAddress, gateway, dns1, dns2);
            if (wifi.getConnectionInfo().getSSID().equalsIgnoreCase("<unknown ssid>")){
                mSsidTv.setText(" ");
            } else {
                mSsidTv.setText(wifi.getConnectionInfo().getSSID().replace("\"", ""));
            }
        }
    }

    private void getConnectionData(String macAddress, String ipAddress, String netMaskAddress, String gateway, String dns1, String dns2) {
        mMacTv.setText(macAddress);
            mIPAddress.setText(ipAddress);
            mNetmaskTv.setText(netMaskAddress);
            mGatewayTv.setText(gateway);
            mDns1Tv.setText(dns1);
            mDns2Tv.setText(dns2);

            mEthernetIpTv.setText("dhcp");

    }

    private void initView() {
        mRelativeConnectivity = findViewById(R.id.linear_connectivity);
        mMacTv = findViewById(R.id.tv_mac);
        mMacTv.setTypeface(rubiklight);
        mIPAddress = findViewById(R.id.tv_ipAddress);
        mIPAddress.setTypeface(rubiklight);
        mNetmaskTv = findViewById(R.id.tv_netmask);
        mNetmaskTv.setTypeface(rubiklight);
        mGatewayTv = findViewById(R.id.tv_gateway);
        mGatewayTv.setTypeface(rubiklight);
        mDns1Tv = findViewById(R.id.tv_dns1);
        mDns1Tv.setTypeface(rubiklight);
        mDns2Tv = findViewById(R.id.tv_dns2);
        mDns2Tv.setTypeface(rubiklight);
        mEthernetIpTv = findViewById(R.id.tv_ethernetIp);
        mEthernetIpTv.setTypeface(rubiklight);
        mSsidTv = findViewById(R.id.tv_ssid);
        mSsidTv.setTypeface(rubiklight);
        mRbInternetConnectivity = findViewById(R.id.radio_connectivity);
        mRbcloudConnectivity = findViewById(R.id.radio_cloud);
        mNetworkAvailableTv = findViewById(R.id.tv_network_available);
        mNetworkAvailableTv.setTypeface(rubiklight);
        mConnectedTv = findViewById(R.id.connected_tv);
        mConnectedTv.setTypeface(rubiklight);
        mMacText = findViewById(R.id.mac_title);
        mMacText.setTypeface(rubiklight);
        mIpAddressText = findViewById(R.id.tv_ipAddressTitle);
        mIpAddressText.setTypeface(rubiklight);
        mNetmaskText = findViewById(R.id.tv_netmaskTitle);
        mNetmaskText.setTypeface(rubiklight);
        mGatewaytext = findViewById(R.id.tv_gatewayTitle);
        mGatewaytext.setTypeface(rubiklight);
        mDns1Text = findViewById(R.id.tv_dns1Title);
        mDns1Text.setTypeface(rubiklight);
        mDns2Text = findViewById(R.id.tv_dns2Title);
        mDns2Text.setTypeface(rubiklight);
        mEthernetIptext = findViewById(R.id.tv_ethernetIpTitle);
        mEthernetIptext.setTypeface(rubiklight);
        mInternetConnectivityText = findViewById(R.id.tv_internetConnectivity);
        mInternetConnectivityText.setTypeface(rubiklight);
        mCloudConnectivityText = findViewById(R.id.tv_cloudConnectivity);
        mCloudConnectivityText.setTypeface(rubiklight);


    }

    public void onConnectivityClick(View v) {
        switch (v.getId()) {
            case R.id.connectivity_back:
                finish();
                break;
        }

    }

    public static String getMacAddress(String networkType) {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase(networkType)) continue;

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

    public static String getIPAddress(boolean useIPv4) {
        try {
            int netmask_hex = 0;
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

                        if (useIPv4) {
                            if (isIPv4)
                                netmask_hex = netmask_to_hex(intf.getInterfaceAddresses().get(0).getNetworkPrefixLength());
                                netMaskAddress = intToIP(netmask_hex);
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "0.0.0.0";
    }

    private void dnsData(List<InetAddress> dnsServers) {
        Iterator<InetAddress> dnsIterator = dnsServers.iterator();
        if (dnsIterator.hasNext()) {
            dns1 = dnsIterator.next().getHostAddress();
        }
        if (dnsIterator.hasNext()) {
            dns2 = dnsIterator.next().getHostAddress();
        }
    }

    private String enthernetGatewayData(List<RouteInfo> routeInfoList) {
        String str = " ";
        try {
            Log.i("routeInfo size", String.valueOf(routeInfoList.size()));
            for (int i = 0; i< routeInfoList.size(); i ++){
                Log.i("routeInfo", String.valueOf(routeInfoList.get(i)));
            }
            for (RouteInfo route : routeInfoList) {
                if (route.isDefaultRoute()) {
                    Log.i("Abc gate", route.getGateway().getHostAddress());
                    str = route.getGateway().getHostAddress();
                    break;
                }

            }
        } catch (Exception ex) {

        }
        return str;
    }

    private static String intToIP(int ipAddress) {
        String ret = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ret;
    }

    private static int netmask_to_hex(int netmask_slash) {
        int r = 0;
        int b = 1;
        for (int i = 0; i < netmask_slash; i++, b = b << 1)
            r |= b;
        return r;
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }

            JSONObject json = new JSONObject(reportInfo);
            if (json.getInt("responseCode") == 1){
                mRbcloudConnectivity.setChecked(true);

            } else {
                mRbcloudConnectivity.setChecked(false);
            }

        } catch (Exception e) {
            Logger.error("onJSONObjectListener(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }
}
