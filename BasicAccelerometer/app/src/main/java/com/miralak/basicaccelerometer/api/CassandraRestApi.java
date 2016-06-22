package com.miralak.basicaccelerometer.api;

import com.miralak.basicaccelerometer.model.Acceleration;
import com.miralak.basicaccelerometer.model.TrainingAcceleration;

import com.miralak.basicaccelerometer.model.TrainingOrientation;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface CassandraRestApi {

    @POST("/acceleration")
    public Response sendAccelerationValues(@Body Acceleration acceleration);


    @POST("/training/acceleration")
    public Response sendTrainingAccelerationValues(@Body TrainingAcceleration trainingAcceleration);

    @POST("/training/orientation")
    public Response sendTrainingOrientationValues(@Body TrainingOrientation trainingOrientation);
}
