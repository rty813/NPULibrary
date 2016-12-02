package com.npu.zhang.npulibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
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

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }
        });

        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                bookName = editText.getText().toString();
                bookName = bookName.replace("+", "%2B");
                bookName = bookName.replace(" ", "+");
                System.out.println(bookName);
                if (bookName.equals("")){
                    return;
                }
                editText.setText("");
                if (progressBar.getVisibility() == View.VISIBLE){
                    progressDialog.show();
                }else
                {
                    System.out.println("正在从onClick方法进入asyncTask");
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
    public class myAsyncTask extends AsyncTask<String, String, String[]> {
        @Override
        protected String[] doInBackground(String... strings) {
            try {
                System.out.println("Begin to connnet!");
                System.out.println(strings[0]);
                Document document = Jsoup.parse(new URL("http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=" + strings[1]), 5000);
                System.out.println("Connect successful!");
                //获取总页数
                Elements spanTags = document.select("span");
                System.out.println("正在获取总页数:" + "http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=" + strings[1]);
                boolean pageNumChanged = false;
                for (Element spanTag : spanTags){
                    if (spanTag.attr("class").equals("pagination")){
                        Elements fontTags = spanTag.select("font");
                        for (Element fontTag : fontTags){
                            if (fontTag.attr("color").equals("black")){
                                pageNum = Integer.parseInt(fontTag.text());
                                pageNumChanged = true;
                                System.out.println("doPageNum: " + pageNum);
                            }
                        }
                    }
                }
                if (!pageNumChanged){
                    pageNum = 1;
                }

                Element link = document.select("ol").first();
                if (link == null){
                    System.out.println("NullPointer!");
                    return strings;
                }

                String thisCount = strings[2];

                Elements liTags = link.select("li");
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                for (Element liTag : liTags){
                    if (Integer.parseInt(thisCount) < count){
                        return null;
                    }
                    Element h3Tag = liTag.select("h3").first();
                    Element aTag = liTag.select("a").first();
                    Element pTag = liTag.select("p").first();
                    if (h3Tag.text().equals("馆藏"))
                        continue;
                    Element bTag = h3Tag.select("b").first();
                    String bookName = h3Tag.text().substring(4);
                    if (bookName.indexOf("图书") == 0){
                        bookName = bookName.substring(2);
                    }
                    String bookLink ="http://202.117.255.187:8080/opac/" + aTag.attr("href");
                    String bookIntroduce = pTag.text().substring(14,pTag.text().length()-6);
                    String bookNameReal = aTag.text().substring(aTag.text().indexOf(".") + 1);
                    StringBuilder builder = new StringBuilder();

                    Document detailDoc = Jsoup.parse(new URL(bookLink), 5000);
                    Elements tableTags = detailDoc.select("table");
                    for (Element tableTag : tableTags){
                        //System.out.println(tableTag.text());
                        if (tableTag.attr("id").equals("item")){
                            Elements trTags = tableTag.select("tr");
                            for (Element trTag : trTags){
                                if (trTag.attr("class").equals("whitetext")){
                                    if (trTag.select("td").first().text().indexOf("订购中") != -1){
                                        break;
                                    }
                                    Element tdTag1 = trTag.select("td").get(4);
                                    Element tdTag2 = trTag.select("td").get(5);
                                    if ((tdTag1 == null) && (tdTag2 == null)){
                                        System.out.println("此图书正在订阅");
                                        continue;
                                    }
                                    builder.append("\n" + tdTag1.text().replace("校区", ""));
                                    if (tdTag2.text().length() < 5)
                                        builder.append(tdTag2.text() + "\n");
                                    else
                                        builder.append("\n" + tdTag2.text() + "\n");
                                }
                            }
                        }
                    }
                    String bookPlace = builder.toString();
                    if (bookPlace.equals("")){
                        bookPlace = "\n此书刊可能正在订购中或者处理中";
                    }
//                    System.out.println("书名：" + bookName);
//                    System.out.println("链接：" + bookLink);
//                    System.out.println("介绍：" + bookIntroduce);
//                    System.out.println("地址：" + bookPlace);
                    System.out.println();
                    Map<String, String> map = new HashMap<String, String>();

                    map.put("bookName", bookName);
                    map.put("bookLink", bookLink);
                    map.put("bookIntroduce", bookIntroduce + "\n" + bookPlace);
                    map.put("bookNameReal", bookNameReal);
                    map.put("bookPlace", bookPlace);
                    lvList.add(map);
                    publishProgress(strings);
                    list.add(map);
                }
                return strings;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String book = values[0];
            String page = values[1];
            String thisCount = values[2];
            if (Integer.parseInt(thisCount) < count){
                lvList.removeAll(lvList);
                simpleAdapter.notifyDataSetChanged();
                //progressBar.setVisibility(View.GONE);
                progressDialog.cancel();
                System.out.println(book);
                System.out.println(bookName);
                new myAsyncTask().execute(bookName, "1", count+"");
                return;
            }
            simpleAdapter.notifyDataSetChanged();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String... strings) {
            if ((strings == null)||(Integer.parseInt(strings[2]) < count)){
                return;
            }
            if (lvList.size() == 0){
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "无结果", Toast.LENGTH_SHORT).show();
                System.out.println("无结果");
                return;
            }
            String book = strings[0];
            String page = strings[1];
            String thisCount = strings[2];
            System.out.println("now pageNum: " + pageNum);
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
