package com.viettel.smsfw.process;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class PatternSWC {
    private static final String PatternSet1 = "(^\\p{Upper}{2}) (\\d{2}) (0\\d{9})";
    private static final String PatternSet2 = "(^\\p{Upper}{1,3}) (\\d{1,10})";
    private static final String PatternSet3 = "(\\p{Upper}{1,3})";
    public String Compare(long mo_id, String content) {
        int result;
        String description;
        if (content == null) {
            description = "Sai cu phap";
        } else {
            if (Pattern.compile(PatternSet1, Pattern.CASE_INSENSITIVE).matcher(content).matches()) {
                result = 1;
            } else if (Pattern.compile(PatternSet2, Pattern.CASE_INSENSITIVE).matcher(content).matches()) {
                result = 2;
            } else if (Pattern.compile(PatternSet3, Pattern.CASE_INSENSITIVE).matcher(content).matches()) {
                result = 3;
            } else result = 0;
            switch (result) {
                case 1: {
                    Matcher matcher = Pattern.compile(PatternSet1, Pattern.CASE_INSENSITIVE).matcher(content);
                    if ((matcher.matches()) && (matcher.group(1).equals("DK"))) {
                        description = "Dang ky";
                    } else {
                        description = "Sai cu phap";
                    }
                    break;
                }
                case 2: {
                    Matcher matcher = Pattern.compile(PatternSet2, Pattern.CASE_INSENSITIVE).matcher(content);
                    if ((matcher.matches()) && (Pattern.compile("(\\d{4})").matcher(matcher.group(2)).matches())) {
                        if ((matcher.group(1).equals("Y"))) {
                            description = "Xac nhan";
                        } else if (matcher.group(1).equals("N")) {
                            description = "Tu choi";
                        } else {
                            description = "Sai cu phap";
                        }
                    } else if (matcher.group(1).equals("HUY")) {
                        if (Pattern.compile("(0\\d{9})").matcher(matcher.group(2)).matches()) {
                            description = "Huy quan he cha con";
                        } else {
                            description = "Sai cu phap";
                        }
                    } else {
                        description = "Sai cu phap";
                    }
                    break;
                }
                case 3: {
                    Matcher matcher = Pattern.compile(PatternSet3, Pattern.CASE_INSENSITIVE).matcher(content);
                    if (matcher.matches() && matcher.group(1).equals("HUY")) {
                        description = "Huy dich vu";
                    } else if (matcher.group(1).equals("KT")) {
                        description = "Kiem tra cuoc";
                    } else {
                        description = "Sai cu phap";
                    }
                    break;
                }
                default: {
                    description = "Sai cu phap";
                }
            }
        }
        return description;
    }
}