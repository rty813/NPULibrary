package com.npu.zhang.npulibrary;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.melnykov.fab.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.wang.avi.AVLoadingIndicatorView;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.utils.SpotlightListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class testActivity extends AppCompatActivity {

    private MaterialSearchView searchView;
    private RecyclerViewAdapter adapter;
    private ArrayList<Map<String, String>> list;
    private SwipeMenuRecyclerView recyclerView;
    private boolean finishFlag = true;
    private String bookname = null;
    private int loadedPages = 1;
    private boolean lastPageFlag = false;
    private int lastPage;
    private ArrayList<String> suggestionList;
    private AMapLocationClient mLocationClient;
    private Toolbar toolbar;
    public static MyDatabase database;
    private String[] history;
    private ProgressDialog progressDialog;
    private String mQuery;
    private String campus = "长安校区";
    private boolean isFirstStart = true;
    private FloatingActionButton floatingActionButton;
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(testActivity.this, StoreActivity.class));
            }
        });
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("NPULibrary");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitle("长安校区");
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (toolbar.getSubtitle().equals("友谊校区")){
                    toolbar.setSubtitle("长安校区");
                    campus = "长安校区";
                }
                else{
                    toolbar.setSubtitle("友谊校区");
                    campus = "友谊校区";
                }
                Toast.makeText(testActivity.this, "请重新检索", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        toolbar.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < toolbar.getChildCount(); i++){
                    View view = toolbar.getChildAt(i);
                    if (view instanceof TextView){
                        final TextView textView = (TextView) view;
                        if (textView.getText().toString().contains("校区")){
                            SpotlightListener spotlightListener = new SpotlightListener() {
                                @Override
                                public void onUserClicked(String s) {
                                    SpotlightListener fablistener = new SpotlightListener() {
                                        @Override
                                        public void onUserClicked(String s) {
                                            searchView.showSearch();
                                        }
                                    };
                                    new SpotlightView.Builder(testActivity.this)
                                            .introAnimationDuration(400)
                                            .enableRevealAnimation(true)
                                            .performClick(true)
                                            .fadeinTextDuration(400)
                                            .headingTvColor(Color.parseColor("#eb273f"))
                                            .headingTvSize(32)
                                            .headingTvText("收藏入口")
                                            .subHeadingTvColor(Color.parseColor("#ffffff"))
                                            .subHeadingTvSize(16)
                                            .subHeadingTvText("点我进入收藏页面~")
                                            .maskColor(Color.parseColor("#dc000000"))
                                            .target(floatingActionButton)
                                            .lineAnimDuration(400)
                                            .lineAndArcColor(Color.parseColor("#eb273f"))
                                            .dismissOnTouch(false)
                                            .dismissOnBackPress(true)
                                            .enableDismissAfterShown(true)
                                            .usageId("提示fab") //UNIQUE ID
                                            .setListener(fablistener)
                                            .show();
                                }
                            };
                            new SpotlightView.Builder(testActivity.this)
                                    .introAnimationDuration(400)
                                    .enableRevealAnimation(true)
                                    .performClick(true)
                                    .fadeinTextDuration(400)
                                    .headingTvColor(Color.parseColor("#eb273f"))
                                    .headingTvSize(32)
                                    .headingTvText("长按选择校区")
                                    .subHeadingTvColor(Color.parseColor("#ffffff"))
                                    .subHeadingTvSize(16)
                                    .subHeadingTvText("我会自己定位校区哦~")
                                    .maskColor(Color.parseColor("#dc000000"))
                                    .target(textView)
                                    .lineAnimDuration(400)
                                    .lineAndArcColor(Color.parseColor("#eb273f"))
                                    .dismissOnTouch(false)
                                    .dismissOnBackPress(true)
                                    .enableDismissAfterShown(true)
                                    .usageId("提示更改校区") //UNIQUE ID
                                    .setListener(spotlightListener)
                                    .show();
                        }
                    }
                }
            }
        });

        initRecyclerView();
        getLocation();
        database = new MyDatabase(this);

//        dialogBuilder = new AlertDialog.Builder(this).setMessage("请稍等片刻");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("请稍等片刻");

        searchView = (MaterialSearchView) findViewById(R.id.materialSearchView);
        history = database.getHistory();
        suggestionList = new ArrayList<>(Arrays.asList(history));

        searchView.setSuggestions(history);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!finishFlag){
                    mQuery = query;
                    progressDialog.show();
                    finishFlag = true;
                    return false;
                }
                avi.setVisibility(View.VISIBLE);
                avi.smoothToShow();
                searchView.closeSearch();
                database.insertaHistory(query, System.currentTimeMillis());
                if (suggestionList.contains(query)){
                    suggestionList.remove(suggestionList.indexOf(query));
                }
                suggestionList.add(0, query);
                history = suggestionList.toArray(new String[suggestionList.size()]);
                searchView.setSuggestions(history);

                lastPageFlag = false;
                list.removeAll(list);
                adapter.notifyDataSetChanged();
                adapter.setFootViewText("正在加载中...");
                recyclerView.setVisibility(View.VISIBLE);
                toolbar.setTitle(query);
                bookname = query.replace("+", "%2B").replace(" ", "+");

                new myAsyncTask().execute(bookname, "1");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void getLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);//自定义的code
        }

        final DPoint point_changan = new DPoint(34.03186, 108.76119);
        final DPoint point_youyi = new DPoint(34.24451, 108.91121);
//声明定位回调监听器
        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    System.out.println("onLocationChanged");
                    if (aMapLocation.getErrorCode() == 0) {
                        DPoint point = new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        if (CoordinateConverter.calculateLineDistance(point, point_changan)
                                > CoordinateConverter.calculateLineDistance(point, point_youyi)){
                            toolbar.setSubtitle("友谊校区");
                            campus = "友谊校区";
                        }
                        else{
                            toolbar.setSubtitle("长安校区");
                            campus = "长安校区";
                        }
                    }
                    else{
                        Log.e("AmapError","location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            }
        };
//初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
//设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //声明AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setOnceLocationLatest(true);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }


    private void initRecyclerView(){
        recyclerView = (SwipeMenuRecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new RecyclerViewAdapter(testActivity.this, list);
        recyclerView.setAdapter(adapter);
        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fab);
        actionButton.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                    int lastPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                    if ((lastPosition >= recyclerView.getLayoutManager().getItemCount() - 2) && (finishFlag) && (!lastPageFlag)) {
                        avi.smoothToShow();
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
                Intent intent = new Intent(testActivity.this, DetailActivity.class);
                intent.putExtra("url", map.get("bookLink"));
                intent.putExtra("bookNameReal", map.get("bookNameReal"));
                startActivity(intent);
            }
        });

        adapter.setOnItemLongClickListener(new RecyclerViewAdapter.onRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongTouch(View v, int position) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(adapter.getCardView(position));
                database.insertStore(list.get(position));
                searchView.showSuggestions();
                Toast.makeText(testActivity.this, "已收藏", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                searchView.showSearch(true);
                searchView.setVisibility(View.VISIBLE);
                break;
            case R.id.action_store:
                startActivity(new Intent(testActivity.this, StoreActivity.class));
                break;
            case R.id.action_clear_history:
                database.clearHistory();
                history = null;
                suggestionList.removeAll(suggestionList);
                searchView.setSuggestions(history);
                Toast.makeText(testActivity.this, "已清空搜索记录", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();

        } else {
            super.onBackPressed();
        }
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
                            lastPageFlag = true;
                        }
                        else{
                            lastPage = Integer.parseInt(font.text());
                        }
                    }
                }

                Elements liTags = link.select("li");
                for (Element liTag : liTags){
                    if (finishFlag){
                        break;
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
                                    String campusStr = tdTag1.text();
                                    if (!campusStr.substring(0,4).equals(campus)){
                                        continue;
                                    }
                                    builder.append("\n" + tdTag1.text() + "    " + tdTag2.text());
                                }
                            }
                        }
                    }
                    String bookPlace = builder.toString();
                    if (bookPlace.equals("")){
//                        publishProgress();
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
                                    if (isbn.indexOf('/') >= 0){
                                        isbn = isbn.substring(0, isbn.indexOf('/')).replace("-", "");
                                    }
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
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    if (isFirstStart){
                        new SpotlightView.Builder(testActivity.this)
                                .introAnimationDuration(400)
                                .enableRevealAnimation(true)
                                .performClick(true)
                                .fadeinTextDuration(400)
                                .headingTvColor(Color.parseColor("#eb273f"))
                                .headingTvSize(32)
                                .headingTvText("长按收藏")
                                .subHeadingTvColor(Color.parseColor("#ffffff"))
                                .subHeadingTvSize(16)
                                .subHeadingTvText("收藏几本好书~")
                                .maskColor(Color.parseColor("#dc000000"))
                                .target(adapter.getCardView(0))
                                .lineAnimDuration(400)
                                .lineAndArcColor(Color.parseColor("#eb273f"))
                                .dismissOnTouch(false)
                                .dismissOnBackPress(true)
                                .enableDismissAfterShown(true)
                                .usageId("提示收藏") //UNIQUE ID
                                .show();
                        isFirstStart = false;
                    }
                }
            });
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String... strings) {
            if (finishFlag){
                progressDialog.dismiss();
                searchView.closeSearch();
                database.insertaHistory(mQuery, System.currentTimeMillis());
                if (suggestionList.contains(mQuery)){
                    suggestionList.remove(suggestionList.indexOf(mQuery));
                }
                suggestionList.add(0, mQuery);
                history = suggestionList.toArray(new String[suggestionList.size()]);
                searchView.setSuggestions(history);

                lastPageFlag = false;
                list.removeAll(list);
                adapter.notifyDataSetChanged();
                adapter.setFootViewText("正在加载中...");
                recyclerView.setVisibility(View.VISIBLE);
                toolbar.setTitle(mQuery);
                bookname = mQuery.replace("+", "%2B").replace(" ", "+");

                new myAsyncTask().execute(bookname, "1");
                return;
            }
            finishFlag = true;
            if (lastPageFlag){
                adapter.setFootViewText("加载完毕");
            }
            if (list.size() == 0){
                adapter.setFootViewText("无查询结果");
            }
            adapter.notifyItemChanged(list.size());
            avi.smoothToHide();
        }

    }

    @Override
    protected void onDestroy() {
        database.closeDB();
        super.onDestroy();
    }
}
