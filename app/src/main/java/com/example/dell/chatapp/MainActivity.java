package com.example.dell.chatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private SectionPagerAdapter msectionPagerAdapter;

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLayout=(TabLayout)findViewById(R.id.main_tab_layout);
        mViewPager=(ViewPager)findViewById(R.id.main_viewPager);

        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApp");

        mAuth = FirebaseAuth.getInstance();

        msectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(msectionPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if User is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
          sendToStart();
        }
    }

    private void sendToStart() {
        Intent intent =new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.logout_button){

            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId()==R.id.settings_button){
            Intent intent=new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.all_user_button){
            Intent intent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }

    return true;
    }
}
