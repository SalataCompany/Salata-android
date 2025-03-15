package com.ar.salata.ui.fragments;

import android.os.Bundle;

import com.ar.salata.repositories.model.Category;
import com.ar.salata.ui.adapters.ProductsGalleryForCategoryPagerAdapter;

public class ProductsGalleryMainCategoryPagerFragment extends MainCategoryPagerFragment {
    private ProductsGalleryForCategoryPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ProductsGalleryForCategoryPagerAdapter(this, getProductCategories());
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
