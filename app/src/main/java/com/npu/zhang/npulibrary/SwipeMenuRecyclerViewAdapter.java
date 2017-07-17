package com.npu.zhang.npulibrary;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.npu.zhang.npulibrary.recyclerview_anim.MetricUtils;
import com.squareup.picasso.Picasso;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by zhang on 2017/7/16.
 */
public class SwipeMenuRecyclerViewAdapter extends SwipeMenuAdapter implements Filterable {

    private ArrayList<Map<String, String>> list;
    private ArrayList<Map<String, String>> rawList;
    private onRecyclerViewItemClickListener itemClickListener = null;
    private onRecyclerViewItemLongClickListener itemLongClickListener = null;
    public CardView firstCardView;
    private Context context;

    public SwipeMenuRecyclerViewAdapter(Context context, ArrayList<Map<String, String>> list){
        super();
        this.list = list;
        this.context = context;
        rawList = (ArrayList<Map<String, String>>) list.clone();
    }

    public void removeItem(String bookLink){
        for (Map<String, String> map : rawList){
            if (map.get("bookLink").equals(bookLink)){
                rawList.remove(map);
                return;
            }
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
//                String prefixString = constraint.toString();
                ArrayList<Map<String, String>> newList = new ArrayList<>();
                for (Map<String, String> map : rawList){
                    if (map.get("bookNameReal").toLowerCase().contains(constraint.toString().toLowerCase())){
                        newList.add(map);
                    }
                }
                results.values = newList;
                results.count = newList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list.removeAll(list);
                list.addAll((ArrayList<Map<String, String>>)results.values);
                notifyDataSetChanged();
            }
        };
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_bookname;
        private TextView tv_bookdetail;
        private ImageView iv_book;
        private CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_bookname = (TextView) itemView.findViewById(R.id.tv_bookname);
            tv_bookdetail = (TextView) itemView.findViewById(R.id.tv_bookdetail);
            iv_book = (ImageView) itemView.findViewById(R.id.iv_book);
            cardView = (CardView) itemView;
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

        public CardView getCardView() {
            return cardView;
        }
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_cardview, parent, false);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null){
                    itemClickListener.onItemClick(v, (Integer) v.getTag());
                }
            }
        });
        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemLongClickListener != null){
                    itemLongClickListener.onItemLongTouch(v, (Integer) v.getTag());
                }
                return true;
            }
        });
        return mView;
    }

    @Override
    public RecyclerView.ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new MyViewHolder(realContentView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        if (position == 0){
            firstCardView = viewHolder.getCardView();
        }
        viewHolder.getTv_bookname().setText(list.get(position).get("bookname"));
        viewHolder.getTv_bookdetail().setText(list.get(position).get("bookdetail"));
        if (list.get(position).get("bookpic") != null){
            Picasso.with(viewHolder.getIv_book().getContext()).load(list.get(position).get("bookpic"))
                    .into(viewHolder.getIv_book());
        }
        viewHolder.itemView.setTag(position);
//        final View itemView = viewHolder.itemView;
//        itemView.post(new Runnable() {
//            @Override
//            public void run() {
//                itemView.setTranslationX(MetricUtils.getScrWidth(context));
//                itemView.animate()
//                        .translationX(0)
//                        .setInterpolator(new DecelerateInterpolator(3.f))
//                        .setDuration(200)
//                        .start();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void setOnItemClickListener(onRecyclerViewItemClickListener listener){
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(onRecyclerViewItemLongClickListener listener){
        this.itemLongClickListener = listener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClick(View v, int position);
    }

    public interface onRecyclerViewItemLongClickListener {
        void onItemLongTouch(View v, int position);
    }
}
