package com.smartcampus.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 上课时间解析与冲突检测工具。
 * 支持格式："周一 1-2节, 周三 3-4节"（中英文逗号分隔）。
 */
public final class ClassTimeParser {

    private static final Pattern SEGMENT_PATTERN =
            Pattern.compile("周([一二三四五六日天])\\s*(\\d+)-(\\d+)节");

    private ClassTimeParser() {
    }

    /** 单个时间段：星期几 + 起止节次 */
    public record Segment(int day, int startPeriod, int endPeriod) {
    }

    /** 解析上课时间串，未匹配到时间段时返回空列表 */
    public static List<Segment> parse(String classTime) {
        List<Segment> segments = new ArrayList<>();
        if (classTime == null || classTime.isBlank()) {
            return segments;
        }
        for (String part : classTime.split("[,，]")) {
            Matcher matcher = SEGMENT_PATTERN.matcher(part.trim());
            if (matcher.find()) {
                segments.add(new Segment(dayOf(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3))));
            }
        }
        return segments;
    }

    /** 判断两门课程的上课时间是否重叠 */
    public static boolean overlaps(String classTimeA, String classTimeB) {
        List<Segment> a = parse(classTimeA);
        List<Segment> b = parse(classTimeB);
        for (Segment sa : a) {
            for (Segment sb : b) {
                if (sa.day() == sb.day()
                        && sa.startPeriod() <= sb.endPeriod()
                        && sb.startPeriod() <= sa.endPeriod()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int dayOf(String chineseDay) {
        return switch (chineseDay) {
            case "一" -> 1;
            case "二" -> 2;
            case "三" -> 3;
            case "四" -> 4;
            case "五" -> 5;
            case "六" -> 6;
            default -> 7;
        };
    }
}
