package com.zcshou.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.elvishew.xlog.XLog;

/**
 * 位置历史数据库类
 * 用于管理位置历史记录的存储和操作
 */
public class DataBaseHistoryLocation extends SQLiteOpenHelper {
    // 数据库常量
    public static final String TABLE_NAME = "HistoryLocation"; // 表名
    public static final String DB_COLUMN_ID = "DB_COLUMN_ID"; // 主键ID
    public static final String DB_COLUMN_LOCATION = "DB_COLUMN_LOCATION"; // 位置名称
    public static final String DB_COLUMN_LONGITUDE_WGS84 = "DB_COLUMN_LONGITUDE_WGS84"; // WGS84经度
    public static final String DB_COLUMN_LATITUDE_WGS84 = "DB_COLUMN_LATITUDE_WGS84"; // WGS84纬度
    public static final String DB_COLUMN_TIMESTAMP = "DB_COLUMN_TIMESTAMP"; // 时间戳
    public static final String DB_COLUMN_LONGITUDE_CUSTOM = "DB_COLUMN_LONGITUDE_CUSTOM"; // 自定义经度
    public static final String DB_COLUMN_LATITUDE_CUSTOM = "DB_COLUMN_LATITUDE_CUSTOM"; // 自定义纬度
    private static final int DB_VERSION = 1; // 数据库版本
    private static final String DB_NAME = "HistoryLocation.db"; // 数据库名称
    
    // 创建表的SQL语句
    private static final String CREATE_TABLE = "create table if not exists " + TABLE_NAME +
            " (DB_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, DB_COLUMN_LOCATION TEXT, " +
            "DB_COLUMN_LONGITUDE_WGS84 TEXT NOT NULL, DB_COLUMN_LATITUDE_WGS84 TEXT NOT NULL, " +
            "DB_COLUMN_TIMESTAMP BIGINT NOT NULL, DB_COLUMN_LONGITUDE_CUSTOM TEXT NOT NULL, DB_COLUMN_LATITUDE_CUSTOM TEXT NOT NULL)";
    /**
     * 构造方法
     * @param context 上下文
     */
    public DataBaseHistoryLocation(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    /**
     * 数据库创建时调用
     * 创建位置历史表
     * @param sqLiteDatabase SQLite数据库实例
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }
    /**
     * 数据库升级时调用
     * 删除旧表并创建新表
     * @param sqLiteDatabase SQLite数据库实例
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
    /**
     * 保存位置历史记录
     * 先删除相同经纬度的记录，再插入新记录
     * @param sqLiteDatabase SQLite数据库实例
     * @param contentValues 位置数据
     */
    public static void saveHistoryLocation(SQLiteDatabase sqLiteDatabase, ContentValues contentValues) {
        try {
            String longitudeWgs84 = contentValues.getAsString(DB_COLUMN_LONGITUDE_WGS84);
            String latitudeWgs84 = contentValues.getAsString(DB_COLUMN_LATITUDE_WGS84);
            // 删除相同经纬度的记录
            sqLiteDatabase.delete(TABLE_NAME,
                            DB_COLUMN_LONGITUDE_WGS84 + " = ? AND " +
                            DB_COLUMN_LATITUDE_WGS84 + " = ?",
                    new String[] {longitudeWgs84, latitudeWgs84});
            // 插入新记录
            sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            XLog.e("DATABASE: insert error");
        }
    }
    /**
     * 更新位置历史记录
     * 根据ID更新位置名称
     * @param sqLiteDatabase SQLite数据库实例
     * @param locID 位置ID
     * @param location 位置名称
     */
    public static void updateHistoryLocation(SQLiteDatabase sqLiteDatabase, String locID, String location) {
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(DB_COLUMN_LOCATION, location);
            sqLiteDatabase.update(TABLE_NAME, contentValues, DB_COLUMN_ID + " = ?", new String[]{locID});
        } catch (Exception e){
            XLog.e("DATABASE: update error");
        }
    }
}