package edu.uah.cpe.amdessapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SensorListActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);
    }

    public void onScanClick(View v)
    {
        // launch the scan activity
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
