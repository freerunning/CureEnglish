package com.mm.cureenglish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PracticeAdapter extends ArrayAdapter<String> {

    public PracticeAdapter(@NonNull Context context) {
        super(context, R.layout.practice_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.practice_item, parent, false);
            holder.indexView = convertView.findViewById(R.id.index);
            holder.contentView = convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.indexView.setText(String.valueOf(position));
        holder.contentView.setText(getItem(position));

        return convertView;
    }

    static class ViewHolder {
        TextView indexView;
        TextView contentView;
    }
}
