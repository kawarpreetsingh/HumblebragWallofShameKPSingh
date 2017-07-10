package com.example.lenovo.humblebragwallofshamekpsingh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class HomeActivity extends AppCompatActivity {

    AlertDialog.Builder msg_dialog;
    ProgressBar progressBar;
    FloatingActionButton fab;
    LinearLayout linearLayout;
    RecyclerView recyclerView;
    ArrayList<Tweet> al, arrayListAdapter;
    MyAdapter myAdapter;
    boolean ready = false, adapterSet = false;
    SwipyRefreshLayout swipyRefreshLayout;
    LinearLayoutManager linearLayoutManager;
    int arrayListLimit = 10;
//    ArrayList<String> usernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        usernames=new ArrayList<>();
        progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        linearLayout = (LinearLayout) (findViewById(R.id.linearLayout));
        fab = (FloatingActionButton) (findViewById(R.id.fab));
        recyclerView = (RecyclerView) (findViewById(R.id.recyclerView));
        al = new ArrayList<>();
        // Giving memory to RecyclerView, LinearLayoutManager and Custom Adapter
        linearLayoutManager = new LinearLayoutManager(this);
        arrayListAdapter = new ArrayList<>();
        swipyRefreshLayout = (SwipyRefreshLayout) (findViewById(R.id.swipyRefreshLayout));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        myAdapter = new MyAdapter(getApplicationContext(), arrayListAdapter);

        // This code will help to make user timeline of @Humblebrag
        //callling this class in thread
        new Thread(new FetchTimeline()).start();
        // To use upper and lower scroll in our Activity for refresh
        swipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    new Thread(new FetchTimeline()).start();
                } else {
                    //To create the effect of scrolling bigger ArrayList is broken into an ArrayList carrying 1/10th of data to create the effect of 10 swipes
                    if (arrayListLimit < 100) {
                        arrayListLimit += 10;
                        fillArrayListAdapter();
                        recyclerView.smoothScrollToPosition(arrayListLimit - 10);
                        myAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HomeActivity.this, "No more tweets", Toast.LENGTH_SHORT).show();
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipyRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
// To judge the click on item of RecyclerView according to our requirement, custom class of this listener named RecyclerTouchListener is build
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent in = new Intent(getApplicationContext(), ProfileActivity.class);
                in.putExtra("position", position);
                in.putExtra("arrayListSize", arrayListAdapter.size());
                startActivity(in);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    // Filling the small arraylist on scrolling or other requirement
    void fillArrayListAdapter() {
        arrayListAdapter.clear();
        for (int i = 0; i < arrayListLimit; i++) {
            arrayListAdapter.add(al.get(i));
        }
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

    // Tweet is shown here is shown by my TweetActivity that was creating problems earlier but now working properly
    public void tweet(View v) {
        //After click on floating button of tweet  TweetActivity will open up for further acccess
        startActivity(new Intent(this, TweetActivity.class));
    }

    // After fetching the timeline according to our requirement, the necessary data is retrieved
    class FetchTimeline implements Runnable {

        @Override
        public void run() {
            //Getting the access to API Client
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            //Getting the StatusServices
            StatusesService statusesService = twitterApiClient.getStatusesService();
            //Choosing user timeline service out of it
            Call<List<Tweet>> call = statusesService.userTimeline(null, "@Humblebrag", 104, null, null, null, null, null, true);
            // the callback will return list of tweets on success and from it any information regarding that user can be fetched
            call.enqueue(new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                    List<Tweet> tweets = result.data;
                    al.clear();
                    for (int i = 0; i < tweets.size(); i++) {
                        // Tweet by Humblebrag
                        Tweet tweet = tweets.get(i);
//                    User user = tweet.user;
                        //Retweets by Humblebrag that is the tweets of other people
                        Tweet retweetedStatus = tweet.retweetedStatus;
                        if (retweetedStatus != null) {
//                        User retweetedUser = retweetedStatus.user;
                            al.add(retweetedStatus);
//                            Log.d("MYMSG", i + "th tweet : " + retweetedStatus);
                        } else {
                            al.add(tweet);
//                            Log.d("MYMSG", i + "th tweet : " + tweet);
                        }
                    }
                    ready = true;
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.d("MYMSG", "Some error occured");
                }
            });
            //Waits till the data  is fetched to do further tasks
            while (!ready) {
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fillArrayListAdapter();
                    if (adapterSet) {
                        myAdapter.notifyDataSetChanged();
                    } else {
                        recyclerView.setAdapter(myAdapter);
                        adapterSet = true;
                    }
                    //Making progress bar invisible to create a great user experience
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipyRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }).start();
                }
            });

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

    //Custom recycler view adapter with view holder managed properly to give the required use that I want
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private LayoutInflater inflater;
        private List<Tweet> al;
        private Context context;
        private int count = 0;

        MyAdapter(Context context, List<Tweet> al) {
            this.context = context;
            this.al = al;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.layout_small_view, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Tweet tweet = al.get(position);
            holder.textViewName.setText(tweet.user.name);
            holder.textViewScreenName.setText("@" + tweet.user.screenName);
            holder.textViewText.setText(tweet.text);
            Picasso.with(context).load(tweet.user.profileImageUrl).into(holder.imageViewProfilePic);
            setFadeAnimation(holder.itemView);
        }

        @Override
        public int getItemCount() {
            return arrayListLimit;
        }

        // used to show fade animation, Will only give that effect when new loading is done
        private void setFadeAnimation(View view) {
            if (count == 0) {
                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(1000);
                view.startAnimation(anim);
                count++;
            }
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textViewName, textViewText, textViewScreenName;
            ImageView imageViewProfilePic;

            public MyViewHolder(View itemView) {
                super(itemView);
                textViewName = (TextView) (itemView.findViewById(R.id.textViewName));
                textViewScreenName = (TextView) (itemView.findViewById(R.id.textViewScreenName));
                textViewText = (TextView) (itemView.findViewById(R.id.textViewText));
                imageViewProfilePic = (ImageView) (itemView.findViewById(R.id.imageViewProfilePic));
            }
        }
    }
}
