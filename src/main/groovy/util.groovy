package me.zhihan.javastyler

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.List
/**
 * A helper class to provide maskng for quoted strings. For example, 
 * some rules such as the parenthesis rule does not apply to parenthesis
 * in a quoted string.
 */
class QuoteMask {

    static class Pair {
        Integer start
        Integer end

        Boolean between(Integer i) {
            return (start <= i) && (i < end)
        }
    }

    List<Pair> masks;

    static Pattern doubleQuoteString = ~/"([^"]|\")*"/
    static Pattern singleQuoteString = ~/'([^']|\')*'/

    private static QuoteMask quote(String line, Pattern pattern) {
        QuoteMask result = new QuoteMask()
        result.masks = []
        Matcher matcher = pattern.matcher(line)
        while (matcher.find()) {
            Pair p = new Pair()
            p.start = matcher.start()
            p.end = matcher.end()
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

