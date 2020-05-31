package jdk8.regex;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

    @Test
    public void greedyOne() {
        // 默认情况下匹配都是贪婪模式, 如果要改成非贪婪模式, 只需要量词后面加上一个问号?
        String target = "aa<div>test1</div>bb<div>test2</div>cc";

        Pattern nonGreedyPattern = Pattern.compile("(<div>.*?</div>)");
        Matcher matcher1 = nonGreedyPattern.matcher(target);
        while (matcher1.find()) {
            System.out.println(matcher1.group());
        }

        Pattern greedyPattern = Pattern.compile("(<div>.*</div>)");
        Matcher matcher2 = greedyPattern.matcher(target);
        while (matcher2.find()) {
            System.out.println(matcher2.group());
        }
    }

    @Test
    public void greedyTwo() {
        // https://www.liaoxuefeng.com/wiki/1252599548343744/1306046731649057
        String target = "1230000";
        Pattern pattern1 = Pattern.compile("(\\d+)(0*)");
        Matcher matcher1 = pattern1.matcher(target);
        if (matcher1.matches()) {
            System.out.println("group1=" + matcher1.group(1)); // "1230000"
            System.out.println("group2=" + matcher1.group(2)); // ""
        }

        Pattern pattern2 = Pattern.compile("(\\d+?)(0*)");
        Matcher matcher2 = pattern2.matcher(target);
        if (matcher2.matches()) {
            System.out.println("group1=" + matcher2.group(1)); // "123"
            System.out.println("group2=" + matcher2.group(2)); // "0000"
        }
    }

    @Test
    public void namedCaptureGroup() {
        String target = "98.23$";
        Pattern pattern1 = Pattern.compile("(?<part1>\\d+)(?:\\.?)(?:\\d+)(?<part2>[$|￥])");
        Matcher matcher1 = pattern1.matcher(target);
        if (matcher1.matches()) {
            System.out.println(matcher1.group("part1"));
            System.out.println(matcher1.group("part2"));
        }
    }

    @Test
    public void nonCaptureGroup() {
        // 取金额中的整数部分和币种
        String target = "98.23$";
        Pattern pattern1 = Pattern.compile("(\\d+)(\\.?)(\\d+)([$|￥])");
        Matcher matcher1 = pattern1.matcher(target);
        if (matcher1.matches()) {
            System.out.println(matcher1.group(1));
            System.out.println(matcher1.group(4));
        }

        // 使用非捕获分组
        Pattern pattern2 = Pattern.compile("(\\d+)(?:\\.?)(?:\\d+)([$])");
        Matcher matcher2 = pattern2.matcher(target);
        if (matcher2.matches()) {
            System.out.println(matcher2.group(1));
            System.out.println(matcher2.group(2));
        }

        // 使用非捕获分组
        Pattern pattern3 = Pattern.compile("(?:a)(\\d{3})");
        Matcher matcher3 = pattern3.matcher("a444b666c888a999");
        while (matcher3.find()) {
            for (int i = 0; i <= matcher3.groupCount(); i++) {
                System.out.println("group" + i + ": " + matcher3.group(i));
            }
        }
    }

    @Test
    public void lookAheadPositive() {
        // a前面有三个数字的这样的字符串,只取出3个数字,不包括a
        Pattern pattern = Pattern.compile("\\d{3}(?=a)");
        Matcher matcher = pattern.matcher("a444b666c888a999");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        Pattern pattern1 = Pattern.compile("Windows(?=95|98|NT|2000)");
        Matcher matcher1 = pattern1.matcher("Windows2000|Windows90|Windows95|2000Windows|4Windows");
        while (matcher1.find()) {
            System.out.println(matcher1.replaceAll("xx"));
        }
    }

    @Test
    public void lookAheadNegative() {
        // 连续三个数字的后面出现的字符不是a
        Pattern pattern = Pattern.compile("\\d{3}(?!a)");
        Matcher matcher = pattern.matcher("a444b666c888a999");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        Pattern pattern1 = Pattern.compile("Windows(?!95|98|NT|2000)");
        Matcher matcher1 = pattern1.matcher("Windows2000|Windows90|Windows95|2000Windows|4Windows");
        while (matcher1.find()) {
            System.out.println(matcher1.replaceAll("xx"));
        }
    }

    @Test
    public void lookBehindPositive() {
        // a后面是连续三个数字
        Pattern pattern = Pattern.compile("(?<=a)\\d{3}");
        Matcher matcher = pattern.matcher("a444b666c888a999");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        Pattern pattern1 = Pattern.compile("(?<=95|98|NT|2000)Windows");
        Matcher matcher1 = pattern1.matcher("Windows2000|Windows90|Windows95|2000Windows|4Windows");
        while (matcher1.find()) {
            System.out.println(matcher1.replaceAll("xx"));
        }
    }

    @Test
    public void lookBehindNegative() {
        // 不是a后面是连续三个数字
        Pattern pattern = Pattern.compile("(?<!a)\\d{3}");
        Matcher matcher = pattern.matcher("a444b666c888a999");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        Pattern pattern1 = Pattern.compile("(?<!95|98|NT|2000)Windows");
        Matcher matcher1 = pattern1.matcher("Windows2000|Windows90|Windows95|2000Windows|4Windows");
        while (matcher1.find()) {
            System.out.println(matcher1.replaceAll("xx"));
        }
    }
}
