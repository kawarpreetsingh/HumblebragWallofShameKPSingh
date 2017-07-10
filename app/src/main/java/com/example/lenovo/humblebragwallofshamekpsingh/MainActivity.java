package com.example.lenovo.humblebragwallofshamekpsingh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class MainActivity extends Activity {

    // Twitter login button
    TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Twitter.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If network connection is on only then proceed further
        if (Services.haveNetworkConnection(this)) {

            // If session is live skip login and go to HomeActivity
            if (TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }

            // Initializing twitterconfig with the required credentials containing consumer key and consumer secret
            TwitterConfig config = new TwitterConfig.Builder(this)
                    .logger(new DefaultLogger(Log.DEBUG))
                    .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                    .debug(true)
                    .build();
            Twitter.initialize(config);

            loginButton = (TwitterLoginButton) findViewById(R.id.login_button);

            //the call back after the login process
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    //success
                    // Here we can get the user information who is logged in
                    TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                    TwitterAuthToken authToken = session.getAuthToken();
                    String token = authToken.token;
                    String secret = authToken.secret;
                    String username = session.getUserName();
//                    Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService().verifyCredentials(true, false, true);
                    Toast.makeText(MainActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }

                @Override
                public void failure(TwitterException exception) {
                    //failure
                    Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If internet connection is off, Ask the user to turn it on.
            Services.showInternetRequiredMessage(this);
        }
    }

    //After login process this function is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
