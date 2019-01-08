package com.example.smakash.yamaharegistration.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.smakash.yamaharegistration.R;

public class GuidelineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guideline);

    }

    @Override
    public void onBackPressed() {
        final Bundle bundle = getIntent().getExtras();

        final String UserId = bundle.getString("UserId");

        bundle.putString("UserId",UserId);

        Intent intent = new Intent(GuidelineActivity.this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}
