package edu.uah.cpe.amdessapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ScanActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
    }

    public void onDoneClick(View v)
    {
        /// @todo stop the Bluetooth scan

        // finish the activity
        finish();
    }
}
