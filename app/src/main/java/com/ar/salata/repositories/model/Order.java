package com.ar.salata.repositories.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ar.salata.ui.utils.TimeFormats;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Order extends RealmObject implements Parcelable {

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    @PrimaryKey
    @SerializedName("id")
    private int orderId = -1;
    //    @SerializedName("order_date_day")
//    private String orderDateDay;
//    @SerializedName("order_date_hour")
    private String orderDateHourFrom;
    private String orderDateHourTo;
    @SerializedName("shift_id")
    private int shiftId;
    @SerializedName("address_id")
    private int addressId;
    @SerializedName("user_id")
    private int userId;
    private String deliveryDate;
    private RealmList<OrderUnit> units = new RealmList<>();
    @SerializedName("total_price")
    private double orderPrice = 0;
    //    @PrimaryKey
//    private String orderLocalId;
    @SerializedName("delivery_shift_from")
    private long deliveryShiftFrom;
    @SerializedName("delivery_shift_to")
    private long deliveryShiftTo;

    @SerializedName("delivery_date")
    private long orderDeliveryMS;
    @SerializedName("expiry")
    private long expiry;
    @SerializedName("brochure_image")
    private String brochureImage;
    @SerializedName("discount")
    private double discount;
    private boolean orderFulfilled;
    private boolean isSubmitted = false;

    //
    private String notes;
    @SerializedName("payment_type")
    private String paymentType;
    private String reference;
    @SerializedName("auth_id")
    private String authId;
    @SerializedName("delivery_fees")
    private Double deliveryFees;

    public Double getDeliveryFees() {
        return deliveryFees;
    }

    public void setDeliveryFees(Double deliveryFees) {
        this.deliveryFees = deliveryFees;
    }

    public Order() {
    }

    public Order(int shiftId,
                 int addressId,
                 int userId,
                 long deliveryDateMS,
                 String deliveryDate,
                 String deliveryHourFrom,
                 String deliveryHourTo) {

        this.shiftId = shiftId;
        this.addressId = addressId;
        this.userId = userId;
        this.deliveryDate = deliveryDate;
        this.orderDeliveryMS = deliveryDateMS;
        this.orderPrice = orderPrice;
        this.orderDateHourFrom = deliveryHourFrom;
        this.orderDateHourTo = deliveryHourTo;

//        this.orderLocalId = UUID.randomUUID().toString();
    }

    protected Order(Parcel in) {
        orderId = in.readInt();
//        orderDateDay = in.readString();
        orderDateHourFrom = in.readString();
        orderDateHourTo = in.readString();
        orderPrice = in.readDouble();
        shiftId = in.readInt();
        addressId = in.readInt();
        userId = in.readInt();
        deliveryDate = in.readString();
//        orderLocalId = in.readString();
        orderDeliveryMS = in.readLong();
        expiry = in.readLong();
        brochureImage = in.readString();
        discount = in.readDouble();
        isSubmitted = in.readByte() != 0;
//        orderImage = in.readInt();
        orderFulfilled = in.readByte() != 0;
        units = new RealmList<OrderUnit>();
        units.addAll(in.createTypedArrayList(OrderUnit.CREATOR));
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderDateDay() {
        return TimeFormats.convertToArabicString("E yyyy/MM/d", orderDeliveryMS);
    }

//    public void setOrderDateDay(String orderDateDay) {
//        this.orderDateDay = orderDateDay;
//    }

    public String getOrderDateHour(boolean fromLong) {
        if (fromLong || orderDateHourFrom == null)
            return TimeFormats.getArabicHour(deliveryShiftFrom) + " ~ " + TimeFormats.getArabicHour(deliveryShiftTo);
        else
            return orderDateHourFrom + " ~ " + orderDateHourTo;
    }

//    public void setOrderDateHour(String orderDateHour) {
//        this.orderDateHour = orderDateHour;
//    }

    public double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(double orderPrice) {
        this.orderPrice = orderPrice;
    }

//    public int getOrderImage() {
//        return orderImage;
//    }

//    public void setOrderImage(int orderImage) {
//        this.orderImage = orderImage;
//    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }


    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeliveryDate() {
        if (deliveryDate == null)
            return TimeFormats.convertToEnglishString("yyyy-MM-dd", orderDeliveryMS);
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public boolean isOrderFulfilled() {
        return orderFulfilled;
    }

    public void setOrderFulfilled(boolean orderFulfilled) {
        this.orderFulfilled = orderFulfilled;
    }

    public RealmList<OrderUnit> getUnits() {
        return units;
    }

    public void setUnits(List<OrderUnit> units) {
        this.units.addAll(units);
    }

    public void addUnit(OrderUnit unit) {
        for (OrderUnit u : this.units) {
            if (u.getProductId() == unit.getProductId()) {
                units.remove(u);
                break;
            }
        }

        if (unit.getCount() != 0) {
            this.units.add(unit);
        }

        orderPrice = 0;
        for (OrderUnit u : this.units) {
            orderPrice += u.getCount() * u.getProductPrice();
        }
    }

//    public String getOrderLocalId() {
//        return orderLocalId;
//    }
//
//    public void setOrderLocalId(String orderLocalId) {
//        this.orderLocalId = orderLocalId;
//    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public long getOrderDeliveryMS() {
        return orderDeliveryMS;
    }

    public void setOrderDeliveryMS(long orderDeliveryMS) {
        this.orderDeliveryMS = orderDeliveryMS;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public boolean isModifiable() {
        Date now = new Date();
        Date expiry = new Date(getExpiry() * 1000);
        if (now.compareTo(expiry) < 0) {
            return true;
        } else {
            return false;
        }
    }

    public long getDeliveryShiftFrom() {
        return deliveryShiftFrom;
    }

    public void setDeliveryShiftFrom(long deliveryShiftFrom) {
        this.deliveryShiftFrom = deliveryShiftFrom;
    }

    public long getDeliveryShiftTo() {
        return deliveryShiftTo;
    }

    public void setDeliveryShiftTo(long deliveryShiftTo) {
        this.deliveryShiftTo = deliveryShiftTo;
    }

    public String getBrochureImage() {
        return brochureImage;
    }

    public void setBrochureImage(String brochureImage) {
        this.brochureImage = brochureImage;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(orderId);
//        dest.writeString(orderDateDay);
        dest.writeString(orderDateHourFrom);
        dest.writeString(orderDateHourTo);
        dest.writeDouble(orderPrice);
        dest.writeInt(shiftId);
        dest.writeInt(addressId);
        dest.writeInt(userId);
        dest.writeString(deliveryDate);
//        dest.writeString(orderLocalId);
        dest.writeLong(orderDeliveryMS);
        dest.writeLong(expiry);
        dest.writeString(brochureImage);
        dest.writeDouble(discount);
        dest.writeByte((byte) (isSubmitted ? 1 : 0));
//        dest.writeInt(orderImage);
        dest.writeByte((byte) (orderFulfilled ? 1 : 0));
        dest.writeTypedList(units);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        for (OrderUnit unit : getUnits()) {
            if (!order.getUnits().contains(unit)) return false;
        }
        for (OrderUnit unit : order.getUnits()) {
            if (!getUnits().contains(unit)) return false;
        }
        return true;
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(getUnits());
//    }
}
