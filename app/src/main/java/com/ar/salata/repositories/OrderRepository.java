package com.ar.salata.repositories;

import androidx.lifecycle.MutableLiveData;

import com.ar.salata.repositories.API.OrderAPI;
import com.ar.salata.repositories.model.APIToken;
import com.ar.salata.repositories.model.DeliveryFees;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderAssociative;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.repositories.model.OrderUnitList;
import com.ar.salata.repositories.model.OrdersResponse;
import com.ar.salata.repositories.model.PaymentMethods;
import com.ar.salata.repositories.model.Product;
import com.ar.salata.repositories.model.ResponseMessage;

import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ar.salata.SalataApplication.BASEURL;

import android.util.Log;

public class OrderRepository {
    private static final String TAG = "OrderRepository";
    private RealmResults<Order> ordersResult;
    private OrderAPI orderAPI;
    private RealmResults<OrderUnit> orderUnitsResult;

    public OrderRepository() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        orderAPI = retrofit.create(OrderAPI.class);
    }

    public MutableLiveData<UserRepository.APIResponse> submitOrder(APIToken token, final Order order) {
        MutableLiveData<UserRepository.APIResponse> apiResponse =
                new MutableLiveData<>(UserRepository.APIResponse.NULL);

        HashMap<String, Double> units = new HashMap<String, Double>();
        for (OrderUnit unit : order.getUnits()) {
            units.put(String.valueOf(unit.getProductId()), unit.getCount());
        }

        OrderAssociative orderAssociative = new OrderAssociative(units,
                token.toString(),
                order.getOrderPrice(),
                order.getShiftId(),
                order.getAddressId(),
                order.getUserId(),
                order.getDeliveryDate(),
                order.getNotes(),
                order.getPaymentType(),
                order.getReference(),
                order.getAuthId()
        );

        orderAPI.submitOrder(orderAssociative).enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.where(APIToken.class).findAll().deleteAllFromRealm();
                    realm.insert(new APIToken(response.body().getToken()));
                    order.setOrderId(response.body().getId());
                    order.setSubmitted(true);
                    realm.insertOrUpdate(order);
                    realm.commitTransaction();
                    realm.close();
                    apiResponse.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    apiResponse.setValue(UserRepository.APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                apiResponse.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return apiResponse;
    }

//    public String createOrder(Order order) {
//        Realm realm = Realm.getDefaultInstance();
//        realm.beginTransaction();
//        Order temp = realm.copyToRealmOrUpdate(order);
//        realm.commitTransaction();
//        realm.close();
//
//        return temp.getOrderLocalId();
//    }

    public MutableLiveData<UserRepository.APIResponse> loadOrderProducts(APIToken token, int id) {
        MutableLiveData<UserRepository.APIResponse> results = new MutableLiveData<>();
        orderAPI.getOrderUnits(token.toString(), id).enqueue(new Callback<OrderUnitList>() {
            @Override
            public void onResponse(Call<OrderUnitList> call, Response<OrderUnitList> response) {
                Realm realm = Realm.getDefaultInstance();
                if (response.isSuccessful()) {
                    realm.beginTransaction();
                    Order order = realm.where(Order.class).equalTo("orderId", id).findFirst();
                    for (OrderUnit unit : response.body().getUnits()) {
                        order.addUnit(unit);
                    }
                    realm.commitTransaction();
                    results.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    results.setValue(UserRepository.APIResponse.ERROR);
                }
                realm.close();
            }

            @Override
            public void onFailure(Call<OrderUnitList> call, Throwable t) {
                results.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return results;
    }

    public void updateOrder(Order order) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(order);
        realm.commitTransaction();
        realm.close();
        return;
    }

    public MutableLiveData<UserRepository.APIResponse> loadOrders(APIToken token) {
        MutableLiveData<UserRepository.APIResponse> apiResponse = new MutableLiveData<>(UserRepository.APIResponse.NULL);
        orderAPI.getOrders(token.toString()).enqueue(new Callback<OrdersResponse>() {
            @Override
            public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.where(Order.class).findAll().deleteAllFromRealm();
                    realm.commitTransaction();
                    for (Order order : response.body().getOrdered()) {
                        realm.beginTransaction();
                        realm.insertOrUpdate(order);
                        realm.commitTransaction();
                    }
                    for (Order order : response.body().getDelivered()) {
                        realm.beginTransaction();
                        order.setOrderFulfilled(true);
                        realm.insertOrUpdate(order);
                        realm.commitTransaction();
                    }
                    realm.close();
                    apiResponse.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    apiResponse.setValue(UserRepository.APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<OrdersResponse> call, Throwable t) {
                apiResponse.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return apiResponse;
    }

    public MutableLiveData<List<Order>> getOrders() {
        MutableLiveData<List<Order>> result = new MutableLiveData<>();
        Realm realm = Realm.getDefaultInstance();
        ordersResult = realm.where(Order.class).greaterThan("orderId", -1).sort("orderId", Sort.DESCENDING).findAllAsync();
        ordersResult.addChangeListener(new RealmChangeListener<RealmResults<Order>>() {
            @Override
            public void onChange(RealmResults<Order> orders) {
                List<Order> orderList = realm.copyFromRealm(orders);
                result.setValue(orderList);
                ordersResult.removeAllChangeListeners();
            }
        });
        realm.close();
        return result;
    }

    public MutableLiveData<UserRepository.APIResponse> deleteOrder(APIToken token, int id) {
        MutableLiveData<UserRepository.APIResponse> result = new MutableLiveData<>();
        orderAPI.deleteOrder(token.toString(), id).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.where(Order.class).equalTo("orderId", id).findFirst().deleteFromRealm();
                    realm.commitTransaction();
                    realm.close();
                    result.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    result.setValue(UserRepository.APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                result.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return result;
    }

    public Order getOrder(int id) {
        Order order = null;
        Realm realm = Realm.getDefaultInstance();
        order = realm.where(Order.class).equalTo("orderId", id).findFirst();
        if (order != null) {
            if (order.getUnits().get(0).getProductImage() == null) {
                realm.beginTransaction();
                for (OrderUnit unit : order.getUnits()) {
                    unit.setProductImage(realm.where(Product.class).equalTo("id", unit.getProductId()).findFirst().getInvoiceImage());
                }
                realm.commitTransaction();
            }
            order = realm.copyFromRealm(order);
        }
        realm.close();
        return order;
    }

    public MutableLiveData<UserRepository.APIResponse> updateOrder(APIToken token, Order order) {
        MutableLiveData<UserRepository.APIResponse> apiResponse =
                new MutableLiveData<>();

        HashMap<String, Double> units = new HashMap<String, Double>();
        for (OrderUnit unit : order.getUnits()) {
            units.put(String.valueOf(unit.getProductId()), unit.getCount());
        }

        OrderAssociative orderAssociative = new OrderAssociative(units,
                token.toString(),
                order.getOrderPrice(),
                order.getShiftId(),
                order.getAddressId(),
                order.getUserId(),
                order.getOrderId(),
                order.getDeliveryDate());

        orderAPI.updateOrder(orderAssociative).enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.where(APIToken.class).findAll().deleteAllFromRealm();
                    realm.insert(new APIToken(response.body().getToken()));
                    realm.where(Order.class).equalTo("orderId", order.getOrderId());
                    order.setOrderId(response.body().getId());
                    realm.insertOrUpdate(order);
                    realm.commitTransaction();
                    realm.close();
                    apiResponse.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    apiResponse.setValue(UserRepository.APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                apiResponse.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return apiResponse;
    }

    public MutableLiveData<List<PaymentMethods>> getPaymentMethods() {
        MutableLiveData<List<PaymentMethods>> paymentMethods = new MutableLiveData<>();

        orderAPI.getPaymentMethods().enqueue(new Callback<List<PaymentMethods>>() {
            @Override
            public void onResponse(Call<List<PaymentMethods>> call, Response<List<PaymentMethods>> response) {
                paymentMethods.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<PaymentMethods>> call, Throwable t) {
                paymentMethods.setValue(null);

                Log.e(TAG, t.toString());
            }
        });
        return paymentMethods;
    }

    public MutableLiveData<DeliveryFees> getDeliveryFees(String addressId, String totalPrice){
        MutableLiveData<DeliveryFees> fees = new MutableLiveData<>();

        orderAPI.deliveryFees(addressId, totalPrice).enqueue(new Callback<DeliveryFees>() {
            @Override
            public void onResponse(Call<DeliveryFees> call, Response<DeliveryFees> response) {
                if(response.body() != null){
                    fees.setValue(response.body());
                }

            }

            @Override
            public void onFailure(Call<DeliveryFees> call, Throwable t) {
                fees.setValue(new DeliveryFees());
            }
        });

        return fees;
    }
}
