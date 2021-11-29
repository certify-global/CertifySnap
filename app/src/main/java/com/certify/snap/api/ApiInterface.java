package com.certify.snap.api;

import com.certify.snap.api.request.AccessLogRequest;
import com.certify.snap.api.request.GetTokenRequest;
import com.certify.snap.api.request.HealthCheckRequest;
import com.certify.snap.api.request.TemperatureRecordRequest;
import com.certify.snap.api.response.AccessLogResponse;
import com.certify.snap.api.response.ApiResponse;
import com.certify.snap.api.response.GetTokenResponse;
import com.certify.snap.api.response.HealthCheckResponse;
import com.certify.snap.api.response.TemperatureRecordResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("DeviceHealthCheck")
    Call<HealthCheckResponse> getDeviceHealthCheck(@Header("DeviceSN") String deviceSN, @Body HealthCheckRequest request);

    @POST("AccessLogs")
    Call<AccessLogResponse> sendAccessLog(@Body AccessLogRequest request);

    @POST("RecordMemberTemperature")
    Call<TemperatureRecordResponse> recordUserTemperature(@Body TemperatureRecordRequest request);

    @POST("GenerateToken")
    Call<String> getToken(@Header("DeviceSN") String deviceSN, @Body GetTokenRequest request);

    /*@GET("GetFaceLicenceInfo")
    Call<LicenseResponse> getLicenseRemote(@Query("deviceSN") String deviceSerialNum);

    @POST("ActivateApplication")
    Call<ActivateApplicationResponse> activateApplication(@Header("DeviceSN") String deviceSN, @Body ActivateApplicationRequest request);

    @POST("GetMemberList")
    Call<GetMembersListResponse> getMembersList(@Header("device_sn") String deviceSN, @Body GetMembersListRequest request);

    @POST("GetMemberById")
    Call<MemberDetailResponse> getMemberById(@Header("device_sn") String deviceSN, @Body MemberDetailRequest request);

    @POST("GetDeviceConfiguration")
    Call<SettingsResponse> getSettings(@Body GetSettingsRequest request);*/


}
