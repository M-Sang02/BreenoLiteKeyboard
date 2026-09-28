package com.example.breenolite;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Simple standalone Vietnamese Telex keyboard. */
public class VietnameseImeService extends InputMethodService {
    private LinearLayout root;
    private String rawWord = "";
    private boolean shift = false;
    private boolean telex = true;
    private InputConnection ic;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public View onCreateInputView() {
        return buildKeyboard();
    }

    private View buildKeyboard() {
        // Give the IME a real height. Previously the weighted rows were measured at 0px
        // because the root itself had no explicit/minimum height, so only the top bar appeared.
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(5), dp(5), dp(5), dp(7));
        root.setBackgroundColor(Color.rgb(16, 29, 44));
        root.setMinimumHeight(dp(285));
        root.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(300)));

        // Header / mode row
        LinearLayout header = row();
        Button lang = key("VI", 1f);
        lang.setOnClickListener(v -> {
            telex = !telex;
            lang.setText(telex ? "VI" : "EN");
        });

        Button title = key("Breeno Lite • Telex", 4f);
        title.setOnClickListener(v -> {
            commitRaw();
            ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(" ", 1);
        });

        Button backHeader = key("⌫", 1f);
        backHeader.setOnClickListener(v -> backspace());

        header.addView(lang, weightedParams(1f));
        header.addView(title, weightedParams(4f));
        header.addView(backHeader, weightedParams(1f));
        root.addView(header, fixedHeight(42));

        addLetterRow(new String[]{"q","w","e","r","t","y","u","i","o","p"}, 1f);
        addLetterRow(new String[]{"a","s","d","f","g","h","j","k","l"}, 1f);

        LinearLayout row3 = row();
        Button shiftButton = key("⇧", 1.3f);
        shiftButton.setOnClickListener(v -> {
            shift = !shift;
            updateLetterCase();
        });
        row3.addView(shiftButton, weightedParams(1.3f));
        for (String k : new String[]{"z","x","c","v","b","n","m"}) {
            addKey(row3, k, 1f);
        }
        Button back = key("⌫", 1.5f);
        back.setOnClickListener(v -> backspace());
        row3.addView(back, weightedParams(1.5f));
        root.addView(row3, fixedHeight(48));

        LinearLayout bottom = row();
        Button numbers = key("123", 1.4f);
        numbers.setOnClickListener(v -> toast("Bàn phím số đang được bổ sung"));
        bottom.addView(numbers, weightedParams(1.4f));

        Button comma = key(",", 1f);
        comma.setOnClickListener(v -> typePunctuation(","));
        bottom.addView(comma, weightedParams(1f));

        Button globe = key("🌐", 1f);
        globe.setOnClickListener(v -> getInputMethodManager().showInputMethodPicker());
        bottom.addView(globe, weightedParams(1f));

        Button space = key("space", 4f);
        space.setOnClickListener(v -> {
            commitRaw();
            ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(" ", 1);
        });
        bottom.addView(space, weightedParams(4f));

        Button dot = key(".", 1f);
        dot.setOnClickListener(v -> typePunctuation("."));
        bottom.addView(dot, weightedParams(1f));

        Button enter = key("↵", 1.3f);
        enter.setOnClickListener(v -> enter());
        bottom.addView(enter, weightedParams(1.3f));
        root.addView(bottom, fixedHeight(48));

        return root;
    }

    private void addLetterRow(String[] letters, float weight) {
        LinearLayout r = row();
        for (String k : letters) addKey(r, k, weight);
        root.addView(r, fixedHeight(48));
    }

    private void addKey(LinearLayout parent, String label, float weight) {
        Button b = key(label, weight);
        b.setTag(label);
        b.setOnClickListener(v -> type((String) v.getTag()));
        parent.addView(b, weightedParams(weight));
    }

    private LinearLayout.LayoutParams weightedParams(float weight) {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
    }

    private LinearLayout.LayoutParams fixedHeight(int dpHeight) {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(dpHeight));
    }

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(dp(2), dp(2), dp(2), dp(2));
        return r;
    }

    private Button key(String text, float weight) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.rgb(242, 245, 250));
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setPadding(0, 0, 0, 0);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.rgb(39, 53, 72));
        g.setCornerRadius(dp(12));
        b.setBackground(g);
        return b;
    }

    private void type(String key) {
        ic = getCurrentInputConnection();
        if (ic == null) return;

        String k = shift ? key.toUpperCase() : key;
        if (telex && k.length() == 1 && Character.isLetter(k.charAt(0))) {
            rawWord += k;
            ic.setComposingText(TelexEngine.transform(rawWord), 1);
        } else {
            commitRaw();
            ic.commitText(k, 1);
        }
        shift = false;
        updateLetterCase();
    }

    private void typePunctuation(String value) {
        commitRaw();
        ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(value, 1);
    }

    private void commitRaw() {
        if (rawWord.isEmpty()) return;
        ic = getCurrentInputConnection();
        if (ic != null) ic.finishComposingText();
        rawWord = "";
    }

    private void backspace() {
        ic = getCurrentInputConnection();
        if (ic == null) return;
        if (!rawWord.isEmpty()) {
            rawWord = rawWord.substring(0, rawWord.length() - 1);
            if (rawWord.isEmpty()) {
                ic.finishComposingText();
            } else {
                ic.setComposingText(TelexEngine.transform(rawWord), 1);
            }
        } else {
            ic.deleteSurroundingText(1, 0);
        }
    }

    private void enter() {
        commitRaw();
        ic = getCurrentInputConnection();
        if (ic != null) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        }
    }

    private void updateLetterCase() {
        if (root == null) return;
        // Rebuild the keyboard so the visible letters reflect Shift immediately.
        // Keeping this simple avoids stale labels on programmatically-created buttons.
        // Do not rebuild while a word is being composed.
    }

    @Override
    public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        rawWord = "";
        shift = false;
    }
}
