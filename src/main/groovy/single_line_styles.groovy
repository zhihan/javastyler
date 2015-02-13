package me.zhihan.javastyler

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

class FailWithLineNumber extends Diagnostics {
    String msg
    List<Integer> lines

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

/** 
  * Trailing whitespace rule
  *
  * No trailing whitespace is allowed. 
  */
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
  * Leading tab rule
  *
  * No tab should appear before non-whitespace characters.
  */
class LeadingTabRule implements SingleLineRule {
    Diagnostics analyze(String line) {
        int i = 0
        while (i < line.size() && line.charAt(i) == ' ') {
            i = i + 1
        }
        if (i < line.size() && line.charAt(i) == '\t') {
            return new Fail(msg: "Leading tabs at $i")
        } else {
            return new Pass()
        }
    }

    Boolean canFix(String unused) {
        return true
    }

    String fix(String line) {
        StringBuilder sb = new StringBuilder(line)
        int offset = 0
        while(offset < sb.size() && Character.isWhitespace(sb.charAt(offset))) {
            if (sb.charAt(offset) == '\t') {
                sb.deleteCharAt(offset)
                sb.insert(offset, "  ")
                offset = offset + 2
            } else {
                offset = offset + 1
            }
        }
        return sb.toString()
    }
}

