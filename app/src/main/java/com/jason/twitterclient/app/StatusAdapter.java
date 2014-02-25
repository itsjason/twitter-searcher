package com.jason.twitterclient.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

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
        content.setText(status.text);
        username.setText(status.user.screen_name);
        time.setText(status.created_at);

        return view;
    }
}
