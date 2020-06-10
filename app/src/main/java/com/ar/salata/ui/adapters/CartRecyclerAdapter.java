package com.ar.salata.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.repositories.model.Product;
import com.ar.salata.ui.utils.ArabicString;
import com.ar.salata.viewmodels.OrderViewModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CartRecyclerAdapter extends RecyclerView.Adapter {
    private final static int HEADER_VIEW = 1;
    private final static int NORMAL_VIEW = 0;

    private ArrayList<Product> products;
    private Context context;
    private OrderViewModel orderViewModel;
    private Order order;

    public CartRecyclerAdapter(Context context, ArrayList<Product> products, OrderViewModel orderViewModel) {
        this.products = products;
        this.context = context;
        this.orderViewModel = orderViewModel;
        order = this.orderViewModel.getOrderMutableLiveData().getValue();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        if (viewType == HEADER_VIEW) {
            view = LayoutInflater.from(context).inflate(R.layout.header_rv_add_to_cart, parent, false);
            viewHolder = new ItemViewHolderHeader(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.itemview_rv_add_to_cart, parent, false);
            viewHolder = new ItemViewHolderNormal(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case HEADER_VIEW: {
                final ItemViewHolderHeader itemViewHolderHeader = (ItemViewHolderHeader) holder;
                itemViewHolderHeader.deliveryDate.setText(ArabicString.toArabic("موعد التسليم: الاحد 2020/3/22 الساعة 01:00 ظهر"));
                // TODO: 5/23/2020 get phone numbers from server
                itemViewHolderHeader.phoneNumber1.setText(ArabicString.toArabic("ت/ 01224567892"));
                itemViewHolderHeader.phoneNumber2.setText(ArabicString.toArabic("ت/ 01224567892"));
                break;
            }
            case NORMAL_VIEW: {
                final ItemViewHolderNormal itemViewHolderNormal = (ItemViewHolderNormal) holder;
                final Double itemPrice = products.get(position - 1).getMaxPrice();
                final String itemImageURL = products.get(position - 1).getInvoiceImage();

                itemViewHolderNormal.itemNameTextView.setText(ArabicString.toArabic(products.get(position - 1).getProductName()));
                itemViewHolderNormal.itemPriceTextView.setText(ArabicString.toArabic(itemPrice.toString()));

                Glide.with(holder.itemView)
                        .load(itemImageURL)
                        .fitCenter()
                        .into(itemViewHolderNormal.itemImage);


                itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(itemViewHolderNormal.weight)));
                itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(itemViewHolderNormal.weight * itemPrice)));

                itemViewHolderNormal.decrementImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemViewHolderNormal.weight > 0) {
                            itemViewHolderNormal.weight--;
                            itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(itemViewHolderNormal.weight)));
                            itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(itemViewHolderNormal.weight * itemPrice)));
                            order.addUnit(new OrderUnit(products.get(position - 1), itemViewHolderNormal.weight));
                            orderViewModel.setOrderValue(order);
                        }
                    }
                });

                itemViewHolderNormal.incrementImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemViewHolderNormal.weight++;
                        itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(itemViewHolderNormal.weight)));
                        itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(itemViewHolderNormal.weight * itemPrice)));
                        order.addUnit(new OrderUnit(products.get(position - 1), itemViewHolderNormal.weight));
                        orderViewModel.setOrderValue(order);
                    }
                });
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER_VIEW;
        else
            return NORMAL_VIEW;
    }

    @Override
    public int getItemCount() {
        return products.size() + 1;
    }

    class ItemViewHolderNormal extends RecyclerView.ViewHolder {
        int weight = 0;
        ImageButton incrementImageButton;
        ImageButton decrementImageButton;

        TextView totalWeightTextView;
        TextView totalPriceTextView;
        TextView itemPriceTextView;
        TextView itemNameTextView;

        ImageView itemImage;

        public ItemViewHolderNormal(@NonNull View itemView) {
            super(itemView);

            incrementImageButton = itemView.findViewById(R.id.btn_increment_weight);
            decrementImageButton = itemView.findViewById(R.id.btn_decrement_weight);

            itemImage = itemView.findViewById(R.id.iv_item_image_add_to_cart);

            totalWeightTextView = itemView.findViewById(R.id.tv_total_weight);
            totalPriceTextView = itemView.findViewById(R.id.tv_total_price);
            itemPriceTextView = itemView.findViewById(R.id.tv_price);
            itemNameTextView = itemView.findViewById(R.id.tv_name);
        }
    }

    class ItemViewHolderHeader extends RecyclerView.ViewHolder {
        TextView deliveryDate;
        TextView phoneNumber1;
        TextView phoneNumber2;

        public ItemViewHolderHeader(@NonNull View itemView) {
            super(itemView);
            deliveryDate = itemView.findViewById(R.id.tv_delivery_date_add_to_cart);
            phoneNumber1 = itemView.findViewById(R.id.tv_phone_1_add_to_cart);
            phoneNumber2 = itemView.findViewById(R.id.tv_phone_2_add_to_cart);
        }
    }
}
