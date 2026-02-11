package com.zcshou.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.elvishew.xlog.XLog;

/**
 * 搜索历史数据库类
 * 用于管理搜索历史记录的存储和操作
 */
public class DataBaseHistorySearch extends SQLiteOpenHelper {
    // 数据库常量
    public static final String TABLE_NAME = "HistorySearch"; // 表名
    public static final String DB_COLUMN_ID = "DB_COLUMN_ID"; // 主键ID
    public static final String DB_COLUMN_KEY = "DB_COLUMN_KEY"; // 搜索关键词
    public static final String DB_COLUMN_DESCRIPTION = "DB_COLUMN_DESCRIPTION"; // 描述信息
    public static final String DB_COLUMN_TIMESTAMP = "DB_COLUMN_TIMESTAMP"; // 时间戳
    public static final String DB_COLUMN_IS_LOCATION = "DB_COLUMN_IS_LOCATION"; // 是否为位置
    public static final String DB_COLUMN_LONGITUDE_WGS84 = "DB_COLUMN_LONGITUDE_WGS84"; // WGS84经度
    public static final String DB_COLUMN_LATITUDE_WGS84 = "DB_COLUMN_LATITUDE_WGS84"; // WGS84纬度
    public static final String DB_COLUMN_LONGITUDE_CUSTOM = "DB_COLUMN_LONGITUDE_CUSTOM"; // 自定义经度
    public static final String DB_COLUMN_LATITUDE_CUSTOM = "DB_COLUMN_LATITUDE_CUSTOM"; // 自定义纬度
    
    // 搜索类型常量
    public static final int DB_SEARCH_TYPE_KEY = 0; // 搜索关键词类型
    public static final int DB_SEARCH_TYPE_RESULT = 1; // 搜索结果类型
    
    // 数据库配置
    private static final int DB_VERSION = 1; // 数据库版本
    private static final String DB_NAME = "HistorySearch.db"; // 数据库名称
    
    // 创建表的SQL语句
    private static final String CREATE_TABLE = "create table if not exists " + TABLE_NAME +
            " (DB_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, DB_COLUMN_KEY TEXT NOT NULL, " +
            "DB_COLUMN_DESCRIPTION TEXT, DB_COLUMN_TIMESTAMP BIGINT NOT NULL, DB_COLUMN_IS_LOCATION INTEGER NOT NULL, " +
            "DB_COLUMN_LONGITUDE_WGS84 TEXT, DB_COLUMN_LATITUDE_WGS84 TEXT, " +
            "DB_COLUMN_LONGITUDE_CUSTOM TEXT, DB_COLUMN_LATITUDE_CUSTOM TEXT)";
    /**
     * 构造方法
     * @param context 上下文
     */
    public DataBaseHistorySearch(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    /**
     * 数据库创建时调用
     * 创建搜索历史表
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
     * 保存搜索历史记录
     * 先删除相同关键词的记录，再插入新记录
     * @param sqLiteDatabase SQLite数据库实例
     * @param contentValues 搜索数据
     */
    public static void saveHistorySearch(SQLiteDatabase sqLiteDatabase, ContentValues contentValues) {
        try {
            String searchKey = contentValues.get(DataBaseHistorySearch.DB_COLUMN_KEY).toString();
            // 删除相同关键词的记录
            sqLiteDatabase.delete(DataBaseHistorySearch.TABLE_NAME, DataBaseHistorySearch.DB_COLUMN_KEY + " = ?", new String[] {searchKey});
            // 插入新记录
            sqLiteDatabase.insert(DataBaseHistorySearch.TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            XLog.e("DATABASE: insert error");
        }
    }
}