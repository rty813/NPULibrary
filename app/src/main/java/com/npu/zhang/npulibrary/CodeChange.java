package com.npu.zhang.npulibrary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhang on 16-11-30.
 */

public class CodeChange {
    public static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
}
