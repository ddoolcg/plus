package com.lcg.plus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Date;

public class Main3Activity extends AppCompatActivity {
    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, Main3Activity.class).putExtra("sdas", "asdfsdf"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Log.i("fast", getIntent().getStringExtra("sdas"));
        getSupportFragmentManager().beginTransaction().add(R.id.fl, WorkFragment.newInstance(true, new Date(), ".............")).commit();
    }
}
