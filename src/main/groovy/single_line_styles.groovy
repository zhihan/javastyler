package me.zhihan.javastyler

import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class Diagnostics {
    abstract Boolean passed()
    abstract String message()
}

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


interface SingleLineRule {
    /** Analyze a line and provide a diagnostics */
    Diagnostics analyze(String line)

    /** A fix can be provided based on the diagnostics */
    Boolean canFix(String line)

    /** Return a fixed line */
    String fix(String line)
} 

/**
  * Line Width
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

class QuoteMask {

    class Pair {
        Integer start
        Integer end
    }

    List<Pair> masks;

    static Pattern doubleQuoteString = ~/"[^"]*"/
    static Pattern singleQuoteString = ~/'[^']*'/

    static QuoteMask quote(String line, Pattern) {
        QuoteMask result = new QuoteMask()
        result.masks = []
        Matcher matcher = doubleQuoteString.matcher(line)
        while (matcher.find()) {
            result.masks.add(new Pair(matcher.start(), matcher.end()))
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
}