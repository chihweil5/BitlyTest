package com.example.chihwei.bitlytest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private Button mButton;
    private EditText newURL;
    private ArrayList<HashMap<String, String>> bitLinkList;
    String bitlyHistoryUrl = "https://api-ssl.bitly.com/v3/user/link_history?access_token=e5137bf26b61baa3c51779306899732bd0f3df44";
    String bitlyClicks = "";
    String bitlyNewUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bitLinkList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.list);
        mButton = (Button) findViewById(R.id.button);
        new GetAPIResponse().execute(bitlyHistoryUrl);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked();
            }
        });

    }

    public void buttonClicked() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        newURL = (EditText) promptView.findViewById(R.id.newUrl);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //resultText.setText("Hello, " + editText.getText());
                        String prefix = "https://api-ssl.bitly.com/v3/user/link_save?access_token=e5137bf26b61baa3c51779306899732bd0f3df44&longUrl=";
                        bitlyNewUrl = prefix + newURL.getText();
                        Log.e("dialog", "new url: " + bitlyNewUrl);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private class GetAPIResponse extends AsyncTask<String, Void, Void> {
        String TAG = "API";

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    if(url.indexOf("/v3/user/link_history") > -1){
                        getHistory(jsonObj);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, bitLinkList,
                    R.layout.list_item, new String[]{ "title", "link", "long_url"},
                    new int[]{R.id.title, R.id.link, R.id.long_url});
            mListView.setAdapter(adapter);
        }
    }

    private void getHistory(JSONObject jsonObj) throws JSONException {
        String TAG = "History";
        // Getting data object
        JSONObject data = jsonObj.getJSONObject("data");
        JSONArray link_history = data.getJSONArray("link_history");
        Log.e(TAG, "link_history: " + link_history);

        // looping through All data
        for (int i = 0; i < link_history.length(); i++) {
            JSONObject attr = link_history.getJSONObject(i);
            String link = attr.getString("link");
            String long_url = attr.getString("long_url");
            String title = attr.getString("title");
            if(title.equals("")){
                title = "Untitled";
            }

            HashMap<String, String> bitlink = new HashMap<>();

            // adding each child node to HashMap key => value
            bitlink.put("title", title);
            bitlink.put("link", link);
            bitlink.put("long_url", long_url);


            // adding contact to contact list
            bitLinkList.add(bitlink);
        }
        Log.e(TAG, "bitLinkList: " + bitLinkList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
