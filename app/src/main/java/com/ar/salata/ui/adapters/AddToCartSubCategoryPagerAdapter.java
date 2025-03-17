package com.ar.salata.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ar.salata.repositories.model.Category;
import com.ar.salata.ui.fragments.AddToCartFragment;

import java.util.List;

public class AddToCartSubCategoryPagerAdapter extends FragmentStateAdapter {
    private final List<Category> subCategories;

    private final Category parentCategory;

    public AddToCartSubCategoryPagerAdapter(Fragment fragment, List<Category> subCategories, Category parentCategory) {
        super(fragment);
        this.subCategories = subCategories;
        this.parentCategory = parentCategory;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(subCategories.isEmpty()){
            return AddToCartFragment.newInstance(parentCategory);
        }
        return AddToCartFragment.newInstance(subCategories.get(position));
    }

    @Override
    public int getItemCount() {
        if (subCategories.isEmpty())
            return 1;

        return subCategories.size();
    }
}
