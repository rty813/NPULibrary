package com.npu.zhang.npulibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    private ArrayList<Map<String, String>> list;
    private RecyclerView recyclerView;
    private boolean finishFlag = true;
    private String bookname = null;
    private int loadedPages = 1;
    private boolean lastPageFlag = false;
    private int lastPage;
    private SearchView searchView;
    private ListView popupListView;
    private ArrayList<String> popupList;
    private ArrayAdapter<String> popupAdapter;
    private PopupWindow popupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initSearchView();
        initRecyclerView();
        initPopupWindow();

    }

    private void initSearchView(){
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("在此输入查询的书籍名称");

        final AppCompatImageView search_button = (AppCompatImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        search_button.setImageResource(R.drawable.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                popupWindow.showAsDropDown(searchView);
                if (!finishFlag){
                    return false;
                }
                SharedPreferences sharedPreferences = getSharedPreferences("history", 0);
                String longHistory = sharedPreferences.getString("history", "");
                sharedPreferences.edit().putString("history", query + "|" + longHistory).apply();
                popupList.add(0, query);
                if (popupList.size() > 10){
                    popupList.remove(10);
                }
                popupAdapter.notifyDataSetChanged();

                searchView.clearFocus();
                lastPageFlag = false;
                list.removeAll(list);
                adapter.notifyDataSetChanged();
                adapter.setFootViewText("正在加载中...");
                recyclerView.setVisibility(View.VISIBLE);
                bookname = query.replace("+", "%2B").replace(" ", "+");
                new myAsyncTask().execute(bookname, "1");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    Toast.makeText(SearchActivity.this, "获得焦点！", Toast.LENGTH_SHORT).show();
                    popupWindow.showAsDropDown(searchView);
                }
                else{
                    Toast.makeText(SearchActivity.this, "失去焦点！", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            }
        });

        EditText textView = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        textView.setTextColor(Color.WHITE);//字体颜色
        textView.setHintTextColor(Color.rgb(150, 150, 150));
    }

    private void initRecyclerView(){


//        RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new RecyclerViewAdapter(this, list);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                    int lastPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                    if ((lastPosition >= recyclerView.getLayoutManager().getItemCount() - 2) && (finishFlag) && (!lastPageFlag)) {
                        loadedPages++;
                        new myAsyncTask().execute(bookname, String.valueOf(loadedPages));
                        if (loadedPages == lastPage){
                            lastPageFlag = true;
                        }
                    }
                }
            }
        });
        adapter.setOnItemClickListener(new RecyclerViewAdapter.onRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                HashMap<String, String> map = (HashMap<String, String>) list.get(position);
                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                intent.putExtra("url", map.get("bookLink"));
                intent.putExtra("bookNameReal", map.get("bookNameReal"));
                startActivity(intent);
            }
        });
    }

    private void initPopupWindow(){
        View contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popupwindow, null);
        contentView.setBackgroundColor(Color.WHITE);

        popupWindow = new PopupWindow(findViewById(R.id.mainLayout), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(contentView);

        popupListView =(ListView)contentView.findViewById(R.id.pop_list);
        popupList = new ArrayList<>();
        popupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, popupList);
        popupListView.setAdapter(popupAdapter);

        popupListView.setItemsCanFocus(false);
        popupListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        popupWindow.setFocusable(true);

        SharedPreferences sharedPreferences = getSharedPreferences("history", 0);
        String longHistory = sharedPreferences.getString("history", null);
        if (longHistory != null){
            String[] history = longHistory.split("\\|");
            int length = Math.min(history.length, 10);
            for (int i = 0; i < length; i++){
                popupList.add(0, history[i]);
            }
            popupAdapter.notifyDataSetChanged();
        }


    }

    @Override
    protected void onResume() {
        searchView.clearFocus();
        super.onResume();
    }

    public class myAsyncTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... strings) {
            try {
                finishFlag = false;
                System.out.println(strings[0]);
                System.out.println("Begin to connnet!");
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=" + strings[1]).openConnection();
                urlConnection.setConnectTimeout(5000);
                System.out.println("Connect successful!");
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine = "";
                StringBuilder builder = new StringBuilder();
                while ((inputLine = br.readLine()) != null){
                    builder.append(inputLine);
                }
                urlConnection.disconnect();
                br.close();

                Document document = Jsoup.parse(builder.toString());

                Element link = document.select("ol").first();
                if (link == null){
                    System.out.println("NullPointer!");
                    lastPageFlag = true;
                    return strings;
                }
                Elements spans = document.select("span");
                for (Element span : spans){
                    if (span.attr("class").equals("pagination")){
                        Element font = span.select("font").last();
                        if (font == null){
                            lastPage = 1;
                        }
                        else{
                            lastPage = Integer.parseInt(font.text());
                        }
                    }
                }


                Elements liTags = link.select("li");
                for (Element liTag : liTags){
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

                    urlConnection = (HttpURLConnection) new URL("http://202.117.255.187:8080/opac/ajax_" + aTag.attr("href")).openConnection();
                    urlConnection.setConnectTimeout(5000);
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while((inputLine = br.readLine()) != null){
                        builder.append(inputLine);
                    }
                    Document detailDoc = Jsoup.parse(builder.toString());

                    builder = new StringBuilder();
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
                                    Element tdTag1 = trTag.select("td").get(3);
                                    Element tdTag2 = trTag.select("td").get(4);
                                    if ((tdTag1 == null) && (tdTag2 == null)){
                                        System.out.println("此图书正在订阅");
                                        continue;
                                    }
                                    builder.append("\n" + tdTag1.text() + "    " + tdTag2.text());
                                }
                            }
                        }
                    }
                    String bookPlace = builder.toString();
                    if (bookPlace.equals("")){
                        publishProgress();
                        continue;
                    }
                    if (bookPlace.equals("")){
                        bookPlace = "\n此书刊可能正在订购中或者处理中";
                    }

                    urlConnection = (HttpURLConnection) new URL(bookLink).openConnection();
                    urlConnection.setConnectTimeout(5000);
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    builder = new StringBuilder();
                    while((inputLine = br.readLine()) != null){
                        builder.append(inputLine);
                    }
                    document = Jsoup.parse(builder.toString());
                    Elements divs = document.select("div");
                    String bookpic = null;
                    String isbn = null;
                    for (Element div : divs){
                        if (div.attr("id").equals("item_detail")){
                            Elements dls = div.select("dl");
                            for (Element dl : dls){
                                if (dl.select("dt").first().text().contains("ISBN")){
                                    isbn = dl.select("dd").first().text();
                                    isbn = isbn.substring(0, isbn.indexOf('/')).replace("-", "");
                                    break;
                                }
                            }
                            System.out.println(isbn);
                        }
                    }
                    urlConnection = (HttpURLConnection) new URL("http://202.117.255.187:8080/opac/ajax_douban.php?isbn=" + isbn).openConnection();
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    builder = new StringBuilder();
                    while((inputLine = br.readLine()) != null){
                        builder.append(inputLine);
                    }
                    JSONObject jsonObject = new JSONObject(builder.toString());
                    bookpic = jsonObject.getString("image");

                    Map<String, String> map = new HashMap<String, String>();

                    map.put("bookname", bookName);
                    map.put("bookLink", bookLink);
                    map.put("bookdetail", bookIntroduce + "\n" + bookPlace);
                    map.put("bookNameReal", bookNameReal);
                    map.put("bookPlace", bookPlace);
                    map.put("bookpic", bookpic);
                    list.add(map);
                    publishProgress();
                }
                return strings;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            adapter.notifyItemInserted(list.size());
            adapter.notifyItemChanged(list.size() + 1);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String... strings) {
            finishFlag = true;
            if (lastPageFlag){
                adapter.setFootViewText("加载完毕");
            }
            if (list.size() == 0){
                adapter.setFootViewText("无查询结果");
            }
            searchView.setEnabled(true);
            adapter.notifyDataSetChanged();
        }

    }
}
