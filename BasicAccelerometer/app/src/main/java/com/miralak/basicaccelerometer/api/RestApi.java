package com.miralak.basicaccelerometer.api;

import com.miralak.basicaccelerometer.model.Acceleration;
import com.miralak.basicaccelerometer.model.TrainingAcceleration;

import com.miralak.basicaccelerometer.model.TrainingOrientation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface RestApi {

    @POST("/acceleration")
    Call<ResponseBody> sendAccelerationValues(@Body Acceleration acceleration);

    @POST("/training/acceleration")
    Call<TrainingAcceleration> sendTrainingAccelerationValues(@Body TrainingAcceleration trainingAcceleration);

    @POST("/training/orientation")
    Call<TrainingAcceleration> sendTrainingOrientationValues(@Body TrainingOrientation trainingOrientation);
}
