package taller2.combilocationiot.com.iottransportcombiserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    protected Location mLastLocation;

    private String mLatitudeValue;
    private String mLongitudeValue;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private OkHttpClient client;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    Handler handler = new Handler();
    String stringDateTime;

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            try {
                saveLocationInDb();
                getLastLocation();
                handler.postDelayed(this, 2000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeValue = "latitude";
        mLongitudeValue = "longitude";
        mLatitudeText =   findViewById((R.id.latitude_text));
        mLongitudeText =  findViewById((R.id.longitude_text));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        client = new OkHttpClient();

        handler.postDelayed(runnableCode, 2000);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(
                int requestCode,
                @NonNull String[] permissions,
                @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                showSnackbar(R.string.textwarn, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.textwarn, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
                                    mLatitudeValue,
                                    mLastLocation.getLatitude()));
                            mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
                                    mLongitudeValue,
                                    mLastLocation.getLongitude()));
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());

                        }
                    }
                });
    }

    private void saveLocationInDb() throws IOException {

        if(mLatitudeValue != null && !mLatitudeValue.isEmpty()  && mLongitudeValue != null && !mLongitudeValue.isEmpty()){

            Date currentDateTime = Calendar.getInstance().getTime();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            stringDateTime = df.format(currentDateTime);
            String valueString = "latitude="+
                    mLastLocation.getLatitude()+
                    "&longitude="+
                    mLastLocation.getLongitude()+
                    "&datetime=2018-11-29%2018%3A33%3A33&undefined=";

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

            RequestBody body = RequestBody.create(mediaType, valueString);

            Request request = new Request.Builder()
                    .url("http://192.168.0.121:8888/writesample")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    Log.i("-->Saved in DB: ", "Latitude: " + mLastLocation.getLatitude() + "Longitude: " + mLastLocation.getLongitude());
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                    }
                }
            });
        }
    }
}

