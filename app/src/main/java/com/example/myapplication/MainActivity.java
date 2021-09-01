package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference dbRef;

    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    private MenuItem selected;

    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> Collection;
    ExpandableListView listV;
    ExpandableListAdapter listVAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        dbRef = db.collection("publicData/").document(mAuth.getUid());

        String username = mAuth.getCurrentUser().getEmail();
        username = username.split("@")[0];

        HashMap<String,Object> name = new HashMap<String,Object>();
        name.put("username",username);

        dbRef.set(name).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });


// Changes nav footer to email or to show guest
        TextView t = findViewById(R.id.navFooter);
        t.setText(mAuth.getCurrentUser().getEmail());

// Configuring navigation and Loading Home Fragment
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();
        selected = navigationView.getMenu().getItem(1);

//        findViewById(R.id.nestedScrollView).setNestedScrollingEnabled(true);

//        loadProfileFragment(selected);
        loadProfileFragment(selected);
    }



    private void loadFragment(Fragment fragment) {
        FrameLayout map = (FrameLayout) findViewById(R.id.MapFragment);
        map.setVisibility(View.GONE);

        findViewById(R.id.nestedScrollView).bringToFront();

        FrameLayout frag = (FrameLayout) findViewById(R.id.FragmentFrame);
        frag.bringToFront();
        frag.setVisibility(View.VISIBLE);

// create a FragmentManager
        FragmentManager fm = getSupportFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction ft = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.FragmentFrame, fragment);
        ft.commit(); // save the changes
    }
    private void loadMap(Fragment fragment) {
        FrameLayout frag = (FrameLayout) findViewById(R.id.FragmentFrame);
        frag.setVisibility(View.GONE);


        FrameLayout map = (FrameLayout) findViewById(R.id.MapFragment);
        map.bringToFront();
        map.setVisibility(View.VISIBLE);

// create a FragmentManager
        FragmentManager fm = getSupportFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction ft = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.MapFragment, fragment);
        ft.commit(); // save the changes
    }
    private void updateNav(MenuItem item) {
        selected.setChecked(false);

        selected = item;
        selected.setChecked(true);

        drawerLayout.closeDrawers();
    }

    public void openNav(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
    }
    public void loadHomeFragment(MenuItem item)
    {
        loadFragment(new HomeFragment());
        updateNav(item);
    }
    public void loadHomeFragment(View view)
    {
        loadFragment(new HomeFragment());
        updateNav(navigationView.getMenu().getItem(0));
    }
    public void loadProfileFragment(MenuItem item)
    {
        loadFragment(new ProfileFragment());
        updateNav(item);
    }
    public void loadCommunityFragment(MenuItem item)
    {
        loadFragment(new CommunityFragment());
        updateNav(item);
    }
    public void loadProfileFragment(View view)
    {
        loadFragment(new ProfileFragment());
        updateNav(navigationView.getMenu().getItem(1));
    }

    public void loadMealFragment(MenuItem item)
    {
        loadFragment(new MealFragment());
        updateNav(item);
    }
    public void loadShoppingFragment(MenuItem item)
    {
        loadFragment(new ShoppingFragment());
        updateNav(item);
    }

    public void loadWorkoutFragment(MenuItem item)
    {
        loadFragment(new WorkoutFragment());
        updateNav(item);
    }
    public void loadWorkoutPlanFragment(MenuItem item)
    {
        loadFragment(new WorkoutPlanFragment());
        updateNav(item);
    }
    public void loadMapFragment(MenuItem item)
    {
        loadMap(new MapsFragment());
        updateNav(item);
    }
    public void loadCommunityMapFragment(MenuItem item)
    {
        loadMap(new CommunityMapFragment());
        updateNav(item);
    }

    public void logout(MenuItem item)
    {
        FirebaseAuth.getInstance().signOut();
//        setContentView(R.layout.activity_login);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        Log.w("Loggert_Froggert", "sign out");
        Toast.makeText(MainActivity.this, "Sign out Successful",
                Toast.LENGTH_SHORT).show();
    }

}