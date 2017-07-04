package com.example.lenovo.humblebragwallofshamekpsingh;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TweetActivity extends AppCompatActivity {


    EditText editText;
    ImageView imageView, imageIcon, locationIcon;
    TextView textCount;
    Button tweetButton;
    Uri imageUri;
    String currentLocation = "";
    boolean locationOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        editText = (EditText) (findViewById(R.id.editText));
        imageView = (ImageView) (findViewById(R.id.imageView));
        imageIcon = (ImageView) (findViewById(R.id.imageIcon));
        locationIcon = (ImageView) (findViewById(R.id.locationIcon));
        textCount = (TextView) (findViewById(R.id.textCount));
        tweetButton = (Button) (findViewById(R.id.tweetButton));

        imageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder location = new AlertDialog.Builder(getApplicationContext());
                location.setTitle("Choose Location");
                location.setMessage("Choose from where you want to take image?");
                location.setPositiveButton("Galary", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent in = new Intent(Intent.ACTION_GET_CONTENT);
                        in.setType("image/*");
                        startActivityForResult(in, 0);
                    }
                });
                location.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(in, 1);
                    }
                });
                location.setIcon(R.drawable.icon_image);
                location.create();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        location.show();
                    }
                });
            }
        });

        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationOn) {
                    locationOn = false;
                    locationIcon.setImageResource(R.drawable.icon_location_off);
                    currentLocation = "";
                } else {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (!gps && !nw) {
                        AlertDialog.Builder msg = new AlertDialog.Builder(getApplicationContext());
                        msg.setTitle("Location");
                        msg.setMessage("Please turn on your location to share it");
                        msg.setCancelable(false);
                        msg.setIcon(R.drawable.icon_location_on);
                        msg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(in);
                            }
                        });
                        msg.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        msg.create();
                        msg.show();
                    } else {
                        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        final MyLocationListener myLocationListener = new MyLocationListener();
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
                    }
                }
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int charLength = editText.getText().length();
                if (charLength > 144 || charLength == 0) {
                    tweetButton.setFocusable(false);
                } else {
                    tweetButton.setFocusable(true);
                }
                textCount.setText(144 - charLength);
                return false;
            }
        });

        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TwitterSession session = TwitterCore.getInstance().getSessionManager()
                        .getActiveSession();
                final Intent intent = new ComposerActivity.Builder(TweetActivity.this)
                        .session(session)
                        .image(imageUri)
                        .text(editText.getText().toString() + "\n" + currentLocation)
                        .createIntent();
                startActivity(intent);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageUri = data.getData();
        }
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses.size() > 0) {
                currentLocation = "Location:" + addresses.get(0).getLocality();
            } else {
                currentLocation = "Location: Unknown Location";
            }
            locationIcon.setImageResource(R.drawable.icon_location_on);
            locationOn = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
