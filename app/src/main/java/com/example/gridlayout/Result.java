package com.example.gridlayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        Intent intent = getIntent();
        String message = intent.getStringExtra("com.example.gridlayout.MESSAGE");

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(message);
    }
}
