package com.example.lenovo.humblebragwallofshamekpsingh;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.io.File;


public class HomeActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final UserTimeline userTimeline = new UserTimeline.Builder()
                .screenName("Humblebrag")
                .build();

        final CustomTweetTimelineListAdapter adapter = new CustomTweetTimelineListAdapter(this, userTimeline);
        setListAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_1, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {

        } else if (item.getItemId() == R.id.tweet) {

            TweetComposer.Builder builder = new TweetComposer.Builder(this)
                    .text("just setting up my Twitter Kit.");
            builder.show();
        }
        return true;
    }

    class CustomTweetTimelineListAdapter extends TweetTimelineListAdapter {

        public CustomTweetTimelineListAdapter(Context context, Timeline<Tweet> timeline) {
            super(context, timeline);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            //disable subviews
            if (view instanceof ViewGroup) {
                disableViewAndSubViews((ViewGroup) view);
            }

            //enable root view and attach custom listener
            view.setEnabled(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long user_id = getItemId(position);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                            try {
//                                URL url= new URL("https://api.twitter.com/1.1/users/show.json?user_id="+user_id);
//                                HttpURLConnection connection=(HttpURLConnection) url.openConnection();
//                                Log.d("MYMSG","Response Code :"+connection.getResponseCode());
//                            } catch (MalformedURLException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }).start();
                }
            });
            return view;
        }

        private void disableViewAndSubViews(ViewGroup layout) {
            layout.setEnabled(false);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ViewGroup) {
                    disableViewAndSubViews((ViewGroup) child);
                } else {
                    child.setEnabled(false);
                    child.setClickable(false);
                    child.setLongClickable(false);
                }
            }
        }

    }

    public class MyResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
                Log.d("MYMSG", "In success");
                // success
                Toast.makeText(context, "Congoratulations!! Your tweet is live now", Toast.LENGTH_SHORT).show();
//                final Long tweetId = intent.getLong(TweetUploadService.EXTRA_TWEET_ID);
            } else {
                // failure
                Toast.makeText(context, "Sorry!! Some error occured. Try again later", Toast.LENGTH_SHORT).show();
                Log.d("MYMSG", "In fail");
//                final Intent retryIntent = intent.getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
            }
        }
    }
}
