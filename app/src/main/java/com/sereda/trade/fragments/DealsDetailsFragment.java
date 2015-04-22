package com.sereda.trade.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sereda.trade.R;
import com.sereda.trade.adapters.DealsAdapter;
import com.sereda.trade.data.Deals;

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

public class DealsDetailsFragment extends Fragment {
    private static final String TRADE_FRAGMENT = "trade_fragment";
    private static final String SYMBOLS = "http://api.ubinary.com/trading/affiliate/112233/user/get/demo/trading/options?data=%7B%22UserId%22:%22%22%7D";
    private static ArrayList<Deals> deals;
    private static ListView listView;
    private static DealsAdapter adapter;
    private static UiRelatedTask<ArrayList<Deals>> dealsNeedle;
    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        deals = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        listView = (ListView) view.findViewById(R.id.lv_details);


        if (null != deals && deals.size() > 0) {
            adapter = new DealsAdapter(getActivity(), R.layout.item_details, deals);
            if (null != listView) {
                listView.setAdapter(adapter);
            }
        } else {
            getDealsData();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Fragment fragment = new TradeFragment();
                Bundle bundle = new Bundle();
                bundle.putString("symbol", deals.get(position).getSymbol());
                bundle.putString("expiration", deals.get(position).getStartAt());
                bundle.putString("payout", String.valueOf(deals.get(position).getPayMatch()));
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment).addToBackStack(TRADE_FRAGMENT).commit();
            }
        });

        return view;
    }

    @SuppressWarnings("deprecation")
    public static void getDealsData() {
        if (null != dealsNeedle) {
            dealsNeedle.cancel();
            dealsNeedle = null;
        }
        if (null != deals && deals.size() > 0) {
            deals.clear();
        }

        dealsNeedle = new UiRelatedTask<ArrayList<Deals>>() {
            @Override
            protected ArrayList<Deals> doWork() {
                Log.e("mylog", "refresh");
                try {
                    DefaultHttpClient hc = new DefaultHttpClient();
                    ResponseHandler<String> res = new BasicResponseHandler();
                    HttpPost postMethod = new HttpPost(SYMBOLS);
                    String response = hc.execute(postMethod, res);

                    try {
                        JSONObject json = new JSONObject(response);
                        JSONArray optionsArray = json.getJSONArray("Options");
                        for (int i = 0; i < optionsArray.length(); i++) {
                            JSONArray dealsArray = optionsArray.getJSONObject(i).getJSONArray("Deals");
                            for (int j = 0; j < dealsArray.length(); j++) {
                                deals.add(new Deals(optionsArray.getJSONObject(i).getString("Symbol"),
                                        dealsArray.getJSONObject(j).getInt("DealId"),
                                        dealsArray.getJSONObject(j).getString("StartAt"),
                                        dealsArray.getJSONObject(j).getString("EndAt"),
                                        dealsArray.getJSONObject(j).getBoolean("ExpirationIsFixed"),
                                        dealsArray.getJSONObject(j).getString("Duration"),
                                        dealsArray.getJSONObject(j).getDouble("PayMatch"),
                                        dealsArray.getJSONObject(j).getDouble("PayNoMatch"),
                                        dealsArray.getJSONObject(j).getDouble("MinStake"),
                                        dealsArray.getJSONObject(j).getDouble("MaxStake")));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return deals;
            }

            @Override
            protected void thenDoUiRelatedWork(ArrayList<Deals> result) {
                createAdapter(result);
            }
        };
        Needle.onBackgroundThread().withThreadPoolSize(1).execute(dealsNeedle);
    }

    private static void createAdapter(ArrayList<Deals> result) {
        if (null != context) {
            adapter = new DealsAdapter(context, R.layout.item_details, result);
            if (null != listView) {
                listView.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity();
    }
}
