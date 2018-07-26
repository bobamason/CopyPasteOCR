package net.masonapps.shippingdataocr;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;

import net.masonapps.shippingdataocr.bluetooth.BluetoothActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BluetoothActivity {

    private static final int PERMISSION_REQUESTS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
        }
        return false;
    };
    private Button testButton;
//    private CommClient comm;

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        comm = new CommClient();

        testButton = findViewById(R.id.buttonTest);
        testButton.setEnabled(false);
        testButton.setOnClickListener(v -> {
            if (isConnected()) {
                Log.d(TAG, "sending test message");
                write("p:Hello World!");
            }
        });
        setup();
    }

    private void setup() {
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        } else {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                displayDeviceListDialog();
            } else {
                requestEnableBluetooth();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (comm != null) {
//            try {
//                comm.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void connected() {
        testButton.setEnabled(true);
    }

    @Override
    public void connecting() {

    }

    @Override
    public void disconnected() {
        testButton.setEnabled(false);
    }

    @Override
    public void onRead(String line) {

    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
}
