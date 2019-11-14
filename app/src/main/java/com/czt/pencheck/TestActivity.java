package com.czt.pencheck;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class TestActivity extends AppCompatActivity {

    private Button button;
    private BottomMenuView menuView;
    private BottomMenuLayout group;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        button = findViewById(R.id.btn_start);
        menuView = findViewById(R.id.menu);
        menuView.addIcons(Arrays.asList(R.mipmap.annotation, R.mipmap.whiteboard,
                R.mipmap.small_blackboard));

        group = findViewById(R.id.menu_group);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!group.isShowing()) {
                    group.showMenu();
                }
            }
        });

        textView = findViewById(R.id.test_text);

        group.setOnItemClickListener(new BottomMenuLayout.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                textView.setText("点击了第 " + position + "个按钮");
            }
        });

        group.setOnShutDownClickListener(new BottomMenuLayout.OnShutDownClickListener() {
            @Override
            public void onShutDownClick() {
                if (group.isShowing()) {
                    group.hideMenu();
                }
            }
        });
    }
}
