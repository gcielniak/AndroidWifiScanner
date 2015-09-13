package com.example.gcielniak.androidwifiscanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    ListView lv;
    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;
    File log_file;
    FileWriter log_writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=(ListView)findViewById(R.id.listView);

        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifi.startScan();
        log_file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "wifi_log.txt");
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        try
        {
            log_writer.close();
            MediaScannerConnection.scanFile(this,
                    new String[] { log_file.toString() },
                    null,
                    null);
        }
        catch (java.io.IOException exc)
        {
            Log.e("MainActivity","Error writing to file.");
        }
        super.onPause();
    }

    protected void onResume() {
        try
        {
            log_writer = new FileWriter(log_file, true);
        }
        catch (java.io.IOException exc)
        {
            Log.e("MainActivity","Error writing to file.");
        }
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss.ss");
            Log.i("MainActivity", sdf.format(new Date()));

            try
            {
                log_writer.write(sdf.format(new Date()));
            }
            catch (java.io.IOException exc)
            {
                Log.e("MainActivity","Error writing to file.");
            }

            for(int i = 0; i < wifiScanList.size(); i++){
//                wifis[i] = ((wifiScanList.get(i)).toString());
                ScanResult result = wifiScanList.get(i);
                wifis[i] = result.SSID + " " + result.timestamp + " " + result.level;
                Log.i("MainActivity", wifis[i]);
                try
                {
                    log_writer.write(wifis[i]);
                }
                catch (java.io.IOException exc)
                {
                    Log.e("MainActivity","Error writing to file.");
                }
            }
            try
            {
                log_writer.write("\n");
                log_writer.flush();
            }
            catch (java.io.IOException exc)
            {
                Log.e("MainActivity","Error writing to file.");
            }
            Log.i("MainActivity", "\n");
            lv.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,wifis));
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
