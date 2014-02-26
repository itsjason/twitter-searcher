package com.jason.twitterclient.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
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
    private ProgressDialog alertDialog;
    private EditText searchField;
    private Button submitButton;
    private TwitterSearchService searchService;
    private boolean haveToken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        searchService = new TwitterSearchService();
        getTwitterToken();
        submitButton = (Button) findViewById(R.id.search_button);
        searchField = (EditText) findViewById(R.id.search_field);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = searchField.getText().toString();
                doSearch(searchText);
            }
        });

        Button clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchField.setText("");
                searchField.requestFocus();
            }
        });

        searchField.requestFocus();
        searchField.setSelection(searchField.getText().length());
    }

    private void getTwitterToken() {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        return searchService.getToken(getString(R.string.twitter_auth_creds));
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(String token) {
                    super.onPostExecute(token);
                    haveToken = token != null;
                }
            }.execute();
    }

    private void doSearch(final String searchText) {

        alertDialog  = new ProgressDialog(this);
        alertDialog.setTitle("Please Hold");
        alertDialog.setMessage("Searching the Twitterverse...");
        alertDialog.setIndeterminate(true);
        alertDialog.show();

        while(!haveToken) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    waitForTwitterToken();
                    TwitterSearchService.StatusResult[] results = searchService.searchForTweets(searchText);
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

    private void waitForTwitterToken() {

    }
}
