package com.secot.thiagow.secot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PalestraActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "SeCoTFile";
    public static final String DEF_IP = "192.168.43.188";
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static String url_palestra_inscritos = "/android_connect/getallinscritos.php";
    private static String url_set_presenca = "/android_connect/confirmarpresenca.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PESSOAS = "pessoas";
    private static final String TAG_ID = "id";
    private static final String TAG_NOME = "nome";
    private static final String TAG_QRID = "qrid";
    private static final String TAG_PRESENTE = "presente";

    private ListView inscritos_list;
    private TextView palestraAlert;
    private boolean conErr = false;
    private boolean presErr = false;
    private int presSuccess = 0;
    private String palestraName;
    private String palestraSpeaker;
    private int palestraId;
    private ArrayAdapter<Inscrito> arrayAdapter;
    private JSONParser jParser;
    private JSONArray inscritosJson = null;
    private List<Inscrito> inscritosList = new ArrayList<Inscrito>();
    private ProgressDialog pDialog;
    private Dialog eDialog;
    private String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palestra);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverIP = settings.getString("serverIP", DEF_IP);

        Intent incomingIntent = getIntent();
        palestraId = Integer.parseInt(incomingIntent.getStringExtra("palestra.id"));
        palestraName = incomingIntent.getStringExtra("palestra.name");
        palestraSpeaker = incomingIntent.getStringExtra("palestra.speaker");

        setTitle(palestraSpeaker + ": " + palestraName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.secot)));

        inscritos_list = (ListView) findViewById(R.id.inscritos_list);
        new LoadInscritos().execute();
    }

    public void startQR(View view){

        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(PalestraActivity.this, "Leitor de QR não encontrado", "Deseja baixa-lo?", "Sim", "Não").show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)    {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                boolean found = false;
                String contents = intent.getStringExtra("SCAN_RESULT");
                for (int i = 0; i < arrayAdapter.getCount(); i++) {
                    if(arrayAdapter.getItem(i).getQRID().equals(contents)) {
                        found = true;
                        if(!inscritos_list.isItemChecked(i)){
                            checkInscrito(i);
                        } else {
                            Toast toast = Toast.makeText(this, "Presença já marcada para " + arrayAdapter.getItem(i).getName(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        break;
                    }
                }
                if(!found) {
                    Toast toast = Toast.makeText(this, "ID não inscrito", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    public void raffle(View view){
        AlertDialog.Builder raffle = new AlertDialog.Builder(PalestraActivity.this);
        raffle.setTitle("Sorteio!");
        raffle.setMessage(" ");
        raffle.setPositiveButton("Sortear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Random myRandomizer = new Random();
                SparseBooleanArray presentes = inscritos_list.getCheckedItemPositions();
                AlertDialog.Builder lucky = new AlertDialog.Builder(PalestraActivity.this);
                lucky.setTitle(":)");
                if(presentes.size() == 0) {
                    lucky.setMessage("Não há ninguém na palestra!");
                } else {
                    int sorteado = presentes.keyAt(myRandomizer.nextInt(presentes.size()));
                    lucky.setMessage(arrayAdapter.getItem(sorteado).getName());
                }
                lucky.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i){
                    }
                });
                lucky.show();
            }
        });
        raffle.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i){
            }
        });
        raffle.show();
    }

    public void refresh(View view){
        inscritosList.clear();
        finish();
        startActivity(getIntent());
    }

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    // TODO: check/uncheck from server
    public void checkInscrito(int position) {
        new setPresenca(arrayAdapter.getItem(position).getID(), Integer.toString(palestraId), "1", position).execute();
    }

    public void uncheckInscrito(int position) {
        new setPresenca(arrayAdapter.getItem(position).getID(), Integer.toString(palestraId), "0", position).execute();
    }

    class LoadInscritos extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            palestraAlert = (TextView) findViewById(R.id.palestraAlert);
            palestraAlert.setText("Carregando...");
            palestraAlert.setVisibility(View.VISIBLE);
        }

        /**
         * getting All palestras from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("palestra_id", Integer.toString(palestraId)));
            // getting JSON string from URL
            jParser = null;
            jParser = new JSONParser();
            JSONObject json = null;

            json = jParser.makeHttpRequest("http://" + serverIP + url_palestra_inscritos, "GET", params);
            if(jParser.getError()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        palestraAlert.setText("Erro de conexão");
                        palestraAlert.setVisibility(View.VISIBLE);
                    }
                });
                conErr = true;
                return null;
            }

            conErr = false;
            // Check your log cat for JSON reponse
            Log.d("Inscritos: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    inscritosJson = json.getJSONArray(TAG_PESSOAS);

                    for (int i = 0; i < inscritosJson.length(); i++) {
                        JSONObject c = inscritosJson.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String nome = c.getString(TAG_NOME);
                        String qrid = c.getString(TAG_QRID);
                        String presente = c.getString(TAG_PRESENTE);

                        inscritosList.add(new Inscrito(id, nome, qrid, presente));
                    }
                } else {
                    Intent i = new Intent(getApplicationContext(),
                            PalestraActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            if(inscritosList.size() == 0 && !conErr){
                palestraAlert.setText("Nenhum inscrito foi encontrado");
                palestraAlert.setVisibility(View.VISIBLE);
            } else if(!conErr) {
                // dismiss the dialog after getting all products
                palestraAlert.setVisibility(View.INVISIBLE);
                // updating UI from Background Thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        arrayAdapter = new ArrayAdapter<Inscrito>(
                                PalestraActivity.this,
                                android.R.layout.simple_list_item_multiple_choice,
                                inscritosList);

                        inscritos_list.setAdapter(arrayAdapter);
                        inscritos_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                        inscritos_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View item,
                                                    final int position, long id) {

                                if (inscritos_list.isItemChecked(position)) {
                                    AlertDialog.Builder confirmar = new AlertDialog.Builder(PalestraActivity.this);
                                    confirmar.setTitle("Confirmar presença");
                                    confirmar.setMessage("Deseja confirmar a presença de " + arrayAdapter.getItem(position).getName() + "?");
                                    confirmar.setCancelable(false);
                                    confirmar.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            checkInscrito(position);
                                            ;
                                        }
                                    });
                                    confirmar.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            inscritos_list.setItemChecked(position, false);
                                        }
                                    });
                                    confirmar.show();
                                } else {
                                    AlertDialog.Builder desconfirmar = new AlertDialog.Builder(PalestraActivity.this);
                                    desconfirmar.setTitle("Desconfirmar presença");
                                    desconfirmar.setMessage("Deseja desconfirmar a presença de " + arrayAdapter.getItem(position).getName() + "?");
                                    desconfirmar.setCancelable(false);
                                    desconfirmar.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            uncheckInscrito(position);
                                        }
                                    });
                                    desconfirmar.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            inscritos_list.setItemChecked(position, true);
                                        }
                                    });
                                    desconfirmar.show();
                                }
                            }
                        });

                        for (int i = 0; i < arrayAdapter.getCount(); i++) {
                            if (arrayAdapter.getItem(i).getPresente().equals("1")) {
                                inscritos_list.setItemChecked(i, true);
                            }
                        }
                    }
                });

            }

        }

    }

    class setPresenca extends AsyncTask<String, String, String> {

        private String id_pessoa;
        private String id_palestra;
        private String presente;
        private int position;
        public setPresenca(String id_pessoa, String id_palestra, String presente, int position){
            this.id_pessoa = id_pessoa;
            this.id_palestra = id_palestra;
            this.presente = presente;
            this.position = position;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PalestraActivity.this);
            pDialog.setMessage("Carregando...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All palestras from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_pessoa", id_pessoa));
            params.add(new BasicNameValuePair("id_palestra", id_palestra));
            params.add(new BasicNameValuePair("presente", presente));

            // getting JSON string from URL
            jParser = null;
            jParser = new JSONParser();
            JSONObject json = null;

            json = jParser.makeHttpRequest("http://" + serverIP + url_set_presenca, "GET", params);
            if(jParser.getError()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(PalestraActivity.this);
                        builder.setMessage("Erro de conexão");
                        builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();
                    }
                });
                presErr = true;
                return null;
            }

            presErr = false;
            presSuccess = 0;
            try {
                presSuccess = json.getInt(TAG_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            if(presErr) {
                if(presente.equals("0")){
                    inscritos_list.setItemChecked(position, true);
                } else {
                    inscritos_list.setItemChecked(position, false);
                }
                return;
            } else if(presSuccess == 0){
                if(presente.equals("0")){
                    inscritos_list.setItemChecked(position, true);
                } else {
                    inscritos_list.setItemChecked(position, false);
                }
                pDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(PalestraActivity.this);
                builder.setMessage("Presença não atualizada");
                builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
                return;
            }
            if(presente.equals("0")){
                inscritos_list.setItemChecked(position, false);
                Toast toast = Toast.makeText(PalestraActivity.this, "Presença desconfirmada: " + arrayAdapter.getItem(position).getName(), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                inscritos_list.setItemChecked(position, true);
                Toast toast = Toast.makeText(PalestraActivity.this, "Presença: " + arrayAdapter.getItem(position).getName(), Toast.LENGTH_SHORT);
                toast.show();
            }
            pDialog.dismiss();
        }

    }
}
