package com.npu.zhang.npulibrary;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Main2Activity extends AppCompatActivity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        textView = (TextView) findViewById(R.id.textView);
        findViewById(R.id.button2).setEnabled(false);
        findViewById(R.id.spinner).setEnabled(false);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("hello");
                new AsyncTask<Void, Void, String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        try {
                            URL url = new URL("http://202.117.255.187:8080/opac/item.php?marc_no=0003682320");
                            Document document = Jsoup.parse(url, 5000);
                            Elements tableTags = document.select("table");
                            for (Element tableTag : tableTags){
                                if (tableTag.attr("id").equals("item")){
                                    Element trTag = tableTag.select("tr").last();
                                    Element tdTag2 = trTag.select("td").get(4);
                                    tdTag2.select("script").first().text("");
                                    Element tdTag3 = trTag.select("td").get(5);
                                    String string =tdTag2.html() + tdTag3.html();
                                    return string;
                                }
                            }

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        System.out.println("asynctask");
                        textView.setText( Html.fromHtml(s));
                        super.onPostExecute(s);
                    }
                }.execute();
            }
        });
    }
}
