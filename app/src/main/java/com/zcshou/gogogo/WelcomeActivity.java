package com.zcshou.gogogo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.Window;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.zcshou.utils.GoUtils;
import java.util.ArrayList;

/**
 * 欢迎活动类
 * 应用启动时的欢迎页面，包含密码验证和直接启动主活动
 */
public class WelcomeActivity extends AppCompatActivity {
    // 常量和变量定义
    private static boolean isPermission = false; // 是否获得权限
    private static final int SDK_PERMISSION_REQUEST = 127; // 权限请求码
    private static final ArrayList<String> ReqPermissions = new ArrayList<>(); // 请求的权限列表
    
    // 密码验证相关常量
    private static final String PASSWORD = "7355608"; // 密码
    private static final String KEY_PASSWORD_VERIFIED = "KEY_PASSWORD_VERIFIED"; // 密码验证状态键
    
    /**
     * 活动创建时调用
     * 初始化并检查是否需要密码验证，然后启动主活动
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false);
        checkDefaultPermissions();
        checkPasswordVerification();
    }
    
    /**
     * 检查密码验证状态
     * 如果是第一次启动，显示密码验证对话框
     * 否则直接启动主活动
     */
    private void checkPasswordVerification() {
        SharedPreferences preferences = getSharedPreferences(KEY_PASSWORD_VERIFIED, MODE_PRIVATE);
        boolean isVerified = preferences.getBoolean(KEY_PASSWORD_VERIFIED, false);
        
        if (!isVerified) {
            showPasswordDialog();
        } else {
            startMainActivity();
        }
    }
    
    /**
     * 显示密码验证对话框
     */
    private void showPasswordDialog() {
        // 使用AlertDialog.Builder创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("密码验证");
        builder.setMessage("请输入密码");
        builder.setCancelable(false);
        
        // 创建密码输入框
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("请输入密码");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        passwordInput.setPadding(40, 20, 40, 20);
        passwordInput.setTextSize(18);
        
        // 设置输入框到对话框
        builder.setView(passwordInput);
        
        // 设置确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            String inputPassword = passwordInput.getText().toString();
            if (PASSWORD.equals(inputPassword)) {
                // 密码正确，标记为已验证
                SharedPreferences preferences = getSharedPreferences(KEY_PASSWORD_VERIFIED, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_PASSWORD_VERIFIED, true);
                editor.apply();
                
                startMainActivity();
            } else {
                // 密码错误，显示提示并重新显示对话框
                Toast.makeText(this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                showPasswordDialog();
            }
        });
        
        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            // 取消，退出应用
            finish();
        });
        
        // 创建并显示对话框
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        
        // 确保对话框显示后，输入框获得焦点并弹出输入法
        passwordInput.post(() -> {
            passwordInput.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(passwordInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });
    }
    
    /**
     * 检查默认权限
     * 检查并请求必要的权限
     */
    private void checkDefaultPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ReqPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ReqPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ReqPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ReqPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ReqPermissions.isEmpty()) {
            isPermission = true;
        } else {
            requestPermissions(ReqPermissions.toArray(new String[0]), SDK_PERMISSION_REQUEST);
        }
    }
    
    /**
     * 启动主活动
     * 检查权限后直接启动主活动
     */
    private void startMainActivity() {
        if (isPermission) {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            WelcomeActivity.this.finish();
        }
    }
    
    /**
     * 权限请求结果回调
     * @param requestCode 请求码
     * @param permissions 请求的权限数组
     * @param grantResults 权限授予结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == SDK_PERMISSION_REQUEST) {
            for (int i = 0; i < ReqPermissions.size(); i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    GoUtils.DisplayToast(this, getResources().getString(R.string.app_error_permission));
                    return;
                }
            }
            isPermission = true;
            checkPasswordVerification();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}