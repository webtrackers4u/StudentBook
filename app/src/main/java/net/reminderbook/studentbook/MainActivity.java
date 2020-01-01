package net.reminderbook.studentbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {
    private ViewPager mainViewPager;
    private MainPagerAdapter adapterViewPager;
    private BottomNavigationView bottomNavigationView;
    //os
    private Global os;

    ////local data
    ///constant
    private boolean filter_active = false;
    private HttpReq  http;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        os = new Global(getApplicationContext());
        http = new HttpReq(getApplicationContext(), Global.Services.API);
        http.activateDebugging(true);
        //view pager
        mainViewPager = (ViewPager) findViewById(R.id.main_pager);

        start_app();

    }


    //logout
    void logout(){
        os.save_logged_user("");
        os.save_member_subjects("");
        os.save_member_deatils("");
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void start_app(){
        adapterViewPager = new MainPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        mainViewPager.setAdapter(adapterViewPager);
        mainViewPager.setOffscreenPageLimit(8);
        /************************
         * Bottom Navigation
         ************************/

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        mainViewPager.setCurrentItem(0,true);
                        os.setStatusBarColor(MainActivity.this, Color.WHITE);
                        break;
                    case R.id.questions:
                        mainViewPager.setCurrentItem(1,true);
                        os.setStatusBarColor(MainActivity.this, Color.WHITE);
                        break;
                    case R.id.suggestion:
                        mainViewPager.setCurrentItem(2,true);
                        os.setStatusBarColor(MainActivity.this, Color.WHITE);
                        break;
                    case R.id.past_year:
                        mainViewPager.setCurrentItem(3,true);
                        os.setStatusBarColor(MainActivity.this, Color.WHITE);
                        break;
                    case R.id.account:
                        mainViewPager.setCurrentItem(4,true);
                        os.setStatusBarColor(MainActivity.this, Color.TRANSPARENT);
                        break;
                }
                return true;
            }
        });


    }
    /**********************************
     * MainPager Adapter
     ********************************/
    public static class MainPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<String> titles;
        private ArrayList<Fragment> fragments;
        private Context context;

        MainPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
            /*
            Fragments
             */
            fragments = new ArrayList<>();
            fragments.add(new HomeFragment());
            fragments.add(new QuestionsFragment());
            fragments.add(new AccountFragment());
            /*
            Titles
             */
            titles = new ArrayList<>();
            titles.add("Student Book");
            titles.add("Questions");
            titles.add("Profile");
        }
        // Returns total number of pages
        @Override
        public int getCount() {
            return fragments.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return  titles.get(position);
        }

    }
    @Override
    public void onBackPressed() {
        if(!filter_active){
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
    /*****************************
     * Results Handle
     *******************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode){
            case FilterActivity.ActivityConstants.FILTER_QUESTION:
                try {
                    QuestionsFragment fr    = ((QuestionsFragment) adapterViewPager.fragments.get(1));
                    fr.filter_favourite     = data.getStringExtra("favourite");
                    fr.filter_importance    = data.getStringExtra("importance");
                    fr.filter_overall_star  = data.getStringExtra("overall_star");

                    fr.clearAllData();
                    fr.populateData("1");
                    mainViewPager.setCurrentItem(1);
                }catch (Exception error){
                    Log.e("Error: ", error.getMessage());
                }
                break;
        }
    }
}
