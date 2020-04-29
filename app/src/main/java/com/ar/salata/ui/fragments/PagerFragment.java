package com.ar.salata.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.ar.salata.R;
import com.ar.salata.model.Category;
import com.ar.salata.model.SliderItem;
import com.ar.salata.ui.activities.HomeActivity;
import com.ar.salata.ui.adapters.CategoryPagerAdapter;
import com.ar.salata.ui.adapters.ImageSliderAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

public class PagerFragment extends Fragment {

    private CategoryPagerAdapter adapter;
    private ViewPager2 viewPager;
    private ArrayList<Category> productCategories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // end points to download categories
        productCategories = new ArrayList<>();
        productCategories.add(new Category("1", "خضروات"));
        productCategories.add(new Category("1", "فاكهة"));
        /*
        if (getArguments() != null) {

        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pager, container, false);

        if (getActivity() instanceof HomeActivity) {
            view.findViewById(R.id.appbar_pager_fragment).setVisibility(View.VISIBLE);
            SliderView sliderView = view.findViewById(R.id.imageSlider);

            ImageSliderAdapter adapter = new ImageSliderAdapter(getContext());

            sliderView.setSliderAdapter(adapter);

            sliderView.setIndicatorAnimation(IndicatorAnimations.SLIDE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
            sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION);
            sliderView.setIndicatorSelectedColor(Color.WHITE);
            sliderView.setIndicatorUnselectedColor(Color.GRAY);

            adapter.addItem(new SliderItem("https://cdn.pixabay.com/photo/2015/12/01/20/28/road-1072823_960_720.jpg",
                    "road forest"));
            adapter.addItem(new SliderItem("https://cdn.pixabay.com/photo/2015/12/01/20/28/green-1072828_960_720.jpg",
                    "green forest"));
            adapter.addItem(new SliderItem("https://cdn.pixabay.com/photo/2015/04/19/08/32/marguerite-729510_960_720.jpg",
                    "flower"));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        TabLayout productsTabs = view.findViewById(R.id.products_tabes);

        new TabLayoutMediator(productsTabs, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(productCategories.get(position).getCategoryName());
            }
        }).attach();
    }

    protected void setAdapter(CategoryPagerAdapter adapter) {
        this.adapter = adapter;
    }

    protected ArrayList<Category> getProductCategories() {
        return productCategories;
    }
}
