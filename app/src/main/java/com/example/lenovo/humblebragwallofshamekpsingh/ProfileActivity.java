package com.example.lenovo.humblebragwallofshamekpsingh;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Applying back button and logo in the action bar of an activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent in = getIntent();
        String username = in.getStringExtra("username");

        getSupportActionBar().setTitle("Profile : " + username);

        webView = (WebView) (findViewById(R.id.webView));
        webView.setWebViewClient(new MyWebView());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://www.twitter.com/" + username);

    }

    // After clicking on back button this activity will close
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Here web view is used to show the profile of the required username came from HomeActivity
    class MyWebView extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
