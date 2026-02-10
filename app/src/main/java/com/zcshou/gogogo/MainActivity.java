package com.zcshou.gogogo;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.zcshou.service.ServiceGo;
import com.zcshou.database.DataBaseHistoryLocation;
import com.zcshou.database.DataBaseHistorySearch;
import com.zcshou.utils.ShareUtils;
import com.zcshou.utils.GoUtils;
import com.zcshou.utils.MapUtils;
import com.elvishew.xlog.XLog;
import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
public class MainActivity extends BaseActivity implements SensorEventListener {
    public static final String LAT_MSG_ID = "LAT_VALUE";
    public static final String LNG_MSG_ID = "LNG_VALUE";
    public static final String ALT_MSG_ID = "ALT_VALUE";
    public static final String POI_NAME = "POI_NAME";
    public static final String POI_ADDRESS = "POI_ADDRESS";
    public static final String POI_LONGITUDE = "POI_LONGITUDE";
    public static final String POI_LATITUDE = "POI_LATITUDE";
    private OkHttpClient mOkHttpClient;
    private SharedPreferences sharedPreferences;
    private NavigationView mNavigationView;
    private CheckBox mPtlCheckBox;
    private final JSONObject mReg = new JSONObject();
    public final static BitmapDescriptor mMapIndicator = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
    public static String mCurrentCity = null;
    private MapView mMapView;
    private static BaiduMap mBaiduMap = null;
    private static LatLng mMarkLatLngMap = new LatLng(36.547743718042415, 117.07018449827267); 
    private static String mMarkName = null;
    private GeoCoder mGeoCoder;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetic;
    private float[] mAccValues = new float[3];
    private float[] mMagValues = new float[3];
    private final float[] mR = new float[9];
    private final float[] mDirectionValues = new float[3];
    private LocationClient mLocClient = null;
    private double mCurrentLat = 0.0;       
    private double mCurrentLon = 0.0;       
    private float mCurrentDirection = 0.0f;
    private boolean isFirstLoc = true; 
    private boolean isMockServStart = false;
    private ServiceGo.ServiceGoBinder mServiceBinder;
    private ServiceConnection mConnection;
    private FloatingActionButton mButtonStart;
    private SQLiteDatabase mLocationHistoryDB;
    private SQLiteDatabase mSearchHistoryDB;
    private SearchView searchView;
    private ListView mSearchList;
    private LinearLayout mSearchLayout;
    private ListView mSearchHistoryList;
    private LinearLayout mHistoryLayout;
    private MenuItem searchItem;
    private SuggestionSearch mSuggestionSearch;
    private DownloadManager mDownloadManager = null;
    private long mDownloadId;
    private BroadcastReceiver mDownloadBdRcv;
    private String mUpdateFilename;
    private View mRoutePanel;
    private boolean isRouteMode = false;
    private ArrayList<LatLng> mRoutePoints = new ArrayList<>();
    private ArrayList<MarkerOptions> mRouteMarkers = new ArrayList<>();
    private com.baidu.mapapi.map.Polyline mRoutePolyline;
    private double mRouteSpeed = 60.0; 
    private int mRouteSpeedVariation = 0; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        XLog.i("MainActivity: onCreate");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mOkHttpClient = new OkHttpClient();
        initNavigationView();
        initMap();
        initMapLocation();
        initMapButton();
        initGoBtn();
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceBinder = (ServiceGo.ServiceGoBinder)service;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        initStoreHistory();
        initSearchView();
        initUpdateVersion();
        initRoutePanel();
        checkUpdateVersion(false);
    }
    @Override
    protected void onPause() {
        XLog.i("MainActivity: onPause");
        mMapView.onPause();
        mSensorManager.unregisterListener(this);
        super.onPause();
    }
    @Override
    protected void onResume() {
        XLog.i("MainActivity: onResume");
        mMapView.onResume();
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMagnetic, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }
    @Override
    protected void onStop() {
        XLog.i("MainActivity: onStop");
        mSensorManager.unregisterListener(this);
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        XLog.i("MainActivity: onDestroy");
        if (isMockServStart) {
            unbindService(mConnection); 
            Intent serviceGoIntent = new Intent(MainActivity.this, ServiceGo.class);
            stopService(serviceGoIntent);
        }
        unregisterReceiver(mDownloadBdRcv);
        mSensorManager.unregisterListener(this);
        mLocClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mSuggestionSearch.destroy();
        mLocationHistoryDB.close();
        mSearchHistoryDB.close();
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(new  MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchLayout.setVisibility(View.INVISIBLE);
                mHistoryLayout.setVisibility(View.INVISIBLE);
                return true;  
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchLayout.setVisibility(View.INVISIBLE);
                List<Map<String, Object>> data = getSearchHistory();
                if (!data.isEmpty()) {
                    SimpleAdapter simAdapt = new SimpleAdapter(
                            MainActivity.this,
                            data,
                            R.layout.search_item,
                            new String[] {DataBaseHistorySearch.DB_COLUMN_KEY,
                                    DataBaseHistorySearch.DB_COLUMN_DESCRIPTION,
                                    DataBaseHistorySearch.DB_COLUMN_TIMESTAMP,
                                    DataBaseHistorySearch.DB_COLUMN_IS_LOCATION,
                                    DataBaseHistorySearch.DB_COLUMN_LONGITUDE_CUSTOM,
                                    DataBaseHistorySearch.DB_COLUMN_LATITUDE_CUSTOM},
                            new int[] {R.id.search_key,
                                    R.id.search_description,
                                    R.id.search_timestamp,
                                    R.id.search_isLoc,
                                    R.id.search_longitude,
                                    R.id.search_latitude});
                    mSearchHistoryList.setAdapter(simAdapt);
                    mHistoryLayout.setVisibility(View.VISIBLE);
                }
                return true;  
            }
        });
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconified(false);
        searchView.onActionViewExpanded();
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                            .keyword(query)
                            .city(mCurrentCity)
                    );
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DataBaseHistorySearch.DB_COLUMN_KEY, query);
                    contentValues.put(DataBaseHistorySearch.DB_COLUMN_DESCRIPTION, "搜索关键字");
                    contentValues.put(DataBaseHistorySearch.DB_COLUMN_IS_LOCATION, DataBaseHistorySearch.DB_SEARCH_TYPE_KEY);
                    contentValues.put(DataBaseHistorySearch.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
                    DataBaseHistorySearch.saveHistorySearch(mSearchHistoryDB, contentValues);
                    mBaiduMap.clear();
                    mSearchLayout.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    GoUtils.DisplayToast(MainActivity.this, getResources().getString(R.string.app_error_search));
                    XLog.d(getResources().getString(R.string.app_error_search));
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                mHistoryLayout.setVisibility(View.INVISIBLE);
                if (newText != null && !newText.isEmpty()) {
                    try {
                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                                .keyword(newText)
                                .city(mCurrentCity)
                        );
                    } catch (Exception e) {
                        GoUtils.DisplayToast(MainActivity.this, getResources().getString(R.string.app_error_search));
                        XLog.d(getResources().getString(R.string.app_error_search));
                    }
                }
                return true;
            }
        });
        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setOnClickListener(v -> {
            EditText et = findViewById(androidx.appcompat.R.id.search_src_text);
            et.setText("");
            searchView.setQuery("", false);
            mSearchLayout.setVisibility(View.INVISIBLE);
            mHistoryLayout.setVisibility(View.VISIBLE);
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mAccValues = sensorEvent.values;
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            mMagValues = sensorEvent.values;
        }
        SensorManager.getRotationMatrix(mR, null, mAccValues, mMagValues);
        SensorManager.getOrientation(mR, mDirectionValues);
        mCurrentDirection = (float) Math.toDegrees(mDirectionValues[0]);    
        if (mCurrentDirection < 0) {    
            mCurrentDirection += 360;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    private void initNavigationView() {
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_normal) {
                isRouteMode = false;
                closeRoutePanel();
                GoUtils.DisplayToast(this, "已返回原版功能模式");
            } else if (id == R.id.nav_route) {
                isRouteMode = true;
                showRoutePanel();
                if (!isMockServStart) {
                    if (mMarkLatLngMap == null) {
                        if (mCurrentLat != 0.0 && mCurrentLon != 0.0) {
                            mMarkLatLngMap = new LatLng(mCurrentLat, mCurrentLon);
                        } else {
                            mMarkLatLngMap = new LatLng(36.667662, 117.027707);
                        }
                    }
                    startGoLocation();
                    mButtonStart.setImageResource(R.drawable.ic_fly);
                }
                GoUtils.DisplayToast(this, "已进入路径导航模式");
            } else if (id == R.id.nav_history) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_dev) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    GoUtils.DisplayToast(this, getResources().getString(R.string.app_error_dev));
                }
            }
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
        initUserInfo();
    }
    private void initUserInfo() {
        View navHeaderView = mNavigationView.getHeaderView(0);
        TextView mUserName = navHeaderView.findViewById(R.id.app_name);
        ImageView mUserIcon = navHeaderView.findViewById(R.id.app_icon);
        if (sharedPreferences.getString("setting_reg_code", null) != null) {
            mUserName.setText(getResources().getString(R.string.app_author));
        } else {
            mUserIcon.setOnClickListener(v -> {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                Uri uri = Uri.parse("https://gitee.com/itexp/gogogo");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            });
            mUserName.setOnClickListener(v -> {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                showRegisterDialog();
            });
        }
    }
    public void showRegisterDialog() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            window.setContentView(R.layout.register);
            window.setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.DialogAnimFadeInFadeOut);
            final TextView mRegReq = window.findViewById(R.id.reg_request);
            final TextView regResp = window.findViewById(R.id.reg_response);
            final TextView regUserName = window.findViewById(R.id.reg_user_name);
            regUserName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= 3) {
                        try {
                            mReg.put("UserName", s.toString());
                            mRegReq.setText(mReg.toString());
                        } catch (JSONException e) {
                            XLog.e("ERROR: username");
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            DatePicker mDatePicker = window.findViewById(R.id.date_picker);
            mDatePicker.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
                try {
                    mReg.put("DateTime", 1111);
                    mRegReq.setText(mReg.toString());
                } catch (JSONException e) {
                    XLog.e("ERROR: DateTime");
                }
            });
            mPtlCheckBox = window.findViewById(R.id.reg_check);
            mPtlCheckBox.setOnClickListener(v -> {
                if (mPtlCheckBox.isChecked()) {
                    showProtocolDialog();
                }
            });
            TextView regCancel = window.findViewById(R.id.reg_cancel);
            regCancel.setOnClickListener(v -> alertDialog.cancel());
            TextView regAgree = window.findViewById(R.id.reg_agree);
            regAgree.setOnClickListener(v -> {
                if (!mPtlCheckBox.isChecked()) {
                    GoUtils.DisplayToast(this, getResources().getString(R.string.app_error_protocol));
                    return;
                }
                if (TextUtils.isEmpty(regUserName.getText())) {
                    GoUtils.DisplayToast(this,  getResources().getString(R.string.app_error_username));
                    return;
                }
                if (TextUtils.isEmpty(regResp.getText())) {
                    GoUtils.DisplayToast(this, getResources().getString(R.string.app_error_code));
                    return;
                }
                try {
                    mReg.put("RegReq", mReg.toString());
                    mReg.put("ReqResp", regResp.toString());
                } catch (JSONException e) {
                    XLog.e("ERROR: reg req");
                }
                alertDialog.cancel();
            });
        }
    }
    private void showProtocolDialog() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);      
            window.setContentView(R.layout.user_agreement);
            window.setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.DialogAnimFadeInFadeOut);
            TextView tvContent = window.findViewById(R.id.tv_content);
            Button tvCancel = window.findViewById(R.id.tv_cancel);
            Button tvAgree = window.findViewById(R.id.tv_agree);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(getResources().getString(R.string.app_agreement));
            tvContent.setMovementMethod(LinkMovementMethod.getInstance());
            tvContent.setText(ssb, TextView.BufferType.SPANNABLE);
            tvCancel.setOnClickListener(v -> {
                mPtlCheckBox.setChecked(false);
                alertDialog.cancel();
            });
            tvAgree.setOnClickListener(v -> {
                mPtlCheckBox.setChecked(true);
                alertDialog.cancel();
            });
        }
    }
    private void initMap() {
        mMapView = findViewById(R.id.bdMapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapTouchListener(event -> {
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (isRouteMode) {
                    addRoutePoint(point);
                    return;
                }
                mMarkLatLngMap = point;
                markMap();
            }
            @Override
            public void onMapPoiClick(MapPoi poi) {
                if (isRouteMode) {
                    addRoutePoint(poi.getPosition());
                    return;
                }
                mMarkLatLngMap = poi.getPosition();
                markMap();
            }
        });
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mMarkLatLngMap = point;
                markMap();
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(point));
            }
        });
        mBaiduMap.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng point) {
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
            }
        });
        View poiView = View.inflate(MainActivity.this, R.layout.location_poi_info, null);
        TextView poiAddress = poiView.findViewById(R.id.poi_address);
        TextView poiLongitude = poiView.findViewById(R.id.poi_longitude);
        TextView poiLatitude = poiView.findViewById(R.id.poi_latitude);
        ImageButton ibSave = poiView.findViewById(R.id.poi_save);
        ibSave.setOnClickListener(v -> {
            recordCurrentLocation(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
            GoUtils.DisplayToast(this, getResources().getString(R.string.app_location_save));
        });
        ImageButton ibCopy = poiView.findViewById(R.id.poi_copy);
        ibCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", mMarkLatLngMap.toString());
            cm.setPrimaryClip(mClipData);
            GoUtils.DisplayToast(this,  getResources().getString(R.string.app_location_copy));
        });
        ImageButton ibShare = poiView.findViewById(R.id.poi_share);
        ibShare.setOnClickListener(v -> ShareUtils.shareText(MainActivity.this, "分享位置", poiLongitude.getText()+","+poiLatitude.getText()));
        ImageButton ibFly = poiView.findViewById(R.id.poi_fly);
        ibFly.setOnClickListener(this::doGoLocation);
        ImageButton ibNav = poiView.findViewById(R.id.poi_nav);
        ibNav.setOnClickListener(v -> {
            if (mMarkLatLngMap == null) {
                GoUtils.DisplayToast(MainActivity.this, "请选择目的地");
                return;
            }
            try {
                final String ak = BuildConfig.MAPS_API_KEY;
            String origin = mCurrentLat + "," + mCurrentLon; 
            String destination = mMarkLatLngMap.latitude + "," + mMarkLatLngMap.longitude;
            String url = "https://api.map.baidu.com/direction/v2/driving?origin=" + origin + "&destination=" + destination + "&ak=" + ak + "&coord_type=bd09ll";
            XLog.d("NAV: url=" + url);
            okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().build();
                final Call call = mOkHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        XLog.e("NAV: route request failed");
                        runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, getResources().getString(R.string.app_error_network)));
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        ResponseBody responseBody = response.body();
                        if (responseBody == null) {
                            runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, getResources().getString(R.string.app_error_network)));
                            return;
                        }
                        String resp = responseBody.string();
                        try {
                            JSONObject obj = new JSONObject(resp);
                            if (obj.has("status") && obj.getInt("status") == 0) {
                                List<LatLng> routePoints = new ArrayList<>();
                                JSONObject result = obj.getJSONObject("result");
                                if (result.has("routes")) {
                                    JSONArray routes = result.getJSONArray("routes");
                                    if (routes.length() > 0) {
                                        JSONObject firstRoute = routes.getJSONObject(0);
                                        if (firstRoute.has("steps")) {
                                            JSONArray steps = firstRoute.getJSONArray("steps");
                                            for (int i = 0; i < steps.length(); i++) {
                                                JSONObject step = steps.getJSONObject(i);
                                                if (step.has("path")) {
                                                    String path = step.getString("path");
                                                    String[] pairs = path.split(";");
                                                    for (String pair : pairs) {
                                                        if (pair.trim().isEmpty()) continue;
                                                        String[] ll = pair.split(",");
                                                        double lng = Double.parseDouble(ll[0]);
                                                        double lat = Double.parseDouble(ll[1]);
                                                        routePoints.add(new LatLng(lat, lng));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (routePoints.isEmpty()) {
                                    runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "未获取到路线"));
                                    return;
                                }
                                runOnUiThread(() -> {
                                    try {
                                        com.baidu.mapapi.map.PolylineOptions polyline = new com.baidu.mapapi.map.PolylineOptions().width(8).color(0xAAFF4081).points(routePoints);
                                        mBaiduMap.addOverlay(polyline);
                                    } catch (Exception e) {
                                        XLog.e("NAV: draw polyline error");
                                    }
                                });
                                ArrayList<double[]> routeWgs = new ArrayList<>();
                                for (LatLng p : routePoints) {
                                    double[] wgs = MapUtils.bd2wgs(p.longitude, p.latitude);
                                    routeWgs.add(new double[] {wgs[0], wgs[1]});
                                }
                                runOnUiThread(() -> {
                                    if (!isMockServStart) {
                                        startGoLocation();
                                        mButtonStart.setImageResource(R.drawable.ic_fly);
                                    }
                                    if (mServiceBinder != null) {
                                        mServiceBinder.startFollowRoute(routeWgs);
                                        GoUtils.DisplayToast(MainActivity.this, "已开始沿路线自动行走");
                                    } else {
                                        GoUtils.DisplayToast(MainActivity.this, "服务尚未启动，请先启动模拟位置");
                                    }
                                });
                            } else {
                                runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "路线规划失败"));
                            }
                        } catch (JSONException e) {
                            XLog.e("NAV: parse json error");
                            runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "路线解析失败"));
                        }
                    }
                });
            } catch (Exception ex) {
                XLog.e("NAV: exception");
                GoUtils.DisplayToast(MainActivity.this, "路线请求失败");
            }
        });
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                XLog.i(geoCodeResult.getLocation());
            }
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    XLog.i("逆地理位置失败!");
                } else {
                    mMarkName = String.valueOf(reverseGeoCodeResult.getAddress());
                    poiLatitude.setText(String.valueOf(reverseGeoCodeResult.getLocation().latitude));
                    poiLongitude.setText(String.valueOf(reverseGeoCodeResult.getLocation().longitude));
                    poiAddress.setText(reverseGeoCodeResult.getAddress());
                    final InfoWindow mInfoWindow = new InfoWindow(poiView, reverseGeoCodeResult.getLocation(), -100);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
            }
        });
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mSensorAccelerometer != null) {
                mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
            }
            mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (mSensorMagnetic != null) {
                mSensorManager.registerListener(this, mSensorMagnetic, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }
    private void initMapLocation() {
        try {
            mLocClient = new LocationClient(this);
            mLocClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    if (bdLocation == null || mMapView == null) {
                        return;
                    }
                    mCurrentCity = bdLocation.getCity();
                    mCurrentLat = bdLocation.getLatitude();
                    mCurrentLon = bdLocation.getLongitude();
                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(bdLocation.getRadius())
                            .direction(mCurrentDirection)
                            .latitude(bdLocation.getLatitude())
                            .longitude(bdLocation.getLongitude()).build();
                    mBaiduMap.setMyLocationData(locData);
                    MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
                    mBaiduMap.setMyLocationConfiguration(configuration);
                    int err = bdLocation.getLocType();
                    XLog.i("Location Type: " + err + ", Latitude: " + bdLocation.getLatitude() + ", Longitude: " + bdLocation.getLongitude());
                    boolean isLocationSuccess = (err == BDLocation.TypeGpsLocation || 
                                                err == BDLocation.TypeNetWorkLocation || 
                                                err == BDLocation.TypeOffLineLocation ||
                                                err == BDLocation.TypeOffLineLocationNetworkFail);
                    if (isLocationSuccess) {
                        mCurrentLat = bdLocation.getLatitude();
                        mCurrentLon = bdLocation.getLongitude();
                        if (isFirstLoc) {
                            isFirstLoc = false;
                            mMarkLatLngMap = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                            MapStatus.Builder builder = new MapStatus.Builder();
                            builder.target(mMarkLatLngMap).zoom(18.0f);
                            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                            XLog.i("First Baidu LatLng: " + mMarkLatLngMap);
                        }
                    } else {
                        XLog.e("Location failed with error code: " + err);
                        mLocClient.requestLocation();
                    }
                }
                @Override
                public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
                    XLog.i("Baidu ERROR: " + locType + "-" + diagnosticType + "-" + diagnosticMessage);
                }
            });
            LocationClientOption locationOption = getLocationClientOption();
            mLocClient.setLocOption(locationOption);
            mLocClient.start();
        } catch (Exception e) {
            XLog.e("ERROR: initMapLocation");
        }
    }
    @NonNull
    private static LocationClientOption getLocationClientOption() {
        LocationClientOption locationOption = new LocationClientOption();
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationOption.setCoorType("bd09ll");
        locationOption.setScanSpan(1000);
        locationOption.setIsNeedAddress(true);
        locationOption.setNeedDeviceDirect(false);
        locationOption.setLocationNotify(true);
        locationOption.setIgnoreKillProcess(true);
        locationOption.setIsNeedLocationDescribe(false);
        locationOption.setIsNeedLocationPoiList(false);
        locationOption.SetIgnoreCacheException(true);
        locationOption.setOpenGnss(true);
        locationOption.setIsNeedAltitude(false);
        return locationOption;
    }
    private void initMapButton() {
        RadioGroup mGroupMapType = this.findViewById(R.id.RadioGroupMapType);
        mGroupMapType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.mapNormal) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            }
            if (checkedId == R.id.mapSatellite) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            }
        });
        ImageButton curPosBtn = this.findViewById(R.id.cur_position);
        curPosBtn.setOnClickListener(v -> resetMap());
        ImageButton zoomInBtn = this.findViewById(R.id.zoom_in);
        zoomInBtn.setOnClickListener(v -> mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomIn()));
        ImageButton zoomOutBtn = this.findViewById(R.id.zoom_out);
        zoomOutBtn.setOnClickListener(v -> mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomOut()));
        ImageButton inputPosBtn = this.findViewById(R.id.input_pos);
        inputPosBtn.setOnClickListener(v -> {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("请输入经度和纬度");
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.location_input, null);
            builder.setView(view);
            dialog = builder.show();
            EditText dialog_lng = view.findViewById(R.id.joystick_longitude);
            EditText dialog_lat = view.findViewById(R.id.joystick_latitude);
            RadioButton rbBD = view.findViewById(R.id.pos_type_bd);
            Button btnGo = view.findViewById(R.id.input_position_ok);
            btnGo.setOnClickListener(v2 -> {
                String dialog_lng_str = dialog_lng.getText().toString();
                String dialog_lat_str = dialog_lat.getText().toString();
                if (TextUtils.isEmpty(dialog_lng_str) || TextUtils.isEmpty(dialog_lat_str)) {
                    GoUtils.DisplayToast(MainActivity.this,getResources().getString(R.string.app_error_input));
                } else {
                    double dialog_lng_double = Double.parseDouble(dialog_lng_str);
                    double dialog_lat_double = Double.parseDouble(dialog_lat_str);
                    if (dialog_lng_double > 180.0 || dialog_lng_double < -180.0) {
                        GoUtils.DisplayToast(MainActivity.this,  getResources().getString(R.string.app_error_longitude));
                    } else {
                        if (dialog_lat_double > 90.0 || dialog_lat_double < -90.0) {
                            GoUtils.DisplayToast(MainActivity.this,  getResources().getString(R.string.app_error_latitude));
                        } else {
                            if (rbBD.isChecked()) {
                                mMarkLatLngMap = new LatLng(dialog_lat_double, dialog_lng_double);
                            } else {
                                double[] bdLonLat = MapUtils.wgs2bd09(dialog_lat_double, dialog_lng_double);
                                mMarkLatLngMap = new LatLng(bdLonLat[1], bdLonLat[0]);
                            }
                            mMarkName = "手动输入的坐标";
                            markMap();
                            MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(mMarkLatLngMap);
                            mBaiduMap.setMapStatus(mapstatusupdate);
                            dialog.dismiss();
                        }
                    }
                }
            });
            Button btnCancel = view.findViewById(R.id.input_position_cancel);
            btnCancel.setOnClickListener(v1 -> dialog.dismiss());
        });
    }
    private void markMap() {
        if (mMarkLatLngMap != null) {
            MarkerOptions ooA = new MarkerOptions().position(mMarkLatLngMap).icon(mMapIndicator);
            mBaiduMap.clear();
            mBaiduMap.addOverlay(ooA);
        }
    }
    private void resetMap() {
        mBaiduMap.clear();
        mMarkLatLngMap = null;
        MyLocationData locData = new MyLocationData.Builder()
                .latitude(mCurrentLat)
                .longitude(mCurrentLon)
                .direction(mCurrentDirection)
                .build();
        mBaiduMap.setMyLocationData(locData);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(new LatLng(mCurrentLat, mCurrentLon)).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    public static boolean showLocation(String name, String bd09Longitude, String bd09Latitude) {
        boolean ret = true;
        try {
            if (!bd09Longitude.isEmpty() && !bd09Latitude.isEmpty()) {
                mMarkName = name;
                mMarkLatLngMap = new LatLng(Double.parseDouble(bd09Latitude), Double.parseDouble(bd09Longitude));
                MarkerOptions ooA = new MarkerOptions().position(mMarkLatLngMap).icon(mMapIndicator);
                mBaiduMap.clear();
                mBaiduMap.addOverlay(ooA);
                MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(mMarkLatLngMap);
                mBaiduMap.setMapStatus(mapstatusupdate);
            }
        } catch (Exception e) {
            ret = false;
            XLog.e("ERROR: showHistoryLocation");
        }
        return ret;
    }
    private void initGoBtn() {
        mButtonStart = findViewById(R.id.faBtnStart);
        mButtonStart.setOnClickListener(this::doGoLocation);
    }
    private void startGoLocation() {
        Intent serviceGoIntent = new Intent(MainActivity.this, ServiceGo.class);
        bindService(serviceGoIntent, mConnection, BIND_AUTO_CREATE);    
        double[] latLng = MapUtils.bd2wgs(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
        serviceGoIntent.putExtra(LNG_MSG_ID, latLng[0]);
        serviceGoIntent.putExtra(LAT_MSG_ID, latLng[1]);
        double alt = Double.parseDouble(sharedPreferences.getString("setting_altitude", "55.0"));
        serviceGoIntent.putExtra(ALT_MSG_ID, alt);
        startForegroundService(serviceGoIntent);
        XLog.d("startForegroundService: ServiceGo");
        isMockServStart = true;
    }
    private void stopGoLocation() {
        unbindService(mConnection); 
        Intent serviceGoIntent = new Intent(MainActivity.this, ServiceGo.class);
        stopService(serviceGoIntent);
        isMockServStart = false;
    }
    private void doGoLocation(View v) {
        if (!GoUtils.isNetworkAvailable(this)) {
            GoUtils.DisplayToast(this, getResources().getString(R.string.app_error_network));
            return;
        }
        if (!GoUtils.isGpsOpened(this)) {
            GoUtils.showEnableGpsDialog(this);
            return;
        }
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            GoUtils.showEnableFloatWindowDialog(this);
            XLog.e("无悬浮窗权限!");
            return;
        }
        if (isMockServStart) {
            if (mMarkLatLngMap == null) {
                stopGoLocation();
                Snackbar.make(v, "模拟位置已终止", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mButtonStart.setImageResource(R.drawable.ic_position);
            } else {
                double[] latLng = MapUtils.bd2wgs(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
                double alt = Double.parseDouble(sharedPreferences.getString("setting_altitude", "55.0"));
                mServiceBinder.setPosition(latLng[0], latLng[1], alt);
                Snackbar.make(v, "已传送到新位置", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                recordCurrentLocation(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
                mBaiduMap.clear();
                mMarkLatLngMap = null;
                if (GoUtils.isWifiEnabled(MainActivity.this)) {
                    GoUtils.showDisableWifiDialog(MainActivity.this);
                }
            }
        } else {
            if (!GoUtils.isAllowMockLocation(this)) {
                GoUtils.showEnableMockLocationDialog(this);
                XLog.e("无模拟位置权限!");
            } else {
                if (mMarkLatLngMap == null) {
                    Snackbar.make(v, "请先点击地图位置或者搜索位置", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    startGoLocation();
                    mButtonStart.setImageResource(R.drawable.ic_fly);
                    Snackbar.make(v, "模拟位置已启动", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    recordCurrentLocation(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
                    mBaiduMap.clear();
                    mMarkLatLngMap = null;
                    if (GoUtils.isWifiEnabled(MainActivity.this)) {
                        GoUtils.showDisableWifiDialog(MainActivity.this);
                    }
                }
            }
        }
    }
    private void initStoreHistory() {
        try {
            DataBaseHistoryLocation dbLocation = new DataBaseHistoryLocation(getApplicationContext());
            mLocationHistoryDB = dbLocation.getWritableDatabase();
            DataBaseHistorySearch dbHistory = new DataBaseHistorySearch(getApplicationContext());
            mSearchHistoryDB = dbHistory.getWritableDatabase();
        } catch (Exception e) {
            XLog.e("ERROR: sqlite init error");
        }
    }
    private List<Map<String, Object>> getSearchHistory() {
        List<Map<String, Object>> data = new ArrayList<>();
        try {
            Cursor cursor = mSearchHistoryDB.query(DataBaseHistorySearch.TABLE_NAME, null,
                    DataBaseHistorySearch.DB_COLUMN_ID + " > ?", new String[] {"0"},
                    null, null, DataBaseHistorySearch.DB_COLUMN_TIMESTAMP + " DESC", null);
            while (cursor.moveToNext()) {
                Map<String, Object> searchHistoryItem = new HashMap<>();
                searchHistoryItem.put(DataBaseHistorySearch.DB_COLUMN_KEY, cursor.getString(1));
                searchHistoryItem.put(DataBaseHistorySearch.DB_COLUMN_DESCRIPTION, cursor.getString(2));
                searchHistoryItem.put(DataBaseHistorySearch.DB_COLUMN_TIMESTAMP, "" + cursor.getInt(3));
                searchHistoryItem.put(DataBaseHistorySearch.DB_COLUMN_IS_LOCATION, "" + cursor.getInt(4));
                searchHistoryItem.put(DataBaseHistorySearch.DB_COLUMN_LONGITUDE_CUSTOM, cursor.getString(7));
                searchHistoryItem.put(DataBaseHistorySearch.DB_COLUMN_LATITUDE_CUSTOM, cursor.getString(8));
                data.add(searchHistoryItem);
            }
            cursor.close();
        } catch (Exception e) {
            XLog.e("ERROR: getSearchHistory");
        }
        return data;
    }
    private void recordCurrentLocation(double lng, double lat) {
        final String safeCode = BuildConfig.MAPS_SAFE_CODE;
        final String ak = BuildConfig.MAPS_API_KEY;
        double[] latLng = MapUtils.bd2wgs(lng, lat);
        String mapApiUrl = "https://api.map.baidu.com/reverse_geocoding/v3/?ak=" + ak + "&output=json&coordtype=bd09ll" + "&location=" + lat + "," + lng + "&mcode=" + safeCode;
        okhttp3.Request request = new okhttp3.Request.Builder().url(mapApiUrl).get().build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                XLog.e("HTTP: HTTP GET FAILED");
                ContentValues contentValues = new ContentValues();
                contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LOCATION, mMarkName);
                contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_WGS84, String.valueOf(latLng[0]));
                contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_WGS84, String.valueOf(latLng[1]));
                contentValues.put(DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
                contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_CUSTOM, Double.toString(lng));
                contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_CUSTOM, Double.toString(lat));
                DataBaseHistoryLocation.saveHistoryLocation(mLocationHistoryDB, contentValues);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String resp = responseBody.string();
                    try {
                        JSONObject getRetJson = new JSONObject(resp);
                        if (Integer.parseInt(getRetJson.getString("status")) == 0) { 
                            JSONObject posInfoJson = getRetJson.getJSONObject("result");
                            String formatted_address = posInfoJson.getString("formatted_address");
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LOCATION, formatted_address);
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_WGS84, String.valueOf(latLng[0]));
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_WGS84, String.valueOf(latLng[1]));
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_CUSTOM, Double.toString(lng));
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_CUSTOM, Double.toString(lat));
                            DataBaseHistoryLocation.saveHistoryLocation(mLocationHistoryDB, contentValues);
                        } else {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LOCATION, mMarkName == null ? getRetJson.getString("message"): mMarkName);
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_WGS84, String.valueOf(latLng[0]));
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_WGS84, String.valueOf(latLng[1]));
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_CUSTOM, Double.toString(lng));
                            contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_CUSTOM, Double.toString(lat));
                            DataBaseHistoryLocation.saveHistoryLocation(mLocationHistoryDB, contentValues);
                        }
                    } catch (JSONException e) {
                        XLog.e("JSON: resolve json error");
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LOCATION, mMarkName == null ? getResources().getString(R.string.history_location_default_name) : mMarkName);
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LOCATION, mMarkName);
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_WGS84, String.valueOf(latLng[0]));
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_WGS84, String.valueOf(latLng[1]));
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_CUSTOM, Double.toString(lng));
                        contentValues.put(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_CUSTOM, Double.toString(lat));
                        DataBaseHistoryLocation.saveHistoryLocation(mLocationHistoryDB, contentValues);
                    }
                }
            }
        });
    }
    private void initSearchView() {
        mSearchLayout = findViewById(R.id.search_linear);
        mHistoryLayout = findViewById(R.id.search_history_linear);
        mSearchList = findViewById(R.id.search_list_view);
        mSearchList.setOnItemClickListener((parent, view, position, id) -> {
            String lng = ((TextView) view.findViewById(R.id.poi_longitude)).getText().toString();
            String lat = ((TextView) view.findViewById(R.id.poi_latitude)).getText().toString();
            mMarkName = ((TextView) view.findViewById(R.id.poi_name)).getText().toString();
            mMarkLatLngMap = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(mMarkLatLngMap);
            mBaiduMap.setMapStatus(mapstatusupdate);
            markMap();
            double[] latLng = MapUtils.bd2wgs(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_KEY, mMarkName);
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_DESCRIPTION, ((TextView) view.findViewById(R.id.poi_address)).getText().toString());
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_IS_LOCATION, DataBaseHistorySearch.DB_SEARCH_TYPE_RESULT);
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_LONGITUDE_CUSTOM, lng);
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_LATITUDE_CUSTOM, lat);
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_LONGITUDE_WGS84, String.valueOf(latLng[0]));
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_LATITUDE_WGS84, String.valueOf(latLng[1]));
            contentValues.put(DataBaseHistorySearch.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
            DataBaseHistorySearch.saveHistorySearch(mSearchHistoryDB, contentValues);
            mSearchLayout.setVisibility(View.INVISIBLE);
            searchItem.collapseActionView();
        });
        mSearchHistoryList = findViewById(R.id.search_history_list_view);
        mSearchHistoryList.setOnItemClickListener((parent, view, position, id) -> {
            String searchDescription = ((TextView) view.findViewById(R.id.search_description)).getText().toString();
            String searchKey = ((TextView) view.findViewById(R.id.search_key)).getText().toString();
            String searchIsLoc = ((TextView) view.findViewById(R.id.search_isLoc)).getText().toString();
            if (searchIsLoc.equals("1")) {
                String lng = ((TextView) view.findViewById(R.id.search_longitude)).getText().toString();
                String lat = ((TextView) view.findViewById(R.id.search_latitude)).getText().toString();
                mMarkLatLngMap = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(mMarkLatLngMap);
                mBaiduMap.setMapStatus(mapstatusupdate);
                markMap();
                double[] latLng = MapUtils.bd2wgs(mMarkLatLngMap.longitude, mMarkLatLngMap.latitude);
                mHistoryLayout.setVisibility(View.INVISIBLE);
                searchItem.collapseActionView();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_KEY, searchKey);
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_DESCRIPTION, searchDescription);
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_IS_LOCATION, DataBaseHistorySearch.DB_SEARCH_TYPE_RESULT);
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_LONGITUDE_CUSTOM, lng);
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_LATITUDE_CUSTOM, lat);
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_LONGITUDE_WGS84, String.valueOf(latLng[0]));
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_LATITUDE_WGS84, String.valueOf(latLng[1]));
                contentValues.put(DataBaseHistorySearch.DB_COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
                DataBaseHistorySearch.saveHistorySearch(mSearchHistoryDB, contentValues);
            } else if (searchIsLoc.equals("0")) { 
                try {
                    searchView.setQuery(searchKey, true);
                } catch (Exception e) {
                    GoUtils.DisplayToast(this, getResources().getString(R.string.app_error_search));
                    XLog.e(getResources().getString(R.string.app_error_search));
                }
            } else {
                XLog.e(getResources().getString(R.string.app_error_param));
            }
        });
        mSearchHistoryList.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("确定要删除该项搜索记录吗?")
                    .setPositiveButton("确定",(dialog, which) -> {
                        String searchKey = ((TextView) view.findViewById(R.id.search_key)).getText().toString();
                        try {
                            mSearchHistoryDB.delete(DataBaseHistorySearch.TABLE_NAME, DataBaseHistorySearch.DB_COLUMN_KEY + " = ?", new String[] {searchKey});
                            List<Map<String, Object>> data = getSearchHistory();
                            if (!data.isEmpty()) {
                                SimpleAdapter simAdapt = new SimpleAdapter(
                                        MainActivity.this,
                                        data,
                                        R.layout.search_item,
                                        new String[] {DataBaseHistorySearch.DB_COLUMN_KEY,
                                                DataBaseHistorySearch.DB_COLUMN_DESCRIPTION,
                                                DataBaseHistorySearch.DB_COLUMN_TIMESTAMP,
                                                DataBaseHistorySearch.DB_COLUMN_IS_LOCATION,
                                                DataBaseHistorySearch.DB_COLUMN_LONGITUDE_CUSTOM,
                                                DataBaseHistorySearch.DB_COLUMN_LATITUDE_CUSTOM}, 
                                        new int[] {R.id.search_key, R.id.search_description, R.id.search_timestamp, R.id.search_isLoc, R.id.search_longitude, R.id.search_latitude});
                                mSearchHistoryList.setAdapter(simAdapt);
                                mHistoryLayout.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            XLog.e("ERROR: delete database error");
                            GoUtils.DisplayToast(MainActivity.this,getResources().getString(R.string.history_delete_error));
                        }
                    })
                    .setNegativeButton("取消",
                            (dialog, which) -> {
                            })
                    .show();
            return true;
        });
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResult -> {
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                GoUtils.DisplayToast(this,getResources().getString(R.string.app_search_null));
            } else {
                List<Map<String, Object>> data = getMapList(suggestionResult);
                SimpleAdapter simAdapt = new SimpleAdapter(
                        MainActivity.this,
                        data,
                        R.layout.search_poi_item,
                        new String[] {POI_NAME, POI_ADDRESS, POI_LONGITUDE, POI_LATITUDE}, 
                        new int[] {R.id.poi_name, R.id.poi_address, R.id.poi_longitude, R.id.poi_latitude});
                mSearchList.setAdapter(simAdapt);
                mSearchLayout.setVisibility(View.VISIBLE);
            }
        });
    }
    @NonNull
    private static List<Map<String, Object>> getMapList(SuggestionResult suggestionResult) {
        List<Map<String, Object>> data = new ArrayList<>();
        int retCnt = suggestionResult.getAllSuggestions().size();
        for (int i = 0; i < retCnt; i++) {
            if (suggestionResult.getAllSuggestions().get(i).pt == null) {
                continue;
            }
            Map<String, Object> poiItem = new HashMap<>();
            poiItem.put(POI_NAME, suggestionResult.getAllSuggestions().get(i).key);
            poiItem.put(POI_ADDRESS, suggestionResult.getAllSuggestions().get(i).city + " " + suggestionResult.getAllSuggestions().get(i).district);
            poiItem.put(POI_LONGITUDE, "" + suggestionResult.getAllSuggestions().get(i).pt.longitude);
            poiItem.put(POI_LATITUDE, "" + suggestionResult.getAllSuggestions().get(i).pt.latitude);
            data.add(poiItem);
        }
        return data;
    }
    private void initUpdateVersion() {
        mDownloadManager =(DownloadManager) MainActivity.this.getSystemService(DOWNLOAD_SERVICE);
        mDownloadBdRcv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                installNewVersion();
            }
        };
        registerReceiver(mDownloadBdRcv, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private void checkUpdateVersion(boolean result) {
        String mapApiUrl = "https://api.github.com/repos/zcshou/gogogo/releases/latest";
        okhttp3.Request request = new okhttp3.Request.Builder().url(mapApiUrl).get().build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                XLog.i("更新检测失败");
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String resp = responseBody.string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject getRetJson = new JSONObject(resp);
                            String curVersion = GoUtils.getVersionName(MainActivity.this);
                            if (curVersion != null
                                    && (!getRetJson.getString("name").contains(curVersion)
                                    || !getRetJson.getString("tag_name").contains(curVersion))) {
                                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.show();
                                alertDialog.setCancelable(false);
                                Window window = alertDialog.getWindow();
                                if (window != null) {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);      
                                    window.setContentView(R.layout.update);
                                    window.setGravity(Gravity.CENTER);
                                    window.setWindowAnimations(R.style.DialogAnimFadeInFadeOut);
                                    TextView updateTitle = window.findViewById(R.id.update_title);
                                    updateTitle.setText(getRetJson.getString("name"));
                                    TextView updateTime = window.findViewById(R.id.update_time);
                                    updateTime.setText(getRetJson.getString("created_at"));
                                    TextView updateCommit = window.findViewById(R.id.update_commit);
                                    updateCommit.setText(getRetJson.getString("target_commitish"));
                                    TextView updateContent = window.findViewById(R.id.update_content);
                                    final Markwon markwon = Markwon.create(MainActivity.this);
                                    markwon.setMarkdown(updateContent, getRetJson.getString("body"));
                                    Button updateCancel = window.findViewById(R.id.update_ignore);
                                    updateCancel.setOnClickListener(v -> alertDialog.cancel());
                                    JSONArray jsonArray = new JSONArray(getRetJson.getString("assets"));
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    String download_url = jsonObject.getString("browser_download_url");
                                    mUpdateFilename = jsonObject.getString("name");
                                    Button updateAgree = window.findViewById(R.id.update_agree);
                                    updateAgree.setOnClickListener(v -> {
                                        alertDialog.cancel();
                                        GoUtils.DisplayToast(MainActivity.this, getResources().getString(R.string.update_downloading));
                                        downloadNewVersion(download_url);
                                    });
                                }
                            } else {
                                if (result) {
                                    GoUtils.DisplayToast(MainActivity.this, getResources().getString(R.string.update_last));
                                }
                            }
                        } catch (JSONException e) {
                            XLog.e("ERROR: resolve json");
                        }
                    });
                }
            }
        });
    }
    private void downloadNewVersion(String url) {
        if (mDownloadManager == null) {
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(GoUtils.getAppName(this));
        request.setDescription("正在下载新版本...");
        request.setMimeType("application/vnd.android.package-archive");
        File file = new File(getExternalFilesDir("Updates"), mUpdateFilename);
        if (file.exists()) {
            if(!file.delete()) {
                return;
            }
        }
        request.setDestinationUri(Uri.fromFile(file));
        mDownloadId = mDownloadManager.enqueue(request);
    }
    private void installNewVersion() {
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri downloadFileUri = mDownloadManager.getUriForDownloadedFile(mDownloadId);
        File file = new File(getExternalFilesDir("Updates"), mUpdateFilename);
        if (downloadFileUri != null) {
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);    
            install.addCategory("android.intent.category.DEFAULT");
            install.setDataAndType(ShareUtils.getUriFromFile(MainActivity.this, file), "application/vnd.android.package-archive");
            startActivity(install);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
            intent.addCategory("android.intent.category.DEFAULT");
            startActivity(intent);
        }
    }
    private EditText mRouteStartPoint;
    private EditText mRouteEndPoint;
    private Button mBtnPlanRoute;
    private boolean mIsUpdatingFromSuggestion = false;
    private void initRoutePanel() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mRoutePanel = inflater.inflate(R.layout.route_panel, null);
        Button btnClear = mRoutePanel.findViewById(R.id.btn_route_clear);
        Button btnDeleteLast = mRoutePanel.findViewById(R.id.btn_route_delete_last);
        Button btnClose = mRoutePanel.findViewById(R.id.btn_route_close);
        Button btnStart = mRoutePanel.findViewById(R.id.btn_route_start);
        Button btnStop = mRoutePanel.findViewById(R.id.btn_route_stop);
        SeekBar speedSeekBar = mRoutePanel.findViewById(R.id.route_speed_seekbar);
        TextView speedValue = mRoutePanel.findViewById(R.id.route_speed_value);
        SeekBar speedVariationSeekBar = mRoutePanel.findViewById(R.id.route_speed_variation_seekbar);
        TextView speedVariationValue = mRoutePanel.findViewById(R.id.route_speed_variation_value);
        mRouteStartPoint = mRoutePanel.findViewById(R.id.route_start_point);
        mRouteEndPoint = mRoutePanel.findViewById(R.id.route_end_point);
        mBtnPlanRoute = mRoutePanel.findViewById(R.id.btn_plan_route);
        ImageButton btnStartSearch = mRoutePanel.findViewById(R.id.btn_start_search);
        ImageButton btnEndSearch = mRoutePanel.findViewById(R.id.btn_end_search);
        Button btnToggleRoutePoints = mRoutePanel.findViewById(R.id.btn_toggle_route_points);
        ScrollView routePointsScrollView = mRoutePanel.findViewById(R.id.route_points_scrollview);
        ImageButton btnTogglePanel = mRoutePanel.findViewById(R.id.btn_toggle_panel);
        LinearLayout routePanelContent = mRoutePanel.findViewById(R.id.route_panel_content);
        LinearLayout routePanelHeader = mRoutePanel.findViewById(R.id.route_panel_header);
        ListView startSuggestionList = mRoutePanel.findViewById(R.id.start_suggestion_list);
        ListView endSuggestionList = mRoutePanel.findViewById(R.id.end_suggestion_list);
        addInputSuggestion(mRouteStartPoint, startSuggestionList, true);
        addInputSuggestion(mRouteEndPoint, endSuggestionList, false);
        btnClear.setOnClickListener(v -> clearRoute());
        btnDeleteLast.setOnClickListener(v -> deleteLastRoutePoint());
        btnClose.setOnClickListener(v -> closeRoutePanel());
        btnStart.setOnClickListener(v -> startRouteNavigation());
        btnStop.setOnClickListener(v -> stopRouteNavigation());
        btnToggleRoutePoints.setOnClickListener(v -> {
            if (routePointsScrollView.getVisibility() == View.GONE) {
                routePointsScrollView.setVisibility(View.VISIBLE);
                btnToggleRoutePoints.setText("收起路径点");
            } else {
                routePointsScrollView.setVisibility(View.GONE);
                btnToggleRoutePoints.setText("显示路径点");
            }
        });
        routePanelHeader.setOnClickListener(v -> {
            if (routePanelContent.getVisibility() == View.VISIBLE) {
                routePanelContent.setVisibility(View.GONE);
                btnTogglePanel.setImageResource(R.drawable.ic_up);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mRoutePanel.getLayoutParams();
                params.height = (int) (60 * getResources().getDisplayMetrics().density); 
                mRoutePanel.setLayoutParams(params);
            } else {
                routePanelContent.setVisibility(View.VISIBLE);
                btnTogglePanel.setImageResource(R.drawable.ic_down);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mRoutePanel.getLayoutParams();
                params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                mRoutePanel.setLayoutParams(params);
            }
        });
        btnTogglePanel.setOnClickListener(v -> routePanelHeader.performClick());
        mBtnPlanRoute.setOnClickListener(v -> planRoute());
        btnStartSearch.setOnClickListener(v -> searchLocation(mRouteStartPoint.getText().toString(), true));
        btnEndSearch.setOnClickListener(v -> searchLocation(mRouteEndPoint.getText().toString(), false));
        speedSeekBar.setProgress(60); 
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double kmhSpeed = progress / 1.0; 
                mRouteSpeed = kmhSpeed; 
                speedValue.setText(String.format("%.1f km/h", kmhSpeed));
                if (mServiceBinder != null) {
                    mServiceBinder.setRouteSpeed(mRouteSpeed);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        speedVariationSeekBar.setProgress(0);
        speedVariationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRouteSpeedVariation = progress;
                speedVariationValue.setText(String.format("±%d%%", mRouteSpeedVariation));
                if (mServiceBinder != null) {
                    mServiceBinder.setRouteSpeedVariation(mRouteSpeedVariation);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mRouteStartPoint.setText("我的位置");
    }
    private void addInputSuggestion(final EditText editText, final ListView suggestionList, final boolean isStartPoint) {
        editText.addTextChangedListener(new TextWatcher() {
            private Timer timer;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mIsUpdatingFromSuggestion) {
                    mIsUpdatingFromSuggestion = false;
                    return;
                }
                if (timer != null) {
                    timer.cancel();
                }
                final String keyword = s.toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
                    return;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getInputSuggestions(keyword, suggestionList, isStartPoint);
                    }
                }, 300);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> suggestionList.setVisibility(View.GONE), 200);
            }
        });
    }
    private void getInputSuggestions(String keyword, final ListView suggestionList, final boolean isStartPoint) {
        if (mCurrentLat == 0.0 || mCurrentLon == 0.0) {
            runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
            return;
        }
        final String ak = BuildConfig.MAPS_API_KEY;
        if (TextUtils.isEmpty(ak)) {
            runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
            return;
        }
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = "https://api.map.baidu.com/place/v2/suggestion?query=" + encodedKeyword + "&location=" + mCurrentLat + "," + mCurrentLon + "&radius=2000&output=json&ak=" + ak;
            XLog.d("SUGGESTION: url=" + url);
            okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    XLog.e("SUGGESTION: failed, " + e.getMessage());
                    runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
                        return;
                    }
                    String resp = responseBody.string();
                    try {
                        JSONObject obj = new JSONObject(resp);
                        if (obj.getInt("status") == 0) {
                            JSONArray results = obj.getJSONArray("result");
                            final List<String> suggestions = new ArrayList<>();
                            final Map<String, LatLng> locationMap = new HashMap<>();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                String name = item.getString("name");
                                String address = item.optString("address", "");
                                String suggestion = name;
                                if (!TextUtils.isEmpty(address)) {
                                    suggestion += " (" + address + ")";
                                }
                                suggestions.add(suggestion);
                                if (item.has("location")) {
                                    JSONObject location = item.getJSONObject("location");
                                    double lat = location.getDouble("lat");
                                    double lng = location.getDouble("lng");
                                    locationMap.put(name, new LatLng(lat, lng));
                                }
                            }
                            runOnUiThread(() -> {
                                if (suggestions.size() > 0) {
                                    List<Map<String, Object>> suggestionItems = new ArrayList<>();
                                    for (int i = 0; i < suggestions.size(); i++) {
                                        String suggestion = suggestions.get(i);
                                        String[] parts = suggestion.split(" ");
                                        String name = parts[0];
                                        String address = parts.length > 1 ? parts[1] : "";
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("name", name);
                                        map.put("address", address);
                                        suggestionItems.add(map);
                                    }
                                    SimpleAdapter adapter = new SimpleAdapter(
                                        MainActivity.this,
                                        suggestionItems,
                                        android.R.layout.simple_list_item_2,
                                        new String[]{"name", "address"},
                                        new int[]{android.R.id.text1, android.R.id.text2}
                                    );
                                    suggestionList.setAdapter(adapter);
                                    suggestionList.setVisibility(View.VISIBLE);
                                    suggestionList.setOnItemClickListener((parent, view, position, id) -> {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(position);
                                        String name = (String) item.get("name");
                                        mIsUpdatingFromSuggestion = true;
                                        if (isStartPoint) {
                                            mRouteStartPoint.setText(name);
                                        } else {
                                            mRouteEndPoint.setText(name);
                                            if (locationMap.containsKey(name)) {
                                                mMarkLatLngMap = locationMap.get(name);
                                                markMap();
                                            }
                                        }
                                        suggestionList.setVisibility(View.GONE);
                                    });
                                } else {
                                    suggestionList.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
                        }
                    } catch (JSONException e) {
                        XLog.e("SUGGESTION: parse json error, " + e.getMessage());
                        runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            XLog.e("SUGGESTION: encode keyword error, " + e.getMessage());
            runOnUiThread(() -> suggestionList.setVisibility(View.GONE));
        }
    }
    private void showSuggestionDialog(List<String> suggestions, final Map<String, LatLng> locationMap, final boolean isStartPoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择位置");
        final String[] items = suggestions.toArray(new String[0]);
        builder.setItems(items, (dialog, which) -> {
            String selectedItem = items[which];
            String name = selectedItem.split(" ")[0];
            if (isStartPoint) {
                mRouteStartPoint.setText(name);
            } else {
                mRouteEndPoint.setText(name);
                if (locationMap.containsKey(name)) {
                    mMarkLatLngMap = locationMap.get(name);
                    markMap();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    private void searchLocation(String keyword, boolean isStartPoint) {
        if (TextUtils.isEmpty(keyword)) {
            GoUtils.DisplayToast(this, "请输入搜索关键词");
            return;
        }
        if (mCurrentLat == 0.0 || mCurrentLon == 0.0) {
            GoUtils.DisplayToast(this, "正在获取位置，请稍后重试");
            return;
        }
        final String ak = BuildConfig.MAPS_API_KEY;
        if (TextUtils.isEmpty(ak)) {
            GoUtils.DisplayToast(this, "API密钥未配置");
            return;
        }
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = "https://api.map.baidu.com/place/v2/search?query=" + encodedKeyword + "&location=" + mCurrentLat + "," + mCurrentLon + "&radius=2000&output=json&ak=" + ak;
            XLog.d("SEARCH: url=" + url);
            okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    XLog.e("SEARCH: search failed, " + e.getMessage());
                    runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "网络错误，搜索失败"));
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        XLog.e("SEARCH: response body is null");
                        runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "搜索失败"));
                        return;
                    }
                    String resp = responseBody.string();
                    XLog.d("SEARCH: response=" + resp);
                    try {
                        JSONObject obj = new JSONObject(resp);
                        int status = obj.getInt("status");
                        String message = obj.optString("message", "未知错误");
                        if (status == 0) {
                            JSONArray results = obj.getJSONArray("results");
                            if (results.length() > 0) {
                                JSONObject firstResult = results.getJSONObject(0);
                                double lat = firstResult.getJSONObject("location").getDouble("lat");
                                double lng = firstResult.getJSONObject("location").getDouble("lng");
                                LatLng point = new LatLng(lat, lng);
                                String name = firstResult.getString("name");
                                runOnUiThread(() -> {
                                    if (isStartPoint) {
                                        mRouteStartPoint.setText(name);
                                    } else {
                                        mRouteEndPoint.setText(name);
                                        mMarkLatLngMap = point;
                                        markMap();
                                    }
                                    GoUtils.DisplayToast(MainActivity.this, "搜索成功");
                                });
                            } else {
                                runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "未找到相关位置"));
                            }
                        } else {
                            XLog.e("SEARCH: api error, status=" + status + ", message=" + message);
                            runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "搜索失败：" + message));
                        }
                    } catch (JSONException e) {
                        XLog.e("SEARCH: parse json error, " + e.getMessage());
                        runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "搜索失败"));
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            XLog.e("SEARCH: encode keyword error, " + e.getMessage());
            GoUtils.DisplayToast(this, "搜索失败");
        }
    }
    private void planRoute() {
        String startText = mRouteStartPoint.getText().toString();
        String endText = mRouteEndPoint.getText().toString();
        if (TextUtils.isEmpty(endText)) {
            GoUtils.DisplayToast(this, "请输入目的地");
            return;
        }
        if (mMarkLatLngMap == null) {
            GoUtils.DisplayToast(this, "请先选择目的地");
            return;
        }
        try {
            final String ak = BuildConfig.MAPS_API_KEY;
            String origin = mCurrentLat + "," + mCurrentLon; 
            String destination = mMarkLatLngMap.latitude + "," + mMarkLatLngMap.longitude;
            String url = "https://api.map.baidu.com/direction/v2/driving?origin=" + origin + "&destination=" + destination + "&ak=" + ak + "&coord_type=bd09ll";
            XLog.d("NAV: url=" + url);
            okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    XLog.e("NAV: route request failed");
                    runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "路线规划失败"));
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "路线规划失败"));
                        return;
                    }
                    String resp = responseBody.string();
                    try {
                        JSONObject obj = new JSONObject(resp);
                        if (obj.has("status") && obj.getInt("status") == 0) {
                            List<LatLng> routePoints = new ArrayList<>();
                            JSONObject result = obj.getJSONObject("result");
                            if (result.has("routes")) {
                                JSONArray routes = result.getJSONArray("routes");
                                if (routes.length() > 0) {
                                    JSONObject firstRoute = routes.getJSONObject(0);
                                    if (firstRoute.has("steps")) {
                                        JSONArray steps = firstRoute.getJSONArray("steps");
                                        for (int i = 0; i < steps.length(); i++) {
                                            JSONObject step = steps.getJSONObject(i);
                                            if (step.has("path")) {
                                                String path = step.getString("path");
                                                String[] pairs = path.split(";\\s*");
                                                for (String pair : pairs) {
                                                    if (pair.trim().isEmpty()) continue;
                                                    String[] ll = pair.split(",");
                                                    double lng = Double.parseDouble(ll[0]);
                                                    double lat = Double.parseDouble(ll[1]);
                                                    routePoints.add(new LatLng(lat, lng));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (routePoints.isEmpty()) {
                                runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "未获取到路线"));
                                return;
                            }
                            runOnUiThread(() -> {
                                try {
                                    clearRoute(); 
                                    mRoutePoints.addAll(routePoints);
                                    updateRoutePolyline();
                                    updateRoutePointsList();
                                    GoUtils.DisplayToast(MainActivity.this, "路线规划成功");
                                } catch (Exception e) {
                                    XLog.e("NAV: draw polyline error");
                                }
                            });
                        } else {
                            runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "路线规划失败"));
                        }
                    } catch (JSONException e) {
                        XLog.e("NAV: parse json error");
                        runOnUiThread(() -> GoUtils.DisplayToast(MainActivity.this, "路线规划失败"));
                    }
                }
            });
        } catch (Exception ex) {
            XLog.e("NAV: exception, " + ex.getMessage());
            GoUtils.DisplayToast(this, "路线规划失败：" + ex.getMessage());
        }
    }
    private void showRoutePanel() {
        if (mRoutePanel.getParent() == null) {
            FrameLayout container = findViewById(R.id.route_panel_container);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM;
            container.addView(mRoutePanel, params);
        }
        mRoutePanel.setVisibility(View.VISIBLE);
        updateRoutePointsList();
    }
    private void closeRoutePanel() {
        mRoutePanel.setVisibility(View.GONE);
        isRouteMode = false;
        clearRoute();
    }
    private void addRoutePoint(LatLng point) {
        if (!isRouteMode) {
            return;
        }
        mRoutePoints.add(point);
        MarkerOptions marker = new MarkerOptions()
            .position(point)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding))
            .zIndex(10);
        mRouteMarkers.add(marker);
        mBaiduMap.addOverlay(marker);
        if (mRoutePoints.size() > 1) {
            updateRoutePolyline();
        }
        updateRoutePointsList();
    }
    private void deleteLastRoutePoint() {
        if (mRoutePoints.isEmpty()) {
            return;
        }
        int lastIndex = mRoutePoints.size() - 1;
        mRoutePoints.remove(lastIndex);
        mBaiduMap.clear();
        mRouteMarkers.clear();
        for (LatLng point : mRoutePoints) {
            MarkerOptions marker = new MarkerOptions()
                .position(point)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding))
                .zIndex(10);
            mRouteMarkers.add(marker);
            mBaiduMap.addOverlay(marker);
        }
        if (mRoutePolyline != null) {
            mRoutePolyline = null;
        }
        if (mRoutePoints.size() > 1) {
            updateRoutePolyline();
        }
        updateRoutePointsList();
    }
    private void clearRoute() {
        mRoutePoints.clear();
        mRouteMarkers.clear();
        mBaiduMap.clear();
        if (mRoutePolyline != null) {
            mRoutePolyline = null;
        }
        updateRoutePointsList();
    }
    private void updateRoutePolyline() {
        if (mRoutePoints.size() < 2) {
            return;
        }
        com.baidu.mapapi.map.PolylineOptions polylineOptions = new com.baidu.mapapi.map.PolylineOptions()
            .width(8)
            .color(0xAAFF4081)
            .points(mRoutePoints);
        mRoutePolyline = (com.baidu.mapapi.map.Polyline) mBaiduMap.addOverlay(polylineOptions);
    }
    private void updateRoutePointsList() {
        LinearLayout container = mRoutePanel.findViewById(R.id.route_points_container);
        container.removeAllViews();
        for (int i = 0; i < mRoutePoints.size(); i++) {
            TextView textView = new TextView(this);
            textView.setText(String.format(getString(R.string.route_point), i + 1));
            textView.setTextSize(12);
            textView.setPadding(8, 4, 8, 4);
            container.addView(textView);
        }
        Button btnStart = mRoutePanel.findViewById(R.id.btn_route_start);
        btnStart.setEnabled(mRoutePoints.size() >= 2);
    }
    private void startRouteNavigation() {
        if (mRoutePoints.size() < 2) {
            GoUtils.DisplayToast(this, getString(R.string.route_empty));
            return;
        }
        if (!isMockServStart) {
            startGoLocation();
            mButtonStart.setImageResource(R.drawable.ic_fly);
        }
        ArrayList<double[]> routeWgs = new ArrayList<>();
        for (LatLng point : mRoutePoints) {
            double[] wgs = MapUtils.bd2wgs(point.longitude, point.latitude);
            routeWgs.add(new double[] {wgs[0], wgs[1]});
        }
        if (mServiceBinder != null) {
            mServiceBinder.setRouteSpeed(mRouteSpeed);
            try {
                java.lang.reflect.Method method = mServiceBinder.getClass().getMethod("setRouteSpeedVariation", int.class);
                method.invoke(mServiceBinder, mRouteSpeedVariation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mServiceBinder.startFollowRoute(routeWgs);
            GoUtils.DisplayToast(this, getString(R.string.route_navigating));
            mRoutePanel.findViewById(R.id.btn_route_start).setEnabled(false);
            mRoutePanel.findViewById(R.id.btn_route_stop).setEnabled(true);
        } else {
            GoUtils.DisplayToast(this, "服务尚未启动，请先启动模拟位置");
        }
    }
    private void stopRouteNavigation() {
        if (mServiceBinder != null) {
            mServiceBinder.stopFollowRoute();
            GoUtils.DisplayToast(this, getString(R.string.route_finished));
            mRoutePanel.findViewById(R.id.btn_route_start).setEnabled(mRoutePoints.size() >= 2);
            mRoutePanel.findViewById(R.id.btn_route_stop).setEnabled(false);
        }
    }
    private void toggleRouteMode() {
        isRouteMode = !isRouteMode;
        if (isRouteMode) {
            showRoutePanel();
            GoUtils.DisplayToast(this, "进入路径模式，点击地图添加路径点");
        } else {
            closeRoutePanel();
        }
    }
}