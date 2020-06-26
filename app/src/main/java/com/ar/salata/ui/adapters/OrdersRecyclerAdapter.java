package com.ar.salata.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.salata.R;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.ui.activities.OrderDetailActivity;
import com.ar.salata.ui.activities.OrderPreviewActivity;
import com.ar.salata.ui.activities.OrdersActivity;
import com.ar.salata.ui.fragments.ErrorDialogFragment;
import com.ar.salata.ui.fragments.OrderEditConfirmationDialogFragment;
import com.ar.salata.ui.utils.ArabicString;
import com.ar.salata.viewmodels.OrderViewModel;
import com.ar.salata.viewmodels.UserViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import static com.ar.salata.viewmodels.OrderViewModel.ORDER_ID;

public class OrdersRecyclerAdapter extends RecyclerView.Adapter {
    private final OrderViewModel orderViewModel;
    private final UserViewModel userViewModel;
    private boolean upcomingOrder;
    private Context context;
    private ArrayList<Order> orders;

    public OrdersRecyclerAdapter(Context context, ArrayList<Order> orders, OrderViewModel orderViewModel, UserViewModel userViewModel, boolean upcomingOrder) {
        this.upcomingOrder = upcomingOrder;
        this.context = context;
        this.orders = orders;
        this.orderViewModel = orderViewModel;
        this.userViewModel = userViewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        if (upcomingOrder) {
            view = LayoutInflater.from(context).inflate(R.layout.itemview_rv_upcoming_orders, parent, false);
            viewHolder = new ItemViewHolderUpcoming(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.itemview_rv_previous_orders, parent, false);
            viewHolder = new ItemViewHolderPrevious(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewHolderUpcoming viewHolderUpcoming = (ItemViewHolderUpcoming) holder;

        viewHolderUpcoming.orderId.setText(ArabicString.toArabic(String.valueOf(orders.get(position).getOrderId())));
        viewHolderUpcoming.orderDateDay.setText(ArabicString.toArabic(orders.get(position).getOrderDateDay()));
        viewHolderUpcoming.orderDateHour.setText(ArabicString.toArabic(orders.get(position).getOrderDateHour(true)));
        viewHolderUpcoming.orderPrice.setText(ArabicString.toArabic(String.valueOf(orders.get(position).getOrderPrice())));

//        viewHolderUpcoming.orderImage.setImageResource(orders.get(position).getOrderImage());
        Glide.with(holder.itemView)
                .load(orders.get(position).getBrochureImage())
                .fitCenter()
                .into(viewHolderUpcoming.orderImage);


        if (!upcomingOrder) {
            ItemViewHolderPrevious viewHolderPrevious;
            viewHolderPrevious = (ItemViewHolderPrevious) holder;
            viewHolderPrevious.orderDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, OrderDetailActivity.class);
                    intent.putExtra(ORDER_ID, orders.get(position).getOrderId());
                    context.startActivity(intent);
                }
            });
        } else {
            viewHolderUpcoming.editOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, OrderPreviewActivity.class);
                    intent.putExtra(ORDER_ID, orders.get(position).getOrderId());
                    context.startActivity(intent);
                }
            });
            viewHolderUpcoming.cancelOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof OrdersActivity) {
                        if (orders.get(position).isModifiable()) {
                            OrderEditConfirmationDialogFragment confirmationDialogFragment =
                                    OrderEditConfirmationDialogFragment.newInstance("تأكيد الالغاء",
                                            "هل تود الغاء الطلب رقم " + orders.get(position).getOrderId(),
                                            orders.get(position).getOrderId());
                            confirmationDialogFragment.show(((OrdersActivity) context).getSupportFragmentManager(), null);
//                            orderViewModel.deleteOrder(userViewModel.getToken(), orders.get(position).getOrderId());
//                            orders.remove(position);
//                            notifyDataSetChanged();
                        } else {
                            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment("خطا", "لا يمكن تعديل الطلب", false);
                            errorDialogFragment.show(((OrdersActivity) context).getSupportFragmentManager(), null);
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class ItemViewHolderUpcoming extends RecyclerView.ViewHolder {
        TextView orderId;
        TextView orderDateDay;
        TextView orderDateHour;
        TextView orderPrice;

        ImageView orderImage;

        private MaterialButton editOrder;
        private MaterialButton cancelOrder;

        ItemViewHolderUpcoming(@NonNull View itemView, boolean notDefault) {
            super(itemView);
        }

        ItemViewHolderUpcoming(@NonNull View itemView) {
            super(itemView);

            orderId = itemView.findViewById(R.id.tv_order_id_value);
            orderDateDay = itemView.findViewById(R.id.tv_day);
            orderDateHour = itemView.findViewById(R.id.tv_hour_value);
            orderPrice = itemView.findViewById(R.id.tv_order_price_value);

            orderImage = itemView.findViewById(R.id.iv_order_image);

            editOrder = itemView.findViewById(R.id.btn_order_edit);
            cancelOrder = itemView.findViewById(R.id.btn_order_cancel);
        }
    }

    public class ItemViewHolderPrevious extends ItemViewHolderUpcoming {
        private MaterialButton orderDetails;

        ItemViewHolderPrevious(@NonNull View itemView) {
            super(itemView, false);

            orderId = itemView.findViewById(R.id.tv_order_id_value_previous);
            orderDateDay = itemView.findViewById(R.id.tv_day_previous);
            orderDateHour = itemView.findViewById(R.id.tv_hour_value_previous);
            orderPrice = itemView.findViewById(R.id.tv_order_price_value_previous);

            orderImage = itemView.findViewById(R.id.iv_order_image_previous);

            orderDetails = itemView.findViewById(R.id.btn_order_details);
        }
    }
}
