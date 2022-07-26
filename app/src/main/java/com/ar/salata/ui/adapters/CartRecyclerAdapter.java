package com.ar.salata.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.salata.R;
import com.ar.salata.repositories.model.APIToken;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.repositories.model.PaginatedResponse;
import com.ar.salata.repositories.model.StockProduct;
import com.ar.salata.repositories.model.StockProductList;
import com.ar.salata.ui.activities.AddToCartActivity;
import com.ar.salata.ui.activities.BaseActivity;
import com.ar.salata.ui.activities.OrderEditActivity;
import com.ar.salata.ui.fragments.ErrorDialogFragment;
import com.ar.salata.ui.fragments.LoadingDialogFragment;
import com.ar.salata.ui.utils.ArabicString;
import com.ar.salata.viewmodels.GoodsViewModel;
import com.ar.salata.viewmodels.OrderViewModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

public class CartRecyclerAdapter extends RecyclerView.Adapter {
    private final static int HEADER_VIEW = 1;
    private final static int NORMAL_VIEW = 0;

    private GoodsViewModel goodsViewModel;
    private ArrayList<StockProduct> products;
    private Context context;
    private OrderViewModel orderViewModel;
    private Order order;
    private List<String> phones;
    private String mode;
    private APIToken token;
    private int catId;
    private PaginatedResponse.Links links;
    private int page = 1;

    public CartRecyclerAdapter(Context context, StockProductList products, OrderViewModel orderViewModel, List<String> phones, String mode, APIToken token, int id) {
        this.products = (ArrayList<StockProduct>) products.getProductList();
        this.context = context;
        this.orderViewModel = orderViewModel;
        order = this.orderViewModel.getOrderMutableLiveData().getValue();
        this.phones = phones;
        this.mode = mode;
        this.token  = token;
        this.catId = id;
        links = products.getLinks();
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
                itemViewHolderHeader.deliveryDate.setText("موعد التسليم: " + order.getOrderDateDay() + " الساعة: " + order.getOrderDateHour(false));
                itemViewHolderHeader.phoneNumber1.setText(ArabicString.toArabic("ت/ " + phones.get(0)));
                itemViewHolderHeader.phoneNumber2.setText(ArabicString.toArabic("ت/ " + phones.get(1)));
                itemViewHolderHeader.phoneNumber3.setText(ArabicString.toArabic("ت/ " + phones.get(2)));
                break;
            }
            case NORMAL_VIEW: {
                goodsViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(GoodsViewModel.class);
                if((position+1) == products.size() && links.getNextPageUrl()!= null) {
                    // Loading...
                    LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                    loadingDialogFragment.show(((BaseActivity) context).getSupportFragmentManager(), null);
                    // Calling next page
                    goodsViewModel.loadProductsWithCategory(token, orderViewModel.getOrderMutableLiveData().getValue().getAddressId(), catId, ++page).observe((LifecycleOwner) context, new Observer<StockProductList>() {
                        @Override
                        public void onChanged(StockProductList stockProductList) {
                            loadingDialogFragment.dismiss();
                            if(stockProductList!=null) {
                                if (context instanceof AddToCartActivity) {
                                    for (StockProduct product : stockProductList.getProductList()) {
                                        if (product.getRemain() > 0)
                                            products.add(product);
                                    }
                                } else if (context instanceof OrderEditActivity) {
                                    Order order = orderViewModel.getOrderMutableLiveData().getValue();
                                    ArrayList<Integer> ids = new ArrayList<>();
                                    for (OrderUnit unit : order.getUnits()) {
                                        ids.add(unit.getProductId());
                                    }
                                    for (StockProduct stockProduct : stockProductList.getProductList()) {
                                        if (stockProduct.getRemain() > 0 || ids.contains(stockProduct.getId())) {
                                            products.add(stockProduct);
                                        }
                                    }
                                }
                                links = stockProductList.getLinks();
                                notifyDataSetChanged();
                            }else{
                                ErrorDialogFragment dialogFragment =
                                        new ErrorDialogFragment("حدث خطأ", "يوجد مشكلة فى الاتصال بالانترنت", true);
                                dialogFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), null);
                            }
                        }
                    });
                }

                final ItemViewHolderNormal itemViewHolderNormal = (ItemViewHolderNormal) holder;
                final Double itemPrice = products.get(position - 1).getPrice();
                final String itemImageURL = products.get(position - 1).getInvoiceImage();
                final String itemUnit = products.get(position - 1).getUnitName();

                double orderUnitRemain = 0;
                for (OrderUnit tempUnit : order.getUnits()) {
                    if (products.get(position - 1).getId() == tempUnit.getProductId()) {
                        products.get(position-1).setWeight(tempUnit.getCount());
                        orderUnitRemain = tempUnit.getCount() + products.get(position - 1).getRemain();
                        break;
                    }
                }

                itemViewHolderNormal.itemNameTextView.setText(ArabicString.toArabic(products.get(position - 1).getProductName()));
                itemViewHolderNormal.itemPriceTextView.setText(ArabicString.toArabic(itemPrice.toString() + " جنيه/" + itemUnit));

                Glide.with(holder.itemView)
                        .load(itemImageURL)
                        .fitCenter()
                        .into(itemViewHolderNormal.itemImage);


                itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(products.get(position-1).getWeight())));
                itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(round(products.get(position-1).getWeight() * itemPrice * 100) / 100.0)));

                itemViewHolderNormal.decrementImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StockProduct stockProduct = products.get(position-1);
                        if (stockProduct.getWeight() > 0) {
                            stockProduct.setWeight(stockProduct.getWeight() - stockProduct.getStep());
                            itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(stockProduct.getWeight())));
                            itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(round(stockProduct.getWeight() * itemPrice * 100) / 100.0)));
                            order.addUnit(new OrderUnit(stockProduct, stockProduct.getWeight()));
                            orderViewModel.setOrderValue(order);
                        }
                    }
                });

                double finalOrderUnitRemain = orderUnitRemain;
                itemViewHolderNormal.incrementImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StockProduct stockProduct = products.get(position-1);
                        if (mode == "create") {
                            if (!(stockProduct.getWeight() + products.get(position - 1).getStep() > products.get(position - 1).getRemain())) {
                                stockProduct.setWeight(stockProduct.getWeight() + stockProduct.getStep());
                                itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(stockProduct.getWeight())));
                                itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(round(stockProduct.getWeight() * itemPrice * 100) / 100.0)));
                                order.addUnit(new OrderUnit(stockProduct, stockProduct.getWeight()));
                                orderViewModel.setOrderValue(order);
                            }
                        } else if (mode == "edit") {
                            if (!(stockProduct.getWeight() + products.get(position - 1).getStep() > finalOrderUnitRemain)) {
                                stockProduct.setWeight(stockProduct.getWeight() + products.get(position - 1).getStep());
                                itemViewHolderNormal.totalWeightTextView.setText(ArabicString.toArabic(String.valueOf(stockProduct.getWeight())));
                                itemViewHolderNormal.totalPriceTextView.setText(ArabicString.toArabic(String.valueOf(round(stockProduct.getWeight() * itemPrice * 100) / 100.0)));
                                order.addUnit(new OrderUnit(products.get(position - 1), products.get(position-1).getWeight()));
                                orderViewModel.setOrderValue(order);
                            }
                        }
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
    public long getItemId(int position) {
        if(products.size()==0)
            return super.getItemId(position);
        else {
            try {
                Log.e("CartRecyclerAdapter", "Cat Id: " + products.get(position).getCategoryId() + "position: " + position);
                return products.get(position).getId();
            }catch (IndexOutOfBoundsException e){
                return products.get(position-1).getId();
            }
        }

    }

    @Override
    public int getItemCount() {
        return products.size()+1;
    }

    class ItemViewHolderNormal extends RecyclerView.ViewHolder {
  //      double weight = 0;
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
        TextView phoneNumber3;

        public ItemViewHolderHeader(@NonNull View itemView) {
            super(itemView);
            deliveryDate = itemView.findViewById(R.id.tv_delivery_date_add_to_cart);
            phoneNumber1 = itemView.findViewById(R.id.tv_phone_1_add_to_cart);
            phoneNumber2 = itemView.findViewById(R.id.tv_phone_2_add_to_cart);
            phoneNumber3 = itemView.findViewById(R.id.tv_phone_3_add_to_cart);
        }
    }
}
