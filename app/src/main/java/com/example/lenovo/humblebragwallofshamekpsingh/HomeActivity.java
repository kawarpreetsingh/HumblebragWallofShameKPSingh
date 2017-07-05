package com.example.lenovo.humblebragwallofshamekpsingh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    ListView list;
    AlertDialog.Builder msg_dialog;
    ProgressBar progressBar;
    FloatingActionButton fab;
    LinearLayout linearLayout;
//    ArrayList<String> usernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        usernames=new ArrayList<>();
        progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        linearLayout = (LinearLayout) (findViewById(R.id.linearLayout));
        fab = (FloatingActionButton) (findViewById(R.id.fab));

        // This code will help to make user timeline of @Humblebrag
        final UserTimeline userTimeline = new UserTimeline.Builder()
                .screenName("Humblebrag")
                .build();
        //The adapter is made custom here to fetch the information on click of its items
        final CustomTweetTimelineListAdapter adapter = new CustomTweetTimelineListAdapter(this, userTimeline);
        list = (ListView) (findViewById(android.R.id.list));
        list.setAdapter(adapter);

    }

    // Logout button is provided in menu as showAsAction
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_1, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Two dialog boxes will appear to complete the whole task of logout
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            msg_dialog = new AlertDialog.Builder(this);
            msg_dialog.setCancelable(false);
            msg_dialog.setTitle("Confirmation");
            msg_dialog.setIcon(R.drawable.alert_2_icon);
            msg_dialog.setMessage("Are you sure to logout?");
            MyListener ml = new MyListener();
            msg_dialog.setPositiveButton("Yes", ml);
            msg_dialog.setNegativeButton("No", ml);
            msg_dialog.create();
            msg_dialog.show();
        }
        return true;
    }

    // Tweet is shown here by twitter's tweet activity as the custom activity have less features and my TweetActivity didn't worked properly
    public void tweet(View v) {
        Toast.makeText(this, "Wait for a moment..", Toast.LENGTH_SHORT).show();
        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text("just setting up my Twitter Kit.");
        builder.show();
    }

    class CustomTweetTimelineListAdapter extends TweetTimelineListAdapter {

        public CustomTweetTimelineListAdapter(Context context, Timeline<Tweet> timeline) {
            super(context, timeline);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

//            Tweet tweet=getItem(position);
//            String text=tweet.text;
//            String u_name=text.substring(text.indexOf("RT")+4,text.indexOf(":"));
//            if(usernames.get(position)==null)
//             usernames.add(position,u_name);
//              Log.d("MYMSG","Position : "+position);
            //disable subviews
            if (view instanceof ViewGroup) {
                disableViewAndSubViews((ViewGroup) view);
            }

            //enable root view and attach custom listener
            view.setEnabled(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // I get the text that is printed in the tweet and from there I ge the username and then send it to the next activity through intent
                    Tweet tweet = getItem(position);
                    String text = tweet.text;
                    String username = text.substring(text.indexOf("RT") + 4, text.indexOf(":"));
//                    Toast.makeText(context, "Username:"+username, Toast.LENGTH_SHORT).show();
                    Intent in = new Intent(getApplicationContext(), ProfileActivity.class);
                    in.putExtra("username", username);
//                    in.putExtra("usernames",usernames);
                    startActivity(in);
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
            // Here as after loading of timeline this function will execute at last so changes in the visibility of views are done here
            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        }

    }

    class MyListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
//                ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                msg_dialog.setIcon(R.drawable.info_icon);
                msg_dialog.setTitle("Message");
                msg_dialog.setMessage("Logout Successful");
                msg_dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
                msg_dialog.setNegativeButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                msg_dialog.create();
                msg_dialog.show();
            }
        }
    }

    public class MyResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
                Log.d("MYMSG", "In success");
                // success
//                Toast.makeText(context, "Congoratulations!! Your tweet is live now", Toast.LENGTH_SHORT).show();
//                final Long tweetId = intent.getLong(TweetUploadService.EXTRA_TWEET_ID);
            } else {
                // failure
//                Toast.makeText(context, "Sorry!! Some error occured. Try again later", Toast.LENGTH_SHORT).show();
                Log.d("MYMSG", "In fail");
//                final Intent retryIntent = intent.getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
            }
        }
    }
}
