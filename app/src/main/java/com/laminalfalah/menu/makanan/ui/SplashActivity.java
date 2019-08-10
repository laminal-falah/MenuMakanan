package com.laminalfalah.menu.makanan.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.github.ybq.android.spinkit.SpinKitView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.laminalfalah.menu.makanan.R;
import com.laminalfalah.menu.makanan.service.ServiceApp;
import com.laminalfalah.menu.makanan.utils.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @BindView(R.id.spin_kit) SpinKitView spinKitView;

    private final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int PERMISSIONS_REQUEST_CODE = 1024;

    private ServiceApp serviceApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        serviceApp = new ServiceApp(this);

        if (Config.DEVELOPMENT) {
            if (checkAndRequestPermission()) {
                if (!isMyServiceRunning(serviceApp.getClass())) {
                    startService(new Intent(getApplicationContext(),serviceApp.getClass()));
                }
                splash();
            }
        } else {
            if (!isMyServiceRunning(serviceApp.getClass())) {
                startService(new Intent(getApplicationContext(),serviceApp.getClass()));
            }
            splash();
        }
    }

    private void splash() {
        final Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } finally {
                    startActivity(new Intent(getApplicationContext(), ReadMakananActivity.class));
                    finish();
                }
            }
        };
        timer.start();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.i("isMyServiceRunning: ", true+"");
                    return true;
                }
            }
        }
        Log.i("isMyServiceRunning: ", false+"");
        return false;
    }

    private boolean checkAndRequestPermission() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
                splash();
            } else {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();
                    String[] msg = getResources().getStringArray(R.array.msg_permissions);
                    String[] pos = getResources().getStringArray(R.array.positive_permission);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        showDialog(getString(R.string.title_permission), msg[0], pos[0], (dialog, which) -> {
                                    dialog.dismiss();
                                    checkAndRequestPermission();
                                },
                                getString(R.string.negative_permission), (dialog, which) -> {
                                    dialog.dismiss();
                                    finish();
                                });
                    } else {
                        showDialog(getString(R.string.title_permission), msg[1], pos[1], (dialog, which) -> {
                                    dialog.dismiss();
                                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                },
                                getString(R.string.negative_permission), (dialog, which) -> {
                                    dialog.dismiss();
                                    finish();
                                });
                        break;
                    }
                }
            }
        }
    }

    private void showDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveClick,
                            String negativeLabel, DialogInterface.OnClickListener negativeClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveClick);
        builder.setNegativeButton(negativeLabel, negativeClick);
        builder.create();
        builder.show();
    }
}
