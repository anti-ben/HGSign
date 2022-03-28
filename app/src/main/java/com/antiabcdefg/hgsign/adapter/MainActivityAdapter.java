package com.antiabcdefg.hgsign.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.antiabcdefg.hgsign.R;

import java.util.ArrayList;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private ArrayList<String> notifitioninfos;

    public MainActivityAdapter(ArrayList<String> notifitioninfos) {
        this.notifitioninfos = notifitioninfos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // 给ViewHolder设置布局文件
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_notification, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.info.setText(notifitioninfos.get(position));
    }

    @Override
    public int getItemCount() {
        // 返回数据总数
        return notifitioninfos == null ? 0 : notifitioninfos.size();
    }

    public String getItem(int position) {
        return notifitioninfos.get(position);
    }

    // 重写的自定义ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView info;

        public ViewHolder(View v) {
            super(v);
            info = (TextView) v.findViewById(R.id.notifitioninfo);
        }
    }
}
