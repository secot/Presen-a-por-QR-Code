package com.secot.thiagow.secot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "SeCoTFile";
    public static final String DEF_IP = "192.168.43.188";
    private static String url_all_palestras = "/android_connect/getallpalestras.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PALESTRAS = "palestras";
    private static final String TAG_ID = "id";
    private static final String TAG_TITULO = "titulo";
    private static final String TAG_PALESTRANTE = "palestrante";

    private ListView palestras_list;
    private TextView mainAlert;
    private boolean conErr = false;
    private JSONParser jParser;
    private JSONArray palestrasJson = null;
    private List<Palestra> palestrasList = new ArrayList<Palestra>();
    private ProgressDialog pDialog;
    private String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.secot)));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverIP = settings.getString("serverIP", DEF_IP);

        palestras_list = (ListView) findViewById(R.id.palestras_list);
        new LoadPalestras().execute();
    }

    class LoadPalestras extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mainAlert = (TextView) findViewById(R.id.mainAlert);
            mainAlert.setText("Carregando...");
            mainAlert.setVisibility(View.VISIBLE);
        }


        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            jParser = null;
            jParser = new JSONParser();
            JSONObject json = null;

            json = jParser.makeHttpRequest("http://" + serverIP + url_all_palestras, "GET", params);
            if(jParser.getError()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainAlert.setText("Erro de conexão");
                        mainAlert.setVisibility(View.VISIBLE);
                    }
                });
                conErr = true;
                return null;
            }
            conErr = false;
            // Check your log cat for JSON reponse
            Log.d("Palestras: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    palestrasJson = json.getJSONArray(TAG_PALESTRAS);

                    // looping through All Products
                    for (int i = 0; i < palestrasJson.length(); i++) {
                        JSONObject c = palestrasJson.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_ID);
                        String titulo = c.getString(TAG_TITULO);
                        String palestrante = c.getString(TAG_PALESTRANTE);

                        palestrasList.add(new Palestra(id, titulo, palestrante));
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            MainActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            if(palestrasList.size() == 0 && !conErr){
                mainAlert.setText("Nenhuma palestra foi encontrada");
                mainAlert.setVisibility(View.VISIBLE);
            } else if(!conErr) {
                // dismiss the dialog after getting all products
                mainAlert.setVisibility(View.INVISIBLE);
                // updating UI from Background Thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        final ArrayAdapter<Palestra> arrayAdapter = new ArrayAdapter<Palestra>(
                                MainActivity.this,
                                android.R.layout.simple_list_item_1,
                                palestrasList);

                        palestras_list.setAdapter(arrayAdapter);
                        palestras_list.setClickable(true);
                        palestras_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View item,
                                                    final int position, long id) {
                                Palestra palestra = arrayAdapter.getItem(position);
                                Intent intent = new Intent(MainActivity.this, PalestraActivity.class);
                                intent.putExtra("palestra.id", palestra.getId());
                                intent.putExtra("palestra.name", palestra.getName());
                                intent.putExtra("palestra.speaker", palestra.getSpeaker());
                                startActivity(intent);
                            }
                        });
                    }
                });
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.preferencias:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                palestrasList.clear();
                finish();
                startActivity(getIntent());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}