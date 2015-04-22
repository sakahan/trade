package com.sereda.trade.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sereda.trade.R;
import com.sereda.trade.data.Deals;
import com.sereda.trade.data.DrawerItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DealsAdapter extends ArrayAdapter<Deals> {
    Context context;
    List<Deals> values;
    int layoutID;

    public DealsAdapter(Context context, int layoutID, List<Deals> values) {
        super(context, layoutID, values);
        this.context = context;
        this.values = values;
        this.layoutID = layoutID;

    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            holder = new ViewHolder();

            view = inflater.inflate(layoutID, parent, false);
            holder.tvSymbol = (TextView) view.findViewById(R.id.tv_symbol);
            holder.tvExpDate = (TextView) view.findViewById(R.id.tv_expiry_date);
            holder.tvPayout = (TextView) view.findViewById(R.id.tv_payout);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Deals item = this.values.get(position);
        holder.tvSymbol.setText(item.getSymbol());
        holder.tvExpDate.setText(formatExpDate(item.getStartAt()));
        holder.tvPayout.setText("Payout (" + formatPayout(item.getPayMatch()) + "%)");

        return view;
    }

    private String formatPayout(double payout) {
        return String.format("%.0f", payout);
    }

    @SuppressWarnings("deprecation")
    private String formatExpDate(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
        try {
            Date date = sdf.parse(string);
            string = "Expiration date: " + addZero(date.getHours()) + ":" + addZero(date.getMinutes()) + ":" + addZero(date.getSeconds());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return string;
    }

    private String addZero(int date) {
        if (date < 10) {
            return "0" + date;
        } else {
            return String.valueOf(date);
        }
    }

    private static class ViewHolder {
        TextView tvSymbol, tvExpDate, tvPayout;
    }
}
