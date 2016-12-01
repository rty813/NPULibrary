package com.npu.zhang.npulibrary;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.ScrollingTabContainerView;
import android.view.View;
import android.widget.AdapterView;
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
        listView = (ListView) findViewById(R.id.listView);

        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String book = editText.getText().toString();
                if (book.equals(null)){
                    return;
                }
                String page = "1";
                AsyncTask(book,page);

            }
        });

        ((ListView) findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition((int) l);
                String link = map.get("bookLink");
                Intent intent = new Intent(MainActivity.this,DetailActivity.class).putExtra("url", link);
                intent.putExtra("bookNameReal", map.get("bookNameReal"));
                startActivity(intent);
            }
        });
    }

    void AsyncTask(String book, String page){
        new AsyncTask<String, Void, List<Map<String, String>>>(){
            @Override
            protected List<Map<String, String>> doInBackground(String... strings) {
                try {
                    System.out.println("Begin to connnet!");
                    Document document = Jsoup.parse(new URL("http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=" + strings[1]), 5000);
                    System.out.println("Connect successful!");
                    Element link = document.select("ol").first();
                    if (link == null){
                        System.out.println("NullPointer!");
                        return null;
                    }

                    Elements liTags = link.select("li");
                    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    for (Element liTag : liTags){
                        Element h3Tag = liTag.select("h3").first();
                        Element aTag = liTag.select("a").first();
                        Element pTag = liTag.select("p").first();
                        if (h3Tag.text().equals("馆藏"))
                            continue;
                        String bookName = h3Tag.text().substring(4);
                        String bookLink ="http://202.117.255.187:8080/opac/" + aTag.attr("href");
                        String bookIntroduce = pTag.text().substring(0,pTag.text().length()-6);
                        String bookNameReal = aTag.text().substring(aTag.text().indexOf(".") + 1);
                        System.out.println("书名：" + bookName);
                        System.out.println("链接：" + bookLink);
                        System.out.println("介绍：" + bookIntroduce);
                        System.out.println();
                        Map<String, String> map = new HashMap<String, String>();

                        map.put("bookName", bookName);
                        map.put("bookLink", bookLink);
                        map.put("bookIntroduce", bookIntroduce);
                        map.put("bookNameReal", bookNameReal);
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
                progressBar.setVisibility(View.GONE);
                if (list == null){
                    Toast.makeText(MainActivity.this, "无结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                listView.setAdapter(new SimpleAdapter(MainActivity.this, list, android.R.layout.simple_list_item_2,
                        new String[] {"bookName","bookIntroduce"}, new int[] {android.R.id.text1, android.R.id.text2}));
                super.onPostExecute(list);
            }

        }.execute(book,page);
    }
}
