package com.npu.zhang.npulibrary;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.VoiceRecognitionService;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{
    private final String version = "ver3.0";
    private static final String TAG = "Touch";
    private static final int EVENT_ERROR = 11;
    private EditText editText;
    private TextView textView;
    private ListView listView;
    private ProgressDialog waitingDialog;
    private List<Map<String, String>> lvList;
    private String bookName;
    private SimpleAdapter simpleAdapter;
    private ProgressBar progressBar;
    private Spinner spinner;
    private Switch aSwitch;
    private Button btnStop;
    private Button btnSearch;
    private ProgressDialog progressDialog;
    private boolean stopFlag;
    private int bookCount;
    private int nowBookCount;
    private InputMethodManager imm;
    private Boolean enableFilter;
    private String campus;
    private String nowCampus;
    private boolean connectFlag;
    HttpURLConnection urlConnection;
    View speechTips;
    View speechWave;
    private SpeechRecognizer speechRecognizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        spinner = (Spinner) findViewById(R.id.spinner);
        aSwitch = (Switch) findViewById(R.id.switch1);
        editText = (EditText) findViewById(R.id.editText2);
        listView = (ListView) findViewById(R.id.listView);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnStop = (Button) findViewById(R.id.btnStop);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        nowCampus = "长安";
        stopFlag = false;
        lvList = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(MainActivity.this, lvList, android.R.layout.simple_list_item_2,
                new String[] {"bookName","bookIntroduce"}, new int[] {android.R.id.text1, android.R.id.text2});

        listView.setAdapter(simpleAdapter);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("请等候");
        progressDialog.setCancelable(true);

//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this,Main2Activity.class));
//            }
//        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] tmpCampus = getResources().getStringArray(R.array.languages);
                nowCampus = tmpCampus[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopFlag = true;
                if (connectFlag) {
                    progressDialog.show();
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                connectFlag = false;
                bookName = editText.getText().toString();
                bookName = bookName.replace("+", "%2B");
                bookName = bookName.replace(" ", "+");
                if (bookName.equals("")){
                    return;
                }
                Toast.makeText(MainActivity.this, "请耐心等候，若下方进度条长时间无变化，请重新查询", Toast.LENGTH_SHORT).show();
                enableFilter = aSwitch.isChecked();
                campus = nowCampus;
                stopFlag = false;
                btnSearch.setEnabled(false);
                btnStop.setEnabled(true);
                lvList.removeAll(lvList);
                simpleAdapter.notifyDataSetChanged();
                nowBookCount = 0;
                new myAsyncTask().execute(bookName, "1");
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
        speechTips = View.inflate(this, R.layout.bd_asr_popup_speech, null);
        speechWave = speechTips.findViewById(R.id.wave);
        speechTips.setVisibility(View.GONE);
        addContentView(speechTips, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {
                final int VTAG = 0xFF00AA01;
                Integer rawHeight = (Integer) speechWave.getTag(VTAG);
                if (rawHeight == null) {
                    rawHeight = speechWave.getLayoutParams().height;
                    speechWave.setTag(VTAG, rawHeight);
                }

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) speechWave.getLayoutParams();
                params.height = (int) (rawHeight * rmsdB * 0.01);
                params.height = Math.max(params.height, speechWave.getMeasuredWidth());
                speechWave.setLayoutParams(params);
            }


            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
                Toast.makeText(MainActivity.this, "请重试", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String json_res = results.getString("origin_result");
                editText.setText(nbest.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (nbest.size() > 0) {
                     editText.setText(nbest.get(0));
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

        findViewById(R.id.btnSpeech).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (Build.VERSION.SDK_INT >= 23){
                            int checkRecordPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
                            if (checkRecordPermission != PackageManager.PERMISSION_GRANTED){
                                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 123);
                            }
                        }
                        speechTips.setVisibility(View.VISIBLE);
                        speechRecognizer.cancel();
                        Intent intent = new Intent();
                        intent.putExtra("vad", "touch");
                        editText.setText("");
                        speechRecognizer.startListening(intent);
                        return true;
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        speechTips.setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });

    }
    public class myAsyncTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... strings) {
            try {
                System.out.println(strings[0]);
                System.out.println("Begin to connnet!");
                urlConnection = (HttpURLConnection) new URL("http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=" + strings[0] + "&page=" + strings[1]).openConnection();
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

                connectFlag = true;
                Document document = Jsoup.parse(builder.toString());
                //获取条数
                Element strongTag = document.select("strong").last();
                if (strongTag == null){
                    bookCount = 0;
                    return strings;
                }else
                    bookCount = Integer.parseInt(strongTag.text());

                Element link = document.select("ol").first();
                if (link == null){
                    System.out.println("NullPointer!");
                    return strings;
                }

                Elements liTags = link.select("li");
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                for (Element liTag : liTags){
                    if (stopFlag){
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
                    nowBookCount++;

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
                                    String campusStr = tdTag1.text().replace("校区", "");
                                    String enableStr = tdTag2.text();
                                    if ((!campusStr.substring(0,2).equals(campus)) || ((enableFilter)&&(!enableStr.equals("可借")))){
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
                        publishProgress();
                        continue;
                    }
                    if (bookPlace.equals("")){
                        bookPlace = "\n此书刊可能正在订购中或者处理中";
                    }
                    Map<String, String> map = new HashMap<String, String>();

                    map.put("bookName", bookName);
                    map.put("bookLink", bookLink);
                    map.put("bookIntroduce", bookIntroduce + "\n" + bookPlace);
                    map.put("bookNameReal", bookNameReal);
                    map.put("bookPlace", bookPlace);
                    lvList.add(map);
                    list.add(map);
                    publishProgress();
                }
                return strings;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            simpleAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(bookCount);
            progressBar.setProgress(nowBookCount);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String... strings) {
            if (stopFlag){
                progressDialog.cancel();
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                btnStop.setEnabled(false);
            }
            if ((strings == null)){
                return;
            }
            if (bookCount == 0){
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "无结果", Toast.LENGTH_SHORT).show();
                System.out.println("无结果");
                btnSearch.setEnabled(true);
                btnStop.setEnabled(false);
                return;
            }
            String book = strings[0];
            String page = strings[1];
            if (nowBookCount < bookCount){
                page = (Integer.parseInt(page) + 1) + "";
                new myAsyncTask().execute(book, page);
            }else
            {
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                btnStop.setEnabled(false);
            }

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
