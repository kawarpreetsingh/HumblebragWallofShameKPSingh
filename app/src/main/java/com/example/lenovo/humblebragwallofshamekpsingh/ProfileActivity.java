package com.example.lenovo.humblebragwallofshamekpsingh;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class ProfileActivity extends AppCompatActivity {

    ArrayList<Tweet> al;
    boolean ready = false;
    RecyclerViewPager recyclerViewPager;
    ProgressBar progressBar;
    MyAdapter myAdapter;
    LinearLayoutManager linearLayoutManager;
    ImageView iconLeft, iconRight;
    int arrayListSize;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Applying back button and logo in the action bar of an activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // to receive the data from previous activity like position used to open in a profile from a list of profiles with size arrayListSize
        Intent in = getIntent();
        final int position = in.getIntExtra("position", -1);
        arrayListSize = in.getIntExtra("arrayListSize", -1);
        if (arrayListSize >= 30) {
            arrayListSize++;
        }
        if (arrayListSize > 71) {
            arrayListSize += 2;
        }
// Thread to fetch timeline again but only for the required ArrayList size
        new Thread(new FetchTimeline(arrayListSize, position)).start();
        al = new ArrayList<>();
        recyclerViewPager = (RecyclerViewPager) (findViewById(R.id.recyclerViewPager));
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPager.setHasFixedSize(true);
        recyclerViewPager.setLayoutManager(linearLayoutManager);
        myAdapter = new MyAdapter(this, al);
        frameLayout = (FrameLayout) (findViewById(R.id.frameLayout));
        recyclerViewPager.setAdapter(myAdapter);
        progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        iconLeft = (ImageView) (findViewById(R.id.iconLeft));
        iconRight = (ImageView) (findViewById(R.id.iconRight));

        // Left and right icons used for navigation through users
        iconLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = recyclerViewPager.getCurrentPosition();
                if (pos == 0) {
                    Toast.makeText(ProfileActivity.this, "This is the first profile", Toast.LENGTH_SHORT).show();
                } else {
                    recyclerViewPager.smoothScrollToPosition(pos - 1);
                }
            }
        });

        iconRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = recyclerViewPager.getCurrentPosition();
                if (arrayListSize >= 30 && pos == arrayListSize - 2 || arrayListSize > 71 && pos == arrayListSize - 4 || pos == arrayListSize - 1) {
                    Toast.makeText(ProfileActivity.this, "This is the last profile", Toast.LENGTH_SHORT).show();
                } else {
                    recyclerViewPager.smoothScrollToPosition(pos + 1);
                }
            }
        });
    }

    // After clicking on back button this activity will close
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //Timeline runnable to fetch tweets again
    class FetchTimeline implements Runnable {

        int arrayListSize;
        int position;

        FetchTimeline(int arrayListSize, int position) {
            this.arrayListSize = arrayListSize;
            this.position = position;
        }

        // Now ArrayList al will be used as data sourcec here to display different user data
        @Override
        public void run() {
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            StatusesService statusesService = twitterApiClient.getStatusesService();
            Call<List<Tweet>> call = statusesService.userTimeline(null, "@Humblebrag", arrayListSize, null, null, null, null, null, true);
            call.enqueue(new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                    List<Tweet> tweets = result.data;
                    al.clear();
                    for (int i = 0; i < tweets.size(); i++) {
                        Tweet tweet = tweets.get(i);
//                    User user = tweet.user;
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
            while (!ready) {
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayoutManager.scrollToPosition(position);
                    myAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    // Custom adapter and view holder to deal with RecyclerViewPager here
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private LayoutInflater inflater;
        private List<Tweet> al;
        private Context context;
        private int lastPosition = -1;

        MyAdapter(Context context, List<Tweet> al) {
            this.context = context;
            this.al = al;
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.layout_profile_view, parent, false);
            MyAdapter.MyViewHolder myViewHolder = new MyAdapter.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            //All the views are bound with data source here
            Tweet tweet = al.get(position);
            holder.name.setText(tweet.user.name);
            holder.twitterHandle.setText("@" + tweet.user.screenName);
            holder.bio.setText(tweet.user.description);
            if (tweet.user.location.trim().isEmpty()) {
                holder.location.setText("No Location Available");
            } else {
                holder.location.setText(tweet.user.location);
            }
            holder.followers.setText(String.valueOf(tweet.user.followersCount));
            holder.following.setText(String.valueOf(tweet.user.friendsCount));
            holder.tweets.setText(String.valueOf(tweet.user.statusesCount));
            Picasso.with(context).load(tweet.user.profileImageUrl).into(holder.profilePic);
            Picasso.with(context).load(tweet.user.profileBackgroundImageUrl).into(holder.coverPic);
            setAnimation(holder.itemView, position);
        }

        @Override
        public int getItemCount() {
            return al.size();
        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position != lastPosition) {
                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(1000);
                viewToAnimate.startAnimation(anim);
                lastPosition = position;
            }
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView name, twitterHandle, bio, location, following, followers, tweets;
            ImageView profilePic, coverPic, iconLocation;

            public MyViewHolder(View itemView) {
                super(itemView);
                //All the views are given memory
                name = (TextView) itemView.findViewById(R.id.name);
                twitterHandle = (TextView) itemView.findViewById(R.id.twitterHandle);
                bio = (TextView) itemView.findViewById(R.id.bio);
                location = (TextView) itemView.findViewById(R.id.location);
                followers = (TextView) itemView.findViewById(R.id.followers);
                following = (TextView) itemView.findViewById(R.id.following);
                tweets = (TextView) itemView.findViewById(R.id.tweets);
                profilePic = (ImageView) itemView.findViewById(R.id.profilePic);
                coverPic = (ImageView) itemView.findViewById(R.id.coverPic);
                iconLocation = (ImageView) itemView.findViewById(R.id.iconLocation);
            }
        }
    }


}
