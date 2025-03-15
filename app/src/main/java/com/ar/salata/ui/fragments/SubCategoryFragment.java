package com.ar.salata.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Category;
import com.ar.salata.repositories.model.Product;
import com.ar.salata.repositories.model.ProductList;
import com.ar.salata.ui.activities.HomeActivity;
import com.ar.salata.ui.adapters.ProductGalleryViewRecyclerAdapter;
import com.ar.salata.ui.utils.OffsetDecoration;
import com.ar.salata.viewmodels.GoodsViewModel;

import java.util.ArrayList;


public class SubCategoryFragment extends Fragment {

    private static final String ARG_SUBCATEGORY_NAME = "subCategoryName";
    private Category categoryOFProductsToBeDisplayed;

    private final ArrayList<Product> productsList = new ArrayList<>();

    private boolean fabVisibility = true;

    public SubCategoryFragment() {
        // Required empty public constructor
    }

    public static SubCategoryFragment newInstance(Category categoryOFProductsToBeDisplayed) {
        SubCategoryFragment fragment = new SubCategoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SUBCATEGORY_NAME, categoryOFProductsToBeDisplayed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sub_category, container, false);


        if (getArguments() != null) {
            categoryOFProductsToBeDisplayed = getArguments().getParcelable(ARG_SUBCATEGORY_NAME);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GoodsViewModel goodsViewModel = new ViewModelProvider(this).get(GoodsViewModel.class);

        RecyclerView productGalleryRecyclerView = view.findViewById(R.id.products_gallery);
        RecyclerView.Adapter productsAdapter = new ProductGalleryViewRecyclerAdapter(productsList, this);
        productGalleryRecyclerView.setAdapter(productsAdapter);

        RecyclerView.LayoutManager productsViewManager = new GridLayoutManager(this.getActivity(), 4);
        productGalleryRecyclerView.setLayoutManager(productsViewManager);

        OffsetDecoration itemDecoration = new OffsetDecoration(getContext(), R.dimen.product_item_offset);
        productGalleryRecyclerView.addItemDecoration(itemDecoration);

        NestedScrollView productsGalleryScrollView = view.findViewById(R.id.sv_product_gallery);

        productsGalleryScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                ((HomeActivity) getActivity()).setEFABVisibility(false);
                fabVisibility = false;
            } else if (oldScrollY > scrollY) {
                ((HomeActivity) getActivity()).setEFABVisibility(true);
                fabVisibility = true;
            }
        });

        MutableLiveData<ProductList> productListMutableLiveData = goodsViewModel.getProducts(categoryOFProductsToBeDisplayed.getCategoryID());
        productListMutableLiveData.observe(getViewLifecycleOwner(), productList -> {
            productsList.addAll(productList.getProductList());
            productsAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) getActivity()).setEFABVisibility(fabVisibility);
    }
}