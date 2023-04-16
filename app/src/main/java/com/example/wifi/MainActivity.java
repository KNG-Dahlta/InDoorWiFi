package com.example.wifi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AndroidWifi";

    private static final int MY_REQUEST_CODE = 123;

    private WifiManager wifiManager;

    private TextView textViewScanResults;

    private WifiBroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        this.wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //
        Button buttonScan = this.findViewById(R.id.button_scan);
        this.textViewScanResults = this.findViewById(R.id.textView_scanResults);

        buttonScan.setOnClickListener(v -> askAndStartScanWifi());
    }

    private void askAndStartScanWifi() {

        // With Android Level >= 23, you have to ask the user for permission to Call.
        int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        // Check for permissions
        if (permission1 != PackageManager.PERMISSION_GRANTED) {

            Log.d(LOG_TAG, "Requesting Permissions");

            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE
                    }, MY_REQUEST_CODE);
            return;
        }
        Log.d(LOG_TAG, "Permissions Already Granted");
        this.doStartScanWifi();
    }

    private void doStartScanWifi() {

        this.wifiManager.startScan();
        Log.d(LOG_TAG, "Start Scan");
    }


    @Override
    protected void onStop() {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
        Log.d(LOG_TAG, "On Stop");
    }


    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            boolean ok = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (ok) {
                Log.d(LOG_TAG, "Scan OK");
                Toast.makeText(MainActivity.this, "Scan OK!", Toast.LENGTH_SHORT).show();

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                List<ScanResult> list = wifiManager.getScanResults();

                MainActivity.this.showNetworksDetails(list);
            } else {
                Log.d(LOG_TAG, "Scan not OK");
                Toast.makeText(MainActivity.this, "Scan not OK!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void showNetworksDetails(List<ScanResult> results) {

        this.textViewScanResults.setText("");
        this.textViewScanResults.setMovementMethod(new ScrollingMovementMethod());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            if (result.SSID.equals("MERCUSYS_531C") ||
                    result.SSID.equals("MERCUSYS_DF54") ||
                    result.SSID.equals("MERCUSYS_DFC4") ||
                    result.SSID.equals("MERCUSYS_DF48")) {
                sb.append("\n SSID: ").append(result.SSID); // Network Name.
                sb.append("\n BSSID: ").append(result.BSSID); // Basic Service Set Identifier
                sb.append("\n level (RSSI): ").append(result.level).append(" dBm\n");
            }
        }
        this.textViewScanResults.setText(sb.toString());
    }
}