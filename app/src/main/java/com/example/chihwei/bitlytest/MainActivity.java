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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView mTotalClicks;
    private ListView mListView;
    private Button mButton;
    private EditText newURL;
    private ArrayList<HashMap<String, String>> bitLinkList;
    String bitlyHistoryUrl = "https://api-ssl.bitly.com/v3/user/link_history?access_token=e5137bf26b61baa3c51779306899732bd0f3df44";
    String bitlyUserClicks = "https://api-ssl.bitly.com/v3/user/clicks?access_token=e5137bf26b61baa3c51779306899732bd0f3df44";
    String bitlylinkClicks;
    String bitlyNewUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bitLinkList = new ArrayList<>();
        mTotalClicks = (TextView) findViewById(R.id.totalClicks);
        mListView = (ListView) findViewById(R.id.list);
        mButton = (Button) findViewById(R.id.button);
        new GetAPIResponse().execute("History");
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
                        String prefix = "https://api-ssl.bitly.com/v3/user/link_save?access_token=e5137bf26b61baa3c51779306899732bd0f3df44&longUrl=";
                        bitlyNewUrl = prefix + newURL.getText();
                        Log.e("dialog", "new url: " + bitlyNewUrl);
                        new GetAPIResponse().execute("Save");
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
        boolean showToast;
        int totalClicks;
        @Override
        protected Void doInBackground(String... params) {

            HttpHandler sh = new HttpHandler();
            if(params[0].equals("Save")){
                String jsonSaveStr = sh.makeServiceCall(bitlyNewUrl);
                Log.e(TAG, "[Save] Response: " + jsonSaveStr);
                if(jsonSaveStr != null){
                    try{
                        JSONObject jsonSaveObj = new JSONObject(jsonSaveStr);
                        if(isDuplicateLink(jsonSaveObj)) {
                            showToast = true;
                        }else{
                            showToast = false;
                        }
                    }catch (final JSONException e) {
                        Log.e(TAG, "[Save] Json parsing error: " + e.getMessage());
                    }
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception e)
                {
                    Log.e("Categories Section:", e.getMessage());
                }
            }

            String jsonUserClicksStr = sh.makeServiceCall(bitlyUserClicks);
            Log.e(TAG, "[Total clicks] Response: " + jsonUserClicksStr);
            if(jsonUserClicksStr != null){
                try{
                    JSONObject jsonUserClicks = new JSONObject(jsonUserClicksStr);
                    totalClicks = getTotalClicks(jsonUserClicks);
                }catch (final JSONException e){
                    Log.e(TAG, "[Total clicks] Json Parsing error: " + e.getMessage());
                }

            }


            String jsonHistoryStr = sh.makeServiceCall(bitlyHistoryUrl);
            Log.e(TAG, "[Hitory] Response: " + jsonHistoryStr);

            if(jsonHistoryStr != null){
                try{
                    JSONObject jsonHistoryObj = new JSONObject(jsonHistoryStr);
                    getHistory(jsonHistoryObj);
                }catch (final JSONException e) {
                    Log.e(TAG, "[History] Json parsing error: " + e.getMessage());

                }
            }

            for(int i = 0; i < bitLinkList.size(); i++) {
                String link = bitLinkList.get(i).get("link");
                bitlylinkClicks = "https://api-ssl.bitly.com/v3/link/clicks?access_token=e5137bf26b61baa3c51779306899732bd0f3df44&link=" + link;
                String jsonLinkClicksStr = sh.makeServiceCall(bitlylinkClicks);
                Log.e(TAG, "[Clicks] Response: " + jsonLinkClicksStr);

                if (jsonLinkClicksStr != null) {
                    try {
                        JSONObject jsonClicksObj = new JSONObject(jsonLinkClicksStr);
                        String clicks = getBitlinkClicks(jsonClicksObj);
                        bitLinkList.get(i).put("clicks", clicks);

                    } catch (final JSONException e) {
                        Log.e(TAG, "[Clicks] Json parsing error: " + e.getMessage());
                    }
                }

            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mTotalClicks.setText("TOTAL CLICKS: " + totalClicks);

            ListAdapter adapter = new SimpleAdapter(MainActivity.this, bitLinkList,
                    R.layout.list_item, new String[]{ "title", "link", "long_url", "clicks"},
                    new int[]{R.id.title, R.id.link, R.id.long_url, R.id.clicks});
            mListView.setAdapter(adapter);
            Log.e(TAG, "showToast : " + showToast);
            if(showToast){
                Toast toast = Toast.makeText(MainActivity.this, "The link has been saved", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private String getBitlinkClicks(JSONObject jsonObj) throws JSONException{
        String TAG = "Bitlink Clicks";
        JSONObject data = jsonObj.getJSONObject("data");
        String bitlinkClicks = data.getString("link_clicks");
        Log.e(TAG, "link_clicks:" + bitlinkClicks);
        return bitlinkClicks;
    }

    private int getTotalClicks(JSONObject jsonObj) throws JSONException{
        String TAG = "Total Clicks";
        JSONObject data = jsonObj.getJSONObject("data");
        int totalClicks = data.getInt("total_clicks");
        Log.e(TAG, "total_clicks: " + totalClicks);

        return totalClicks;
    }

    private void getHistory(JSONObject jsonObj) throws JSONException {
        String TAG = "History";

        // Getting link history object
        JSONObject data = jsonObj.getJSONObject("data");
        JSONArray link_history = data.getJSONArray("link_history");
        Log.e(TAG, "link_history: " + link_history);
        bitLinkList.clear();
        // Getting attributes
        for (int i = 0; i < link_history.length(); i++) {
            JSONObject links = link_history.getJSONObject(i);
            String link = links.getString("link");
            String long_url = links.getString("long_url");
            String title = links.getString("title");
            if(title.equals("")){
                title = "Untitled";
            }

            HashMap<String, String> bitlink = new HashMap<>();

            bitlink.put("title", title);
            bitlink.put("link", link);
            bitlink.put("long_url", long_url);

            bitLinkList.add(bitlink);
        }
        Log.e(TAG, "bitLinkList: " + bitLinkList);
    }

    private boolean isDuplicateLink(JSONObject jsonObj) throws JSONException {
        String TAG = "Save";

        // Getting link history object
        JSONObject data = jsonObj.getJSONObject("data");
        JSONObject link_save = data.getJSONObject("link_save");
        Log.e(TAG, "link_save: " + link_save);

        String new_link = link_save.getString("new_link");
        Log.e(TAG, "Duplicate Link: " + new_link);

        boolean isDuplicate;

        if(new_link.equals("1")){
            isDuplicate = false;
        }else{
            isDuplicate = true;
        }

        return isDuplicate;
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
