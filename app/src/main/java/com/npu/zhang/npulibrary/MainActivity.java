package com.npu.zhang.npulibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String version = "ver1.3";
    private EditText editText;
    private TextView textView;
    private ListView listView;
    private ProgressDialog waitingDialog;
    private List<Map<String, String>> lvList;
    private String bookName;
    private SimpleAdapter simpleAdapter;
    private ProgressBar progressBar;
    private int pageNum;
    private int count;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText2);
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        lvList = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(MainActivity.this, lvList, android.R.layout.simple_list_item_2,
                new String[] {"bookName","bookIntroduce"}, new int[] {android.R.id.text1, android.R.id.text2});
        listView.setAdapter(simpleAdapter);
        count = 0;
        final myAsyncTask[] asyncTask = new myAsyncTask[1000];
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("请等候");
        progressDialog.setCancelable(true);

        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                bookName = editText.getText().toString();
                if (bookName.equals("")){
                    return;
                }
                editText.setText("");
                if (progressBar.getVisibility() == View.VISIBLE){
                    progressDialog.show();
                }else
                {
                    lvList.removeAll(lvList);
                    simpleAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.VISIBLE);
                    asyncTask[count] = (myAsyncTask) new myAsyncTask().execute(bookName, "1", count+"");
                }
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
    public class myAsyncTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... strings) {
            try {
                System.out.println("Begin to connnet!");
                Document document = Jsoup.parse(new URL("http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=" + strings[1]), 5000);
                System.out.println("Connect successful!");
                //获取总页数
                Elements spanTags = document.select("span");
                for (Element spanTag : spanTags){
                    if (spanTag.attr("class").equals("pagination")){
                        Elements fontTags = spanTag.select("font");
                        for (Element fontTag : fontTags){
                            if (fontTag.attr("color").equals("black")){
                                pageNum = Integer.parseInt(fontTag.text());
                            }
                        }
                    }
                }

                Element link = document.select("ol").first();
                if (link == null){
                    System.out.println("NullPointer!");
                    return strings;
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
                    if (bookName.indexOf("图书") == 0){
                        bookName = bookName.substring(2);
                    }
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
                    lvList.add(map);
                    list.add(map);
                }
                return strings;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String... strings) {
            if (lvList.size() == 0){
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "无结果", Toast.LENGTH_SHORT).show();
                System.out.println("无结果");
                return;
            }
            String book = strings[0];
            String page = strings[1];
            String thisCount = strings[2];
            if (Integer.parseInt(thisCount) < count){
                lvList.removeAll(lvList);
                simpleAdapter.notifyDataSetChanged();
                //progressBar.setVisibility(View.GONE);
                progressDialog.cancel();
                new myAsyncTask().execute(bookName, "1", count+"");
                return;
            }
            simpleAdapter.notifyDataSetChanged();
            if ((Integer.parseInt(page) < pageNum)){
                page = (Integer.parseInt(page) + 1) + "";
                new myAsyncTask().execute(book, page, thisCount);
            }else
                progressBar.setVisibility(View.GONE);
            super.onPostExecute(strings);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("关于");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("关于");
        normalDialog.setMessage(version + "\n本项目已开源，\n网址：https://github.com/rty813/NPULibrary\nBy 西北工业大学 张金阳\nQQ：523213189");
        normalDialog.setPositiveButton("确定",null);
        normalDialog.show();
        return super.onOptionsItemSelected(item);
    }
}
