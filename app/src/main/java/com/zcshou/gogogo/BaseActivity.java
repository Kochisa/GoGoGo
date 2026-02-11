package com.zcshou.gogogo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 基础活动类
 * 所有活动的父类，提供通用功能
 */
public class BaseActivity extends AppCompatActivity {
    /**
     * 活动创建时调用
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}