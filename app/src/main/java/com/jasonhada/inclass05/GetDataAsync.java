package com.jasonhada.inclass05;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GetDataAsync extends AsyncTask<String, Void, ArrayList<Article>> {

    IData iData;

    public GetDataAsync(IData iData) {
        this.iData = iData;
    }

    @Override
    protected void onPreExecute() {
        iData.showProgressDialog("Loading News...");
        super.onPreExecute();
    }

    @Override
    protected ArrayList<Article> doInBackground(String... params) {
        HttpURLConnection connection = null;
        ArrayList<Article> result = new ArrayList<>();
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String json = IOUtils.toString(connection.getInputStream(), "UTF8");

                JSONObject root = new JSONObject(json);
                JSONArray articles = root.getJSONArray("articles");
                for (int i=0;i<articles.length();i++) {
                    JSONObject articleJson = articles.getJSONObject(i);
                    Article article = new Article();
                    article.author = articleJson.getString("author");
                    article.description = articleJson.getString("description");
                    article.url = articleJson.getString("url");
                    article.title = articleJson.getString("title");
                    article.urlToImage = articleJson.getString("urlToImage");
                    article.publishedAt = articleJson.getString("publishedAt");
                    article.content = articleJson.getString("content");

                    JSONObject sourceJson = articleJson.getJSONObject("source");
                    Source source = new Source();
                    source.id = sourceJson.getString("id");
                    source.name = sourceJson.getString("name");

                    article.source = source;
                    result.add(article);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Article> result) {
        iData.hideProgressDialog();
        iData.handleData(result);
    }

    public static interface IData {
        public void handleData(ArrayList<Article> data);
        public void showProgressDialog(String loadMessage);
        public void hideProgressDialog();

    }
}