package com.czt.pencheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WindowBroadcastReceiver extends BroadcastReceiver {

    private BottomMenuLayout leftView;
    private BottomMenuLayout rightView;

    public static final int MSG_SHOW_LEFT_WINDOW = 1;
    public static final int MSG_HIDE_LEFT_WINDOW = 2;
    public static final int MSG_SHOW_RIGHT_WINDOW = 3;
    public static final int MSG_HIDE_RIGHT_WINDOW = 4;

    public static final String WINDOW_CHANGE_ACTION = "WindowChange";


    public WindowBroadcastReceiver(BottomMenuLayout leftView, BottomMenuLayout rightView) {
        this.leftView = leftView;
        this.rightView = rightView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (leftView != null && rightView != null && intent != null) {
            int msg = intent.getIntExtra("msg", 0);
            switch (msg) {
                case MSG_SHOW_LEFT_WINDOW:
                    leftView.showMenu();
                    break;
                case MSG_HIDE_LEFT_WINDOW:
                    leftView.hideMenu();
                    break;
                case MSG_SHOW_RIGHT_WINDOW:
                    rightView.showMenu();
                    break;
                case MSG_HIDE_RIGHT_WINDOW:
                    rightView.hideMenu();
                    break;
                default:
                    if (msg % 10 == 0 && msg < 100) {
                        rightView.addItem(R.mipmap.annotation, "批注", 2, false);
                    } else if (msg % 100 == 0) {
                        rightView.removeItemAt(2, false);
                    }

            }
        }
    }
}
