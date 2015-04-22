package com.sereda.trade.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sereda.trade.R;
import com.sereda.trade.data.DrawerItem;

import java.util.ArrayList;
import java.util.List;

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {
    Context context;
    List<DrawerItem> drawerItemList;
    int layoutResID;

    public DrawerAdapter(Context context, int layoutResourceID, List<DrawerItem> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;

    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new ViewHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.tvName = (TextView) view.findViewById(R.id.tv_drawer_item);

            view.setTag(drawerHolder);
        } else {
            drawerHolder = (ViewHolder) view.getTag();
        }

        DrawerItem dItem = this.drawerItemList.get(position);
        drawerHolder.tvName.setText(dItem.getName());

        return view;
    }

    private static class ViewHolder {
        TextView tvName;
    }
}
