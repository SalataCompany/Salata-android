package com.ar.salata.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.ar.salata.repositories.API.UserAPI;
import com.ar.salata.repositories.model.APIToken;
import com.ar.salata.repositories.model.AuthenticationUser;
import com.ar.salata.repositories.model.Order;
import com.ar.salata.repositories.model.OrderUnit;
import com.ar.salata.repositories.model.ResponseMessage;
import com.ar.salata.repositories.model.User;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ar.salata.SalataApplication.BASEURL;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private UserAPI userAPI;

    public UserRepository() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        userAPI = retrofit.create(UserAPI.class);
    }

    public User getUser() {
        Realm realm = Realm.getDefaultInstance();
        User user = realm.where(User.class).findFirst();
        if (user != null) user = realm.copyFromRealm(user);
        realm.close();
        return user;
    }

    public MutableLiveData<APIResponse> getUser(APIToken token) {
        MutableLiveData<APIResponse> apiResponse = new MutableLiveData<>(APIResponse.NULL);
        userAPI.getUser(token.toString()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(User.class).findAll().deleteAllFromRealm();
                            realm.insert(user);
                        }
                    });
                    apiResponse.setValue(APIResponse.SUCCESS);
                    realm.close();
                } else {
                    apiResponse.setValue(APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                apiResponse.setValue(APIResponse.FAILED);
            }
        });
        return apiResponse;
    }

    public APIToken getToken() {
        Realm realm = Realm.getDefaultInstance();
        APIToken token = realm.where(APIToken.class).findFirst();
        if (token != null) token = realm.copyFromRealm(token);
        realm.close();
        return token;
    }

    public MutableLiveData<APIResponse> signOut(APIToken token) {
        MutableLiveData<APIResponse> apiResponse = new MutableLiveData<>(APIResponse.NULL);
        userAPI.logout(token.toString()).enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    clearUserDate();
                    apiResponse.setValue(APIResponse.SUCCESS);
                } else {
                    apiResponse.setValue(APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                apiResponse.setValue(APIResponse.FAILED);
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
        return apiResponse;
    }

    public void clearUserDate() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(User.class).findAll().deleteAllFromRealm();
                realm.where(APIToken.class).findAll().deleteAllFromRealm();
                realm.where(Order.class).findAll().deleteAllFromRealm();
            }
        });
        realm.close();
    }

    public MutableLiveData<Authentication> registerUser(AuthenticationUser user) {
        final MutableLiveData<Authentication> authenticationState = new MutableLiveData<>(Authentication.NOT_AUTHENTICATED);
        userAPI.registerUser(user.getPhoneNumber(),
                user.getName(),
                user.getPassword(),
                user.getPasswordConfirmation(),
                user.getZoneID(),
                user.getAddress())
                .enqueue(new Callback<APIToken>() {
                    @Override
                    public void onResponse(Call<APIToken> call, Response<APIToken> response) {
                        if (response.isSuccessful()) {
                            APIToken token = response.body();
                            authenticationState.setValue(Authentication.AUTHENTICATED);

                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            realm.where(APIToken.class).findAll().deleteAllFromRealm();
                            realm.where(Order.class).findAll().deleteAllFromRealm();
                            realm.where(OrderUnit.class).findAll().deleteAllFromRealm();
                            realm.insert(token);
                            realm.commitTransaction();
                            realm.close();

                        } else {
                            authenticationState.setValue(Authentication.AUTHENTICATION_ERROR);
                        }
                    }

                    @Override
                    public void onFailure(Call<APIToken> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                        authenticationState.setValue(Authentication.AUTHENTICATION_FAILED);
                    }
                });
        return authenticationState;
    }

    public MutableLiveData<Authentication> loginUser(AuthenticationUser user) {
        final MutableLiveData<Authentication> authenticationState = new MutableLiveData<>(Authentication.NOT_AUTHENTICATED);
        Realm realm = Realm.getDefaultInstance();
        User temp = realm.where(User.class).equalTo("phoneNumber", user.getPhoneNumber()).findFirst();
        if (temp == null) {
            userAPI.loginUser(user.getPhoneNumber(), user.getPassword())
                    .enqueue(new Callback<APIToken>() {
                        @Override
                        public void onResponse(Call<APIToken> call, Response<APIToken> response) {
                            if (response.isSuccessful()) {
                                APIToken token = response.body();
                                authenticationState.setValue(Authentication.AUTHENTICATED);

                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                realm.where(APIToken.class).findAll().deleteAllFromRealm();
                                realm.where(Order.class).findAll().deleteAllFromRealm();
                                realm.where(OrderUnit.class).findAll().deleteAllFromRealm();
                                realm.insert(token);
                                realm.commitTransaction();
                                realm.close();

                            } else {
                                // TODO: 5/17/2020 return error dependant on error code
                                authenticationState.setValue(Authentication.AUTHENTICATION_ERROR);
                            }
                        }

                        @Override
                        public void onFailure(Call<APIToken> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                            authenticationState.setValue(Authentication.AUTHENTICATION_FAILED);
                        }
                    });
        }
        realm.close();
        return authenticationState;
    }

//	public void clearUser() {
//		Realm realm = Realm.getDefaultInstance();
//		realm.beginTransaction();
//		realm.where(User.class).findAll().deleteAllFromRealm();
//		realm.where(APIToken.class).findAll().deleteAllFromRealm();
//		realm.deleteAll();
//		realm.commitTransaction();
//		realm.close();
//	}

    public MutableLiveData<APIResponse> updateUser(APIToken token, String name, String phone) {
        MutableLiveData<APIResponse> result = new MutableLiveData<>();
        userAPI.updateUser(token.toString(), phone, name).enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    result.setValue(APIResponse.SUCCESS);
                } else {
                    result.setValue(APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                result.setValue(APIResponse.FAILED);
            }
        });
        return result;
    }

    public MutableLiveData<APIResponse> updatePassword(APIToken token, String currentPassword, String password, String passwordConfirmation) {
        MutableLiveData<APIResponse> result = new MutableLiveData<>();
        userAPI.updatePassword(token.toString(), currentPassword, password, passwordConfirmation).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    clearUserDate();
                    result.setValue(APIResponse.SUCCESS);
                } else {
                    result.setValue(APIResponse.ERROR);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                result.setValue(APIResponse.FAILED);
            }
        });
        return result;
    }

    public enum Authentication {
        AUTHENTICATED, NOT_AUTHENTICATED, AUTHENTICATION_FAILED, AUTHENTICATION_ERROR
    }

    public enum APIResponse {
        SUCCESS, ERROR, FAILED, NULL
    }
}
