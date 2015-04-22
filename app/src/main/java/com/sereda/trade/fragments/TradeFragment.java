package com.sereda.trade.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.sereda.trade.R;
import com.sereda.trade.data.CandleChartData;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import needle.Needle;
import needle.UiRelatedTask;

public class TradeFragment extends Fragment {
    private static final String YAHOO = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22GOOG%22%20and%20startDate%20%3D%20%222014-01-24%22%20and%20endDate%20%3D%20%222015-01-24%22&format=json&diagnostics=true&env=http%3A%2F%2Fdatatables.org%2Falltables.env&callback";
    private String symbol, expiration, payout;
    private TextView tvSymbol, tvExpiration, tvPayout;
    private ArrayList<CandleChartData> chartData = new ArrayList<>();
    private UiRelatedTask<ArrayList<CandleEntry>> chartNeedle;
    private ArrayList<CandleEntry> candleEntries = new ArrayList<>();
    private CandleStickChart candleStickChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (null != bundle) {
            symbol = bundle.getString("symbol");
            expiration = bundle.getString("expiration");
            payout = bundle.getString("payout");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade, container, false);

        tvSymbol = (TextView) view.findViewById(R.id.tv_symbol);
        tvExpiration = (TextView) view.findViewById(R.id.tv_expiry_date);
        tvPayout = (TextView) view.findViewById(R.id.tv_payout);
        candleStickChart = (CandleStickChart) view.findViewById(R.id.chart);

        getCandleChartData();

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

    private void getCandleChartData() {
        chartNeedle = new UiRelatedTask<ArrayList<CandleEntry>>() {
            @Override
            protected ArrayList<CandleEntry> doWork() {
                try {
                    DefaultHttpClient hc = new DefaultHttpClient();
                    ResponseHandler<String> res = new BasicResponseHandler();
                    HttpPost postMethod = new HttpPost(YAHOO);
                    String response = hc.execute(postMethod, res);

                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject queryObject = json.getJSONObject("query");
                        JSONObject resultsObject = queryObject.getJSONObject("results");
                        JSONArray quoteArray = resultsObject.getJSONArray("quote");
                        for (int i = 0; i < quoteArray.length(); i++) {
                            chartData.add(new CandleChartData(i,
                                    quoteArray.getJSONObject(i).getString("Symbol"),
                                    quoteArray.getJSONObject(i).getString("Date"),
                                    quoteArray.getJSONObject(i).getDouble("Open"),
                                    quoteArray.getJSONObject(i).getDouble("High"),
                                    quoteArray.getJSONObject(i).getDouble("Low"),
                                    quoteArray.getJSONObject(i).getDouble("Close"),
                                    quoteArray.getJSONObject(i).getInt("Volume"),
                                    quoteArray.getJSONObject(i).getDouble("Adj_Close")));

                            candleEntries.add(new CandleEntry(i,
                                    Float.parseFloat(String.valueOf(quoteArray.getJSONObject(i).getDouble("High"))),
                                    Float.parseFloat(String.valueOf(quoteArray.getJSONObject(i).getDouble("Low"))),
                                    Float.parseFloat(String.valueOf(quoteArray.getJSONObject(i).getDouble("Open"))),
                                    Float.parseFloat(String.valueOf(quoteArray.getJSONObject(i).getDouble("Close")))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return candleEntries;
            }

            @Override
            protected void thenDoUiRelatedWork(ArrayList<CandleEntry> result) {
                CandleDataSet candleDataSet = new CandleDataSet(result, symbol);
                candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                candleDataSet.setColor(Color.rgb(80, 80, 80));
                candleDataSet.setShadowColor(Color.DKGRAY);
                candleDataSet.setShadowWidth(0.7f);
                candleDataSet.setDecreasingColor(Color.RED);
                candleDataSet.setDecreasingPaintStyle(Paint.Style.STROKE);
                candleDataSet.setIncreasingColor(Color.rgb(122, 242, 84));
                candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);

                XAxis xAxis = candleStickChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                YAxis leftAxis = candleStickChart.getAxisLeft();
                YAxis rightAxis = candleStickChart.getAxisRight();
                leftAxis.setStartAtZero(false);
                rightAxis.setStartAtZero(false);

                ArrayList<String> xVals = new ArrayList<>();
                for (int i = 0; i < chartData.size(); i++) {
                    xVals.add(chartData.get(i).getDate());
                }

                CandleData data = new CandleData(xVals, candleDataSet);

                candleStickChart.setData(data);
                candleStickChart.invalidate();
            }
        };
        Needle.onBackgroundThread().withThreadPoolSize(1).execute(chartNeedle);
    }
}
