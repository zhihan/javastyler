package me.zhihan.javastyler

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.List

/**
 * Single line styles 
 * 
 * The styles that can be checked and enforced using a single line of code. 
 */
abstract class Diagnostics {
    abstract Boolean passed()
    abstract String message()
}

/**
 * Diagnostics 
 * Either pass or fail.
 */
class Pass extends Diagnostics {
    Boolean passed() {
        return true
    }

    String message() {
        "Passed"
    }
}

class Fail extends Diagnostics {
    String msg
    Boolean passed() {
        return false
    }

    String message() {
        msg
    }
}

/**
  * Single-line rules
  */
interface SingleLineRule {
    /** Analyze a line and provide a diagnostics */
    Diagnostics analyze(String line)

    /** A fix can be provided based on the diagnostics */
    Boolean canFix(String line)

    /** Return a fixed line */
    String fix(String line)
} 

/**
  * Line Width rule
  *
  * A line in the source file should not exceeds 80 or 100 characters. 
  * No fix provided.
  */
class LineWidthRule implements SingleLineRule {
    Integer width

    Diagnostics analyze(String line) {
        if (line.size() > 80) {
            new Fail(msg: "Line is longer than ${width} characters")
        } else {
            new Pass()
        }
    }

    Boolean canFix(String line) {
        false
    }

    String fix(String line) {
        line
    }
}

class TrailingSpaceRule implements SingleLineRule {
    Diagnostics analyze(String line) {
        if (Character.isWhitespace(line.charAt(line.size() - 1))) {
            return new Fail(msg: "Trailing whitespace found")
        } else {
            return new Pass()
        }
    }

    Boolean canFix(String line) {
        return true
    }

    String fix(String line) {
        int i = line.size() - 1
        while (Character.isWhitespace(line.charAt(i)) && i > 0) {
            i = i - 1;
        }
        line.substring(0, i + 1)
    }
}

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

class LeftParenthesisRule implements SingleLineRule {
    private Boolean isSpace(char x) {
        return Character.isWhitespace(x)
    }

    Diagnostics analyze(String line) {
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)

        while (offset >= 0 && offset < line.size()) {
            offset = line.indexOf("(", offset)
            if (offset <0) {
                return new Pass()
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }

            if (offset > 0 && isSpace(line.charAt(offset - 1))) {
                return new Fail(msg: "Extra space before '(' at $offset")
            }
            if (offset < line.size() - 2 && isSpace(line.charAt(offset + 1)) ) {
                return new Fail(msg: "Extra space after '(' at $offset")
            }
            offset = offset + 1
        }
        return new Pass()
    }

    /** A fix can be provided based on the diagnostics */
    Boolean canFix(String line) {
        return true
    }

    /** Return a fixed line */
    String fix(String line) {
        StringBuilder sb = new StringBuilder(line)
        
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)
        while (offset >= 0 ) {
            offset = sb.indexOf("(", offset)
            if (offset < 0) {
                break
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }
            if (offset > 0 && isSpace(sb.charAt(offset - 1))) {
                int r = offset - 1
                while(r >=0 && isSpace(sb.charAt(r))) {
                    sb.deleteCharAt(r)
                    r = r - 1
                }
                offset = r + 2
            }
            if (offset < sb.size() - 2 && isSpace(sb.charAt(offset + 1)) ) {
                int r2 = offset + 1
                while (r2 < sb.size() - 1 && isSpace(sb.charAt(r2))) {
                    sb.deleteCharAt(r2)
                }
                offset = r2
            }
        }
        return sb.toString();
    }    
}