package com.ar.salata.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ar.salata.R;
import com.ar.salata.repositories.OrderRepository;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.ui.utils.ArabicString;
import com.ar.salata.viewmodels.UserViewModel;

import java.util.ArrayList;

import static com.ar.salata.ui.fragments.ChooseAddressDialogFragment.ADDRESS_ID;
import static com.ar.salata.ui.fragments.ChooseAddressDialogFragment.DELIVERY_DATE;
import static com.ar.salata.ui.fragments.ChooseAddressDialogFragment.SHIFT_ID;
import static com.ar.salata.ui.fragments.HomeFragment.USER_ID;

public class AddToCartActivity extends BaseActivity {
	private static final String TAG = "AddToCartActivity";
	private static final int REQUESTPAY = 2;
	private Button button;
	private TextView totalValueTextView;
	private UserViewModel userViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
		button = findViewById(R.id.btn_bill_confirm);
		totalValueTextView = findViewById(R.id.tv_total_value_add_to_card);

		Intent intent = getIntent();

		Order order = new Order(intent.getIntExtra(SHIFT_ID, 1),
				intent.getIntExtra(ADDRESS_ID, 1),
				intent.getIntExtra(USER_ID, 0),
				intent.getStringExtra(DELIVERY_DATE),
				500);
		ArrayList<OrderUnit> temp = new ArrayList<OrderUnit>();
		temp.add(new OrderUnit(1, 5));
		temp.add(new OrderUnit(2, 7));
		order.setUnits(temp);

		OrderRepository repository = new OrderRepository();
		repository.createOrder(userViewModel.getToken(), order).observe(this, new Observer<UserRepository.APIResponse>() {
			@Override
			public void onChanged(UserRepository.APIResponse apiResponse) {
				switch (apiResponse) {
					case SUCCESS:
						Log.d(TAG, "onChanged: success");
						break;
					case FAILED:
						Log.d(TAG, "onChanged: failed");
						break;
					case ERROR:
						Log.d(TAG, "onChanged: error");
						break;
				}
			}
		});

		totalValueTextView.setText(ArabicString.toArabic("12.50 جنيه"));
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