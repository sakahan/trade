package com.sereda.trade;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sereda.trade.adapters.DrawerAdapter;
import com.sereda.trade.data.DrawerItem;
import com.sereda.trade.fragments.DealsDetailsFragment;
import com.sereda.trade.fragments.PrefFragment;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private static final String PREFERENCE_FRAGMENT = "preference_fragment";
    private static final String DETAILS_FRAGMENT = "details_fragment";
    private static final int DEFAULT_REFRESH_TIME = 60;
    private static final int MILLI_SECONDS = 1000;
    private static long back_pressed;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private FrameLayout frameLayout;
    private DrawerAdapter adapter;
    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int backStack = 1, openFragment;
    private boolean closeDrawer = false;
    private static SharedPreferences sp;
    public static Handler handler = new Handler();
    public static Runnable runnable = new Runnable() {
        public void run() {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        DealsDetailsFragment.getDealsData();
                    }
                });

                if (null != sp) {
                    String refreshTime = sp.getString("ref_time", "");
                    if (!refreshTime.isEmpty()) {
                        handler.postDelayed(runnable, MILLI_SECONDS * Integer.parseInt(refreshTime));
                    }
                } else {
                    handler.postDelayed(this, MILLI_SECONDS * DEFAULT_REFRESH_TIME);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            if (count == backStack) {
                if (back_pressed + 2000 > System.currentTimeMillis()) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                } else {
                    Toast.makeText(getBaseContext(), "To exit, please touch one more time", Toast.LENGTH_SHORT).show();
                }
                back_pressed = System.currentTimeMillis();
            } else {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.lv_drawer);
        frameLayout = (FrameLayout) findViewById(R.id.container);

        setDrawer();

        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case 0:
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                slideContainer(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (closeDrawer) {
                    displayView(openFragment);
                }
                closeDrawer = false;
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());

        openDetailsFragment();
    }

    private void openDetailsFragment() {
        Fragment fragment = new DealsDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.addToBackStack(DETAILS_FRAGMENT);
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.replace(R.id.container, fragment, DETAILS_FRAGMENT);
        }
        transaction.commit();
    }

    private void slideContainer(float slideOffset) {
        float lastTranslate = 0.0f;
        float moveFactor = (drawerListView.getWidth() * slideOffset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            frameLayout.setTranslationX(moveFactor);
        } else {
            TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
            anim.setDuration(0);
            anim.setFillAfter(true);
            frameLayout.startAnimation(anim);
            lastTranslate = moveFactor;
        }
    }

    private void setDrawer() {
        ArrayList<DrawerItem> drawerData = new ArrayList<>();
        drawerData.add(new DrawerItem("Settings"));
        drawerData.add(new DrawerItem("Refresh"));
        drawerData.add(new DrawerItem("Share"));

        adapter = new DrawerAdapter(this, R.layout.item_drawer, drawerData);
        if (null != drawerListView) {
            drawerListView.setAdapter(adapter);
        }
    }

    private void displayView(int position) {
        Fragment fragment = null;
        FragmentTransaction transaction;

        switch (position) {
            case 0:
                fragment = new PrefFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.addToBackStack(PREFERENCE_FRAGMENT);
                if (fragment.isAdded()) {
                    transaction.show(fragment);
                } else {
                    transaction.replace(R.id.container, fragment, PREFERENCE_FRAGMENT);
                }
                transaction.commit();
                break;
            case 1:
                DealsDetailsFragment.getDealsData();
                break;
            case 2:
                //TODO create share
                showToast("I am using \"Trade\" with common social platforms...");
                break;
            default:
                break;
        }

        if (fragment != null) {
            drawerListView.setItemChecked(position, true);
            drawerListView.setSelection(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != handler && null != runnable) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != handler && null != runnable) {
            if (null != sp) {
                String refreshTime = sp.getString("ref_time", "");
                if (!refreshTime.isEmpty()) {
                    handler.postDelayed(runnable, MILLI_SECONDS * Integer.parseInt(refreshTime));
                }
            } else {
                handler.postDelayed(runnable, MILLI_SECONDS * DEFAULT_REFRESH_TIME);
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            openFragment = position;
            closeDrawer = true;
            drawerLayout.closeDrawer(Gravity.START);
        }
    }
}
