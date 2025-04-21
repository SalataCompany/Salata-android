package com.ar.salata.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import static com.ar.salata.viewmodels.OrderViewModel.NOTES;
import static com.ar.salata.viewmodels.OrderViewModel.ORDER_ID;

public class OrderPreviewActivity extends BaseActivity {

    private static final int REQUESTORDERPREVIEW = 2;
    private AppConfigViewModel appConfigViewModel;
    private ExtendedFloatingActionButton orderEditEFAB;
    private OrderViewModel orderViewModel;
    private UserViewModel userViewModel;
    private MutableLiveData<UserRepository.APIResponse> orderProductsLiveData;
    private Order order;
    private int orderId;
    private FinalBillRecyclerAdapter adapter;
    private TextInputLayout txInputNotesOrderDetails;
    private TextView paymentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        txInputNotesOrderDetails = findViewById(R.id.et_notes_order_preview);
        paymentType = findViewById(R.id.payment_type);

        appConfigViewModel = new ViewModelProvider(this).get(AppConfigViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        orderId = getIntent().getIntExtra(ORDER_ID, 0);

        if(getIntent().getStringExtra(NOTES)!=null) {
            txInputNotesOrderDetails.getEditText().setText(getIntent().getStringExtra(NOTES));
        }

        orderEditEFAB = findViewById(R.id.efab_edit_order_preview);
        orderEditEFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Order order = orderViewModel.getOrder(orderId);
                if (order.isModifiable()) {
                    Intent intent = new Intent(OrderPreviewActivity.this, OrderEditActivity.class);
                    intent.putExtras(getIntent());
                    startActivityForResult(intent, REQUESTORDERPREVIEW);
                } else {
                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment("خطا", "لا يمكن تعديل الطلب", false);
                    errorDialogFragment.show(getSupportFragmentManager(), null);
                }
            }
        });


        RecyclerView recyclerView = findViewById(R.id.rv_order_preview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);

        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getSupportFragmentManager(), null);

        orderProductsLiveData = orderViewModel.loadOrderProducts(userViewModel.getToken(), orderId);
        orderProductsLiveData.observe(this, response -> {
            switch (response) {
                case SUCCESS: {
                    order = orderViewModel.getOrder(orderId);
                    if(order.getPaymentType().equals("credit_card")) {
                        paymentType.setText("طريقة الدفع: الدفع أونلاين");
                    }else if(order.getPaymentType().equals("cash_on_delivery")){
                        paymentType.setText("طريقة الدفع: الدفع عند الاستــلام");
                    }

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
    }

    @Override
    Toolbar deliverToolBar() {
        return findViewById(R.id.toolbar_Order_preview);
    }

    @Override
    int deliverLayout() {
        return R.layout.activity_order_preview;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTORDERPREVIEW && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}

