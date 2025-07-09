package com.ar.salata.repositories;

import androidx.lifecycle.MutableLiveData;

import com.ar.salata.repositories.API.SliderAPI;
import com.ar.salata.repositories.model.SliderItem;
import com.ar.salata.repositories.model.SliderItemList;

import java.util.List;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ar.salata.SalataApplication.BASEURL;

public class SliderItemRepository {
    private SliderAPI sliderAPI;

    public SliderItemRepository() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        sliderAPI = retrofit.create(SliderAPI.class);
    }

    public MutableLiveData<UserRepository.APIResponse> loadSliderItems() {
        MutableLiveData<UserRepository.APIResponse> apiResponse = new MutableLiveData<>();
        sliderAPI.getSliderItems().enqueue(new Callback<SliderItemList>() {
            @Override
            public void onResponse(Call<SliderItemList> call, Response<SliderItemList> response) {
                if (response.isSuccessful()) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(SliderItem.class).findAll().deleteAllFromRealm();
                            realm.insertOrUpdate(response.body().getList());
                        }
                    });
                    realm.close();
                    apiResponse.setValue(UserRepository.APIResponse.SUCCESS);
                } else {
                    apiResponse.setValue(UserRepository.APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<SliderItemList> call, Throwable t) {
                apiResponse.setValue(UserRepository.APIResponse.FAILED);
            }
        });
        return apiResponse;
    }

    public List<SliderItem> getSliderItems() {
        Realm realm = Realm.getDefaultInstance();
        List<SliderItem> items = realm.copyFromRealm(realm.where(SliderItem.class).findAll());
        realm.close();
        return items;
    }
}
