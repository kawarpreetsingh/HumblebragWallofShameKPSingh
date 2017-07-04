package com.example.lenovo.humblebragwallofshamekpsingh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    TwitterLoginButton loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Twitter.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Services.haveNetworkConnection(this)) {

            if (TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }

            TwitterConfig config = new TwitterConfig.Builder(this)
                    .logger(new DefaultLogger(Log.DEBUG))
                    .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                    .debug(true)
                    .build();
            Twitter.initialize(config);

            loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                    TwitterAuthToken authToken = session.getAuthToken();
                    String token = authToken.token;
                    String secret = authToken.secret;
                    String username = session.getUserName();
                    Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService().verifyCredentials(true, false, true);
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.d("MYMSG", "Didn't logged in");
                }
            });
        } else {
            Services.showInternetRequiredMessage(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
