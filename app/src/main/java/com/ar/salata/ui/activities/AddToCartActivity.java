package com.ar.salata.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ar.salata.R;

public class AddToCartActivity extends BaseActivity {
    private static final int REQUESTPAY = 2;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        button = findViewById(R.id.btn_bill_confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                startActivityForResult(intent, REQUESTPAY);
            }
        });

    }

    @Override
    Toolbar deliverToolBar() {
        return findViewById(R.id.toolbar_add_to_cart);
    }

    @Override
    int deliverLayout() {
        return R.layout.activity_add_to_cart;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTPAY && resultCode == RESULT_OK) finish();
    }
}
