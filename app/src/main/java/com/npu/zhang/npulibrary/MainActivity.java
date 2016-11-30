package com.npu.zhang.npulibrary;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.ScrollingTabContainerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private EditText editText;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        editText = (EditText) findViewById(R.id.editText2);
        textView = (TextView) findViewById(R.id.textView2);

        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String book = editText.getText().toString();
                new AsyncTask<String, String, String>(){
                    @Override
                    protected String doInBackground(String... strings) {

                        try {
                            URL url = new URL("http://202.117.255.187:8080/opac/openlink.php");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoOutput(true);
                            BufferedWriter bw = new BufferedWriter(
                                    new OutputStreamWriter(connection.getOutputStream(),"utf-8")
                            );
                            bw.write("strSearchType=title&strText=" + strings[0]);
                            bw.flush();

                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(connection.getInputStream())
                            );
                            String line;
                            StringBuilder builder = new StringBuilder();
                            while ((line = br.readLine()) != null){
//                                line = line.replace("&#x" , "\\u");
//                                line = line.replace(";\\u","\\u");
                                publishProgress(line);
                                builder.append(line);
                                System.out.println(line);
                            }
                            br.close();
                            bw.close();
                            return builder.toString();

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(String string) {
                        //textView.setText(string);
                        progressBar.setVisibility(View.GONE);
                        super.onPostExecute(string);
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        String str = values[0];
                        if (str.indexOf("中文图书") != -1){
                            String finalbook = "";
                            String[] bookname = str.split("&#x");
                            int i = bookname.length;
                            for (String book : bookname){
                                String book1 = book.substring(0,4);
                                if (book1.indexOf(" ") != -1)
                                    continue;
                                book = "\\u" + book;
                                String bookinChinese = CodeChange.unicodeToString(book);
                                //bookinChinese.replace(";" , "");
                                //System.out.print(bookinChinese);
                                finalbook += bookinChinese;
                            }
                            finalbook.replaceAll(";","");
                            System.out.print(finalbook.indexOf(";") + " ");
                            System.out.println(finalbook);
                            textView.append(finalbook);
                        }
                        super.onProgressUpdate(values);
                    }
                }.execute(book);
            }
        });
    }
}
