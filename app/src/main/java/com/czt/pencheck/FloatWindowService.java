package com.czt.pencheck;

import android.app.ActionBar;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class FloatWindowService extends Service {

    BottomMenuLayout leftView;
    BottomMenuLayout rightView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void addWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

                DisplayMetrics outMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(outMetrics);
                int w = outMetrics.widthPixels;
                int h = outMetrics.heightPixels;

                LayoutInflater inflater = LayoutInflater.from(this);
                leftView = (BottomMenuLayout) inflater.inflate(R.layout.bootom_menu, null);
                rightView = (BottomMenuLayout) inflater.inflate(R.layout.bottom_menu_right, null);


                WindowManager.LayoutParams leftLayoutParams = new WindowManager.LayoutParams();
                WindowManager.LayoutParams rightLayoutParams = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    leftLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    rightLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    leftLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                    rightLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                }

                leftLayoutParams.format = PixelFormat.RGBA_8888;
                leftLayoutParams.width = 550;
                leftLayoutParams.height = 550;
                leftLayoutParams.x = - w;
                leftLayoutParams.y = h - 550;
                leftLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

                rightLayoutParams.format = PixelFormat.RGBA_8888;
                rightLayoutParams.width = 550;
                rightLayoutParams.height = 550;
                rightLayoutParams.x = w - 550;
                rightLayoutParams.y = h - 550;
                rightLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

                leftView.setOnShutDownClickListener(new BottomMenuLayout.OnShutDownClickListener() {
                    @Override
                    public void onShutDownClick() {
                        leftView.hideMenu();
                    }
                });

                rightView.setOnShutDownClickListener(new BottomMenuLayout.OnShutDownClickListener() {
                    @Override
                    public void onShutDownClick() {
                        rightView.hideMenu();
                    }
                });


                windowManager.addView(leftView, leftLayoutParams);
                windowManager.addView(rightView, rightLayoutParams);

            }
        }

        WindowBroadcastReceiver receiver = new WindowBroadcastReceiver(leftView, rightView);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WindowBroadcastReceiver.WINDOW_CHANGE_ACTION);
        registerReceiver(receiver, intentFilter);
    }


}
