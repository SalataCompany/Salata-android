package com.ar.salata.ui.activities;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.salata.R;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.ui.adapters.FinalBillRecyclerAdapter;
import com.ar.salata.ui.fragments.ErrorDialogFragment;
import com.ar.salata.ui.fragments.LoadingDialogFragment;
import com.ar.salata.viewmodels.AppConfigViewModel;
import com.ar.salata.viewmodels.OrderViewModel;
import com.ar.salata.viewmodels.UserViewModel;
import com.google.android.material.textfield.TextInputLayout;

import static com.ar.salata.viewmodels.OrderViewModel.NOTES;
import static com.ar.salata.viewmodels.OrderViewModel.ORDER_ID;

public class OrderDetailActivity extends BaseActivity {

    private AppConfigViewModel appConfigViewModel;
    private OrderViewModel orderViewModel;
    private UserViewModel userViewModel;
    private int orderId;
    private MutableLiveData<UserRepository.APIResponse> orderProductsLiveData;
    private Order order;
    private FinalBillRecyclerAdapter adapter;
    private TextInputLayout txInputNotesOrderDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        txInputNotesOrderDetails = findViewById(R.id.et_notes_order_details);

        appConfigViewModel = new ViewModelProvider(this).get(AppConfigViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        orderId = getIntent().getIntExtra(ORDER_ID, 0);

        if(getIntent().getStringExtra(NOTES)!=null) {
            txInputNotesOrderDetails.getEditText().setText(getIntent().getStringExtra(NOTES));
        }

        RecyclerView recyclerView = findViewById(R.id.rv_order_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);

        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getSupportFragmentManager(), null);

        orderProductsLiveData = orderViewModel.loadOrderProducts(userViewModel.getToken(), orderId);
        orderProductsLiveData.observe(this, response -> {
            switch (response) {
                case SUCCESS: {
                    order = orderViewModel.getOrder(orderId);

                    adapter = new FinalBillRecyclerAdapter(getApplicationContext(), order, order.getDeliveryFees(), appConfigViewModel.getPhones());
                    adapter.setHasStableIds(true);
                    recyclerView.setAdapter(adapter);
                    loadingDialogFragment.dismiss();
                    break;
                }
                case ERROR: {
                    loadingDialogFragment.dismiss();
                    ErrorDialogFragment dialogFragment =
                            new ErrorDialogFragment("حدث خطأ", "فشلت عملية تحميل بيانات المستخدم", true);
                    dialogFragment.show(getSupportFragmentManager(), null);
                    break;
                }
                case FAILED: {
                    loadingDialogFragment.dismiss();
                    ErrorDialogFragment dialogFragment =
                            new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), true);
                    dialogFragment.show(getSupportFragmentManager(), null);
                    break;
                }
            }
        });


        FinalBillRecyclerAdapter adapter = new FinalBillRecyclerAdapter(getApplicationContext(), new Order(), 0, appConfigViewModel.getPhones());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    Toolbar deliverToolBar() {
        return findViewById(R.id.toolbar_Order_detail);
    }

    @Override
    int deliverLayout() {
        return R.layout.activity_order_detail;
    }
}
