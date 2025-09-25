package com.ar.salata.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Category;
import com.ar.salata.ui.adapters.AddToCartSubCategoryPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class AddToCartSubCategoryFragment extends Fragment {
    private static final String PRODUCTS_CATEGORY = "category";
    private Category category;

    private TabLayout subTabLayout;
    private ViewPager2 subViewPager;

    public AddToCartSubCategoryFragment() {
        // Required empty public constructor
    }

    public static AddToCartSubCategoryFragment newInstance(Category categoryOFProductsToBeDisplayed) {
        AddToCartSubCategoryFragment fragment = new AddToCartSubCategoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRODUCTS_CATEGORY, categoryOFProductsToBeDisplayed);
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
        View view = inflater.inflate(R.layout.fragment_add_to_cart_sub_category, container, false);
        subTabLayout = view.findViewById(R.id.add_to_cart_sub_tab_layout);
        subViewPager = view.findViewById(R.id.add_to_cart_sub_view_pager);

        if (getArguments() != null) {
            category = getArguments().getParcelable(PRODUCTS_CATEGORY);
        }

        if (category != null) {
            if (category.getSubCats().size() > 0) {
                subTabLayout.setVisibility(View.VISIBLE);

                ArrayList<Category> newList = new ArrayList<>();

                // create Category to show all products of the parent category
                if(category.getHasProducts() == 1) {
                    Category all = new Category(category.getCategoryID(), getString(R.string.all), 0, category.getLevel() + 1, category.getHasProducts());
                    newList.add(all);
                    newList.addAll(category.getSubCats());
                }
                AddToCartSubCategoryPagerAdapter adapter = new AddToCartSubCategoryPagerAdapter(this, newList, category);
                subViewPager.setAdapter(adapter);

                new TabLayoutMediator(subTabLayout, subViewPager, (tab, position) ->
                        tab.setText(newList.get(position).getCategoryName())
                ).attach();
            } else {
                AddToCartSubCategoryPagerAdapter adapter = new AddToCartSubCategoryPagerAdapter(this, new ArrayList<>(), category);
                subViewPager.setAdapter(adapter);
            }
        }

        return view;
    }
}