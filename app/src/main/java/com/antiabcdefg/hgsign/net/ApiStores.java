package com.antiabcdefg.hgsign.net;

import com.antiabcdefg.hgsign.bean.DataResponseEntity;
import com.antiabcdefg.hgsign.bean.DeviceResponseEntity;
import com.antiabcdefg.hgsign.bean.MacInfoResponseEntity;
import com.antiabcdefg.hgsign.bean.NotificationEntity;
import com.antiabcdefg.hgsign.bean.UnBindSiteEntity;
import com.antiabcdefg.hgsign.bean.UserEntity;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiStores {

    String URL = "http://xxxx.xxx";
    String writeURL = URL + ":8080/KQSystem/";
    String readURL = URL + ":8080/KQSystem/";

    @POST("login_client")
    Call<UserEntity> getUser(@Query("username") String username,
                             @Query("password") String password);

    @POST("verifyDeviceInfo_client")
    Call<DeviceResponseEntity> checkDevice(@Query("username") String username,
                                           @Query("deviceNum") String deviceNum);

    @POST("unbindSite_client")
    Call<UnBindSiteEntity> getUnBindSite();

    @POST("verifyRouterMac_client")
    Call<MacInfoResponseEntity> checkMac(@Query("username") String username,
                                         @Query("routerMac") String routerMac);

    @POST("notification_client")
    Call<NotificationEntity> getNotification(@Query("userid") String userid,
                                             @Query("device") String device);

    @POST("checkServiceByLocation_client")
    Call<DataResponseEntity> postGPS(@Query("userid") String userid,
                                     @Query("locationY") String locationY,
                                     @Query("locationX") String locationX,
                                     @Query("locationName") String locationName,
                                     @Query("checkTime") String checkTime,
                                     @Query("steps") String steps);

    @POST("checkService_client")
    Call<DataResponseEntity> postMac(@Query("userid") String userid,
                                     @Query("address") String address,
                                     @Query("checkTime") String checkTime,
                                     @Query("wifiMac") String wifiMac);

}