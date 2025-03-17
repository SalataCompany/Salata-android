package com.ar.salata.ui.fragments;

import android.os.Bundle;

import androidx.lifecycle.Observer;

import com.ar.salata.repositories.model.Category;
import com.ar.salata.repositories.model.CategoryList;
import com.ar.salata.ui.adapters.CartPagerAdapter;

public class AddToCartPagerFragment extends MainCategoryPagerFragment {
    private CartPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new CartPagerAdapter(this, getProductCategories());
        goodsViewModel.getCategories().observe(this, categoryList -> {
            for(Category category: categoryList.getCategoryList()){
                if(category.getLevel() == 1){
                    parentCats.add(category);
                }
            }
            productCategories.addAll(parentCats);
            adapter.notifyDataSetChanged();
        });
        setAdapter(adapter);
    }
}
