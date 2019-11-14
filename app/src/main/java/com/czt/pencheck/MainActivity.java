package com.czt.pencheck;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.czt.pencheck.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgPublish;
    private TextView textView1;
    private TextView textView2;

    private boolean isMenuOpen = false;

    private List<TextView> textViews = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgPublish = (ImageView) findViewById(R.id.img_publish);
        textView1 = (TextView) findViewById(R.id.tv_1);
        textView2 = (TextView) findViewById(R.id.tv_2);

        textViews.add(textView1);
        textViews.add(textView2);

        imgPublish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_publish:

                if (!isMenuOpen) {
                    showOpenAnim(80);
                    imgPublish.setImageResource(R.mipmap.ic_launcher);
                }else {
                    showCloseAnim(80);
                    imgPublish.setImageResource(R.mipmap.ic_launcher);
                }
                break;
        }

    }

    //打开扇形菜单的属性动画， dp为半径长度
    private void showOpenAnim(int dp) {
        textView1.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);


        //for循环来开始小图标的出现动画
        for (int i = 0; i < textViews.size(); i++) {
            AnimatorSet set = new AnimatorSet();
            //标题1与x轴负方向角度为20°，标题2为100°，转换为弧度
            double a = -Math.cos(20 * Math.PI / 180 * (i * 2 + 1));
            double b = -Math.sin(20 * Math.PI / 180 * (i * 2 + 1));
            double x = a * dip2px(dp);
            double y = b * dip2px(dp);

            set.playTogether(
                    ObjectAnimator.ofFloat(textViews.get(i), "translationX", (float) (x * 0.25), (float) x),
                    ObjectAnimator.ofFloat(textViews.get(i), "translationY", (float) (y * 0.25), (float) y)
                    , ObjectAnimator.ofFloat(textViews.get(i), "alpha", 0, 1).setDuration(2000)
            );
            set.setInterpolator(new BounceInterpolator());
            set.setDuration(500).setStartDelay(100);
            set.start();

            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    //菜单状态置打开
                    isMenuOpen = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        //转动加号大图标本身45°
        ObjectAnimator rotate = ObjectAnimator.ofFloat(imgPublish, "rotation", 0, 90).setDuration(300);
        rotate.setInterpolator(new BounceInterpolator());
        rotate.start();

    }

    //关闭扇形菜单的属性动画，参数与打开时相反
    private void showCloseAnim(int dp) {


        //for循环来开始小图标的出现动画
        for (int i = 0; i < textViews.size(); i++) {
            AnimatorSet set = new AnimatorSet();
            double a = -Math.cos(20 * Math.PI / 180 * (i * 2 + 1));
            double b = -Math.sin(20 * Math.PI / 180 * (i * 2 + 1));
            double x = a * dip2px(dp);
            double y = b * dip2px(dp);

            set.playTogether(
                    ObjectAnimator.ofFloat(textViews.get(i), "translationX", (float) x, (float) (x * 0.25)),
                    ObjectAnimator.ofFloat(textViews.get(i), "translationY", (float) y, (float) (y * 0.25)),
                    ObjectAnimator.ofFloat(textViews.get(i), "alpha", 1, 0).setDuration(2000)
            );
//      set.setInterpolator(new AccelerateInterpolator());
            set.setDuration(500);
            set.start();

            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    textView1.setVisibility(View.GONE);
                    textView2.setVisibility(View.GONE);

                    //菜单状态置关闭
                    isMenuOpen = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }


        //转动加号大图标本身45°
        ObjectAnimator rotate = ObjectAnimator.ofFloat(imgPublish, "rotation", 0, 90).setDuration(300);
        rotate.setInterpolator(new BounceInterpolator());
        rotate.start();


    }

    private int dip2px(int value) {
        float density = getResources()
                .getDisplayMetrics().density;
        return (int) (density * value + 0.5f);
    }

}
 