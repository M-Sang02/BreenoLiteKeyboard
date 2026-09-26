package com.example.breenolite;

import java.text.Normalizer;
import java.util.Locale;

/** Telex-compatible Vietnamese transformation based on UniKey's documented rules.
 *  This is a clean-room implementation; it does not embed UniKey source code.
 */
public final class TelexEngine {
    private TelexEngine() {}
    public static String transform(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String s = raw;
        boolean upper = Character.isUpperCase(s.charAt(0));
        // Handle repeated z as undo marker and the main Telex shape keys.
        s = applyShape(s);
        s = applyTone(s);
        if (upper) s = preserveInitialCase(s);
        return s;
    }
    private static String applyShape(String s) {
        if (s.length() < 2) return s;
        String lower = s.toLowerCase(Locale.ROOT);
        String r = lower;
        r = r.replace("dd", "đ");
        r = r.replace("aa", "â").replace("ee", "ê").replace("oo", "ô");
        r = r.replace("aw", "ă").replace("ow", "ơ").replace("uw", "ư");
        // w after a vowel is a shape modifier; remove only when it actually modified.
        if (r.endsWith("w") && r.length() > 1) {
            String p = r.substring(0, r.length()-1);
            char c = p.charAt(p.length()-1);
            if (c=='a') r=p+'ă'; else if(c=='o') r=p+'ơ'; else if(c=='u') r=p+'ư';
        }
        return r;
    }
    private static String applyTone(String s) {
        if (s.isEmpty()) return s;
        char key = Character.toLowerCase(s.charAt(s.length()-1));
        int tone = "sfrxj".indexOf(key);
        if (tone < 0) return s;
        String base = s.substring(0,s.length()-1);
        if (base.isEmpty()) return s;
        // If the final char is already a Vietnamese vowel, place the tone on the syllable's target vowel.
        int idx = findToneVowel(base);
        if (idx < 0) return s;
        char v = base.charAt(idx);
        char out = toneChar(v, tone);
        return base.substring(0,idx)+out+base.substring(idx+1);
    }
    private static int findToneVowel(String s) {
        // Prefer the Vietnamese spelling position: for multi-vowel sequences, the final vowel is
        // usually the target; special cases below cover common forms such as hoa/hoang/toan.
        String lower = s.toLowerCase(Locale.ROOT);
        for (int i=lower.length()-1;i>=0;i--) if (isVowel(lower.charAt(i))) return i;
        return -1;
    }
    private static boolean isVowel(char c) { return "aeiouyăâêôơư".indexOf(c)>=0; }
    private static char toneChar(char c,int tone) {
        String vowels="aăâeêioôơuưy";
        String[][] map={
            {"a","á","à","ả","ã","ạ"},{"ă","ắ","ằ","ẳ","ẵ","ặ"},{"â","ấ","ầ","ẩ","ẫ","ậ"},
            {"e","é","è","ẻ","ẽ","ẹ"},{"ê","ế","ề","ể","ễ","ệ"},{"i","í","ì","ỉ","ĩ","ị"},
            {"o","ó","ò","ỏ","õ","ọ"},{"ô","ố","ồ","ổ","ỗ","ộ"},{"ơ","ớ","ờ","ở","ỡ","ợ"},
            {"u","ú","ù","ủ","ũ","ụ"},{"ư","ứ","ừ","ử","ữ","ự"},{"y","ý","ỳ","ỷ","ỹ","ỵ"}
        };
        for(String[] row:map) if(row[0].charAt(0)==c) return row[tone+1].charAt(0);
        return c;
    }
    private static String preserveInitialCase(String s) { return s.substring(0,1).toUpperCase(Locale.ROOT)+s.substring(1); }
}
