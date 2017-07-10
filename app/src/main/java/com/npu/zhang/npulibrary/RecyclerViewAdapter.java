package com.npu.zhang.npulibrary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by zhang on 2017/7/9.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter {

    private final int NORMAL_TYPE = 0;
    private final int FOOT_TYPE = 1;
    private String footViewText = "正在加载中...";
    private ArrayList<Map<String, String>> list;
    private onRecyclerViewItemClickListener itemClickListener = null;
    public RecyclerViewAdapter(){
        super();
        list = new ArrayList<>();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView footView;
        private TextView tv_bookname;
        private TextView tv_bookdetail;
        private ImageView iv_book;

        public MyViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == NORMAL_TYPE){
                tv_bookname = (TextView) itemView.findViewById(R.id.tv_bookname);
                tv_bookdetail = (TextView) itemView.findViewById(R.id.tv_bookdetail);
                iv_book = (ImageView) itemView.findViewById(R.id.iv_book);
            }
            else if (viewType == FOOT_TYPE){
                footView = (TextView) itemView.findViewById(R.id.footView);
            }
        }

        public TextView getFootView() {
            return footView;
        }

        public ImageView getIv_book() {
            return iv_book;
        }

        public TextView getTv_bookdetail() {
            return tv_bookdetail;
        }

        public TextView getTv_bookname() {
            return tv_bookname;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOT_TYPE){
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.listview_bottom_layout, parent, false), viewType);
        }
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_cardview, parent, false);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null){
                    itemClickListener.onItemClick(v, (Integer) v.getTag());
                }
            }
        });
        return new MyViewHolder(mView, NORMAL_TYPE);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == NORMAL_TYPE){
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.getTv_bookname().setText(list.get(position).get("bookname"));
            viewHolder.getTv_bookdetail().setText(list.get(position).get("bookdetail"));
            if (list.get(position).get("bookpic") != null){
                Picasso.with(viewHolder.getIv_book().getContext()).load(list.get(position).get("bookpic"))
                        .into(viewHolder.getIv_book());
            }
            viewHolder.itemView.setTag(position);
        }
        else if (getItemViewType(position) == FOOT_TYPE){
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.getFootView().setText(footViewText);
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    public ArrayList<Map<String, String>> getList() {
        return list;
    }

    public void setFootViewText(String footViewText) {
        this.footViewText = footViewText;
    }

    @Override
    public int getItemViewType(int position) {
        System.out.println(getItemCount());
        if (position == getItemCount() - 1){
            return FOOT_TYPE;
        }
        return NORMAL_TYPE;
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener listener){
        this.itemClickListener = listener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClick(View v, int position);
    }
}