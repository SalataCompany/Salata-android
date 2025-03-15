package com.ar.salata.repositories.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject implements Parcelable, Comparable<Category> {
	public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @PrimaryKey
    @SerializedName("id")
    private int categoryID;
    @SerializedName("name")
    private String categoryName;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("priority")
    private int priority;
    @SerializedName("level")
    private int level;
    @SerializedName("subCats")
    private RealmList<Category> subCats;

    public Category(int categoryID, String categoryName, int priority, int level) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.priority = priority;
        this.level = level;
        this.subCats = new RealmList<>();
    }

    protected Category(Parcel in) {
        categoryID = in.readInt();
        categoryName = in.readString();
    }

    public Category() {
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public RealmList<Category> getSubCats() {
        return subCats;
    }

    public void setSubCats(RealmList<Category> subCats) {
        this.subCats = subCats;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(categoryID);
        parcel.writeString(categoryName);
    }

    @Override
    public int compareTo(Category cat) {
        return Integer.compare(this.priority, cat.priority);
    }
}
