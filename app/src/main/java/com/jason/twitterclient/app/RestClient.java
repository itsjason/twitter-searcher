package com.jason.twitterclient.app;

import android.content.res.Resources;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import com.flurry.android.FlurryAgent;

/**
 * Created by carlsona on 7/29/13.
 */
public class RestClient {

    private Header[] responseHeaders;
    //private static String accessToken;
    //private static long accessTokenExpirationTime;
    private InputStream instream;

    public enum RequestMethod {POST, GET, PUT}

    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;

    private String url;

    private int responseCode;
    private String message;

    private String response;

    public String getResponse() {
        return response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public RestClient(String url) {
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
        //this.context = context;
    }

    public Map<String, String> getHeaders() {
        HashMap<String, String> results = new HashMap<String, String>();
        for (Header header : responseHeaders) {
            results.put(header.getName(), header.getValue());
        }
        return results;
    }

    public void AddParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    public void AddParam(String name, int value) {
        params.add(new BasicNameValuePair(name, Integer.toString(value)));
    }

    public void AddHeader(String name, String value) {
        headers.add(new BasicNameValuePair(name, value));
    }

    public void Execute(RequestMethod method) throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("URL", this.url);
        parameters.put("Method", method.name());
        //FlurryAgent.logEvent(Events.REST_REQUEST, parameters, true);

        //make sure we have valid token
        //if (tokenIsInvalid()) {
            //Logger.logAdbVerbose("REST", "Token is INVALID. Token: " + accessToken + " expired at " + accessTokenExpirationTime);
            //getToken();
        //} else {
            //Logger.logAdbVerbose("REST", "Token is valid. Token: " + accessToken + " expires at " + accessTokenExpirationTime);
       // }

        //Logger.logAdbDebug("REST", "Making call to: " + this.url);

        //this.AddHeader("Authorization", "Bearer " + accessToken);

        switch (method) {
            case GET: {
                sendGet();
                break;
            }

            case POST: {
                sendPost();
                break;
            }
            case PUT: {
                throw new Exception("Not Implemented");
            }
        }

        //FlurryAgent.endTimedEvent(Events.REST_REQUEST);
    }

    //private boolean tokenIsInvalid() {
        //if (accessToken == null) return true;
        //if (accessTokenExpirationTime < System.currentTimeMillis()) return true;
      //  return false;
    //}

    private void sendPost() throws Exception {
        HttpPost request = new HttpPost(url);
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }

        if (!params.isEmpty()) {
            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        }

        executeRequest(request, url);
    }

    private void sendGet() throws Exception {
        //add parameters
        String combinedParams = "";
        if (!params.isEmpty()) {
            combinedParams += "?";
            for (NameValuePair p : params) {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if (combinedParams.length() > 1) {
                    combinedParams += "&" + paramString;
                } else {
                    combinedParams += paramString;
                }
            }
        }

        HttpGet request = new HttpGet(url + combinedParams);

        //add headers
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }

        executeRequest(request, url);
    }

    private void executeRequest(HttpUriRequest request, String url) throws Exception {
        //Logger.logAdbVerbose("REST", "Sending Request....");

        HttpClient client = new DefaultHttpClient();

        HttpResponse httpResponse;

        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            //Logger.logAdbVerbose("REST", "HTTP Response Code: " + responseCode);
            message = httpResponse.getStatusLine().getReasonPhrase();
            //Logger.logAdbVerbose("REST", "HTTP Message: " + message);
            HttpEntity entity = httpResponse.getEntity();

            responseHeaders = httpResponse.getAllHeaders();

            if (entity != null) {

                instream = entity.getContent();

                boolean isImage = false;
                for (Header header : responseHeaders) {
                    if (!header.getName().equalsIgnoreCase("Content-Type")) continue;
                    //Logger.logAdbVerbose("REST", "Content Type: " + header.getValue());
                    if (header.getValue().contains("image")) isImage = true;
                }

                if (!isImage)
                    response = readFully(instream);
                else
                    //Logger.logAdbVerbose("REST", "Got image response.");

                //Logger.logAdbVerbose("REST", "HTTP Response: " + response);

                // Closing the input stream will trigger connection release
                if (!isImage)
                    instream.close();
            }

        } catch (ClientProtocolException e) {
            //Logger.logAdbError("REST", "ERROR: " + e.getLocalizedMessage());
            client.getConnectionManager().shutdown();
            throw e;
        } catch (IOException e) {
            //Logger.logAdbError("REST", "ERROR: " + e.getLocalizedMessage());
            client.getConnectionManager().shutdown();
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            //Logger.logAdbError("REST", "Other error: " + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String readFully(InputStream inputStream)
            throws IOException {
        return new String(readBytes(inputStream));
    }

    private byte[] readBytes(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }

    private static String aconvertStreamToString(InputStream is) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e("REST", "Rest Error. Current Data: " + sb.toString());
            throw e;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /*public String getToken() throws Exception {
        //Logger.logAdbInfo("REST", "Getting Resources....");

        FCSAMobileApplication app = FCSAMobileApplication.getInstance();
        Resources resources = app.getResources();
        String encodedKey = resources.getString(R.string.api_key);
        String serviceUsername = resources.getString(R.string.api_username);
        String servicePassword = resources.getString(R.string.api_password);
        String tokenUrl = resources.getString(R.string.token_url);
        //Logger.logAdbInfo("REST", "Newing up the RestClient");

        //Logger.logAdbInfo("REST", "Getting Token From: " + tokenUrl);
        RestClient client = new RestClient(tokenUrl);

        //Logger.logAdbInfo("REST", "Adding Headers. Key: " + encodedKey);
        client.AddHeader("Authorization", "Basic " + encodedKey);
        client.AddHeader("Content-Type", "application/x-www-form-urlencoded");

        client.AddParam("grant_type", "password");
        client.AddParam("username", serviceUsername);
        client.AddParam("password", servicePassword);
        client.AddParam("scope", "PRODUCTION");

        //Logger.logAdbInfo("REST", "Sending POST To: " + tokenUrl);
        client.sendPost();
        String response = client.getResponse();
        //Logger.logAdbInfo("REST", "Got Response For Token:" + response);
        Gson gson = new Gson();
        TokenResult tokenResult = gson.fromJson(response, TokenResult.class);
        accessToken = tokenResult.access_token;
        accessTokenExpirationTime = System.currentTimeMillis() + (tokenResult.expires_in * 1000);
        //Logger.logAdbInfo("k2", "TOKEN Service Response Code: " + client.getResponseCode() + " Token: " + accessToken);
        return accessToken;
    }*/
}