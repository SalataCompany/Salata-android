package com.ar.salata.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ar.salata.repositories.model.Category;
import com.ar.salata.ui.fragments.SubCategoryFragment;

import java.util.List;

public class SubCategoryPagerAdapter extends FragmentStateAdapter {
    private final List<Category> subCategories;

    private final Category parentCategory;
    public SubCategoryPagerAdapter(Fragment fragment, List<Category> subCategories, Category parentCategory){
        super(fragment);
        this.subCategories = subCategories;
        this.parentCategory = parentCategory;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(subCategories.isEmpty()){
            return SubCategoryFragment.newInstance(parentCategory);
        }
        return SubCategoryFragment.newInstance(subCategories.get(position));
    }

    @Override
    public int getItemCount() {
        if(subCategories.isEmpty())
            return 1;
        return subCategories.size();
    }

}
