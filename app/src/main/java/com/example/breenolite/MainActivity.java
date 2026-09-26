package com.example.breenolite;

import android.app.Activity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(48,48,48,48);
        TextView title = new TextView(this); title.setText("Breeno Lite Keyboard"); title.setTextSize(26); title.setTextColor(Color.WHITE);
        TextView info = new TextView(this); info.setText("Bàn phím độc lập • Không xác minh beta • Telex kiểu UniKey\n\n1. Bật bàn phím trong Cài đặt hệ thống.\n2. Chọn Breeno Lite Keyboard làm bàn phím hiện tại."); info.setTextSize(16); info.setTextColor(Color.LTGRAY); info.setPadding(0,32,0,32);
        Button enable = new Button(this); enable.setText("Mở cài đặt bàn phím"); enable.setOnClickListener(v -> ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker());
        box.setBackgroundColor(Color.rgb(12,25,40)); box.addView(title); box.addView(info); box.addView(enable); setContentView(box);
    }
}
