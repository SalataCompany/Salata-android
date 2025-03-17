package com.ar.salata.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Category;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.repositories.model.StockProduct;
import com.ar.salata.repositories.model.StockProductList;
import com.ar.salata.ui.activities.AddToCartActivity;
import com.ar.salata.ui.activities.OrderEditActivity;
import com.ar.salata.ui.adapters.CartRecyclerAdapter;
import com.ar.salata.viewmodels.AppConfigViewModel;
import com.ar.salata.viewmodels.GoodsViewModel;
import com.ar.salata.viewmodels.OrderViewModel;
import com.ar.salata.viewmodels.UserViewModel;

import java.util.ArrayList;

public class AddToCartFragment extends Fragment {
    private static final String PRODUCTS_CATEGORY = "category";
    private RecyclerView cartRecyclerView;
    private StockProductList productList = new StockProductList(new ArrayList<StockProduct>());
    private Category categoryOFProductsToBeDisplayed;
    private GoodsViewModel goodsViewModel;
    private OrderViewModel orderViewModel;
    private AppConfigViewModel appConfigViewModel;
    private UserViewModel userViewModel;
    private int page = 1;

    public static AddToCartFragment newInstance(Category category) {
        AddToCartFragment fragment = new AddToCartFragment();

        Bundle args = new Bundle();
        args.putParcelable(PRODUCTS_CATEGORY, category);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryOFProductsToBeDisplayed = getArguments().getParcelable(PRODUCTS_CATEGORY);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_to_cart, container, false);

        cartRecyclerView = view.findViewById(R.id.rv_add_to_cart);
        appConfigViewModel = new ViewModelProvider(this).get(AppConfigViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        goodsViewModel = new ViewModelProvider(this).get(GoodsViewModel.class);

        String mode = "create";
        if (getActivity() instanceof AddToCartActivity) {
            orderViewModel = ((AddToCartActivity) getActivity()).getOrderViewModel();
        } else if (getActivity() instanceof OrderEditActivity) {
            orderViewModel = ((OrderEditActivity) getActivity()).getOrderViewModel();
            mode = "edit";
        }

        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getActivity().getSupportFragmentManager(), null);

        CartRecyclerAdapter cartRecyclerAdapter = new CartRecyclerAdapter(getActivity(), productList, orderViewModel, appConfigViewModel.getPhones(), mode, userViewModel.getToken(), categoryOFProductsToBeDisplayed.getCategoryID());
        cartRecyclerAdapter.setHasStableIds(true);
        cartRecyclerView.setAdapter(cartRecyclerAdapter);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        goodsViewModel.loadProductsWithCategory(userViewModel.getToken(), orderViewModel.getOrderMutableLiveData().getValue().getAddressId(), categoryOFProductsToBeDisplayed.getCategoryID(), page).observe(getViewLifecycleOwner(), new Observer<StockProductList>() {
            @Override
            public void onChanged(StockProductList stockProductList) {
                loadingDialogFragment.dismiss();

                if(stockProductList!=null) {

                    if (getActivity() instanceof AddToCartActivity) {
                        for (StockProduct product : stockProductList.getProductList()) {
                            if (product.getRemain() > 0)
                                productList.addProduct(product);
                        }
                    } else if (getActivity() instanceof OrderEditActivity) {
                        Order order = orderViewModel.getOrderMutableLiveData().getValue();
                        ArrayList<Integer> ids = new ArrayList<>();
                        for (OrderUnit unit : order.getUnits()) {
                            ids.add(unit.getProductId());
                        }
                        for (StockProduct stockProduct : stockProductList.getProductList()) {
                            if (stockProduct.getRemain() > 0 || ids.contains(stockProduct.getId())) {
                                productList.addProduct(stockProduct);
                            }
                        }
                    }
                    productList.getLinks().setNextPageUrl(stockProductList.getLinks().getNextPageUrl());
                    cartRecyclerAdapter.notifyDataSetChanged();
                }else{
                    ErrorDialogFragment dialogFragment =
                            new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), true);
                    dialogFragment.show(AddToCartFragment.this.getActivity().getSupportFragmentManager(), null);
                }
            }
        });

        return view;
    }
}
