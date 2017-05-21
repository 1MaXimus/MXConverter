package com.example.maxim.mxconverter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String url = "http://www.cbr.ru/scripts/XML_daily.asp";
    EditText editText;
    Spinner fromcurr;
    Spinner tocurr;
    TextView result;
    TextView upDate;
    currList currList;
    Button doIt;
    TextView scode;
    TextView rescode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.amount);
        fromcurr = (Spinner) findViewById(R.id.fromcur);
        tocurr = (Spinner) findViewById(R.id.tocur);
        result = (TextView) findViewById(R.id.result);
        upDate = (TextView) findViewById(R.id.updeted);
        doIt = (Button) findViewById(R.id.button);
        scode = (TextView) findViewById(R.id.scode);
        rescode = (TextView) findViewById(R.id.rescode);
        doIt.setEnabled(false);
        editText.clearFocus();
       // new loadXML().execute(url);

    }


    private void configViews() {
        fromcurr.setAdapter(makeAdapter());
        fromcurr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                scode.setText(currList.list.get(position).scode);
                rescode.setText("");
                result.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        fromcurr.setSelection(pos);
        fromcurr.setPrompt(getString(android.R.string.unknownName));
        tocurr.setAdapter(makeAdapter());

        tocurr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rescode.setText("");
                result.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tocurr.setPrompt(getString(android.R.string.unknownName));
        tocurr.setSelection(pos);
        String upd = currList == null? "": getString(R.string.updated) +" " + currList.date;
        upDate.setText(upd);
        doIt.setEnabled(true);
        doIt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!editText.getText().toString().contentEquals("")) {
            editText.setHintTextColor(Color.GRAY);
            float val = Float.parseFloat(editText.getText().toString());
            float from = currList.list.get(fromcurr.getSelectedItemPosition()).value;
            float to = currList.list.get(tocurr.getSelectedItemPosition()).value;
            float res = from / to * val;
            result.setText(String.format(Locale.getDefault(), "%.2f", res));
            rescode.setText(currList.list.get(tocurr.getSelectedItemPosition()).scode);
            editText.clearFocus();
        } else {
            editText.setHintTextColor(Color.RED);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currList == null) {
            InputStream fis = null;
            try {
                fis = new FileInputStream(this.getFileStreamPath("currency.xml"));
                currList = new XMLParser().parse(fis);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        configViews();
    }
    int pos = 0;
    private ArrayAdapter<String> makeAdapter() {
        ArrayAdapter<String> sAdapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        sAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (currList != null) {
            ArrayList<Curren> arr = currList.list;
            for (int i = 0, j = arr.size(); i < j; i++) {
               sAdapt.add(arr.get(i).name);
                if (arr.get(i).scode.contentEquals("USD")){
                    pos = i;
                }
            }
        }

        return sAdapt;
    }


    private class loadXML extends AsyncTask<String, Void, currList> {

        protected void onPostExecute(currList params) {
            if( params != null) {
                currList = params;
                configViews();
                new saveXML().execute(url);
            }
        }

        @Override
        protected currList doInBackground(String... params) {
            XMLParser pars = new XMLParser();
            currList list = null;
            InputStream in = null;
            try {
                in = downloadUrl(params[0]);

                list = pars.parse(in);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return list;
        }
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
    private class saveXML extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            InputStream in = null;
            try {
                in = downloadUrl(params[0]);
                InputStreamReader ior = new InputStreamReader(in,"windows-1251");
                File f = getBaseContext().getFileStreamPath("currency.xml");
                if (f.exists()){
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
                OutputStream out = getBaseContext().openFileOutput("currency.xml", Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
                char[] buf = new char[1];
                int b = ior.read(buf);
                osw.write("\uFEFF");
                while (b!= -1){

                    osw.write(buf);
                    b = ior.read(buf);

                }

                osw.flush();
                osw.close();
                ior.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
