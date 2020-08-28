package com.example.bluetoothsmart.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.example.bluetoothsmart.R;

public class StartActivity extends BaseActivity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        imageView=findViewById(R.id.start_img);
        alphaAnimation(2000);
    }
    /**
     * 启动APP动画
     */
    private void alphaAnimation(long delayMillis) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);
        alphaAnimation.setDuration(delayMillis);// 设定动画时间
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        imageView.setAnimation(alphaAnimation);
        alphaAnimation.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //发送消息到主线程
                handler.sendEmptyMessage(1);
            }
        }, delayMillis); //延时delayMillis毫秒
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 切换到登陆页面
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    StartActivity.this.finish();
                    break;
            }
        }
    };
}