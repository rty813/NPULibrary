package com.npu.zhang.npulibrary;

import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.ScrollingTabContainerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private EditText editText;
    private TextView textView;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        editText = (EditText) findViewById(R.id.editText2);
        textView = (TextView) findViewById(R.id.textView2);
        listView = (ListView) findViewById(R.id.listView);

        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String book = editText.getText().toString();

                new AsyncTask<String, Void, List<Map<String, String>>>(){
                    @Override
                    protected List<Map<String, String>> doInBackground(String... strings) {
                        try {
                            System.out.println("Begin to connnet!");
                            Document document = Jsoup.parse(new URL("http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=1"), 5000);
                            System.out.println("Connect successful!");
                            Element link = document.select("ol").first();
                            Elements atags = link.select("a");
                            Elements h3Tags = link.select("h3");
                            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                            for (Element h3Tag : h3Tags){
                                if (h3Tag.text().equals("馆藏"))
                                    continue;
                                System.out.println("书名：" + h3Tag.text().substring(4));
                                System.out.println("    链接：" + h3Tag.select("a").attr("href"));
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("Name", h3Tag.text().substring(4));
                                map.put("href","http://202.117.255.187:8080/opac/" + h3Tag.select("a").attr("href"));
                                list.add(map);
                            }
                            return list;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(List<Map<String, String>> list) {
                        listView.setAdapter(new SimpleAdapter(MainActivity.this, list, android.R.layout.simple_list_item_2,
                                new String[] {"Name","href"}, new int[] {android.R.id.text1, android.R.id.text2}));
                        progressBar.setVisibility(View.GONE);
                        super.onPostExecute(list);
                    }
                }.execute(book);
            }
        });
    }
}
