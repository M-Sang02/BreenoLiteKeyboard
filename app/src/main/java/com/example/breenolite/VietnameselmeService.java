package com.example.breenolite;

import android.inputmethodservice.InputMethodService;
import android.view.*;
import android.view.inputmethod.InputConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import java.util.*;

public class VietnameseImeService extends InputMethodService {
    private LinearLayout root, keys;
    private String rawWord="";
    private boolean shift=false, telex=true;
    private InputConnection ic;

    @Override public View onCreateInputView() { return buildKeyboard(); }
    private View buildKeyboard() {
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(8,8,8,10); root.setBackgroundColor(Color.rgb(16,29,44));
        LinearLayout bar=row();
        Button lang=key("VI",1); lang.setOnClickListener(v->{telex=!telex; lang.setText(telex?"VI":"EN");});
        Button space=key("",4); space.setText("Breeno Lite • Telex");
        Button back=key("⌫",1); back.setOnClickListener(v->backspace());
        bar.addView(lang); bar.addView(space); bar.addView(back); root.addView(bar,new LinearLayout.LayoutParams(-1,54));
        String[][] rows={{"q","w","e","r","t","y","u","i","o","p"},{"a","s","d","f","g","h","j","k","l"},{"⇧","z","x","c","v","b","n","m","⌫"},{"123",",","🌐","space",".","↵"}};
        for(String[] rr:rows){ LinearLayout r=row(); for(String k:rr){ Button b=key(k,1); if(k.equals("space")) b.setText(" "); if(k.equals("⇧")) b.setOnClickListener(v->{shift=!shift; refreshCaps();}); else if(k.equals("⌫")) b.setOnClickListener(v->backspace()); else if(k.equals("↵")) b.setOnClickListener(v->enter()); else if(k.equals("🌐")) b.setOnClickListener(v->getInputMethodManager().showInputMethodPicker()); else if(k.equals("123")) b.setOnClickListener(v->toast("Bàn phím số đang được bổ sung")); else b.setOnClickListener(v->type(k)); r.addView(b,new LinearLayout.LayoutParams(0,1,1)); } root.addView(r,new LinearLayout.LayoutParams(-1,0,1)); }
        return root;
    }
    private InputMethodManager getInputMethodManager(){ return (android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); }
    private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_SHORT).show(); }
    private LinearLayout row(){ LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); r.setPadding(3,3,3,3); return r; }
    private Button key(String s,int w){ Button b=new Button(this); b.setText(s); b.setTextColor(Color.rgb(242,245,250)); b.setTextSize(16); b.setAllCaps(false); GradientDrawable g=new GradientDrawable(); g.setColor(Color.rgb(39,53,72)); g.setCornerRadius(18); b.setBackground(g); b.setPadding(0,0,0,0); return b; }
    private void type(String key){ ic=getCurrentInputConnection(); if(ic==null)return; String k=shift?key.toUpperCase():key; if(telex && k.length()==1 && Character.isLetter(k.charAt(0))){ rawWord+=k; ic.setComposingText(TelexEngine.transform(rawWord),1); } else { commitRaw(); ic.commitText(k,1); } shift=false; refreshCaps(); }
    private void commitRaw(){ if(rawWord.isEmpty())return; ic=getCurrentInputConnection(); if(ic!=null){ic.finishComposingText();} rawWord=""; }
    private void backspace(){ ic=getCurrentInputConnection(); if(ic==null)return; if(!rawWord.isEmpty()){ rawWord=rawWord.substring(0,rawWord.length()-1); if(rawWord.isEmpty()) ic.commitText("",1); else ic.setComposingText(TelexEngine.transform(rawWord),1); } else ic.deleteSurroundingText(1,0); }
    private void enter(){ commitRaw(); ic=getCurrentInputConnection(); if(ic!=null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER)); }
    private void refreshCaps(){ }
    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting){ super.onStartInputView(info,restarting); rawWord=""; }
}
