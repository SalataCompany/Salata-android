package com.ar.salata.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.ui.utils.ArabicString;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

public class FinalBillRecyclerAdapter extends RecyclerView.Adapter {
    private final static int FOOTER_VIEW = 2;
    private final static int HEADER_VIEW = 1;
    private final static int NORMAL_VIEW = 0;

    private ArrayList<OrderUnit> data;
    private Context context;
    private Order order;
    private List<String> phones;
    private double fees;

    public FinalBillRecyclerAdapter(Context context, Order order, double fees, List<String> phones) {
        this.context = context;
        this.data = new ArrayList<OrderUnit>(order.getUnits());
        this.order = order;
        this.phones = phones;
        this.fees = fees;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case FOOTER_VIEW:
                view = LayoutInflater.from(context).inflate(R.layout.footer_rv_final_bill, parent, false);
                viewHolder = new FooterItemViewHolder(view);
                break;
            case HEADER_VIEW:
                view = LayoutInflater.from(context).inflate(R.layout.header_rv_bill, parent, false);
                viewHolder = new HeaderItemViewHolder(view);
                break;
            default:
                view = LayoutInflater.from(context).inflate(R.layout.itemview_rv_bill, parent, false);
                viewHolder = new NormalItemViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HeaderItemViewHolder headerItemViewHolder;
        FooterItemViewHolder footerItemViewHolder;
        NormalItemViewHolder normalItemViewHolder;

        switch (getItemViewType(position)) {
            case HEADER_VIEW:
                headerItemViewHolder = (HeaderItemViewHolder) holder;
                headerItemViewHolder.deliveryDate.setText("موعد التسليم: " + order.getOrderDateDay() + " الساعة: " + order.getOrderDateHour(false));
                headerItemViewHolder.phoneNumber1.setText(ArabicString.toArabic("ت/ " + phones.get(0)));
                headerItemViewHolder.phoneNumber2.setText(ArabicString.toArabic("ت/ " + phones.get(1)));
                headerItemViewHolder.phoneNumber3.setText(ArabicString.toArabic("ت/ " + phones.get(2)));
                break;
            case FOOTER_VIEW:
                footerItemViewHolder = (FooterItemViewHolder) holder;
                double orderPrice = round(order.getOrderPrice() * 100) / 100.0;

                footerItemViewHolder.billPrice.setText(ArabicString.toArabic("اجمالى المنتجات: " + orderPrice + " جنيه"));
                footerItemViewHolder.fees.setText(ArabicString.toArabic("مصاريف الخدمة: " + this.fees + " جنيه"));
                footerItemViewHolder.totalPrice.setText(ArabicString.toArabic("إجمالي الفاتورة: " +  (orderPrice + this.fees) + " جنيه"));
                break;
            case NORMAL_VIEW:
                normalItemViewHolder = (NormalItemViewHolder) holder;
                OrderUnit orderUnit = data.get(position - 1);
                normalItemViewHolder.itemName.setText(ArabicString.toArabic(orderUnit.getProductName()));
                normalItemViewHolder.itemPrice.setText(ArabicString.toArabic(orderUnit.getProductPrice() + " جنيه/" + orderUnit.getProductUnitName()));
                normalItemViewHolder.itemTotalWeight.setText(ArabicString.toArabic(String.valueOf(orderUnit.getCount())));
                normalItemViewHolder.itemTotalPrice.setText(ArabicString.toArabic(String.valueOf(round(orderUnit.getCount() * orderUnit.getProductPrice() * 100) / 100.0)));
                Glide.with(holder.itemView)
                        .load(orderUnit.getProductImage())
                        .fitCenter()
                        .into(normalItemViewHolder.itemImage);

                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER_VIEW;
        else if (position == data.size() + 1)
            return FOOTER_VIEW;
        else
            return NORMAL_VIEW;
    }

    @Override
    public int getItemCount() {
        return data.size() + 2;
    }

    public void updateFees(double newFees) {
        this.fees = round(newFees * 100) / 100.0;
    }

    class NormalItemViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;
        TextView itemPrice;
        TextView itemTotalWeight;
        TextView itemTotalPrice;

        ImageView itemImage;

        public NormalItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.tv_item_name);
            itemPrice = itemView.findViewById(R.id.tv_item_price);
            itemTotalWeight = itemView.findViewById(R.id.tv_item_total_weight);
            itemTotalPrice = itemView.findViewById(R.id.tv_item_total_price);
            itemImage = itemView.findViewById(R.id.iv_item_image_bill);
        }

    }

    class FooterItemViewHolder extends RecyclerView.ViewHolder {

        TextView billPrice;
        TextView fees;
        TextView totalPrice;

        public FooterItemViewHolder(@NonNull View itemView) {
            super(itemView);
            billPrice = itemView.findViewById(R.id.tv_bill_price);
            fees      = itemView.findViewById(R.id.tv_delivery_fees);
            totalPrice = itemView.findViewById(R.id.tv_total_price);
        }
    }

    class HeaderItemViewHolder extends RecyclerView.ViewHolder {
        TextView deliveryDate;
        TextView phoneNumber1;
        TextView phoneNumber2;
        TextView phoneNumber3;

        public HeaderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            deliveryDate = itemView.findViewById(R.id.tv_delivery_date_bill);
            phoneNumber1 = itemView.findViewById(R.id.tv_phone_1_bill);
            phoneNumber2 = itemView.findViewById(R.id.tv_phone_2_bill);
            phoneNumber3 = itemView.findViewById(R.id.tv_phone_3_bill);
        }
    }
}
