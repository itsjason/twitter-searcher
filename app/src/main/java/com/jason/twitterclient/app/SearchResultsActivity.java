package com.jason.twitterclient.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class SearchResultsActivity extends Activity {

    public static final String EXTRA_STATUSES = "EXTRA_STATUSES";
    private static TwitterSearchService.StatusResult[] statusResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        //TwitterSearchService.StatusResult[] statuses = (TwitterSearchService.StatusResult[]) getIntent().getSerializableExtra(EXTRA_STATUSES);
        ListView tweetList = (ListView) findViewById(R.id.tweet_list);
        tweetList.setAdapter(new StatusAdapter(this, R.id.tweet_list, statusResults));
    }

    public static void setStatusResults(TwitterSearchService.StatusResult[] results) {
        statusResults = results;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

