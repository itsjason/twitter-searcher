package com.jason.twitterclient.app;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by NorrisJ on 2/24/14.
 */
public class TwitterSearchService {

    private String accessToken;

    public String getToken(String authcreds) throws Exception {
        RestClient client = new RestClient("https://api.twitter.com/oauth2/token");
        client.AddHeader("Authorization", "Basic " + authcreds);
        client.AddHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        client.AddParam("grant_type", "client_credentials");
        client.Execute(RestClient.RequestMethod.POST);

        Gson gson = new Gson();
        TokenResult result = gson.fromJson(client.getResponse(), TokenResult.class);

        if(!result.token_type.equalsIgnoreCase("bearer")) {
            throw new Exception("Received invalid token type");
        }

        this.accessToken = result.access_token;
        return result.access_token;
    }

    public StatusResult[] searchForTweets(String searchText) throws Exception {
        RestClient client = new RestClient("https://api.twitter.com/1.1/search/tweets.json");
        client.AddHeader("Authorization", "Bearer " + accessToken);
        client.AddParam("q", searchText);
        client.Execute(RestClient.RequestMethod.GET);
        String result = client.getResponse();

        Gson gson = new Gson();
        StatusResult[] results = gson.fromJson(result, SearchResults.class).statuses;
        return results;
    }

    class TokenResult  {
        public String token_type;
        public String access_token;

    }

    public class SearchResults {
        public StatusResult[] statuses;
    }

    public class StatusResult implements Serializable {
        public String created_at;
        public String text;
        public TwitterUser user;
    }

    public class TwitterUser implements Serializable {
        public String profile_image_url;
        public String name;
        public String screen_name;
    }
}

