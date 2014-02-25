package com.jason.twitterclient.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "MAIN";
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        Button submitButton = (Button) findViewById(R.id.search_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.search_field);
                String searchText = editText.getText().toString();
                doSearch(searchText);
            }
        });
    }

    private void doSearch(final String searchText) {

        alertDialog = new AlertDialog.Builder(this)
                .setMessage("Searching the Twitterverse...")
                .create();
        alertDialog.show();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    TwitterSearchService service = new TwitterSearchService();
                    String token = service.getToken(getString(R.string.twitter_auth_creds));
                    TwitterSearchService.StatusResult[] results = service.searchForTweets(searchText);
                    Intent listIntent = new Intent(MainActivity.this, SearchResultsActivity.class);
                    //listIntent.putExtra(SearchResultsActivity.EXTRA_STATUSES, results);
                    SearchResultsActivity.setStatusResults(results);
                    startActivity(listIntent);
                } catch (Exception e) {
                    //Toast.makeText(MainActivity.this, "Error contacting Twitter", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                alertDialog.dismiss();
            }
        }.execute();
    }
}
