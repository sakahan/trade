package com.sereda.trade.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sereda.trade.R;
import com.sereda.trade.data.BarChartData;
import com.sereda.trade.data.CandleChartData;
import com.sereda.trade.data.LineChartData;
import needle.Needle;
import needle.UiRelatedTask;
import org.apache.http.Header;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChartFragment extends Fragment {
    private static final int CANDLE_STICK_CHART = 1;
    private static final int BAR_CHART = 2;
    private static final int LINE_CHART = 3;
    private Button btCandleChart, btBarChart, btLineChart, btAddChart;
    private EditText etAssetName, etStartDate, etEndDate;
    private CandleStickChart candleStickChart;
    private BarChart barChart;
    private LineChart lineChart;
    private ArrayList<CandleChartData> candleChartData;
    private ArrayList<BarChartData> barChartData;
    private ArrayList<LineChartData> lineChartData;
    private UiRelatedTask<ArrayList<CandleChartData>> candleChartNeedle;
    private UiRelatedTask<ArrayList<BarChartData>> barChartNeedle;
    private UiRelatedTask<ArrayList<LineChartData>> lineChartNeedle;
    private ArrayList<CandleEntry> candleEntries;
    private ArrayList<BarEntry> barEntries;
    private ArrayList<Entry> lineEntries;
    private ArrayList<BarDataSet> barDataSet;
    private ArrayList<LineDataSet> lineDataSet;
    private CandleDataSet candleDataSet;
    private String end = null, start = null;
    private AsyncHttpClient client;
    private String request;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        candleChartData = new ArrayList<>();
        barChartData = new ArrayList<>();
        lineChartData = new ArrayList<>();
        candleEntries = new ArrayList<>();
        barEntries = new ArrayList<>();
        lineEntries = new ArrayList<>();
        barDataSet = new ArrayList<>();
        lineDataSet = new ArrayList<>();

        client = new AsyncHttpClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        findView(view);

        btAddChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStartEndDate();
                request = createRequest(symbolName());
                if (null != request && !request.isEmpty()) {
                    httpRequest(request, CANDLE_STICK_CHART);
                }

                btCandleChart.setEnabled(false);
                btBarChart.setEnabled(true);
                btLineChart.setEnabled(true);
                candleStickChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                lineChart.setVisibility(View.GONE);
            }
        });
        btCandleChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChart(CANDLE_STICK_CHART);
            }
        });
        btBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChart(BAR_CHART);
            }
        });
        btLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChart(LINE_CHART);
            }
        });

        return view;
    }

    private String symbolName() {
        if (!etAssetName.getText().toString().isEmpty()) {
            return etAssetName.getText().toString();
        } else {
            return null;
        }
    }

    private String createRequest(String symbol) {
        return "https://query.yahooapis.com/v1/public/yql?q=select%20%2A%20from%20yahoo.finance.historicaldata%20" +
                "where%20symbol%20%3D%20%22" + symbol
                + "%22%20and%20startDate%20%3D%20%22" + start
                + "%22%20and%20endDate%20%3D%20%22" + end
                + "%22&format=json&diagnostics=true&env=http%3A%2F%2Fdatatables.org%2Falltables.env&callback%22";
    }

    private void httpRequest(String request, final int chartType) {
        if (null != client) {
            client.cancelAllRequests(true);
        }

        client.get(request, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                switch (chartType) {
                    case CANDLE_STICK_CHART:
                        getCandleChartData(response);
                        break;
                    case BAR_CHART:
                        getBarChartData(response);
                        break;
                    case LINE_CHART:
                        getLineChartData(response);
                        break;
                    default:
                        getCandleChartData(response);
                }
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

    private void changeChart(int chosenButton) {
        switch (chosenButton) {
            case CANDLE_STICK_CHART:
                setStartEndDate();
                request = createRequest(symbolName());
                if (null != request && !request.isEmpty()) {
                    httpRequest(request, CANDLE_STICK_CHART);
                }
                btCandleChart.setEnabled(false);
                btBarChart.setEnabled(true);
                btLineChart.setEnabled(true);

                candleStickChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                lineChart.setVisibility(View.GONE);
                break;
            case BAR_CHART:
                setStartEndDate();
                request = createRequest(symbolName());
                if (null != request && !request.isEmpty()) {
                    httpRequest(request, BAR_CHART);
                }
                btCandleChart.setEnabled(true);
                btBarChart.setEnabled(false);
                btLineChart.setEnabled(true);

                candleStickChart.setVisibility(View.GONE);
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                break;
            case LINE_CHART:
                setStartEndDate();
                request = createRequest(symbolName());
                if (null != request && !request.isEmpty()) {
                    httpRequest(request, LINE_CHART);
                }
                btCandleChart.setEnabled(true);
                btBarChart.setEnabled(true);
                btLineChart.setEnabled(false);

                candleStickChart.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                lineChart.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void findView(View view) {
        etAssetName = (EditText) view.findViewById(R.id.et_asset_name);
        etStartDate = (EditText) view.findViewById(R.id.et_start_date);
        etEndDate = (EditText) view.findViewById(R.id.et_end_date);

        btCandleChart = (Button) view.findViewById(R.id.bt_candle_chart);
        btBarChart = (Button) view.findViewById(R.id.bt_bar_chart);
        btLineChart = (Button) view.findViewById(R.id.bt_line_chart);
        btAddChart = (Button) view.findViewById(R.id.bt_add_chart);

        candleStickChart = (CandleStickChart) view.findViewById(R.id.candle_chart);
        barChart = (BarChart) view.findViewById(R.id.bar_chart);
        lineChart = (LineChart) view.findViewById(R.id.line_chart);
    }

    private String setDate(Date date) {
        return new SimpleDateFormat("yyyy", Locale.ENGLISH).format(date) + "-"
                + new SimpleDateFormat("MM", Locale.ENGLISH).format(date) + "-"
                + new SimpleDateFormat("dd", Locale.ENGLISH).format(date);
    }

    private void setStartEndDate() {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.setTime(endDate);
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();

        if (etEndDate.getText().toString().isEmpty()) {
            end = setDate(endDate);
        } else {
            end = etEndDate.getText().toString();
        }

        if (etStartDate.getText().toString().isEmpty()) {
            start = setDate(startDate);
        } else {
            start = etStartDate.getText().toString();
        }
    }

    private void getCandleChartData(final byte[] response) {
        if (null != candleChartNeedle) {
            candleChartNeedle.cancel();
            candleChartNeedle = null;
        }
        removeLineDataSet(CANDLE_STICK_CHART);

        candleChartNeedle = new UiRelatedTask<ArrayList<CandleChartData>>() {
            @Override
            protected ArrayList<CandleChartData> doWork() {
                    try {
                        JSONObject json = new JSONObject(new String(response));
                        JSONObject queryObject = json.getJSONObject("query");
                        JSONObject resultsObject = queryObject.getJSONObject("results");
                        JSONArray quoteArray = resultsObject.getJSONArray("quote");
                        for (int i = 0; i < quoteArray.length(); i++) {
                            candleChartData.add(new CandleChartData(i,
                                    quoteArray.getJSONObject(i).getString("Symbol"),
                                    quoteArray.getJSONObject(i).getString("Date"),
                                    quoteArray.getJSONObject(i).getDouble("Open"),
                                    quoteArray.getJSONObject(i).getDouble("High"),
                                    quoteArray.getJSONObject(i).getDouble("Low"),
                                    quoteArray.getJSONObject(i).getDouble("Close"),
                                    quoteArray.getJSONObject(i).getInt("Volume"),
                                    quoteArray.getJSONObject(i).getDouble("Adj_Close")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                return candleChartData;
            }

            @Override
            protected void thenDoUiRelatedWork(ArrayList<CandleChartData> result) {
                if (null != result && result.size() > 0) {
                    Collections.reverse(result);
                    for (int i = 0; i < result.size(); i++) {
                        candleEntries.add(new CandleEntry(i,
                                Float.parseFloat(String.valueOf(result.get(i).getHigh())),
                                Float.parseFloat(String.valueOf(result.get(i).getLow())),
                                Float.parseFloat(String.valueOf(result.get(i).getOpen())),
                                Float.parseFloat(String.valueOf(result.get(i).getClose()))));
                    }

                    if (null != candleDataSet) {
                        candleDataSet = null;
                    }
                    if (null != candleEntries && !candleEntries.isEmpty()) {
                        candleDataSet = new CandleDataSet(candleEntries, etAssetName.getText().toString());
                        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                        candleDataSet.setColor(Color.rgb(80, 80, 80));
                        candleDataSet.setShadowColor(Color.DKGRAY);
                        candleDataSet.setShadowWidth(0.7f);
                        candleDataSet.setDecreasingColor(Color.RED);
                        candleDataSet.setDecreasingPaintStyle(Paint.Style.STROKE);
                        candleDataSet.setIncreasingColor(Color.rgb(122, 242, 84));
                        candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);

                        if (null != candleStickChart) {
                            XAxis xAxis = candleStickChart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis leftAxis = candleStickChart.getAxisLeft();
                            YAxis rightAxis = candleStickChart.getAxisRight();
                            leftAxis.setStartAtZero(false);
                            rightAxis.setStartAtZero(false);
                        }

                        ArrayList<String> xVals = new ArrayList<>();
                        if (null != candleChartData && candleChartData.size() > 0) {
                            for (CandleChartData aCandleChartData : candleChartData) {
                                xVals.add(aCandleChartData.getDate());
                            }
                        }

                        if (xVals.size() > 0 && candleDataSet != null) {
                            CandleData data = new CandleData(xVals, candleDataSet);

                            if (null != candleStickChart) {
                                candleStickChart.setData(data);
                                candleStickChart.invalidate();
                            }
                        }
                    }
                }
            }
        };
        Needle.onBackgroundThread().withThreadPoolSize(1).execute(candleChartNeedle);
    }

    private void getBarChartData(final byte[] response) {
        if (null != barChartNeedle) {
            barChartNeedle.cancel();
            barChartNeedle = null;
        }
        if (null != barChartData && barChartData.size() > 0) {
            barChartData.clear();
        }
        if (null != barEntries && barEntries.size() > 0) {
            barEntries.clear();
        }

        barChartNeedle = new UiRelatedTask<ArrayList<BarChartData>>() {
            @Override
            protected ArrayList<BarChartData> doWork() {
                    try {
                        JSONObject json = new JSONObject(new String(response));
                        JSONObject queryObject = json.getJSONObject("query");
                        JSONObject resultsObject = queryObject.getJSONObject("results");
                        JSONArray quoteArray = resultsObject.getJSONArray("quote");
                        for (int i = 0; i < quoteArray.length(); i++) {
                            barChartData.add(new BarChartData(i,
                                    quoteArray.getJSONObject(i).getString("Date"),
                                    quoteArray.getJSONObject(i).getDouble("Close")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                return barChartData;
            }

            @Override
            protected void thenDoUiRelatedWork(ArrayList<BarChartData> result) {
                removeLineDataSet(BAR_CHART);

                Collections.reverse(result);
                for (int i = 0; i < result.size(); i++) {
                    barEntries.add(new BarEntry(Float.parseFloat(String.valueOf(result.get(i).getClose())), i));
                }

                ArrayList<String> xVals = new ArrayList<>();
                for (BarChartData aBarChartData : barChartData) {
                    xVals.add(aBarChartData.getDate());
                }

                ArrayList<BarEntry> yVals = new ArrayList<>();
                if (null != barEntries && !barEntries.isEmpty()) {
                    for (BarEntry entries : barEntries) {
                        yVals.add(entries);
                    }

                    BarDataSet set = new BarDataSet(yVals, etAssetName.getText().toString());
                    set.setBarSpacePercent(35f);

                    barDataSet.add(set);

                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    YAxis leftAxis = barChart.getAxisLeft();
                    YAxis rightAxis = barChart.getAxisRight();
                    leftAxis.setStartAtZero(false);
                    rightAxis.setStartAtZero(false);

                    BarData barData = new BarData(xVals, barDataSet);
                    barChart.setData(barData);
                    barChart.invalidate();
                }
            }
        };
        Needle.onBackgroundThread().withThreadPoolSize(1).execute(barChartNeedle);
    }

    private void getLineChartData(final byte[] response) {
        if (null != lineChartNeedle) {
            lineChartNeedle.cancel();
            lineChartNeedle = null;
        }
        if (null != lineChartData && lineChartData.size() > 0) {
            lineChartData.clear();
        }
        if (null != lineEntries && lineEntries.size() > 0) {
            lineEntries.clear();
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
                removeLineDataSet(LINE_CHART);

                Collections.reverse(result);
                for (int i = 0; i < result.size(); i++) {
                    lineEntries.add(new BarEntry(Float.parseFloat(String.valueOf(result.get(i).getClose())), i));
                }

                ArrayList<String> xVals = new ArrayList<>();
                for (LineChartData aLineChartData : lineChartData) {
                    xVals.add(aLineChartData.getDate());
                }

                ArrayList<Entry> yVals = new ArrayList<>();
                if (null != lineEntries && !lineEntries.isEmpty()) {
                    for (Entry entries : lineEntries) {
                        yVals.add(entries);
                    }

                    LineDataSet set = new LineDataSet(yVals, etAssetName.getText().toString());
                    set.setColor(Color.BLACK);
                    set.setCircleColor(Color.BLACK);
                    set.setLineWidth(1f);
                    set.setCircleSize(2f);
                    set.setDrawCircleHole(false);
                    set.setValueTextSize(9f);
                    set.setFillAlpha(65);
                    set.setFillColor(Color.BLACK);

                    lineDataSet.add(set);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    YAxis leftAxis = lineChart.getAxisLeft();
                    YAxis rightAxis = lineChart.getAxisRight();
                    leftAxis.setStartAtZero(false);
                    rightAxis.setStartAtZero(false);

                    LineData data = new LineData(xVals, lineDataSet);

                    lineChart.setData(data);
                    lineChart.invalidate();
                }
            }
        };
        Needle.onBackgroundThread().withThreadPoolSize(1).execute(lineChartNeedle);
    }

    private void removeLineDataSet(int chosenChart) {
        switch (chosenChart) {
            case CANDLE_STICK_CHART:
                if (null != candleStickChart) {
                    CandleData data = candleStickChart.getData();
                    if (null != data) {
                        data.clearValues();

                        if (null != candleChartData && candleChartData.size() > 0) {
                            candleChartData.clear();
                        }
                        if (null != candleEntries && candleEntries.size() > 0) {
                            candleEntries.clear();
                        }

                        candleStickChart.notifyDataSetChanged();
                        candleStickChart.invalidate();
                    }
                }
                break;
            case BAR_CHART:
                if (null != barChart) {
                    BarData data = barChart.getData();
                    if (null != data) {
                        data.clearValues();
                        barChart.notifyDataSetChanged();
                        barChart.invalidate();
                    }
                }
                break;
            case LINE_CHART:
                if (null != lineChart) {
                    LineData data = lineChart.getData();
                    if (null != data) {
                        data.clearValues();
                        lineChart.notifyDataSetChanged();
                        lineChart.invalidate();
                    }
                }
                break;
        }
    }
}
