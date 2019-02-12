package com.example.uploadion;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.luseen.simplepermission.permissions.Permission;
import com.luseen.simplepermission.permissions.PermissionActivity;
import com.luseen.simplepermission.permissions.SinglePermissionCallback;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import java.io.File;

public class MainActivity extends PermissionActivity {
    private boolean mIsUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsUploading) {
                    return;
                }
                requestPermission(Permission.READ_EXTERNAL_STORAGE, new SinglePermissionCallback() {
                    @Override
                    public void onPermissionResult(boolean granted, boolean isDeniedForever) {
                        if(granted) {
                            showFilePicker();
                        } else {
                            Toast.makeText(getBaseContext(), "eorror", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void showFilePicker() {
        new MaterialFilePicker()
                .withActivity(MainActivity.this)
                .withRequestCode(555)
                .start();
    }

    @Override
    public void onActivityResult(int rqCode, int resCode, Intent intent) {
        if(rqCode != 555 && resCode != RESULT_OK) {
            return;
        }
        String path = intent.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

        final Notification.Builder notifBuilder = new Notification.Builder(getBaseContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("uploading");

        final int id = 1122;
        final NotificationManager notifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mIsUploading = true;

        Ion.with(this)
                .load("http://7d84d314.ngrok.io/pro-android/upload.php")
                .uploadProgress(new ProgressCallback() {
                    @Override
                    public void onProgress(long loaded,  long total) {
                        notifBuilder.setProgress((int) total, (int) loaded, false);
                        Notification notif = notifBuilder.build();
                        notifMan.notify(id, notif);
                    }
                })
                .setMultipartFile("upload_file", new File(path))
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        notifBuilder.setProgress(100, 100, false);
                        notifBuilder.setContentText(result);
                        Notification notif = notifBuilder.build();
                        notifMan.notify(id, notif);
                        mIsUploading = false;
                        Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
                    }
                });
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
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
