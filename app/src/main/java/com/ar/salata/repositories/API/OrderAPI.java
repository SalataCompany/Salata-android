package com.ar.salata.repositories.API;

import com.ar.salata.repositories.model.DeliveryFees;
import com.ar.salata.repositories.model.OrderAssociative;
import com.ar.salata.repositories.model.OrderUnitList;
import com.ar.salata.repositories.model.OrdersResponse;
import com.ar.salata.repositories.model.PaymentMethods;
import com.ar.salata.repositories.model.ResponseMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface OrderAPI {

    @POST("/api/createOrder")
    Call<ResponseMessage> submitOrder(@Body OrderAssociative units);

    @GET("/api/getOrders")
    Call<OrdersResponse> getOrders(@Query("api_token") String token);

    @GET("api/getOrderDetails")
    Call<OrderUnitList> getOrderUnits(@Query("api_token") String token, @Query("order_id") int id);

    @DELETE("/api/deleteOrder")
    Call<String> deleteOrder(@Query("api_token") String token, @Query("order_id") int id);

    @PUT("/api/updateOrder")
    Call<ResponseMessage> updateOrder(@Body OrderAssociative order);

    @GET("/api/getPaymentMethods")
    Call<List<PaymentMethods>> getPaymentMethods();

    @POST("api/getDeliveryFees")
    Call<DeliveryFees> deliveryFees(@Query("address_id") String addressId, @Query("total_price") String totalPrice);
}
