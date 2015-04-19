package me.zhihan.javastyler

import groovy.transform.ToString
import groovy.transform.CompileStatic 
import java.util.regex.Matcher
import java.util.regex.Pattern

/** 
 * A pair of integer representing a range. Including the left boundary, 
 * exluding the right. 
 */
@CompileStatic
class PairInt {
    Integer start
    Integer end

    /** Returns true if the integer is between the two ends. */
    Boolean between(Integer i) {
        return (start <= i) && (i < end)
    }

    /** Returns whether the pair of int represents an empty range. */
    Boolean isEmpty() {
        return start >= end
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
    /** 
     * Returns the last token preceding the offset in the string. 
     * assumes the string at (offset - 1) is non-whitespace. Returns
     * empty if char at offset is whitespace.
     */
    @CompileStatic
    static String lastToken(String s, int offset) {
        int i = offset - 1
        while (i >= 0 && !Character.isWhitespace(s.charAt(i))) {
            i = i - 1
        }
        if (i == -1) {
            s.substring(0, offset)
        } else {
            s.substring(i+1, offset)
        }
    }

    @CompileStatic
    /** If the string is a control flow keyword, e.g., 'if' or 'while'. */
    static boolean isControlKeyword(String s) {
        return s.equals("if") || s.equals("while") || s.equals("for")
    }

    /** Returns the first token of the line. */
    static String findToken(String s) {
        Pattern pattern = ~/[^\s]+/
        Matcher matcher = pattern.matcher(s)
        if (matcher.find()) {
            s.substring(matcher.start(), matcher.end())
        } else {
            ""
        }
    }

    static int lastNonWhitespace(String s, int offset) {
        int pos = offset - 1
        while(pos >= 0 && Character.isWhitespace(s.charAt(pos))) {
            pos = pos - 1
        }
        pos
    }
}
