package com.example.ping.Fragments;

import com.example.ping.Notification.MyResponse;
import com.example.ping.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAbehiyow:APA91bFSbM-qluOHyzDzjUI91ld4cTNCVhw7YIomdYqDQEyBvPES8jVMeK8pFpd0vd2KWk1gykWjNAdOoZlhxdMRnbAlSdPP5owmOaSULrGUVKA6PK_tJjcX36BGccaR6JE1ApSoA5_E"

            }
    )

    @POST("fcm/send")
    Call<MyResponse>sendNotification(@Body Sender sender);
}
