package com.ar.salata.repositories;

import androidx.lifecycle.MutableLiveData;

import com.ar.salata.repositories.API.GoodsAPI;
import com.ar.salata.repositories.model.APIToken;
import com.ar.salata.repositories.model.Category;
import com.ar.salata.repositories.model.CategoryList;
import com.ar.salata.repositories.model.Product;
import com.ar.salata.repositories.model.ProductList;
import com.ar.salata.repositories.model.StockProduct;
import com.ar.salata.repositories.model.StockProductList;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ar.salata.SalataApplication.BASEURL;
import static com.ar.salata.repositories.UserRepository.APIResponse.ERROR;
import static com.ar.salata.repositories.UserRepository.APIResponse.FAILED;
import static com.ar.salata.repositories.UserRepository.APIResponse.NULL;
import static com.ar.salata.repositories.UserRepository.APIResponse.SUCCESS;

import android.util.Log;

public class GoodsRepository {
    private GoodsAPI goodsAPI;
    private RealmResults<Category> resultsCategory;
    private RealmResults<StockProduct> resultsStockProduct;
    private RealmResults<Product> resultsProduct;

    public GoodsRepository() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        goodsAPI = retrofit.create(GoodsAPI.class);
    }

    public MutableLiveData<UserRepository.APIResponse> loadCategories() {
        MutableLiveData<UserRepository.APIResponse> apiResponse = new MutableLiveData<>(NULL);
        goodsAPI.getCategories().enqueue(new Callback<CategoryList>() {
            @Override
            public void onResponse(Call<CategoryList> call, Response<CategoryList> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(Category.class).findAll().deleteAllFromRealm();
                            List<Category> sortedCats = response.body().getCategoryList();
                            Collections.sort(sortedCats);
                            realm.insertOrUpdate(sortedCats);
                        }
                    });
                    realm.close();
                    apiResponse.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    apiResponse.setValue(UserRepository.APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<CategoryList> call, Throwable t) {
                apiResponse.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return apiResponse;
    }

    public MutableLiveData<CategoryList> getCategories() {
        MutableLiveData<CategoryList> categoryList = new MutableLiveData<>();
        Realm realm = Realm.getDefaultInstance();
        resultsCategory = realm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> categories) {
                List<Category> list = realm.copyFromRealm(categories);
                categoryList.setValue(new CategoryList(list));
            }
        });
        return categoryList;
    }

    public MutableLiveData<UserRepository.APIResponse> loadProducts(APIToken token, int addressId) {
        MutableLiveData<UserRepository.APIResponse> apiResponse = new MutableLiveData<>(NULL);
        goodsAPI.getAllProducts(token.toString(), addressId).enqueue(new Callback<StockProductList>() {
            @Override
            public void onResponse(Call<StockProductList> call, Response<StockProductList> response) {
                if (response.isSuccessful()) {
//                    Log.e("test1",response.body().getProductList().toString());
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(StockProduct.class).findAll().deleteAllFromRealm();
                            for (StockProduct product : response.body().getProductList()) {
                                product.setAddressId(addressId);
                            }
                            realm.insertOrUpdate(response.body().getProductList());
                        }
                    });
                    realm.close();
                    apiResponse.setValue(SUCCESS);
                } else {
                    apiResponse.setValue(ERROR);
                }
            }

            @Override
            public void onFailure(Call<StockProductList> call, Throwable t) {
                apiResponse.setValue(FAILED);
            }
        });
        return apiResponse;
    }

    public MutableLiveData<StockProductList> loadProductsWithCategory(APIToken token, int addressId, int catID, int page){
        MutableLiveData<StockProductList> products = new MutableLiveData<>();
        goodsAPI.getAllProductsWithCategory(token.toString(), addressId, catID, page).enqueue(new Callback<StockProductList>() {
            @Override
            public void onResponse(Call<StockProductList> call, Response<StockProductList> response) {
                products.setValue(response.body());
            }

            @Override
            public void onFailure(Call<StockProductList> call, Throwable t) {
                products.setValue(null);
                Log.e("Goods Repository", t.toString());
            }
        });
        return products;
    }

    public MutableLiveData<UserRepository.APIResponse> loadProducts() {
        MutableLiveData<UserRepository.APIResponse> apiResponse = new MutableLiveData<>(NULL);
        goodsAPI.getAllProducts().enqueue(new Callback<ProductList>() {
            @Override
            public void onResponse(Call<ProductList> call, Response<ProductList> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(Product.class).findAll().deleteAllFromRealm();
                            realm.insertOrUpdate(response.body().getProductList());
                        }
                    });
                    realm.close();
                    apiResponse.setValue(SUCCESS);
                } else {
                    apiResponse.setValue(ERROR);
                }
            }

            @Override
            public void onFailure(Call<ProductList> call, Throwable t) {
                apiResponse.setValue(FAILED);
            }
        });
        return apiResponse;
    }

    public MutableLiveData<StockProductList> getProducts(int categoryId, int addressId) {
        MutableLiveData<StockProductList> productList = new MutableLiveData<>();
        Realm realm = Realm.getDefaultInstance();
        resultsStockProduct = realm.where(StockProduct.class)
                .equalTo("categoryId", categoryId)
                .and()
                .equalTo("addressId", addressId)
                .findAllAsync();
        resultsStockProduct.addChangeListener(new RealmChangeListener<RealmResults<StockProduct>>() {
            @Override
            public void onChange(RealmResults<StockProduct> products) {
                productList.setValue(new StockProductList(realm.copyFromRealm(products)));
                resultsStockProduct.removeAllChangeListeners();
            }
        });
        return productList;
    }

    public MutableLiveData<ProductList> getProducts(int categoryId) {
        MutableLiveData<ProductList> productList = new MutableLiveData<>();
        Realm realm = Realm.getDefaultInstance();
        resultsProduct = realm.where(Product.class)
                .equalTo("categoryId", categoryId)
                .findAllAsync();
        resultsProduct.addChangeListener(new RealmChangeListener<RealmResults<Product>>() {
            @Override
            public void onChange(RealmResults<Product> products) {
                productList.setValue(new ProductList(realm.copyFromRealm(products)));
            }
        });
        return productList;
    }
}
