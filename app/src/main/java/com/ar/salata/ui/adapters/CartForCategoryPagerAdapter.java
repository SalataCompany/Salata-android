package com.ar.salata.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ar.salata.model.Category;
import com.ar.salata.ui.fragments.AddToCartFragment;

import java.util.ArrayList;

public class CartForCategoryPagerAdapter extends CategoryPagerAdapter {
    public CartForCategoryPagerAdapter(@NonNull Fragment fragment, ArrayList<Category> productCategories) {
        super(fragment, productCategories);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return AddToCartFragment.newInstance();
    }
}
