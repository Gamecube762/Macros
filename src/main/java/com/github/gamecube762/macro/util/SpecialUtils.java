package com.github.gamecube762.macro.util;


import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Class of random utilities; This class may not stay consistent and can change at anytime.
 *
 * Created by gamec on 4/8/2017.
 */
public class SpecialUtils {

    public static String toString(Collection<String> strings) {
        return toString(strings, " ", "", "");
    }

    public static String toString(Collection<String> strings, String delimiter) {
        return toString(strings, delimiter, "", "");
    }

    public static String toString(Collection<String> strings, String delimiter, CharSequence prefix, CharSequence suffix) {
        return strings.isEmpty() ? "" : strings.stream().collect(Collectors.joining(" ", prefix, suffix));
    }

    public static Optional<Integer> parseInt(String s) {
        try {return Optional.of(Integer.parseInt(s));}
        catch (NumberFormatException ignore) {return Optional.empty();}
    }

    public static boolean isNullOrEmpty(Collection l) {
        return l != null && !l.isEmpty();
    }

    public static <T extends Throwable> void isNullOrEmpty(Collection l, T T) throws T {
        if (isNullOrEmpty(l))
            throw T;
    }

    public static void debugMatcherGroups(Matcher m) {
        for(int i = 0; i <= m.groupCount(); i++)
            if (m.group(i) != null)
                System.out.println(i + ": " + m.group(i));
    }

}
