package com.example.lenovo.humblebragwallofshamekpsingh;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;


//  This is the custom tweet activity that I made earlier but due to some issues cannot use it properly


public class TweetActivity extends AppCompatActivity {


    EditText editText;
    ImageView imageView, imageGallery, imageCamera, locationIcon;
    TextView textCount, textLocation, textView;
    Button tweetButton;
    Uri imageUri;
    boolean locationChoosen = false;
    File tweetMediaFile;
    LinearLayout linearLayout;
    Double latitude,longitude;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        // Applying back button and logo in the action bar of an activity

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        editText = (EditText) (findViewById(R.id.editText));
        imageView = (ImageView) (findViewById(R.id.imageView));
        imageGallery = (ImageView) (findViewById(R.id.imageGallary));
        imageCamera = (ImageView) (findViewById(R.id.imageCamera));
        locationIcon = (ImageView) (findViewById(R.id.locationIcon));
        textCount = (TextView) (findViewById(R.id.textCount));
        tweetButton = (Button) (findViewById(R.id.tweetButton));
        textLocation = (TextView) (findViewById(R.id.locationText));
        tweetButton.setEnabled(false);
        textView = (TextView) (findViewById(R.id.textView));
        linearLayout = (LinearLayout) (findViewById(R.id.linearLayout));
        frameLayout=(FrameLayout) (findViewById(R.id.frameLayout));

        // Listeners on the click of different views

        imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isPermissionGranted()) {
                        openImagePicker();
                    } else {
                        requestPermission();
                    }
                }
                else{
                    openImagePicker();
                }
            }
        });
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isPermissionGranted()) {
                        openCamera();
                    } else {
                        requestPermission();
                    }
                }
                else{
                    openCamera();
                }
            }
        });


        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationChoosen) {
                    locationChoosen = false;
                    locationIcon.setImageResource(R.drawable.icon_location_off);
                    textLocation.setText("");
                    textLocation.setVisibility(View.GONE);
                    latitude=null;
                    longitude=null;
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (isLocationPermissionGranted()) {
                            checkLocation();
                        } else {
                            requestLocationPermission();
                        }
                    }
                    else{
                        checkLocation();
                    }

                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (characterCountOk(s.toString())) {
                    tweetButton.setEnabled(true);
                } else {
                    tweetButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final TwitterSession session = TwitterCore.getInstance().getSessionManager()
//                        .getActiveSession();
//                final Intent intent = new ComposerActivity.Builder(TweetActivity.this)
//                        .session(session)
//                        .image(imageUri)
//                        .text(editText.getText().toString() + "\n" + currentLocation)
//                        .createIntent();
//                startActivity(intent);
                linearLayout.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                if (tweetMediaFile != null) {
                    uploadImage(tweetMediaFile, editText.getText().toString());
                } else {
                    postTweet(editText.getText().toString(), null);
                }
            }
        });
    }

    private  void checkLocation(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps && !nw) {
            Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(in);
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
    private void postTweet(String text, String imageId) {
        StatusesService statusesService = TwitterCore.getInstance().getApiClient().getStatusesService();
        Call<Tweet> call = statusesService.update(text, null, false, latitude, longitude, null, false, false, imageId);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Toast.makeText(TweetActivity.this, "Tweet Composed Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(TweetActivity.this, "Some error occurred, try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean characterCountOk(String text) {
        int numerUrls = 0;
        int lengthAllUrls = 0;

        String regex = "\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&@#/%?=-_()|!:,.;]*[-A-Za-z0-9+&@#/%=-_()|]";
        Pattern urlPattern = Pattern.compile(regex);
        Matcher urlMatcher = urlPattern.matcher(text);
        while (urlMatcher.find()) {
            lengthAllUrls += urlMatcher.group().length();
            numerUrls++;
        }
        int tweetLength = text.length() - lengthAllUrls + numerUrls * 23;
        textCount.setText(Integer.toString(140 - tweetLength));

        if (tweetLength > 0 && tweetLength <= 140) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    // After clicking on back button this activity will close
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Location listener to fetch current location by mobile

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            latitude= location.getLatitude();
            longitude = location.getLongitude();
            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses.size() > 0) {
                textLocation.setText("Location:" + addresses.get(0).getLocality());
            } else {
                textLocation.setText("Location: Unknown Location");
            }
            locationIcon.setImageResource(R.drawable.icon_location_on);
            locationChoosen = true;
            textLocation.setVisibility(View.VISIBLE);
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

    private void openImagePicker() {
        Intent in = new Intent(Intent.ACTION_GET_CONTENT);
        in.setType("image/*");
        startActivityForResult(in, 0);
    }

    private void openCamera(){
        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(in,1);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                imageUri = data.getData();
            } else if (requestCode == 1) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(this, bitmap);
            }
            String filePath = DocumentHelper.getPath(this, imageUri);
            tweetMediaFile = new File(filePath);
            if (tweetMediaFile.length() > 5242880) {
                Toast.makeText(this, "Image size is too much. Choose some other images", Toast.LENGTH_SHORT).show();
                tweetMediaFile = null;
            } else {
//                imageView.setImageURI(imageUri);
//                ImageLoader imageLoader=ImageLoader.getInstance();
//                Toast.makeText(this, imageLoader+"", Toast.LENGTH_SHORT).show();
//                Bitmap bmp = imageLoader.loadImageSync(imageUri.toString());
//                imageView.setImageBitmap(bmp);

                Picasso.with(this).load(imageUri).into(imageView);
                frameLayout.setVisibility(View.VISIBLE);
                if (characterCountOk(editText.getText().toString())) {
                    tweetButton.setEnabled(true);
                } else {
                    tweetButton.setEnabled(false);
                }
            }
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImage(File imageFile, final String tweetText) {
        MediaType type = MediaType.parse("image/*");
        RequestBody body = RequestBody.create(type, imageFile);
        TwitterApiClient twitterApiClient = new TwitterApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
        MediaService mediaService = twitterApiClient.getMediaService();
        Call<Media> call = mediaService.upload(body, null, null);
        call.enqueue(new Callback<Media>() {
            @Override
            public void success(Result<Media> result) {
                postTweet(tweetText, result.data.mediaIdString);
                tweetMediaFile = null;
                imageView.setImageDrawable(null);
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(TweetActivity.this, "Some error occured in Image File", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission taken, now click that icon again", Toast.LENGTH_SHORT).show();
        }
    }
}
