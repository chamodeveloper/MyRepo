package com.mytest.myservice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean isBackPressed = false;
    private String selectedItem = null;
    private ActionBar _actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        _actionBar = getSupportActionBar();
        _actionBar.setLogo(R.drawable.home);
        _actionBar.setDisplayUseLogoEnabled(true);
        _actionBar.setDisplayShowHomeEnabled(true);
        checkPermissionGranted();

    }

    public void checkPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    || !(checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
                    || !(checkSelfPermission(Manifest.permission.SET_DEBUG_APP) == PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.SET_DEBUG_APP}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        }
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onRestart(){
        super.onRestart();

    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onStop(){
        super.onStop();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        isBackPressed = true;

    }


    public void onSelect(View v){

        if(v == findViewById(R.id.DOB_Numerology)){
            Toast.makeText(getBaseContext()," DOB Selected ",Toast.LENGTH_SHORT).show();
            Intent dobIntent = new Intent(MainActivity.this,DOBNumerology.class);
            startActivity(dobIntent);
        }

        else if (v == findViewById(R.id.Name_Numerology)){
            Toast.makeText(getBaseContext()," Name Selected ",Toast.LENGTH_SHORT).show();
            Intent nameIntent = new Intent(MainActivity.this,NameNumerology.class );
            startActivity(nameIntent);
        }
    }
}
