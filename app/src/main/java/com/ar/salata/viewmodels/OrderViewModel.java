package com.ar.salata.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ar.salata.repositories.OrderRepository;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.APIToken;
import com.ar.salata.repositories.model.DeliveryFees;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.PaymentMethods;

import java.util.List;

public class OrderViewModel extends ViewModel {
    public static final String ORDER = "Order";
    public static final String ORDER_ID = "OrderID";
    public static final String NOTES = "notes";
    private OrderRepository orderRepository = new OrderRepository();
    private MutableLiveData<Order> orderMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<UserRepository.APIResponse> submitOrder(APIToken token, Order order) {
        return orderRepository.submitOrder(token, order);
    }

//    public String createOrder(Order order) {
//        return orderRepository.createOrder(order);
//    }

    public MutableLiveData<UserRepository.APIResponse> loadOrderProducts(APIToken token, int id) {
        return orderRepository.loadOrderProducts(token, id);
    }

    public void updateOrder(Order order) {
        orderRepository.updateOrder(order);
    }

    public void setOrderValue(Order order) {
        orderMutableLiveData.setValue(order);
    }

    public MutableLiveData<Order> getOrderMutableLiveData() {
        return orderMutableLiveData;
    }

    public MutableLiveData<UserRepository.APIResponse> loadOrders(APIToken token) {
        return orderRepository.loadOrders(token);
    }

    public MutableLiveData<List<Order>> getOrders() {
        return orderRepository.getOrders();
    }

    public Order getOrder(int id) {
        return orderRepository.getOrder(id);
    }

    public MutableLiveData<UserRepository.APIResponse> deleteOrder(APIToken token, int id) {
        return orderRepository.deleteOrder(token, id);
    }

    public MutableLiveData<UserRepository.APIResponse> updateOrder(APIToken token, Order order) {
        return orderRepository.updateOrder(token, order);
    }

    public MutableLiveData<List<PaymentMethods>> getPaymentMethods(){
        return orderRepository.getPaymentMethods();
    }

    public MutableLiveData<DeliveryFees> deliveryFees(String addressId, String totalPrice){
        return orderRepository.getDeliveryFees(addressId, totalPrice);
    }
}
