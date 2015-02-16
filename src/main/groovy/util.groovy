package me.zhihan.javastyler

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.List

class PairInt {
    Integer start
    Integer end
    Boolean between(Integer i) {
        return (start <= i) && (i < end)
    }
}
/**
 * A helper class to provide maskng for quoted strings. For example, 
 * some rules such as the parenthesis rule does not apply to parenthesis
 * in a quoted string.
 */
class QuoteMask {
    List<PairInt> masks;

    static Pattern doubleQuoteString = ~/"([^"]|\")*"/
    static Pattern singleQuoteString = ~/'([^']|\')*'/

    private static QuoteMask quote(String line, Pattern pattern) {
        QuoteMask result = new QuoteMask()
        result.masks = []
        Matcher matcher = pattern.matcher(line)
        while (matcher.find()) {
            PairInt p = new PairInt(start: matcher.start(), end: matcher.end())
            result.masks.add(p)
        }
        result
    }

    static QuoteMask doubleQuote(String line) {
        quote(line, doubleQuoteString)
    }

    static QuoteMask singleQuote(String line) {
        quote(line, singleQuoteString)
    }

    // Visible for tests
    List<Integer> rawMasks() {
        List<Integer> result = []
        masks.each {
            result.add(it.start)
            result.add(it.end)
        }
        result
    }

    Boolean masked(Integer i) {
        return masks.any{ it.between(i)}
    }
}

/** Utility for stirng manipulations */
class StringUtil {
    static lastToken(String s, int offset) {
        int i = offset - 1
        while (i >= 0 && !Character.isWhitespace(s.charAt(i))) {
            i = i - 1
        }
        if (i == 0) {
            s
        } else {
            s.substring(i+1, offset)
        }
    }

    static Boolean isControlKeyword(String s) {
        return s.equals("if") || s.equals("while") || s.equals("for")
    }

    static String findToken(String s) {
        Pattern pattern = ~/[^\s]+/
        Matcher matcher = pattern.matcher(s)
        if (matcher.find()) {
            s.substring(matcher.start(), matcher.end())
        } else {
            ""
        }
    }
}

/** 
 * Simple class to represent a position in the source file by
 * line and column number 
 */
class LineColumn {
    Integer line
    Integer column
}
