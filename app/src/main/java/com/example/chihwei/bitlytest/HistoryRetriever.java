package com.example.chihwei.bitlytest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class HistoryRetriever extends AsyncTask<Object, Object, Void> {
    String TAG = "History";

    private Context context;
    private ListView lv;
    private ArrayList<HashMap<String, String>> bitLinkList;

    public HistoryRetriever(Context context, ListView lv) {
        this.context = context;
        this.lv = lv;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Object... params) {
        String bitlyHistoryUrl = "https://api-ssl.bitly.com/v3/user/link_history?access_token=e5137bf26b61baa3c51779306899732bd0f3df44";
        HttpHandler sh = new HttpHandler();
        String jsonStr = sh.makeServiceCall(bitlyHistoryUrl);
        bitLinkList = new ArrayList<>();
        Log.e(TAG, "Response from url: " + jsonStr);

        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

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
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());

            }
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        ListAdapter adapter = new SimpleAdapter(context, bitLinkList,
                R.layout.list_item, new String[]{ "title", "link", "long_url"},
                new int[]{R.id.title, R.id.link, R.id.long_url});
        lv.setAdapter(adapter);
    }



}
