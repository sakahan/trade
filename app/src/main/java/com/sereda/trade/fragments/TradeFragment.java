package com.sereda.trade.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sereda.trade.R;
import com.sereda.trade.data.Deals;
import com.sereda.trade.data.LineChartData;
import needle.Needle;
import needle.UiRelatedTask;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TradeFragment extends Fragment {
    private String symbol, expiration, payout;
    private Deals deal;
    private TextView tvSymbol, tvExpiration, tvPayout;
    private LineChart lineChart;
    private ArrayList<LineChartData> lineChartData = new ArrayList<>();
    private UiRelatedTask<ArrayList<LineChartData>> lineChartNeedle;
    private ArrayList<Entry> lineEntries_1 = new ArrayList<>();
    private ArrayList<Entry> lineEntries_2 = new ArrayList<>();
    private String dealID, startAt, endAt, duration, payMatch, payNoMatch, minStake, maxStake;
    private ProgressBar progressBar;
    private AsyncHttpClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle bundle = this.getArguments();
        if (null != bundle) {
            deal = (Deals) bundle.getSerializable("deals");
            symbol = deal.getSymbol();
            expiration = deal.getStartAt();
            payout = String.valueOf(deal.getPayMatch());

            dealID = String.valueOf(deal.getDealID());
            startAt = String.valueOf(deal.getStartAt());
            endAt = String.valueOf(deal.getEndAt());
            duration = String.valueOf(deal.getDuration());
            payMatch = String.valueOf(deal.getPayMatch());
            payNoMatch = String.valueOf(deal.getPayNoMatch());
            minStake = String.valueOf(deal.getMinStake());
            maxStake = String.valueOf(deal.getMaxStake());
        }

        client = new AsyncHttpClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade, container, false);

        tvSymbol = (TextView) view.findViewById(R.id.tv_symbol);
        tvExpiration = (TextView) view.findViewById(R.id.tv_expiry_date);
        tvPayout = (TextView) view.findViewById(R.id.tv_payout);
        lineChart = (LineChart) view.findViewById(R.id.line_chart);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        String request = createRequest();
        if (null != request && !request.isEmpty()) {
            httpRequest(request);
        }

        if (null != symbol && !symbol.isEmpty()) {
            tvSymbol.setText(symbol);
        }
        if (null != expiration && !expiration.isEmpty()) {
            tvExpiration.setText(expiration);
        }
        if (null != payout && !payout.isEmpty()) {
            tvPayout.setText(payout);
        }

        return view;
    }

    private String setDate(Date date) {
        return new SimpleDateFormat("yyyy", Locale.ENGLISH).format(date) + "-"
                + new SimpleDateFormat("MM", Locale.ENGLISH).format(date) + "-"
                + new SimpleDateFormat("dd", Locale.ENGLISH).format(date);
    }

    private Date getDate(String string) {
        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

        try {
            return format.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createRequest() {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.setTime(endDate);
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();

        if (null != symbol && !symbol.isEmpty()) {
            return "https://query.yahooapis.com/v1/public/yql?q=select%20%2A%20from%20yahoo.finance.historicaldata%20" +
                    "where%20symbol%20%3D%20%22" + getYahooSymbol(symbol)
                    + "%22%20and%20startDate%20%3D%20%22" + setDate(startDate)
                    + "%22%20and%20endDate%20%3D%20%22" + setDate(endDate)
                    + "%22&format=json&diagnostics=true&env=http%3A%2F%2Fdatatables.org%2Falltables.env&callback%22";
        } else {
            return null;
        }
    }

    private void httpRequest(String request) {
        if (null != client) {
            client.cancelAllRequests(true);
        }

        client.get(request, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                getLineChartData(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
                Toast.makeText(getActivity(), "Looks like request is bad", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    private void getLineChartData(final byte[] response) {
        if (null != lineChartNeedle) {
            lineChartNeedle.cancel();
            lineChartNeedle = null;
        }
        if (null != lineChartData && lineChartData.size() > 0) {
            lineChartData.clear();
        }
        if (null != lineEntries_1 && lineEntries_1.size() > 0) {
            lineEntries_1.clear();
        }
        if (null != lineEntries_2 && lineEntries_2.size() > 0) {
            lineEntries_2.clear();
        }

        lineChartNeedle = new UiRelatedTask<ArrayList<LineChartData>>() {
            @Override
            protected ArrayList<LineChartData> doWork() {
                try {
                    JSONObject json = new JSONObject(new String(response));
                    JSONObject queryObject = json.getJSONObject("query");
                    JSONObject resultsObject = queryObject.getJSONObject("results");
                    JSONArray quoteArray = resultsObject.getJSONArray("quote");
                    for (int i = 0; i < quoteArray.length(); i++) {
                        lineChartData.add(new LineChartData(i,
                                quoteArray.getJSONObject(i).getString("Date"),
                                quoteArray.getJSONObject(i).getDouble("Close")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return lineChartData;
            }

            @Override
            protected void thenDoUiRelatedWork(ArrayList<LineChartData> result) {
                progressBar.setVisibility(View.GONE);
                lineChart.setVisibility(View.VISIBLE);

                float open = 0;
                float close = 0;

                Date startDate = getDate(startAt);
                String start = setDate(startDate);

                Date endDate = getDate(endAt);
                String end = setDate(endDate);

                Collections.reverse(result);
                for (int i = 0; i < result.size(); i++) {
                    lineEntries_1.add(
                            new BarEntry(Float.parseFloat(String.valueOf(result.get(i).getClose())), i));
                }
                ArrayList<String> xVals = new ArrayList<>();
                for (int i = 0; i < lineChartData.size(); i++) {
                    xVals.add(lineChartData.get(i).getDate());

                    if (lineChartData.get(i).getDate().equals(start)) {
                        lineEntries_2.add(new BarEntry(Float.parseFloat(
                                String.valueOf(lineChartData.get(i).getClose())), i));
                        open = Float.parseFloat(String.valueOf(lineChartData.get(i).getClose()));
                    }
//                    if (lineChartData.get(i).getDate().equals(end)) {
                    if (lineChartData.get(i).getDate().equals("2015-05-01")) {
                        lineEntries_2.add(new BarEntry(Float.parseFloat(
                                String.valueOf(lineChartData.get(i).getClose())), i));
                        close = Float.parseFloat(String.valueOf(lineChartData.get(i).getClose()));
                    }
                }

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                YAxis leftAxis = lineChart.getAxisLeft();
                YAxis rightAxis = lineChart.getAxisRight();
                leftAxis.setStartAtZero(false);
                rightAxis.setStartAtZero(false);

                LineDataSet set_1 = new LineDataSet(lineEntries_1, symbol);
                set_1.setColor(Color.CYAN);
                set_1.setCircleColor(Color.CYAN);
                set_1.setLineWidth(1f);
                set_1.setCircleSize(0f);
                set_1.setDrawCircleHole(false);
                set_1.setValueTextSize(0f);
                set_1.setFillAlpha(65);
                set_1.setFillColor(Color.CYAN);

                LineDataSet set_2 = new LineDataSet(lineEntries_2, "deal id: " + dealID);
                if (open <= close) {
                    set_2.setColor(Color.GREEN);
                    set_2.setCircleColor(Color.GREEN);
                    set_2.setLineWidth(1f);
                    set_2.setCircleSize(1f);
                    set_2.setDrawCircleHole(false);
                    set_2.setValueTextSize(9f);
                    set_2.setFillAlpha(65);
                    set_2.setFillColor(Color.GREEN);
                } else {
                    set_2.setColor(Color.RED);
                    set_2.setCircleColor(Color.RED);
                    set_2.setLineWidth(1f);
                    set_2.setCircleSize(1f);
                    set_2.setDrawCircleHole(false);
                    set_2.setValueTextSize(9f);
                    set_2.setFillAlpha(65);
                    set_2.setFillColor(Color.RED);
                }

                ArrayList<LineDataSet> dataSets = new ArrayList<>();

                dataSets.add(set_1);
                dataSets.add(set_2);

                LineData data = new LineData(xVals, dataSets);

                lineChart.setData(data);
                lineChart.invalidate();
            }
        };
        Needle.onBackgroundThread().withThreadPoolSize(1).execute(lineChartNeedle);
    }

    private YahooSymbols getYahooSymbol(String symbol) {
        switch (InnerSymbols.valueOf(symbol)) {
            case AP1USD:
                return YahooSymbols.AAPL;
            case CC1USD:
                return YahooSymbols.CCE;
            case EB1USD:
                return YahooSymbols.EBAY;
            case FB1USD:
                return YahooSymbols.FB;
            case FO1USD:
                return YahooSymbols.F;
            case GO1USD:
                return YahooSymbols.GOOG;
            case IB1USD:
                return YahooSymbols.IBM;
            default:
                return null;
        }
    }

    private enum YahooSymbols {AAPL, CCE, EBAY, FB, F, GOOG, IBM}

    private enum InnerSymbols {AP1USD, CC1USD, EB1USD, FB1USD, FO1USD, GO1USD, IB1USD}
}
