package com.plexus.main.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.plexus.R;
import com.plexus.information_centre.Covid_Information;
import com.plexus.main.fragments.HomeFragment;
import com.plexus.main.fragments.NotificationsFragment;
import com.plexus.main.fragments.ProfileFragment;
import com.plexus.main.fragments.SearchFragment;
import com.plexus.model.account.User;
import com.plexus.posts.activity.saved_posts.SavedPostsActivity;
import com.plexus.services.LocationService;
import com.plexus.startup.PermissionActivity;
import com.plexus.utils.AccountUtil;

/******************************************************************************
 * Copyright (c) 2020. Plexus, Inc.                                           *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 *  limitations under the License.                                            *
 ******************************************************************************/

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    //Navigation Drawer
    private SimpleDraweeView profile_image;
    private TextView fullname_nav, view_profile;
    private LinearLayout signout;
    private CardView covid_information_centre, my_posts, followers, groups, shopping, saved_posts;

    ImageView back;
    TextView toolbar_name;

    String profileid;
    SharedPreferences prefs;
    Fragment selectedfragment = null;

    FirebaseUser firebaseUser;

    String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profile_image = findViewById(R.id.profile_image);
        fullname_nav = findViewById(R.id.fullname_nav);
        view_profile = findViewById(R.id.view_profile);
        signout = findViewById(R.id.signout);
        saved_posts = findViewById(R.id.saved_posts);
        covid_information_centre = findViewById(R.id.covid_information_centre);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
        mDrawer = findViewById(R.id.drawer_layout);
        nvDrawer = findViewById(R.id.navigation_view);
        back = findViewById(R.id.back);
        toolbar_name = findViewById(R.id.toolbar_name);

        back.setVisibility(View.GONE);
        toolbar_name.setText("PLEXUS");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        drawerToggle = setupDrawerToggle();

        // Setup toggle to display hamburger icon with nice animation
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        prefs = getSharedPreferences("plexus", MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        init();
    }

    private void init(){
        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("plexus", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.feed_nav:
                    selectedfragment = new HomeFragment();
                    break;

                case R.id.notifications_nav:
                    selectedfragment = new NotificationsFragment();
                    break;

                case R.id.profile_nav:
                    selectedfragment = new ProfileFragment();
                    break;

                case R.id.watch_nav:
                    selectedfragment = new SearchFragment();
                    break;
            }
            if (selectedfragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedfragment).commit();
            }

            return true;
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean loggedInRecently = extras.getBoolean("loggedIn", false);
            if (loggedInRecently) {
                LocationService.LocationResult locationResult = new LocationService.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {
                        AccountUtil.addLoginDetails(location.getLatitude(), location.getLongitude(), getContentResolver());
                    }
                };

                LocationService locationService = new LocationService();
                locationService.getLocation(this, locationResult);
            }
        }

        AccountUtil.getUnreadNotifications(bottomNavigationView);

        if (!hasPermissions(this, PERMISSIONS)) {
            startActivity(new Intent(getApplicationContext(), PermissionActivity.class));
        }

        navigationDrawerData();
        handleDrawerNavigation();
    }

    private void navigationDrawerData(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                fullname_nav.setText(user.getName() + " " + user.getSurname());
                profile_image.setImageURI(user.getImageurl());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleDrawerNavigation(){
        view_profile.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();

            mDrawer.closeDrawers();
        });

        covid_information_centre.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Covid_Information.class));
            mDrawer.closeDrawers();
        });

        saved_posts.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SavedPostsActivity.class));

            mDrawer.closeDrawers();
        });
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static @NonNull Intent clearTop(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK  |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (!selectedfragment.equals(new HomeFragment())) {
            bottomNavigationView.setSelectedItemId(R.id.feed_nav);
            selectedfragment = new HomeFragment();
        } else {
            super.onBackPressed();
        }
    }
}
