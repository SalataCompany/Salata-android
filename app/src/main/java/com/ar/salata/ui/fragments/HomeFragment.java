package com.ar.salata.ui.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ar.salata.R;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.User;
import com.ar.salata.ui.activities.AddAddressActivity;
import com.ar.salata.ui.activities.AddToCartActivity;
import com.ar.salata.ui.activities.OrdersActivity;
import com.ar.salata.ui.activities.SignInActivity;
import com.ar.salata.ui.activities.SignUpActivity;
import com.ar.salata.viewmodels.AddressViewModel;
import com.ar.salata.viewmodels.UserViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import static android.app.Activity.RESULT_OK;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final int DIALOGREQUESTCODE = 1;
    public static final String USER_ID = "UserId";
    private ExtendedFloatingActionButton eFABWeigh;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private LinearLayout loggedInLinearLayout;
    private LinearLayout loggedOutLinearLayout;

    private UserViewModel userViewModel;
    private AddressViewModel addressViewModel;
    private Map<Integer, Boolean> fabStates = new HashMap<>();

    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == DIALOGREQUESTCODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getContext(), AddToCartActivity.class);
            intent.putExtras(data);
            intent.putExtra(USER_ID, userViewModel.getUser().getId());
            startActivity(intent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) return null;

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        View view = inflater.inflate(R.layout.fragment_home, container);
        loggedInLinearLayout = view.findViewById(R.id.logged_in_ll);
        loggedOutLinearLayout = view.findViewById(R.id.logged_out_ll);

        eFABWeigh = view.findViewById(R.id.efab_weigh);
        eFABWeigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = userViewModel.getUser();
                if (user != null) {
                    LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                    loadingDialogFragment.show(getActivity().getSupportFragmentManager(), null);

                    MutableLiveData<UserRepository.APIResponse> addressResponse = addressViewModel.loadAddresses(userViewModel.getToken());
                    addressResponse.observe(HomeFragment.this.getActivity(), new Observer<UserRepository.APIResponse>() {
                        @Override
                        public void onChanged(UserRepository.APIResponse apiResponse) {
                            switch (apiResponse) {
                                case ERROR: {
                                    loadingDialogFragment.dismiss();
                                    ErrorDialogFragment dialogFragment =
                                            new ErrorDialogFragment("حدث خطأ", "فشلت عملية تحميل بيانات المستخدم", false);
                                    dialogFragment.show(HomeFragment.this.getActivity().getSupportFragmentManager(), null);
                                    break;
                                }
                                case FAILED: {
                                    loadingDialogFragment.dismiss();
                                    ErrorDialogFragment dialogFragment =
                                            new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), false);
                                    dialogFragment.show(HomeFragment.this.getActivity().getSupportFragmentManager(), null);
                                    break;
                                }
                                case SUCCESS: {
                                    loadingDialogFragment.dismiss();
                                    ChooseAddressDialogFragment dialogFragment = new ChooseAddressDialogFragment(userViewModel.getUser().getAddresses(), userViewModel.getToken());
                                    dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                                    dialogFragment.setTargetFragment(HomeFragment.this, DIALOGREQUESTCODE);
                                    break;
                                }
                            }
                        }
                    });
                } else {
                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment(
                            "انت لست مسجلا",
                            "من فضلك سجل الدخول او قم باضافة مستخدم جديد",
                            false);
                    errorDialogFragment.show(getActivity().getSupportFragmentManager(), null);
                }
            }
        });

        ExtendedFloatingActionButton myOrdersFab = view.findViewById(R.id.my_orders_fab);
        ExtendedFloatingActionButton addAddressFab = view.findViewById(R.id.add_address_fab);
        ExtendedFloatingActionButton signOutFab = view.findViewById(R.id.sign_out_fab);

        ExtendedFloatingActionButton signUpFab = view.findViewById(R.id.sign_up_fab);
        ExtendedFloatingActionButton loginFab = view.findViewById(R.id.login_fab);

        // Initialize all buttons in shrunk state
        myOrdersFab.shrink();
        addAddressFab.shrink();
        signOutFab.shrink();

        signUpFab.shrink();
        loginFab.shrink();

        fabStates.put(myOrdersFab.getId(), false);
        fabStates.put(addAddressFab.getId(), false);
        fabStates.put(signOutFab.getId(), false);

        fabStates.put(signUpFab.getId(), false);
        fabStates.put(loginFab.getId(), false);

        setupFabClick(myOrdersFab);
        setupFabClick(addAddressFab);
        setupFabClick(signOutFab);

        setupFabClick(signUpFab);
        setupFabClick(loginFab);

        toolbar = view.findViewById(R.id.toolbar_home);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        setDrawer();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDrawer() {
        navigationView = getActivity().findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) getActivity());

        drawer = getActivity().findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorAccent));
        toggle.syncState();

    }

    public void setEFABVisibility(boolean isVisible) {
        if (isVisible)
            eFABWeigh.show();
        else
            eFABWeigh.hide();
    }

    private void setupFabClick(ExtendedFloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = fabStates.get(fab.getId());

                if (isExpanded) {
                    // Perform action when clicking the expanded button
                    fabButtonsClicked(fab.getId());
                }

                // Toggle state
                fabStates.put(fab.getId(), !isExpanded);

                if (fabStates.get(fab.getId())) {
                    fab.extend();  // Expand to show text
                } else {
                    fab.shrink();  // Shrink to show only icon
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(userViewModel.getToken() == null){
            loggedInLinearLayout.setVisibility(View.GONE);
            loggedOutLinearLayout.setVisibility(View.VISIBLE);
        }else{
            loggedInLinearLayout.setVisibility(View.VISIBLE);
            loggedOutLinearLayout.setVisibility(View.GONE);
        }
    }

    public void fabButtonsClicked(int fabId){
        Intent intent = null;

        if (fabId == R.id.my_orders_fab) {
            intent = new Intent(getContext(), OrdersActivity.class);
        } else if (fabId == R.id.add_address_fab) {
            intent = new Intent(getContext(), AddAddressActivity.class);
        } else if (fabId == R.id.sign_up_fab) {
            intent = new Intent(getContext(), SignUpActivity.class);
        } else if (fabId == R.id.login_fab) {
            intent = new Intent(getContext(), SignInActivity.class);
        } else if (fabId == R.id.sign_out_fab) {
            LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(getActivity().getSupportFragmentManager(), null);

            MutableLiveData<UserRepository.APIResponse> response = userViewModel.signOut(userViewModel.getToken());
            response.observe(this, new Observer<UserRepository.APIResponse>() {
                @Override
                public void onChanged(UserRepository.APIResponse apiResponse) {
                    switch (apiResponse) {
                        case SUCCESS:
                            loadingDialogFragment.dismiss();
                            getActivity().finish();
                            startActivity(getActivity().getIntent());
                            break;
                        case FAILED: {
                            loadingDialogFragment.dismiss();
                            ErrorDialogFragment dialogFragment =
                                    new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), false);
                            dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                            break;
                        }
                        case ERROR: {
                            loadingDialogFragment.dismiss();
                            userViewModel.clearUser();
                            break;
                        }
                    }
                    drawer.closeDrawer(GravityCompat.START);
                }
            });
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
