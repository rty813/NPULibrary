package com.npu.zhang.npulibrary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.wooplr.spotlight.SpotlightView;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private MyDatabase database;
    private ArrayList<Map<String, String>> list;
    private SwipeMenuRecyclerViewAdapter adapter;
    private SearchView searchView;
    private boolean isCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Toolbar toolbar = (Toolbar) findViewById(R.id.store_toolbar);
        setSupportActionBar(toolbar);

        database = testActivity.database;
        list = database.getStore();
        if (list.size() == 0){
            findViewById(R.id.tv_store_hint).setVisibility(View.VISIBLE);
        }

//        searchView = (SearchView) findViewById(R.id.store_searchView);

        final SwipeMenuRecyclerView recyclerView = (SwipeMenuRecyclerView) findViewById(R.id.store_recyclervier);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SwipeMenuRecyclerViewAdapter(this, list);
        adapter.setOnItemClickListener(new SwipeMenuRecyclerViewAdapter.onRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                HashMap<String, String> map = (HashMap<String, String>) list.get(position);
                Intent intent = new Intent(StoreActivity.this, DetailActivity.class);
                intent.putExtra("url", map.get("bookLink"));
                intent.putExtra("bookNameReal", map.get("bookNameReal"));
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setItemViewSwipeEnabled(true);
        recyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public void onItemDismiss(final int position) {
                final Map<String, String> map = list.get(position);
                list.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, list.size() - position);
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.store_container);
                isCancel = false;
                Snackbar.make(coordinatorLayout, "取消收藏", Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isCancel = true;
                                list.add(position, map);
                                adapter.notifyItemInserted(position);
                                adapter.notifyItemRangeChanged(position + 1, list.size() - position);
                            }
                        })
                        .setCallback(new MyCallBack(map.get("bookLink")))
                        .show();
            }
        });
        if (list.size() > 0){
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    new SpotlightView.Builder(StoreActivity.this)
                            .introAnimationDuration(400)
                            .enableRevealAnimation(true)
                            .performClick(true)
                            .fadeinTextDuration(400)
                            .headingTvColor(Color.parseColor("#eb273f"))
                            .headingTvSize(32)
                            .headingTvText("取消收藏")
                            .subHeadingTvColor(Color.parseColor("#ffffff"))
                            .subHeadingTvSize(16)
                            .subHeadingTvText("向左右两侧滑动")
                            .maskColor(Color.parseColor("#dc000000"))
                            .target(adapter.firstCardView)
                            .lineAnimDuration(400)
                            .lineAndArcColor(Color.parseColor("#eb273f"))
                            .dismissOnTouch(false)
                            .dismissOnBackPress(true)
                            .enableDismissAfterShown(true)
                            .usageId("提示取消收藏") //UNIQUE ID
                            .show();
                }
            });
        }
    }

    private class MyCallBack extends Snackbar.Callback{
        private String bookLink;
        public MyCallBack(String bookLink){
            super();
            this.bookLink = bookLink;
        }
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            if (isCancel){
                return;
            }
            System.out.println("remove");
            adapter.removeItem(bookLink);
            database.removeStore(bookLink);
            super.onDismissed(transientBottomBar, event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_store, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);

        SearchView.SearchAutoComplete textView = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.DKGRAY);

        ImageView imageView = (ImageView) searchView.findViewById(R.id.search_close_btn);
        imageView.setImageResource(R.drawable.ic_action_navigation_close_inverted);

        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }
}
