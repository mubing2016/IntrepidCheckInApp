package app.com.example.mubingliu.intrepidcheckin;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationClickReceiver extends BroadcastReceiver {

    private static final String LOG_POST = NotificationClickReceiver.class.getSimpleName();
    private static final String SLACK_POST_TEXT = "I am here.";
    private static final String WEBHOOK_URL = "https://hooks.slack.com/services/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";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();


    private void post(String url, String json) {
        new RequestAsyncTask().execute(json, url);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_POST, "POST SERVICE START.");
        //pass string to json constructor
        SlackJsonString jsonStr = new SlackJsonString(SLACK_POST_TEXT);
        Gson gson = new Gson();
        String json = gson.toJson(jsonStr, SlackJsonString.class);
        post(WEBHOOK_URL, json);
    }

    public class RequestAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if (params.length < 2) {
                return null;
            }

            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(params[1])
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                //log out post status
                Log.d(LOG_POST, response.code() + " " + response.body().string());
                Log.d(LOG_POST, response.code() + " " + response.message());
            } catch (IOException e) {
                Log.e("TAG", "MESSAGE", e);
            }

            return null;
        }
    }

}