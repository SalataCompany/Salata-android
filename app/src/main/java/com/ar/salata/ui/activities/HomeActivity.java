package com.ar.salata.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ar.salata.R;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.User;
import com.ar.salata.ui.fragments.ErrorDialogFragment;
import com.ar.salata.ui.fragments.HomeFragment;
import com.ar.salata.ui.fragments.LoadingDialogFragment;
import com.ar.salata.ui.utils.ArabicString;
import com.ar.salata.viewmodels.AppConfigViewModel;
import com.ar.salata.viewmodels.UserViewModel;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private HomeFragment homeFragment;
    private UserViewModel userViewModel;
    private AppConfigViewModel appConfigViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        appConfigViewModel = new AppConfigViewModel();

        setContentView(R.layout.activity_home);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        //setDrawer();

        fragmentManager = getSupportFragmentManager();
        homeFragment = HomeFragment.newInstance();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_home, homeFragment)
                .commit();

    }
/*
    private void setDrawer() {
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getHeaderView(0).findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        drawer = findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.setDrawerIndicatorEnabled(true);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorAccent));
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                navigationView.setCheckedItem(R.id.nav_main);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }
*/
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.nav_main:
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_orders:
                intent = new Intent(HomeActivity.this, OrdersActivity.class);
                break;
            case R.id.nav_settings:
                intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                break;
            case R.id.nav_address:
                intent = new Intent(HomeActivity.this, AddAddressActivity.class);
                break;
            case R.id.nav_sign_up:
                intent = new Intent(HomeActivity.this, SignUpActivity.class);
                break;
            case R.id.nav_about:
                intent = new Intent(HomeActivity.this, AboutActivity.class);
                break;
            case R.id.nav_contact:
                intent = new Intent(HomeActivity.this, ContactUsActivity.class);
                break;
            case R.id.nav_facebook:
                try {
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=http://www.facebook.com/TechnologyBrotherhood"));
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appConfigViewModel.getFacebook()[0]));
                    startActivity(intent);
                } catch (Exception exception) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appConfigViewModel.getFacebook()[1]));
                    startActivity(intent);
                }
                intent = null;
                break;
            case R.id.nav_twitter:
                try {
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=EtisalatMisr"));
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appConfigViewModel.getTwitter()[0]));
                    startActivity(intent);
                } catch (Exception exception) {
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/EtisalatMisr"));
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appConfigViewModel.getTwitter()[1]));
                    startActivity(intent);
                }
                intent = null;
                break;
            case R.id.nav_instagram:
                try {
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/etisalatmisr"));
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appConfigViewModel.getInstagram()[0]));
                    startActivity(intent);
                } catch (Exception exception) {
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.instagram.com/etisalatmisr"));
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appConfigViewModel.getInstagram()[1]));
                    startActivity(intent);
                }
                intent = null;
                break;
            case R.id.btn_sign_in:
                intent = new Intent(HomeActivity.this, SignInActivity.class);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            default:
                // TODO: 3/30/2020
        }
        if (intent != null) {
            startActivity(intent);
        }
        navigationView.setCheckedItem(item);
        return true;
    }

    private void signOut() {
        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getSupportFragmentManager(), null);

        MutableLiveData<UserRepository.APIResponse> response = userViewModel.signOut(userViewModel.getToken());
        response.observe(this, new Observer<UserRepository.APIResponse>() {
            @Override
            public void onChanged(UserRepository.APIResponse apiResponse) {
                switch (apiResponse) {
                    case SUCCESS:
                        loadingDialogFragment.dismiss();
                        finish();
                        startActivity(getIntent());
                        break;
                    case FAILED: {
                        loadingDialogFragment.dismiss();
                        ErrorDialogFragment dialogFragment =
                                new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), false);
                        dialogFragment.show(getSupportFragmentManager(), null);
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawer != null) {
            navigationView.setCheckedItem(R.id.nav_main);
            drawer.closeDrawer(GravityCompat.START);

            if (userViewModel.getUser() == null && userViewModel.getToken() == null) {
                showLoginHeader();
            } else if ((userViewModel.getToken() != null) /*||
                    !(userViewModel.getUser().getToken()).equals(userViewModel.getToken().toString())*/) {

                LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                loadingDialogFragment.show(getSupportFragmentManager(), null);

                MutableLiveData<UserRepository.APIResponse> userResponse = userViewModel.getUser(userViewModel.getToken());

                userResponse.observe(this, new Observer<UserRepository.APIResponse>() {
                    @Override
                    public void onChanged(UserRepository.APIResponse apiResponse) {
                        switch (apiResponse) {
                            case ERROR: {
                                loadingDialogFragment.dismiss();
                                ErrorDialogFragment dialogFragment =
                                        new ErrorDialogFragment("حدث خطأ", "فشلت عملية تحميل بيانات المستخدم", false);
                                dialogFragment.show(getSupportFragmentManager(), null);
                                userViewModel.clearUser();
                                showLoginHeader();
                                break;
                            }
                            case FAILED: {
                                loadingDialogFragment.dismiss();
                                ErrorDialogFragment dialogFragment =
                                        new ErrorDialogFragment("حدث خطأ", getResources().getString(R.string.server_connection_error), false);
                                dialogFragment.show(getSupportFragmentManager(), null);
                                break;
                            }
                            case SUCCESS:
                                loadingDialogFragment.dismiss();
                                showProfileHeader();
                                // TODO: 5/21/2020 end loading dialog
                                break;
                        }
                    }
                });
            } else {
                showProfileHeader();
            }
        }
    }

    private void showLoginHeader() {
        navigationView.getMenu().findItem(R.id.nav_sign_out).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_orders).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_settings).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_address).setVisible(false);
        navigationView.getHeaderView(0).findViewById(R.id.btn_sign_in).setVisibility(View.VISIBLE);
        navigationView.getHeaderView(0).findViewById(R.id.nav_profile).setVisibility(View.GONE);
    }

    private void showProfileHeader() {
        User user = userViewModel.getUser();
        navigationView.getHeaderView(0).findViewById(R.id.btn_sign_in).setVisibility(View.GONE);
        View view = navigationView.getHeaderView(0).findViewById(R.id.nav_profile);
        view.setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.header_nav_username)).setText(user.getName());
        ((TextView) view.findViewById(R.id.header_nav_phone_number)).setText(ArabicString.toArabic(user.getPhoneNumber()));
        navigationView.getMenu().findItem(R.id.nav_sign_out).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_orders).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_settings).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_address).setVisible(true);
    }

    public void setEFABVisibility(boolean isVisible) {
        homeFragment.setEFABVisibility(isVisible);
    }
}
