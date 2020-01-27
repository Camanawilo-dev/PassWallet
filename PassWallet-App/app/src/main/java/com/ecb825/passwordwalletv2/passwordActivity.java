package com.ecb825.passwordwalletv2;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class passwordActivity extends AppCompatActivity implements passwords.OnFragmentInteractionListener, settings.OnFragmentInteractionListener,extensions.OnFragmentInteractionListener,bottompassoptions.BottomSheetListener,sendstep1.OnFragmentInteractionListener{

    private passwords pass;

    public items getItem() {
        return item;
    }

    public void setItem(items item) {
        this.item = item;
    }

    private items item;

    public passwords getFrag(){
        return pass;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);



//        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
//        bottomNav.setOnNavigationItemSelectedListener(navListener);

        pass =new passwords();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, pass);
        ft.addToBackStack(null);
        // Start the animated transition.
        ft.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFrag = null;

                    switch (menuItem.getItemId()){
                        case R.id.passwords:
                            selectedFrag = new passwords();
                            break;
                        case R.id.extentions:
                            selectedFrag = new extensions();
                            break;
                        case R.id.settings:
                            selectedFrag = new settings();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFrag).commit();
                    return true;

                }
            };




    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onButtonClicked(int id, String mode) throws GeneralSecurityException, IOException {
        if(mode.equals("delete")){
            pass.removeItem(id);
        }
        if(mode.equals("send")){
            item = pass.getItem(id);
            sendstep1 settings = new sendstep1();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left,R.anim.slide_in_left,R.anim.slide_out_right);
            ft.replace(R.id.fragment_container, settings);
            ft.addToBackStack("step1");
            ft.commit();
        }
    }
}
