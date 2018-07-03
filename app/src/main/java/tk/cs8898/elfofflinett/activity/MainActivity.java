package tk.cs8898.elfofflinett.activity;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.services.FetchTimeTableService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String MARKED_VIEW = "tk.cs8898.elfofflinett.view.marked";
    private static final String ALL_VIEW = "tk.cs8898.elfofflinett.view.all";

    private WeekView mWeekView;
    private MyWeekViewListener mWeekViewListener;

    private static final String JSON_URL = "https://hive.ddnss.de/elf18tt_min.json";
    private String currentView;
    private SubMenu filterMenu;

    private Set<String> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentView = MARKED_VIEW;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //STUFF DONE BY WeekView
        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.my_weekview);

        mWeekViewListener = new MyWeekViewListener();

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(mWeekViewListener);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(mWeekViewListener);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(mWeekViewListener);

        MarkedActsService.setWeekView(mWeekView);

        //WEEKViewSetup

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWeekView.goToToday();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        filters = new HashSet<>();
        filterMenu = navigationView.getMenu().findItem(R.id.nav_filter_menu).getSubMenu();

        FetchTimeTableService.startActionFetchTimetable(this, JSON_URL);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchTimeTableService.startActionFetchTimetable(this, JSON_URL);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            currentView = MARKED_VIEW;
            mWeekView.notifyDatasetChanged();
        } else if (id == R.id.nav_all) {
            currentView = ALL_VIEW;
            mWeekView.notifyDatasetChanged();
        } else if (id == R.id.nav_licence) {
            Intent settingIntent = new Intent(this, LicenseActivity.class);
            startActivity(settingIntent);
        } else {
            //Toast.makeText(this, "Not Implemented", Toast.LENGTH_LONG).show();
            if (filters.contains(item.getTitle())) {
                filters.remove(item.getTitle());
                item.setIcon(R.drawable.ic_check_box);
            } else {
                filters.add((String) item.getTitle());
                item.setIcon(R.drawable.ic_check_box_outline);
            }
            mWeekView.notifyDatasetChanged();
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MyWeekViewListener implements MonthLoader.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener {

        @Override
        public void onEventClick(WeekViewEvent event, RectF eventRect) {
            InternalActEntity act = MarkedActsService.findAct(event.getId());
            if (act != null && currentView.equals(ALL_VIEW)) {
                act.setMarked(!act.isMarked());
                //event.setColor(act.getColor(getApplicationContext()));
                Log.d("MainActivity","Marked Event size: "+MarkedActsService.getMarkedString().size());
                mWeekView.notifyDatasetChanged();
                MarkedActsService.saveMarks(getApplicationContext());
            }
        }

        @Override
        public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
            InternalActEntity act = MarkedActsService.findAct(event.getId());
            if (act != null) {
                act.setMarked(!act.isMarked());
                //event.setColor(act.getColor(getApplicationContext()));
                mWeekView.notifyDatasetChanged();
                MarkedActsService.saveMarks(getApplicationContext());
            }
        }

        @Override
        public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            //return null;
            //List<WeekViewEvent> events = getEvents(newYear, newMonth);
            poulateFilters();
            Collection<InternalActEntity> acts;
            if (currentView.equals(MARKED_VIEW)) {
                acts = MarkedActsService.getMarked();
            } else if (currentView.equals(ALL_VIEW)) {
                acts = MarkedActsService.getActs();
            } else {
                acts = new ArrayList<>();
            }
            List<WeekViewEvent> eventsList = new ArrayList<WeekViewEvent>();
            for (InternalActEntity act : acts) {
                boolean skip = false;
                Calendar actStart = act.getTime();
                Calendar actEnd = act.getEnd();
                if (actStart != null && actEnd != null) {
                    if (actStart.get(Calendar.YEAR) == newYear && actStart.get(Calendar.MONTH) == newMonth) {
                        //FILTERS
                        if (filters.contains(act.getLocation()))
                            continue;
                        WeekViewEvent event = new WeekViewEvent(act.hashCode(), act.getName(), act.getLocation(), actStart, actEnd);
                        event.setColor(act.getColor(getApplicationContext()));
                        eventsList.add(event);
                    }
                }
            }
            return eventsList;
        }
    }

    private void poulateFilters() {
        filterMenu.clear();
        //filters.clear();
        int i = 0;
        for (String location : MarkedActsService.getLocations()) {
            //filters.add(location);
            MenuItem item = filterMenu.add(R.id.nav_filter_group, Menu.NONE, i++, location);
            //item.setCheckable(false);
            if (filters.contains(location))
                item.setIcon(R.drawable.ic_check_box_outline);
            else
                item.setIcon(R.drawable.ic_check_box);
        }
    }

    @Override
    public void onDestroy(){
        MarkedActsService.saveMarks(getApplicationContext(),true);
        super.onDestroy();
    }
}
