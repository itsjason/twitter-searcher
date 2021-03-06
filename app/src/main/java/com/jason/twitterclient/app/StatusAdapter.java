package com.jason.twitterclient.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusAdapter extends ArrayAdapter<TwitterSearchService.StatusResult> {

    private final Context _context;
    private final TwitterSearchService.StatusResult[] statuses;
    private View view;

    public StatusAdapter(Context context, int resource, TwitterSearchService.StatusResult[] objects) {
        super(context, resource, objects);
        this._context = context;
        this.statuses = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        this.view = convertView;
        if(view ==null) {
            LayoutInflater layoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.status_item, null);
        }

        final ImageView image = (ImageView) view.findViewById(R.id.tweet_image);
        image.setImageBitmap(null);

        TextView content = (TextView) view.findViewById(R.id.tweet_content);
        TextView username = (TextView) view.findViewById(R.id.tweet_user);
        TextView time = (TextView) view.findViewById(R.id.tweet_time);

        final TwitterSearchService.StatusResult status = statuses[position];

        // Convert twitter names to links
        String usernameRegex = "@([a-zA-Z0-9_]+)";
        String usernamesReplaced = status.text.replaceAll(usernameRegex, "<a href=\"https://twitter.com/$1\">$0</a>");

        // Convert hashtags to links
        String finalContent = usernamesReplaced;

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {

                try {
                    URL newurl = new URL(status.user.profile_image_url);
                    return BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                image.setImageBitmap(bitmap);
            }
        }.execute();


        //image.setImageURI(Uri.parse(status.user.profile_image_url));
        Log.i("CONTENT", finalContent);
        content.setText(Html.fromHtml(finalContent));
        Linkify.addLinks(content, Linkify.WEB_URLS);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        username.setText("@" + status.user.screen_name);

        try {
            Log.i("TWEET", "Date: " + status.created_at);
            Date tweetDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy").parse(status.created_at);
            DateTime dateTime = new DateTime(tweetDate).minusHours(5);

            String dateString = dateTime.toString("MM/yy hh:mm aa");
            //time.setText(status.created_at);
            time.setText(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) view;
                String username = textView.getText().toString().substring(1);
                String url = "https://twitter.com/" + username;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                _context.startActivity(browserIntent);
            }
        });

        return view;
    }
}
