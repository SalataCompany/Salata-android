package com.ar.salata.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Category;
import com.ar.salata.ui.adapters.SubCategoryPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class ProductsGalleryForCategoryFragment extends Fragment {

    private static final String PRODUCTS_CATEGORY = "category";
    private Category category;

    private TabLayout subTabLayout;
    private ViewPager2 subViewPager;

    public ProductsGalleryForCategoryFragment() {
    }

    public static ProductsGalleryForCategoryFragment newInstance(Category categoryOFProductsToBeDisplayed) {
        ProductsGalleryForCategoryFragment fragment = new ProductsGalleryForCategoryFragment();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_gallery_for_category, container, false);
        subTabLayout = view.findViewById(R.id.subTabLayout);
        subViewPager = view.findViewById(R.id.subViewPager);

        if (getArguments() != null) {
            category = getArguments().getParcelable(PRODUCTS_CATEGORY);
        }

        if (category != null) {
            if (category.getSubCats().size() > 0) {
                subTabLayout.setVisibility(View.VISIBLE);

                ArrayList<Category> newList = new ArrayList<>();

                // create Category to show all products of the parent category
                Category all = new Category(category.getCategoryID(), getString(R.string.all), 0, category.getLevel() +1);
                newList.add(all);
                newList.addAll(category.getSubCats());

                SubCategoryPagerAdapter adapter = new SubCategoryPagerAdapter(this, newList, category);
                subViewPager.setAdapter(adapter);

                new TabLayoutMediator(subTabLayout, subViewPager, (tab, position) ->
                        tab.setText(newList.get(position).getCategoryName())
                ).attach();
            }else{
                SubCategoryPagerAdapter adapter = new SubCategoryPagerAdapter(this, new ArrayList<>(), category);
                subViewPager.setAdapter(adapter);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
