package com.jasonhada.inclass05;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity {

        String[] categories = new String[7];
        int articleIndex;
        ArrayList<Article> articles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categories[0] = "business";
        categories[1] = "entertainment";
        categories[2] = "general";
        categories[3] = "health";
        categories[4] = "science";
        categories[5] = "sports";
        categories[6] = "technology";

        ImageButton prev_btn = (ImageButton) findViewById(R.id.prev_btn);
        ImageButton next_btn = (ImageButton) findViewById(R.id.next_btn);

        prev_btn.setVisibility(View.INVISIBLE);
        next_btn.setVisibility(View.INVISIBLE);

        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "index is " + articleIndex);
                if(articleIndex == 0) {
                    Log.d("demo", "urls size is " + articles.size());
                    articleIndex = articles.size() - 1;
                } else {
                    articleIndex--;
                }
                if(isConnected()){
                    displayArticle(articles.get(articleIndex));
                }
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(articleIndex == articles.size() - 1) {
                    articleIndex = 0;
                } else {
                    articleIndex++;
                }
                if(isConnected()) {
                    displayArticle(articles.get(articleIndex));
                }
            }
        });

        findViewById(R.id.go_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    Toast.makeText(MainActivity.this, "Internet Present", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("Choose a Category").setItems(categories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextView category = findViewById(R.id.category_tv);
                            category.setText(categories[which]);
                            articles = null;
                            String selectedCat = category.getText().toString();
                            new GetDataAsync().execute("https://newsapi.org/v2/top-headlines?country=us&apiKey=d1adb23ff70d40dc91fed1f1ff44c746&category=" + selectedCat);
                        }
                    });
                    alert.create();
                    alert.show();

                } else{
                    Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private void displayArticle(Article article) {

        Log.d("demo", article.urlToImage);
        ImageView imageView = (ImageView) findViewById(R.id.news_iv);
        Picasso.get().load(article.urlToImage).into(imageView);

        TextView title = (TextView) findViewById(R.id.title_tv);
        title.setText(article.title);

        TextView published = (TextView) findViewById(R.id.published_tv);
        published.setText(article.publishedAt);

        TextView details = (TextView) findViewById(R.id.details_tv);
        details.setText(article.description);

        TextView progress = (TextView) findViewById(R.id.progress_tv);
        progress.setText(articleIndex+1 + " out of " + articles.size());

    }

    private class GetDataAsync extends AsyncTask<String, Void, ArrayList<Article>> {
        private ProgressDialog dialog;

        private GetDataAsync() {
            dialog = new ProgressDialog(MainActivity.this);
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
        protected void onPreExecute() {
            dialog.setMessage("Loading News...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<Article> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result.size() > 0) {
                Log.d("demo", result.toString());
                articles = result;
                articleIndex = 0;

                findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
                findViewById(R.id.prev_btn).setVisibility(View.VISIBLE);

                Article article = result.get(articleIndex);
                displayArticle(article);
            } else {
                Toast.makeText(MainActivity.this, "No news found", Toast.LENGTH_SHORT).show();
                Article article = new Article();
                displayArticle(article  );
                Log.d("demo", "empty result");
            }
        }
    }

}
