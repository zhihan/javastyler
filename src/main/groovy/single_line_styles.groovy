package me.zhihan.javastyler

import groovy.transform.CompileStatic
/**
 * Single line styles 
 * 
 * The styles that can be checked and enforced using a single line of code. 
 */

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

    /** Whether should skip this location */
    void setSkip(Closure isComment)
} 

/**
  * Line Width rule
  *
  * A line in the source file should not exceeds 80 or 100 characters. 
  * No fix provided.
  */
@CompileStatic
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

    void setSkip(Closure isComment) {
    }
}

/** 
  * Trailing whitespace rule
  *
  * No trailing whitespace is allowed. 
  */
@CompileStatic  
class TrailingSpaceRule implements SingleLineRule {
    Diagnostics analyze(String line ) {
        if (line.size() == 0) {
            return new Pass()
        }
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
        while (i >=0 && Character.isWhitespace(line.charAt(i))) {
            i = i - 1;
        }
        line.substring(0, i + 1)
    }

    void setSkip(Closure notUsed) {} // Must implement
}

/** 
  * Leading tab rule
  *
  * No tab should appear before non-whitespace characters.
  */
@CompileStatic
class LeadingTabRule implements SingleLineRule {
    Diagnostics analyze(String line) {
        int i = 0
        while (i < line.size() && line.charAt(i) == ' ') {
            i = i + 1
        }
        if (i < line.size() && line.charAt(i) == '\t') {
            return new Fail(msg: "Leading tabs found")
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
    void setSkip(Closure isComment) {
    }
}

/** 
 * No whilecard in import statements
 */ 
@CompileStatic
class ImportNoWildcardRule implements SingleLineRule {
    Boolean canFix(String unused) {
        false
    }

    String fix(String line) {
        line
    }

    Diagnostics analyze(String line) {
        String firstToken = StringUtil.findToken(line)
        if (firstToken.equals("import") &&
            line.contains("*")) {
            new Fail(msg: "Cannot use wildcard in import statement")
        } else {
            new Pass()
        }
    }
    void setSkip(Closure isComment) {
        // TODO(zhihan): Consider commented out import statements.
    }
} 



