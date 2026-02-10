package com.zcshou.service;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.elvishew.xlog.XLog;
import com.zcshou.gogogo.MainActivity;
import com.zcshou.gogogo.R;
import com.zcshou.joystick.JoyStick;
import java.util.ArrayList;
public class ServiceGo extends Service {
    public static final double DEFAULT_LAT = 36.667662;
    public static final double DEFAULT_LNG = 117.027707;
    public static final double DEFAULT_ALT = 55.0D;
    public static final float DEFAULT_BEA = 0.0F;
    private double mCurLat = DEFAULT_LAT;
    private double mCurLng = DEFAULT_LNG;
    private double mCurAlt = DEFAULT_ALT;
    private float mCurBea = DEFAULT_BEA;
    private double mSpeed = 4.3;        
    private volatile boolean isFollowingRoute = false;
    private ArrayList<double[]> mRoutePoints = new ArrayList<>(); 
    private int mRouteIndex = 0;
    private double mRouteSpeed = 4.3; 
    private int mRouteSpeedVariation = 0; 
    private double mRouteProgress = 0.0; 
    private double mCurrentRouteSpeed = 4.3; 
    private long mLastSpeedUpdateTime = 0; 
    private static final int HANDLER_MSG_ID = 0;
    private static final String SERVICE_GO_HANDLER_NAME = "ServiceGoLocation";
    private LocationManager mLocManager;
    private HandlerThread mLocHandlerThread;
    private Handler mLocHandler;
    private boolean isStop = false;
    private static final int SERVICE_GO_NOTE_ID = 1;
    private static final String SERVICE_GO_NOTE_ACTION_JOYSTICK_SHOW = "ShowJoyStick";
    private static final String SERVICE_GO_NOTE_ACTION_JOYSTICK_HIDE = "HideJoyStick";
    private static final String SERVICE_GO_NOTE_CHANNEL_ID = "SERVICE_GO_NOTE";
    private static final String SERVICE_GO_NOTE_CHANNEL_NAME = "SERVICE_GO_NOTE";
    private NoteActionReceiver mActReceiver;
    private JoyStick mJoyStick;
    private final ServiceGoBinder mBinder = new ServiceGoBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mLocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        removeTestProviderNetwork();
        addTestProviderNetwork();
        removeTestProviderGPS();
        addTestProviderGPS();
        initGoLocation();
        initNotification();
        initJoyStick();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCurLng = intent.getDoubleExtra(MainActivity.LNG_MSG_ID, DEFAULT_LNG);
        mCurLat = intent.getDoubleExtra(MainActivity.LAT_MSG_ID, DEFAULT_LAT);
        mCurAlt = intent.getDoubleExtra(MainActivity.ALT_MSG_ID, DEFAULT_ALT);
        mJoyStick.setCurrentPosition(mCurLng, mCurLat, mCurAlt);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        isStop = true;
        mLocHandler.removeMessages(HANDLER_MSG_ID);
        mLocHandlerThread.quit();
        mJoyStick.destroy();
        removeTestProviderNetwork();
        removeTestProviderGPS();
        unregisterReceiver(mActReceiver);
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }
    private void initNotification() {
        mActReceiver = new NoteActionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_GO_NOTE_ACTION_JOYSTICK_SHOW);
        filter.addAction(SERVICE_GO_NOTE_ACTION_JOYSTICK_HIDE);
        registerReceiver(mActReceiver, filter);
        NotificationChannel mChannel = new NotificationChannel(SERVICE_GO_NOTE_CHANNEL_ID, SERVICE_GO_NOTE_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(mChannel);
        }
        Intent clickIntent = new Intent(this, MainActivity.class);
        PendingIntent clickPI = PendingIntent.getActivity(this, 1, clickIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent showIntent = new Intent(SERVICE_GO_NOTE_ACTION_JOYSTICK_SHOW);
        PendingIntent showPendingPI = PendingIntent.getBroadcast(this, 0, showIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent hideIntent = new Intent(SERVICE_GO_NOTE_ACTION_JOYSTICK_HIDE);
        PendingIntent hidePendingPI = PendingIntent.getBroadcast(this, 0, hideIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, SERVICE_GO_NOTE_CHANNEL_ID)
                .setChannelId(SERVICE_GO_NOTE_CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.app_service_tips))
                .setContentIntent(clickPI)
                .addAction(new NotificationCompat.Action(null, getResources().getString(R.string.note_show), showPendingPI))
                .addAction(new NotificationCompat.Action(null, getResources().getString(R.string.note_hide), hidePendingPI))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(SERVICE_GO_NOTE_ID, notification);
    }
    private void initJoyStick() {
        mJoyStick = new JoyStick(this);
        mJoyStick.setListener(new JoyStick.JoyStickClickListener() {
            @Override
            public void onMoveInfo(double speed, double disLng, double disLat, double angle) {
                mSpeed = Math.max(0.1, Math.min(speed, 50.0));
                mCurLng += disLng / (111.320 * Math.cos(Math.abs(mCurLat) * Math.PI / 180));
                mCurLat += disLat / 110.574;
                mCurLng = Math.max(-180.0, Math.min(180.0, mCurLng));
                mCurLat = Math.max(-90.0, Math.min(90.0, mCurLat));
                mCurBea = (float) angle;
            }
            @Override
            public void onPositionInfo(double lng, double lat, double alt) {
                mCurLng = Math.max(-180.0, Math.min(180.0, lng));
                mCurLat = Math.max(-90.0, Math.min(90.0, lat));
                mCurAlt = Math.max(0.0, alt);
            }
        });
        mJoyStick.show();
    }
    private void initGoLocation() {
        mLocHandlerThread = new HandlerThread(SERVICE_GO_HANDLER_NAME, Process.THREAD_PRIORITY_FOREGROUND);
        mLocHandlerThread.start();
        mLocHandler = new Handler(mLocHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                try {
                    Thread.sleep(100);
                    if (!isStop) {
                        if (isFollowingRoute && mRouteIndex < mRoutePoints.size()) {
                            double[] current = mRoutePoints.get(mRouteIndex);
                            double[] next = null;
                            if (mRouteIndex + 1 < mRoutePoints.size()) {
                                next = mRoutePoints.get(mRouteIndex + 1);
                            }
                            if (next != null) {
                                double dx = next[0] - current[0];
                                double dy = next[1] - current[1];
                                double lat1 = Math.toRadians(current[1]);
                                double lat2 = Math.toRadians(next[1]);
                                double dLat = Math.toRadians(dy);
                                double dLon = Math.toRadians(dx);
                                double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                                        Math.cos(lat1) * Math.cos(lat2) *
                                        Math.sin(dLon/2) * Math.sin(dLon/2);
                                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                                double distance = 6371000 * c; 
                                mCurrentRouteSpeed = mRouteSpeed;
                                if (mRouteSpeedVariation > 0) {
                                    double variationPercent = (Math.random() * 2 * mRouteSpeedVariation - mRouteSpeedVariation) / 100.0;
                                    mCurrentRouteSpeed = mRouteSpeed * (1 + variationPercent);
                                }
                                double moveDistance = (mCurrentRouteSpeed / 3.6) * 0.1; 
                                XLog.i("SERVICEGO: speed=" + String.format("%.2f", mCurrentRouteSpeed) + " km/h, moveDistance=" + String.format("%.4f", moveDistance) + " 米");
                                mRouteProgress += moveDistance / distance;
                                if (mRouteProgress >= 1.0) {
                                    mRouteIndex++;
                                    mRouteProgress = 0.0;
                                    mCurLng = next[0];
                                    mCurLat = next[1];
                                } else {
                                    mCurLng = current[0] + dx * mRouteProgress;
                                    mCurLat = current[1] + dy * mRouteProgress;
                                }
                                mCurBea = (float) Math.toDegrees(Math.atan2(dy, dx));
                                if (mCurBea < 0) {
                                    mCurBea += 360;
                                }
                                mCurBea = (mCurBea + 360) % 360;
                            } else {
                                mCurLng = current[0];
                                mCurLat = current[1];
                                isFollowingRoute = false;
                                mRoutePoints.clear();
                                mRouteIndex = 0;
                                mRouteProgress = 0.0;
                                XLog.i("SERVICEGO: finish follow route");
                            }
                        }
                        setLocationNetwork();
                        setLocationGPS();
                        sendEmptyMessage(HANDLER_MSG_ID);
                    }
                } catch (InterruptedException e) {
                    XLog.e("SERVICEGO: ERROR - handleMessage");
                    Thread.currentThread().interrupt();
                }
            }
        };
        mLocHandler.sendEmptyMessage(HANDLER_MSG_ID);
    }
    private void removeTestProviderGPS() {
        try {
            if (mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
                mLocManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception e) {
            XLog.e("SERVICEGO: ERROR - removeTestProviderGPS");
        }
    }
    @SuppressLint("wrongconstant")
    private void addTestProviderGPS() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocManager.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false,
                        false, true, true, true, ProviderProperties.POWER_USAGE_HIGH, ProviderProperties.ACCURACY_FINE);
            } else {
                mLocManager.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false,
                        false, true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
            }
            if (!mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            }
        } catch (Exception e) {
            XLog.e("SERVICEGO: ERROR - addTestProviderGPS");
        }
    }
    private void setLocationGPS() {
        try {
            Location loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setAccuracy(Criteria.ACCURACY_FINE);    
            loc.setAltitude(mCurAlt);                     
            loc.setBearing(mCurBea);                       
            loc.setLatitude(mCurLat);                   
            loc.setLongitude(mCurLng);                  
            loc.setTime(System.currentTimeMillis());    
            if (isFollowingRoute) {
                loc.setSpeed((float) (mCurrentRouteSpeed / 3.6));
            } else {
                loc.setSpeed((float) (mSpeed / 3.6));
            }
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            Bundle bundle = new Bundle();
            bundle.putInt("satellites", 7);
            loc.setExtras(bundle);
            mLocManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc);
        } catch (Exception e) {
            XLog.e("SERVICEGO: ERROR - setLocationGPS");
        }
    }
    private void removeTestProviderNetwork() {
        try {
            if (mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
                mLocManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception e) {
            XLog.e("SERVICEGO: ERROR - removeTestProviderNetwork");
        }
    }
    @SuppressLint("wrongconstant")
    private void addTestProviderNetwork() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocManager.addTestProvider(LocationManager.NETWORK_PROVIDER, true, false,
                        true, true, true, true,
                        true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE);
            } else {
                mLocManager.addTestProvider(LocationManager.NETWORK_PROVIDER, true, false,
                        true, true, true, true,
                        true, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE);
            }
            if (!mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
            }
        } catch (SecurityException e) {
            XLog.e("SERVICEGO: ERROR - addTestProviderNetwork");
        }
    }
    private void setLocationNetwork() {
        try {
            Location loc = new Location(LocationManager.NETWORK_PROVIDER);
            loc.setAccuracy(Criteria.ACCURACY_COARSE);  
            loc.setAltitude(mCurAlt);                     
            loc.setBearing(mCurBea);                       
            loc.setLatitude(mCurLat);                   
            loc.setLongitude(mCurLng);                  
            loc.setTime(System.currentTimeMillis());    
            if (isFollowingRoute) {
                loc.setSpeed((float) (mCurrentRouteSpeed / 3.6));
            } else {
                loc.setSpeed((float) (mSpeed / 3.6));
            }
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mLocManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, loc);
        } catch (Exception e) {
            XLog.e("SERVICEGO: ERROR - setLocationNetwork");
        }
    }
    public class NoteActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(SERVICE_GO_NOTE_ACTION_JOYSTICK_SHOW)) {
                    mJoyStick.show();
                }
                if (action.equals(SERVICE_GO_NOTE_ACTION_JOYSTICK_HIDE)) {
                    mJoyStick.hide();
                }
            }
        }
    }
    public class ServiceGoBinder extends Binder {
        public void setPosition(double lng, double lat, double alt) {
            mLocHandler.removeMessages(HANDLER_MSG_ID);
            mCurLng = Math.max(-180.0, Math.min(180.0, lng));
            mCurLat = Math.max(-90.0, Math.min(90.0, lat));
            mCurAlt = Math.max(0.0, alt);
            mLocHandler.sendEmptyMessage(HANDLER_MSG_ID);
            mJoyStick.setCurrentPosition(mCurLng, mCurLat, mCurAlt);
        }
        public void startFollowRoute(ArrayList<double[]> routeWgs) {
            if (routeWgs == null || routeWgs.isEmpty()) return;
            mRoutePoints.clear();
            for (double[] point : routeWgs) {
                if (point.length >= 2) {
                    double lng = Math.max(-180.0, Math.min(180.0, point[0]));
                    double lat = Math.max(-90.0, Math.min(90.0, point[1]));
                    mRoutePoints.add(new double[]{lng, lat});
                }
            }
            mRouteIndex = 0;
            mRouteProgress = 0.0;
            isFollowingRoute = true;
            XLog.i("SERVICEGO: start follow route, points=" + mRoutePoints.size());
        }
        public void stopFollowRoute() {
            isFollowingRoute = false;
            mRoutePoints.clear();
            mRouteIndex = 0;
            mRouteProgress = 0.0;
        }
        public void setRouteSpeed(double speed) {
            mRouteSpeed = Math.max(0.1, Math.min(speed, 400.0)); 
            XLog.i("SERVICEGO: set route speed=" + String.format("%.2f", mRouteSpeed) + " km/h");
        }
        public void setRouteSpeedVariation(int variation) {
            mRouteSpeedVariation = Math.max(0, Math.min(variation, 20));
            XLog.i("SERVICEGO: set route speed variation=" + mRouteSpeedVariation + "%");
        }
    }
}