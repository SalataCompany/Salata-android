package com.ar.salata.ui.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ar.salata.R;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.APIToken;
import com.ar.salata.repositories.model.OpaySetting;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.PaymentMethods;
import com.ar.salata.ui.activities.OrdersActivity;
import com.ar.salata.viewmodels.OpayViewModel;
import com.ar.salata.viewmodels.OrderViewModel;

import java.util.List;
import java.util.UUID;

import kotlin.Unit;
import team.opay.business.cashier.sdk.api.PayInput;
import team.opay.business.cashier.sdk.api.UserInfo;
import team.opay.business.cashier.sdk.pay.PaymentTask;

public class PaymentMethodDialogFragment extends DialogFragment {

    public static final String CREDIT_CARD = "credit_card";
    private OrderViewModel orderViewModel;
    private OpayViewModel opayViewModel;
    private Order order;
    private APIToken token;
    private List<PaymentMethods> paymentMethods;
    private Button confirmButton, cancelButton;
    private MutableLiveData<UserRepository.APIResponse> submitOrderResponse;
    PayInput payInput = null;
    UUID uuid= UUID.randomUUID();

    public PaymentMethodDialogFragment(List<PaymentMethods> paymentMethods, APIToken token, Order order){
        this.paymentMethods = paymentMethods;
        this.token = token;
        this.order = order;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pay_method, null);
        builder.setView(view);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        opayViewModel  = new ViewModelProvider(this).get(OpayViewModel.class);

        RadioGroup paymentRadioGroup = (RadioGroup) view.findViewById(R.id.dialog_payment_options);

        confirmButton = view.findViewById(R.id.btn_payment_confirm_dialog);
        cancelButton  = view.findViewById(R.id.btn_payment_cancel_dialog);

        // Show Payment Methods Radio Buttons
        for(PaymentMethods payment: paymentMethods){
            // this id =4  for Apple Pay and it is a method for IOS users only
            if(payment.getId()==4){
                continue;
            }
            RadioButton paymentRadioButton = new RadioButton(getContext(), null, R.attr.RadioButtonStyle, R.style.RadioButtonStyle);
            paymentRadioButton.setText(payment.getName());
            paymentRadioButton.setId(payment.getId());
            paymentRadioGroup.addView(paymentRadioButton);
        }
        if(paymentRadioGroup!= null)
            ((RadioButton) paymentRadioGroup.getChildAt(0)).setChecked(true);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                loadingDialogFragment.show(getActivity().getSupportFragmentManager(), null);

                int tempId = paymentRadioGroup.getCheckedRadioButtonId();
                Log.e("PaymentMethod", String.valueOf(tempId));
                // Id = 1   for cash_on_delivery
                if(tempId == 1){
                    order.setPaymentType("cash_on_delivery");
                    submitOrderResponse = orderViewModel.submitOrder(token, order);
                    submitOrderResponse.observe(getViewLifecycleOwner(), new Observer<UserRepository.APIResponse>() {
                        @Override
                        public void onChanged(UserRepository.APIResponse apiResponse) {
                            switch (apiResponse) {
                                case SUCCESS: {
                                    loadingDialogFragment.dismiss();
                                    Intent intent = new Intent(getContext(), OrdersActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    dismiss();
                                    getActivity().finish();
//                                order.setSubmitted(true);
//                                orderViewModel.updateOrder(order);
                                    break;
                                }
                                case ERROR: {
                                    loadingDialogFragment.dismiss();
                                    ErrorDialogFragment dialogFragment =
                                            new ErrorDialogFragment("حدث خطأ", "فشلت عملية تحميل بيانات المستخدم", false);
                                    dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                                    break;
                                }
                                case FAILED: {
                                    loadingDialogFragment.dismiss();
                                    ErrorDialogFragment dialogFragment =
                                            new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), false);
                                    dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                                    break;
                                }
                            }
                        }
                    });
                // Id = 2   for credit_card
                }
                else if(tempId == 2){   //Id = 2   for OpayCash
                    String reference = "android-" + order.getUserId() + "-" +uuid.toString();
                    order.setPaymentType(CREDIT_CARD);
                    order.setReference(reference);

                    // getting opay settings
                    opayViewModel.getOpaySetting().observe(getViewLifecycleOwner(), new Observer<OpaySetting>() {
                        @Override
                        public void onChanged(OpaySetting opaySetting) {
                            // in testing mode set the sandBox to true:
                            // False for real payments
                            if(opaySetting!=null) {
                                PaymentTask.Companion.setSandBox(opaySetting.isMode());
                                payInput = new PayInput(opaySetting.getPublicKey(),
                                        opaySetting.getMerchantId(),
                                        "Salata", //merchant name
                                        reference, // reference
                                        "EG",//uppercase//country
                                        (long) (order.getOrderPrice() * 100),//amount
                                        "EGP", //uppercase //currency
                                        "Vegetables & Fruits",//ProductName
                                        "testtest",//ProductDescription
                                        opaySetting.getCallbackurl(),
                                        opaySetting.getPayMethod(),//Payment Type
                                        30,//expire at
                                        "110.246.160.183",// user ip
                                        new UserInfo("UserId", "UserName", "UserPhone", "Email")
                                );

                                Log.e("Payment task", payInput.toString());

                                opayViewModel.setOpayPaymentInput(order.getUserId(), reference).observe(getViewLifecycleOwner(), new Observer<UserRepository.APIResponse>() {
                                    @Override
                                    public void onChanged(UserRepository.APIResponse apiResponse) {
                                        Log.e("Payment task", "onChanged");
                                        switch (apiResponse) {
                                            case SUCCESS:
                                                Log.e("Payment task", "success api response");
                                                new PaymentTask(getActivity()).createOrder(
                                                        payInput, ((status, orderInfoHttpResponse) -> {
                                                            switch (status) {
                                                                case LOADING:
                                                                    Log.e("Payment task", "loading payment task");
                                                                    break;
                                                                case CANCEL:
                                                                    Log.e("Payment task", "cancel payment task");
                                                                    break;
                                                                case SUCCESS:
                                                                    Log.e("Payment task", "success payment task");
                                                                    dismiss();
                                                                    break;
                                                                default:
                                                                    Log.e("Payment task", "status: " + status.toString() + "orderInfo " + orderInfoHttpResponse);
                                                            }
                                                            return Unit.INSTANCE;
                                                        })
                                                );
                                                break;
                                            case ERROR:
                                            case FAILED:
                                                ErrorDialogFragment dialogFragment =
                                                        new ErrorDialogFragment("حدث خطأ", "الاتصال بالانترنت ضعيف الرجاء الحاوله مره اخرى", false);
                                                dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                                                break;
                                            default:
                                                Log.e("Payment task", "default case");

                                        }
                                        loadingDialogFragment.dismiss();
                                    }
                                });
                            }else{
                                ErrorDialogFragment dialogFragment =
                                        new ErrorDialogFragment("حدث خطأ", "يوجد مشكلة فى الاتصال بالانترنت", true);
                                dialogFragment.show(PaymentMethodDialogFragment.this.getActivity().getSupportFragmentManager(), null);
                            }
                        }
                    });
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_pay_method, container, false);

    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e("onActivityResult", "onActivityResult0");
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult", "onActivityResult1");
        if (requestCode == PaymentTask.REQUEST_PAYMENT) {
            Log.e("onActivityResult", "onActivityResult2");
            if (resultCode == PaymentTask.RESULT_PAYMENT) {
                Log.e("onActivityResult", "onActivityResult3");
                WebJsResponse response = (WebJsResponse) data.getExtras().getSerializable(PaymentTask.RESPONSE_DATA);
                switch (response.getOrderStatus()) {
                    case PaymentStatus.INITIAL: {
                        Log.e("onActivityResult", response.getOrderStatus());
                        break;
                    }
                    case PaymentStatus.SUCCESS: {
                        Log.e("onActivityResult", response.getOrderStatus());
                        break;
                    }
                    case PaymentStatus.FAIL: {
                        Log.e("onActivityResult", response.getOrderStatus());
                        break;
                    }
                    case PaymentStatus.PENDING: {
                        Log.e("onActivityResult", response.getOrderStatus());
                        break;
                    }
                }
            }
        }
    }
*/
}
